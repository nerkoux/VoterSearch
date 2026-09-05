package com.keofi.poonamashishmehta_votergen.data.importer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.keofi.poonamashishmehta_votergen.data.db.entity.ImportJobEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterListEntity
import com.keofi.poonamashishmehta_votergen.data.parser.VoterParser
import com.keofi.poonamashishmehta_votergen.data.repository.VoterListRepository
import com.keofi.poonamashishmehta_votergen.data.repository.VoterRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}


sealed interface ImportState {
    data object Idle : ImportState
    data class Processing(
        val listName: String,
        val currentPage: Int,
        val totalPages: Int,
        val votersDetected: Int,
        val progressPercent: Float,
        val isOcrMode: Boolean,
        val isSpreadsheet: Boolean = false,
        val currentRecord: Int = 0,
        val totalRecords: Int = 0
    ) : ImportState
    data class Success(
        val listId: Long,
        val listName: String,
        val totalPages: Int,
        val totalVoters: Int,
        val highConfidence: Int,
        val needsReview: Int,
        val epicDetected: Int,
        val isSpreadsheet: Boolean = false
    ) : ImportState
    data class Error(val message: String) : ImportState
}

class PdfImportManager(
    private val context: Context,
    private val voterRepository: VoterRepository,
    private val voterListRepository: VoterListRepository
) {
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val latinRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val hindiRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
    }

    init {
        try {
            PDFBoxResourceLoader.init(context)
        } catch (_: Exception) {
            // Already initialized or fallback available
        }
    }

    /**
     * Inspects a selected PDF URI to get file name and page count.
     */
    suspend fun inspectPdf(uri: Uri): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri) ?: "Voter_List.pdf"
            var pageCount = 0

            // Try Android PdfRenderer first
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    pageCount = renderer.pageCount
                }
            }

            // Fallback to PDFBox if PdfRenderer failed or returned 0
            if (pageCount == 0) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        pageCount = doc.numberOfPages
                    }
                }
            }

            if (pageCount > 0) {
                Result.success(Pair(fileName, pageCount))
            } else {
                Result.failure(Exception("Unable to read pages from this PDF."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Unable to read this PDF."))
        }
    }

    /**
     * Runs the complete import pipeline on background thread.
     */
    suspend fun importPdf(
        uri: Uri,
        customListName: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        var pfd: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var pdDocument: PDDocument? = null
        var pdfStream: InputStream? = null

        val detectedName = getFileName(uri) ?: "Voter_List.pdf"
        val listName = if (!customListName.isNullOrBlank()) customListName.trim() else detectedName.substringBeforeLast(".")

        try {
            // 1. Create VoterListEntity record
            val listEntity = VoterListEntity(
                name = listName,
                sourceFilePath = uri.toString(),
                totalPages = 0,
                totalVoters = 0,
                highConfidence = 0,
                needsReview = 0,
                epicDetected = 0,
                status = "Processing"
            )
            val listId = voterListRepository.createList(listEntity)

            // 2. Create ImportJobEntity
            val jobId = voterListRepository.createImportJob(
                ImportJobEntity(
                    voterListId = listId,
                    listName = listName,
                    currentPage = 0,
                    totalPages = 0,
                    votersDetected = 0,
                    status = "Processing"
                )
            )

            // 3. Open PDF document
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd == null) {
                throw Exception("Could not access PDF file.")
            }
            pdfRenderer = PdfRenderer(pfd)
            val totalPages = pdfRenderer.pageCount

            pdfStream = context.contentResolver.openInputStream(uri)
            pdDocument = if (pdfStream != null) {
                try { PDDocument.load(pdfStream) } catch (_: Exception) { null }
            } else null

            var totalVotersCount = 0
            var highConfidenceCount = 0
            var needsReviewCount = 0
            var epicDetectedCount = 0
            var currentPartNumber: String? = null
            var currentPollingStation: String? = null

            val textStripper = if (pdDocument != null) PDFTextStripper() else null
            val batchVoters = mutableListOf<VoterEntity>()

            // 4. Iterate over pages
            for (pageIndex in 0 until totalPages) {
                val pageNumber = pageIndex + 1
                var pageText = ""
                var isOcr = false

                // Attempt 1: Direct selectable text extraction via PDFBox
                if (pdDocument != null && textStripper != null) {
                    try {
                        textStripper.startPage = pageNumber
                        textStripper.endPage = pageNumber
                        val extracted = textStripper.getText(pdDocument)
                        if (isUsableText(extracted)) {
                            pageText = extracted
                        }
                    } catch (_: Exception) {
                        pageText = ""
                    }
                }

                // Attempt 2: Fallback to On-Device OCR only if direct selectable text is truly missing (e.g. scanned images)
                if (pageText.isBlank()) {
                    isOcr = true
                    val pageBitmap = renderPageToBitmap(pdfRenderer, pageIndex)
                    if (pageBitmap != null) {
                        try {
                            val inputImage = InputImage.fromBitmap(pageBitmap, 0)
                            // Run Hindi/Devanagari OCR first (which also recognizes Latin/digits)
                            val hindiResult = hindiRecognizer.process(inputImage).awaitTask()
                            val ocrHindi = hindiResult.text

                            // Also run standard Latin OCR for high-accuracy alphanumeric EPIC codes
                            val latinResult = latinRecognizer.process(inputImage).awaitTask()
                            val ocrLatin = latinResult.text

                            // Combine with clear separation
                            pageText = "$ocrHindi\n$ocrLatin"
                        } catch (_: Exception) {
                            pageText = ""
                        } finally {
                            pageBitmap.recycle()
                        }
                    }
                }

                // Parse voters from page text
                val parsedResults = VoterParser.parsePage(
                    pageText = pageText,
                    sourcePdf = listName,
                    pageNumber = pageNumber,
                    voterListId = listId,
                    defaultPartNumber = currentPartNumber,
                    defaultPollingStation = currentPollingStation
                )

                for (res in parsedResults) {
                    if (!res.voter.partNumber.isNullOrBlank()) {
                        currentPartNumber = res.voter.partNumber
                    }
                    if (!res.voter.pollingStation.isNullOrBlank()) {
                        currentPollingStation = res.voter.pollingStation
                    }

                    batchVoters.add(res.voter)
                    totalVotersCount++
                    if (res.isHighConfidence) {
                        highConfidenceCount++
                    } else {
                        needsReviewCount++
                    }
                    if (res.voter.epicNumber.isNotBlank()) {
                        epicDetectedCount++
                    }
                }

                // Batch insert every 250 records or at end for optimal SQLite throughput
                if (batchVoters.size >= 250 || pageIndex == totalPages - 1) {
                    if (batchVoters.isNotEmpty()) {
                        voterRepository.insertVoters(batchVoters)
                        batchVoters.clear()
                    }
                }

                // Update real-time progress state
                val progressPercent = (pageNumber.toFloat() / totalPages.toFloat())
                _importState.value = ImportState.Processing(
                    listName = listName,
                    currentPage = pageNumber,
                    totalPages = totalPages,
                    votersDetected = totalVotersCount,
                    progressPercent = progressPercent,
                    isOcrMode = isOcr
                )

                // Update DB job record periodically
                if (pageIndex % 10 == 0 || pageIndex == totalPages - 1) {
                    voterListRepository.updateImportJob(
                        ImportJobEntity(
                            id = jobId,
                            voterListId = listId,
                            listName = listName,
                            currentPage = pageNumber,
                            totalPages = totalPages,
                            votersDetected = totalVotersCount,
                            status = "Processing"
                        )
                    )
                }
            }

            // 5. Finalize VoterListEntity stats
            val updatedList = VoterListEntity(
                id = listId,
                name = listName,
                sourceFilePath = uri.toString(),
                totalPages = totalPages,
                totalVoters = totalVotersCount,
                highConfidence = highConfidenceCount,
                needsReview = needsReviewCount,
                epicDetected = epicDetectedCount,
                status = "Completed"
            )
            voterListRepository.updateList(updatedList)

            voterListRepository.finishJob(
                id = jobId,
                status = "Completed",
                error = null
            )

            val successState = ImportState.Success(
                listId = listId,
                listName = listName,
                totalPages = totalPages,
                totalVoters = totalVotersCount,
                highConfidence = highConfidenceCount,
                needsReview = needsReviewCount,
                epicDetected = epicDetectedCount
            )
            _importState.value = successState

            Result.success(listId)
        } catch (e: CancellationException) {
            _importState.value = ImportState.Error("Import was cancelled.")
            Result.failure(e)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Failed to import PDF voter list."
            _importState.value = ImportState.Error(errorMsg)
            Result.failure(e)
        } finally {
            try { pdDocument?.close() } catch (_: Exception) {}
            try { pdfStream?.close() } catch (_: Exception) {}
            try { pdfRenderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }

    private fun renderPageToBitmap(renderer: PdfRenderer, pageIndex: Int): Bitmap? {
        return try {
            renderer.openPage(pageIndex).use { page ->
                // Render at 2x resolution for crisp OCR of small voter text
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isUsableText(text: String?): Boolean {
        if (text.isNullOrBlank() || text.length < 20) return false
        // If the page already has any recognizable EPIC codes, direct text is definitely present
        if (VoterParser.EPIC_REGEX.containsMatchIn(text)) return true
        val lower = text.lowercase(java.util.Locale.ROOT)
        val keywords = listOf(
            "photo is", "available", "deleted",
            "name", "elector", "epic", "voter", "house", "age", "father", "husband",
            "मतदाता", "नाम", "पिता", "पति", "उम्र", "आयु", "संख्या", "मकान", "वार्ड",
            "मतदपतप", "नपम", "आयप", "मकपन", "सनखयप", "नगरपपनलकप", "ललग", "पपरष", "मपग"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        name = it.getString(nameIdx)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }
}

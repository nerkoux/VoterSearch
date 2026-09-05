package com.keofi.poonamashishmehta_votergen.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.keofi.poonamashishmehta_votergen.data.db.entity.ImportJobEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterListEntity
import com.keofi.poonamashishmehta_votergen.data.repository.VoterListRepository
import com.keofi.poonamashishmehta_votergen.data.repository.VoterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

class SpreadsheetImportManager(
    private val context: Context,
    private val voterRepository: VoterRepository,
    private val voterListRepository: VoterListRepository
) {
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun resetState() {
        _importState.value = ImportState.Idle
    }

    suspend fun inspectFile(uri: Uri): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri) ?: "voter_list.csv"
            val isXlsx = fileName.endsWith(".xlsx", ignoreCase = true)
            var totalRows = 0

            if (isXlsx) {
                val tempFile = copyUriToTempFile(uri, "inspect_excel.xlsx")
                try {
                    ZipFile(tempFile).use { zip ->
                        val sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml")
                            ?: throw Exception("Invalid Excel format: sheet1.xml not found")
                        zip.getInputStream(sheetEntry).use { input ->
                            totalRows = countXlsxRows(input)
                        }
                    }
                } finally {
                    tempFile.delete()
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
                    // Count lines (subtract 1 for header if > 0)
                    parseCsv(reader) { _, _ ->
                        totalRows++
                    }
                }
            }

            // If rows > 1, voter count is rows - 1 (excluding header)
            val detectedVoters = if (totalRows > 1) totalRows - 1 else totalRows
            Result.success(Pair(fileName, detectedVoters))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to inspect file"))
        }
    }

    suspend fun importSpreadsheet(
        uri: Uri,
        customListName: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        val detectedName = getFileName(uri) ?: "voter_list.csv"
        val listName = if (!customListName.isNullOrBlank()) customListName.trim() else detectedName.substringBeforeLast(".")
        val isXlsx = detectedName.endsWith(".xlsx", ignoreCase = true)
        val estimatedTotal = inspectFile(uri).getOrNull()?.second ?: 0

        try {
            val listEntity = VoterListEntity(
                name = listName,
                sourceFilePath = uri.toString(),
                totalPages = 1,
                totalVoters = 0,
                highConfidence = 0,
                needsReview = 0,
                epicDetected = 0,
                status = "Processing"
            )
            val listId = voterListRepository.createList(listEntity)

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

            var totalVotersCount = 0
            var highConfidenceCount = 0
            var needsReviewCount = 0
            var epicDetectedCount = 0

            val batchVoters = mutableListOf<VoterEntity>()
            var headerMapping: ColumnMapping? = null

            fun processRow(rowIndex: Int, row: List<String>) {
                if (rowIndex == 0) {
                    headerMapping = ColumnMapping.detect(row)
                    return
                }

                val mapping = headerMapping ?: ColumnMapping.DEFAULT
                val voter = mapping.toVoterEntity(
                    row = row,
                    listId = listId,
                    fallbackSource = detectedName
                )

                if (voter.name.isNotBlank() || voter.epicNumber.isNotBlank()) {
                    totalVotersCount++
                    if (voter.name.isNotBlank() && voter.epicNumber.isNotBlank()) {
                        highConfidenceCount++
                    } else {
                        needsReviewCount++
                    }
                    if (voter.epicNumber.isNotBlank()) {
                        epicDetectedCount++
                    }
                    batchVoters.add(voter)
                }

                if (batchVoters.size >= 500) {
                    kotlinx.coroutines.runBlocking {
                        voterRepository.insertVoters(batchVoters)
                    }
                    batchVoters.clear()

                    val progress = if (estimatedTotal > 0) {
                        (totalVotersCount.toFloat() / estimatedTotal.toFloat()).coerceIn(0f, 1f)
                    } else 0.5f

                    _importState.value = ImportState.Processing(
                        listName = listName,
                        currentPage = 1,
                        totalPages = 1,
                        votersDetected = totalVotersCount,
                        progressPercent = progress,
                        isOcrMode = false,
                        isSpreadsheet = true,
                        currentRecord = totalVotersCount,
                        totalRecords = estimatedTotal
                    )
                }
            }

            if (isXlsx) {
                val tempFile = copyUriToTempFile(uri, "import_excel.xlsx")
                try {
                    ZipFile(tempFile).use { zip ->
                        // Load shared strings if available
                        val sharedStrings = mutableListOf<String>()
                        val sstEntry = zip.getEntry("xl/sharedStrings.xml")
                        if (sstEntry != null) {
                            zip.getInputStream(sstEntry).use { input ->
                                parseSharedStrings(input, sharedStrings)
                            }
                        }

                        val sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml")
                            ?: throw Exception("sheet1.xml not found in Excel file")
                        zip.getInputStream(sheetEntry).use { input ->
                            parseXlsxSheet(input, sharedStrings) { rowIndex, row ->
                                processRow(rowIndex, row)
                            }
                        }
                    }
                } finally {
                    tempFile.delete()
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
                    parseCsv(reader) { rowIndex, row ->
                        processRow(rowIndex, row)
                    }
                }
            }

            if (batchVoters.isNotEmpty()) {
                voterRepository.insertVoters(batchVoters)
                batchVoters.clear()
            }

            // Update list record
            val updatedList = VoterListEntity(
                id = listId,
                name = listName,
                sourceFilePath = uri.toString(),
                totalPages = 1,
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
                totalPages = 1,
                totalVoters = totalVotersCount,
                highConfidence = highConfidenceCount,
                needsReview = needsReviewCount,
                epicDetected = epicDetectedCount,
                isSpreadsheet = true
            )
            _importState.value = successState
            Result.success(listId)
        } catch (c: CancellationException) {
            _importState.value = ImportState.Idle
            Result.failure(c)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Failed to import spreadsheet."
            _importState.value = ImportState.Error(errorMsg)
            Result.failure(Exception(errorMsg))
        }
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) return cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
        }
        return uri.path?.substringAfterLast('/')
    }

    private fun copyUriToTempFile(uri: Uri, tempName: String): File {
        val tempFile = File(context.cacheDir, tempName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Could not read file from storage.")
        return tempFile
    }

    companion object {
        /**
         * Streaming RFC-4180 CSV parser handling multiline headers/values, quotes, and escaped quotes.
         */
        inline fun parseCsv(reader: BufferedReader, onRow: (rowIndex: Int, row: List<String>) -> Unit) {
            var rowIndex = 0
            val currentRow = mutableListOf<String>()
            val currentField = StringBuilder()
            var inQuotes = false
            var isFirstCharOfStream = true

            var intChar = reader.read()
            while (intChar != -1) {
                var ch = intChar.toChar()

                // Strip UTF-8 BOM if at stream start
                if (isFirstCharOfStream) {
                    isFirstCharOfStream = false
                    if (ch == '\uFEFF') {
                        intChar = reader.read()
                        continue
                    }
                }

                if (inQuotes) {
                    if (ch == '"') {
                        // Check next char for escaped quote ""
                        reader.mark(1)
                        val next = reader.read()
                        if (next != -1 && next.toChar() == '"') {
                            currentField.append('"')
                        } else {
                            inQuotes = false
                            reader.reset()
                        }
                    } else {
                        currentField.append(ch)
                    }
                } else {
                    when (ch) {
                        '"' -> {
                            inQuotes = true
                        }
                        ',' -> {
                            currentRow.add(currentField.toString().trim())
                            currentField.setLength(0)
                        }
                        '\r' -> {
                            // Check next char for \n
                            reader.mark(1)
                            val next = reader.read()
                            if (next != -1 && next.toChar() != '\n') {
                                reader.reset()
                            }
                            currentRow.add(currentField.toString().trim())
                            currentField.setLength(0)
                            if (currentRow.isNotEmpty() && currentRow.any { it.isNotBlank() }) {
                                onRow(rowIndex++, currentRow.toList())
                            }
                            currentRow.clear()
                        }
                        '\n' -> {
                            currentRow.add(currentField.toString().trim())
                            currentField.setLength(0)
                            if (currentRow.isNotEmpty() && currentRow.any { it.isNotBlank() }) {
                                onRow(rowIndex++, currentRow.toList())
                            }
                            currentRow.clear()
                        }
                        else -> {
                            currentField.append(ch)
                        }
                    }
                }
                intChar = reader.read()
            }

            if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
                currentRow.add(currentField.toString().trim())
                if (currentRow.isNotEmpty() && currentRow.any { it.isNotBlank() }) {
                    onRow(rowIndex, currentRow.toList())
                }
            }
        }

        fun countXlsxRows(inputStream: InputStream): Int {
            val factory = SAXParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newSAXParser()
            var count = 0
            val handler = object : DefaultHandler() {
                override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                    if (qName.equals("row", ignoreCase = true)) {
                        count++
                    }
                }
            }
            parser.parse(inputStream, handler)
            return count
        }

        fun parseSharedStrings(inputStream: InputStream, result: MutableList<String>) {
            val factory = SAXParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newSAXParser()
            val handler = object : DefaultHandler() {
                val currentString = StringBuilder()
                var insideSi = false
                var insideT = false

                override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                    if (qName.equals("si", ignoreCase = true)) {
                        insideSi = true
                        currentString.setLength(0)
                    } else if (qName.equals("t", ignoreCase = true) && insideSi) {
                        insideT = true
                    }
                }

                override fun characters(ch: CharArray, start: Int, length: Int) {
                    if (insideSi && insideT) {
                        currentString.append(ch, start, length)
                    }
                }

                override fun endElement(uri: String?, localName: String?, qName: String) {
                    if (qName.equals("t", ignoreCase = true)) {
                        insideT = false
                    } else if (qName.equals("si", ignoreCase = true)) {
                        insideSi = false
                        result.add(currentString.toString())
                    }
                }
            }
            parser.parse(inputStream, handler)
        }

        fun parseXlsxSheet(
            inputStream: InputStream,
            sharedStrings: List<String>,
            onRow: (rowIndex: Int, row: List<String>) -> Unit
        ) {
            val factory = SAXParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newSAXParser()

            val handler = object : DefaultHandler() {
                var rowIndex = 0
                val rowCells = mutableMapOf<Int, String>()
                var currentCellIndex = -1
                var cellType: String? = null
                val cellValue = StringBuilder()
                var insideV = false
                var insideT = false

                override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                    if (qName.equals("row", ignoreCase = true)) {
                        rowCells.clear()
                    } else if (qName.equals("c", ignoreCase = true)) {
                        val cellRef = attributes.getValue("r") ?: ""
                        currentCellIndex = colRefToIndex(cellRef)
                        cellType = attributes.getValue("t")
                        cellValue.setLength(0)
                    } else if (qName.equals("v", ignoreCase = true)) {
                        insideV = true
                    } else if (qName.equals("t", ignoreCase = true)) {
                        insideT = true
                    }
                }

                override fun characters(ch: CharArray, start: Int, length: Int) {
                    if (insideV || insideT) {
                        cellValue.append(ch, start, length)
                    }
                }

                override fun endElement(uri: String?, localName: String?, qName: String) {
                    if (qName.equals("v", ignoreCase = true)) {
                        insideV = false
                    } else if (qName.equals("t", ignoreCase = true)) {
                        insideT = false
                    } else if (qName.equals("c", ignoreCase = true)) {
                        if (currentCellIndex >= 0) {
                            val str = when (cellType) {
                                "s" -> {
                                    val sstIdx = cellValue.toString().toIntOrNull()
                                    if (sstIdx != null && sstIdx in sharedStrings.indices) {
                                        sharedStrings[sstIdx]
                                    } else ""
                                }
                                else -> cellValue.toString()
                            }
                            rowCells[currentCellIndex] = str
                        }
                        currentCellIndex = -1
                        cellType = null
                    } else if (qName.equals("row", ignoreCase = true)) {
                        if (rowCells.isNotEmpty()) {
                            val maxCol = rowCells.keys.maxOrNull() ?: 0
                            val rowList = (0..maxCol).map { col -> rowCells[col] ?: "" }
                            if (rowList.any { it.isNotBlank() }) {
                                onRow(rowIndex++, rowList)
                            }
                        }
                    }
                }
            }
            parser.parse(inputStream, handler)
        }

        fun colRefToIndex(cellRef: String): Int {
            var col = 0
            for (ch in cellRef) {
                if (ch in 'A'..'Z') {
                    col = col * 26 + (ch - 'A' + 1)
                } else if (ch in 'a'..'z') {
                    col = col * 26 + (ch - 'a' + 1)
                } else {
                    break
                }
            }
            return if (col > 0) col - 1 else 0
        }
    }

    data class ColumnMapping(
        val sourceFileIdx: Int = 0,
        val constituencyIdx: Int = 1,
        val localBodyIdx: Int = 2,
        val wardNoIdx: Int = 3,
        val partNoIdx: Int = 4,
        val sectionNameIdx: Int = 5,
        val pageNoIdx: Int = 6,
        val serialNoIdx: Int = 7,
        val statusCodeIdx: Int = 8,
        val epicNoIdx: Int = 9,
        val voterNameIdx: Int = 10,
        val relationTypeIdx: Int = 11,
        val relativeNameIdx: Int = 12,
        val houseNoIdx: Int = 13,
        val ageIdx: Int = 14,
        val genderIdx: Int = 15,
        val statusIdx: Int = 16
    ) {
        fun toVoterEntity(row: List<String>, listId: Long, fallbackSource: String): VoterEntity {
            fun get(idx: Int): String = if (idx in row.indices) row[idx].trim() else ""

            val epic = get(epicNoIdx)
            val voterName = get(voterNameIdx)
            val relType = get(relationTypeIdx)
            val relName = get(relativeNameIdx)
            val house = get(houseNoIdx)
            val age = get(ageIdx).toIntOrNull()
            val gender = get(genderIdx)
            val serial = get(serialNoIdx).toIntOrNull()
            val part = get(partNoIdx)
            val section = get(sectionNameIdx)
            val ward = get(wardNoIdx)
            val constituency = get(constituencyIdx)
            val status = get(statusIdx).ifBlank { "Active" }
            val page = get(pageNoIdx).toIntOrNull() ?: 1
            val srcFile = get(sourceFileIdx).ifBlank { fallbackSource }

            val addressParts = listOfNotNull(
                section.ifBlank { null },
                if (ward.isNotBlank()) "वार्ड नं. $ward" else null
            )
            val address = if (addressParts.isNotEmpty()) addressParts.joinToString(", ") else null

            val pollingStation = listOfNotNull(
                constituency.ifBlank { null },
                section.ifBlank { null }
            ).joinToString(" - ").ifBlank { null }

            val cleanEpic = epic.uppercase().replace(Regex("[^A-Z0-9/]"), "")
            val normalizedEpic = cleanEpic.replace(Regex("[^A-Z0-9]"), "")

            return VoterEntity(
                epicNumber = cleanEpic,
                normalizedEpic = normalizedEpic,
                name = voterName,
                nameHindi = voterName,
                normalizedName = com.keofi.poonamashishmehta_votergen.util.TextNormalizer.normalizeName(voterName),
                relativeName = relName.ifBlank { null },
                relationship = relType.ifBlank { null },
                age = age,
                gender = gender.ifBlank { null },
                houseNumber = house.ifBlank { null },
                partNumber = part.ifBlank { null },
                serialNumber = serial,
                address = address,
                pollingStation = pollingStation,
                status = status,
                voterListId = listId,
                sourcePdf = srcFile,
                sourcePage = page,
                rawOcrText = "",
                confidence = 1.0f
            )
        }

        companion object {
            val DEFAULT = ColumnMapping()

            fun detect(headers: List<String>): ColumnMapping {
                var sourceIdx = -1
                var constituencyIdx = -1
                var localBodyIdx = -1
                var wardIdx = -1
                var partIdx = -1
                var sectionIdx = -1
                var pageIdx = -1
                var serialIdx = -1
                var statusCodeIdx = -1
                var epicIdx = -1
                var nameIdx = -1
                var relTypeIdx = -1
                var relNameIdx = -1
                var houseIdx = -1
                var ageIdx = -1
                var genderIdx = -1
                var statusIdx = -1

                for ((idx, rawHeader) in headers.withIndex()) {
                    val h = rawHeader.lowercase().replace("\n", " ").trim()
                    when {
                        h.contains("epic") || h.contains("पहचान") -> epicIdx = idx
                        h.contains("relative_name") || h.contains("संबंधी का नाम") || h.contains("relative name") -> relNameIdx = idx
                        h.contains("voter_name") || h.contains("मतदाता का नाम") || h.contains("voter name") -> nameIdx = idx
                        h.contains("relation_type") || (h.contains("संबंध") && !h.contains("संबंधी")) -> relTypeIdx = idx
                        h.contains("house") || h.contains("मकान") -> houseIdx = idx
                        h.contains("age") || h.contains("आयु") || h.contains("उम्र") -> ageIdx = idx
                        h.contains("gender") || h.contains("लिंग") -> genderIdx = idx
                        h.contains("status_code") || h.contains("स्थिति कोड") -> statusCodeIdx = idx
                        h.contains("status") || h.contains("स्थिति") -> statusIdx = idx
                        h.contains("serial") || (h.contains("क्रम") && !h.contains("पहचान")) || h.contains("sr") -> serialIdx = idx
                        h.contains("page") || h.contains("पृष्ठ") -> pageIdx = idx
                        h.contains("constituency") || h.contains("विधानसभा") -> constituencyIdx = idx
                        h.contains("section") || h.contains("अनुभाग") || (h.contains("क्षेत्र") && !h.contains("विधानसभा")) -> sectionIdx = idx
                        h.contains("part") || h.contains("booth") || (h.contains("भाग") && !h.contains("अनुभाग")) -> partIdx = idx
                        h.contains("ward") || h.contains("वार्ड") -> wardIdx = idx
                        h.contains("local") || h.contains("निकाय") -> localBodyIdx = idx
                        h.contains("source") || h.contains("स्रोत") -> sourceIdx = idx
                        nameIdx == -1 && h.contains("name") && !h.contains("relative") && !h.contains("section") -> nameIdx = idx
                    }
                }

                // If essential columns were found, return mapped indices with default fallbacks
                return if (epicIdx != -1 || nameIdx != -1) {
                    ColumnMapping(
                        sourceFileIdx = if (sourceIdx != -1) sourceIdx else DEFAULT.sourceFileIdx,
                        constituencyIdx = if (constituencyIdx != -1) constituencyIdx else DEFAULT.constituencyIdx,
                        localBodyIdx = if (localBodyIdx != -1) localBodyIdx else DEFAULT.localBodyIdx,
                        wardNoIdx = if (wardIdx != -1) wardIdx else DEFAULT.wardNoIdx,
                        partNoIdx = if (partIdx != -1) partIdx else DEFAULT.partNoIdx,
                        sectionNameIdx = if (sectionIdx != -1) sectionIdx else DEFAULT.sectionNameIdx,
                        pageNoIdx = if (pageIdx != -1) pageIdx else DEFAULT.pageNoIdx,
                        serialNoIdx = if (serialIdx != -1) serialIdx else DEFAULT.serialNoIdx,
                        statusCodeIdx = if (statusCodeIdx != -1) statusCodeIdx else DEFAULT.statusCodeIdx,
                        epicNoIdx = if (epicIdx != -1) epicIdx else DEFAULT.epicNoIdx,
                        voterNameIdx = if (nameIdx != -1) nameIdx else DEFAULT.voterNameIdx,
                        relationTypeIdx = if (relTypeIdx != -1) relTypeIdx else DEFAULT.relationTypeIdx,
                        relativeNameIdx = if (relNameIdx != -1) relNameIdx else DEFAULT.relativeNameIdx,
                        houseNoIdx = if (houseIdx != -1) houseIdx else DEFAULT.houseNoIdx,
                        ageIdx = if (ageIdx != -1) ageIdx else DEFAULT.ageIdx,
                        genderIdx = if (genderIdx != -1) genderIdx else DEFAULT.genderIdx,
                        statusIdx = if (statusIdx != -1) statusIdx else DEFAULT.statusIdx
                    )
                } else {
                    DEFAULT
                }
            }
        }
    }
}

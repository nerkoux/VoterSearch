package com.keofi.poonamashishmehta_votergen.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.keofi.poonamashishmehta_votergen.data.slip.DigitalSlipRenderer
import com.keofi.poonamashishmehta_votergen.data.slip.SlipData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SlipShareManager(
    private val context: Context,
    private val digitalSlipRenderer: DigitalSlipRenderer
) {
    private val slipsCacheDir: File
        get() = File(context.cacheDir, "slips").apply { if (!exists()) mkdirs() }

    /**
     * Prepares and shares PNG digital slip via Android Sharesheet.
     */
    suspend fun shareImage(slipData: SlipData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileName = buildSlipFileName(slipData, "png")
            val file = File(slipsCacheDir, fileName)
            digitalSlipRenderer.generateSlipPngFile(slipData, file)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Voter Slip - ${slipData.voterName}")
                putExtra(Intent.EXTRA_TEXT, "Voter Slip: ${slipData.voterName} (EPIC: ${slipData.epicNumber})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Voter Slip via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Prepares and shares PDF digital slip via Android Sharesheet.
     */
    suspend fun sharePdf(slipData: SlipData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileName = buildSlipFileName(slipData, "pdf")
            val file = File(slipsCacheDir, fileName)
            digitalSlipRenderer.generateSlipPdfFile(slipData, file)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Voter Slip PDF - ${slipData.voterName}")
                putExtra(Intent.EXTRA_TEXT, "Voter Slip Document: ${slipData.voterName} (EPIC: ${slipData.epicNumber})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Voter Slip PDF via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves PNG to device storage (Pictures/VoterSlips).
     */
    suspend fun saveImageToDevice(slipData: SlipData): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = buildSlipFileName(slipData, "png")
            val tempFile = File(slipsCacheDir, fileName)
            digitalSlipRenderer.generateSlipPngFile(slipData, tempFile)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/VoterSlips")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create storage record.")

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(tempFile).use { it.copyTo(out) }
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "VoterSlips")
                if (!dir.exists()) dir.mkdirs()
                val dest = File(dir, fileName)
                FileInputStream(tempFile).use { input ->
                    FileOutputStream(dest).use { input.copyTo(it) }
                }
            }

            Result.success("Saved to Pictures/VoterSlips: $fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves PDF to device storage (Documents/VoterSlips).
     */
    suspend fun savePdfToDevice(slipData: SlipData): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = buildSlipFileName(slipData, "pdf")
            val tempFile = File(slipsCacheDir, fileName)
            digitalSlipRenderer.generateSlipPdfFile(slipData, tempFile)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/VoterSlips")
                }
                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    ?: throw Exception("Failed to create storage record.")

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(tempFile).use { it.copyTo(out) }
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "VoterSlips")
                if (!dir.exists()) dir.mkdirs()
                val dest = File(dir, fileName)
                FileInputStream(tempFile).use { input ->
                    FileOutputStream(dest).use { input.copyTo(it) }
                }
            }

            Result.success("Saved to Documents/VoterSlips: $fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun buildSlipFileName(slipData: SlipData, extension: String): String {
            val safeName = getCleanVoterNameForFile(slipData.voterName)
            val cleanEpic = slipData.epicNumber
                .replace(Regex("[^a-zA-Z0-9]"), "")
                .trim()

            return when {
                cleanEpic.isNotEmpty() -> "Slip_${safeName}_${cleanEpic}.$extension"
                slipData.serialNumber != null -> "Slip_${safeName}_Sno${slipData.serialNumber}.$extension"
                else -> "Slip_${safeName}_${System.currentTimeMillis()}.$extension"
            }
        }

        fun getCleanVoterNameForFile(voterName: String): String {
            if (voterName.isBlank()) return "Voter"

            // 1. Try dictionary-based transliterations
            val dictTranslit = com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder
                .getTransliterations(voterName)
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .joinToString("_") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
                }

            val candidate = if (dictTranslit.isNotBlank()) {
                dictTranslit
            } else {
                com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder.transliterateDevanagariToAscii(voterName)
            }

            // 2. Clean up characters: Allow letters, digits, and underscores
            val cleaned = candidate
                .replace(Regex("[\\\\/:*?\"<>|\\x00-\\x1F]"), "")
                .replace(Regex("[\\s\\-_]+"), "_")
                .replace(Regex("_+"), "_")
                .trim('_')

            return cleaned.take(30).ifEmpty { "Voter" }
        }
    }
}


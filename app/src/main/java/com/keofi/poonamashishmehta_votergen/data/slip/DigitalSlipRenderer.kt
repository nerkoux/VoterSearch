package com.keofi.poonamashishmehta_votergen.data.slip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DigitalSlipRenderer(private val context: Context) {

    companion object {
        const val DIGITAL_WIDTH = 1080
        private const val PADDING = 48f
        private const val SAFFRON_COLOR = 0xFFFF7A00.toInt()
        private const val NAVY_COLOR = 0xFF172554.toInt()
        private const val TEXT_PRIMARY = 0xFF111827.toInt()
        private const val TEXT_SECONDARY = 0xFF6B7280.toInt()
        private const val BORDER_COLOR = 0xFFE5E7EB.toInt()
        private const val BG_LIGHT = 0xFFF9FAFB.toInt()
    }

    private val regularTypeface: Typeface by lazy {
        com.keofi.poonamashishmehta_votergen.util.DevanagariFontManager.getRegularTypeface(context)
    }

    private val boldTypeface: Typeface by lazy {
        com.keofi.poonamashishmehta_votergen.util.DevanagariFontManager.getBoldTypeface(context)
    }

    /**
     * Renders a high-resolution 1080px portrait digital slip Bitmap (for PNG / WhatsApp).
     */
    fun renderDigitalSlipBitmap(slipData: SlipData): Bitmap {
        val measuredHeight = measureRequiredHeight(slipData)
        val bitmap = Bitmap.createBitmap(DIGITAL_WIDTH, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            typeface = regularTypeface
        }

        val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            typeface = boldTypeface
        }

        val paintSaffron = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SAFFRON_COLOR
        }

        val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BORDER_COLOR
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val paintBoxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BG_LIGHT
            style = Paint.Style.FILL
        }

        // Top Saffron Accent Stripe
        canvas.drawRect(0f, 0f, DIGITAL_WIDTH.toFloat(), 18f, paintSaffron)

        var currentY = 56f

        // 1. Header (Custom Image or Candidate info)
        if (slipData.slipType == SlipType.CUSTOM_IMAGE) {
            val customBitmap = loadCustomHeaderImage(slipData.headerImageUri)
            if (customBitmap != null) {
                val availableWidth = (DIGITAL_WIDTH - (PADDING * 2)).toInt()
                val scaled = ImageConverter.resizeToWidth(customBitmap, availableWidth)
                val left = (DIGITAL_WIDTH - scaled.width) / 2f
                canvas.drawBitmap(scaled, left, currentY, null)
                currentY += scaled.height + 36f
                scaled.recycle()
            } else {
                currentY = drawDefaultCandidateHeader(canvas, slipData, currentY, paintBold, paintText)
            }
        } else {
            currentY = drawTextOnlyHeader(canvas, slipData, currentY, paintBold, paintText)
        }

        // Horizontal Divider
        canvas.drawLine(PADDING, currentY, DIGITAL_WIDTH - PADDING, currentY, paintBorder)
        currentY += 48f

        // 2. Voter Name (Large Hero Text)
        paintBold.textSize = 58f
        paintBold.color = NAVY_COLOR
        canvas.drawText(slipData.voterName, PADDING, currentY, paintBold)
        currentY += 64f

        // Only show Hindi name if distinct from primary voter name
        if (!slipData.voterNameHindi.isNullOrBlank() &&
            !slipData.voterNameHindi.equals(slipData.voterName, ignoreCase = true)
        ) {
            paintText.textSize = 44f
            paintText.color = TEXT_PRIMARY
            canvas.drawText(slipData.voterNameHindi, PADDING, currentY, paintText)
            currentY += 56f
        }

        // EPIC Number Highlight Card
        val epicBoxTop = currentY
        val epicBoxHeight = 110f
        val epicBoxRect = RectF(PADDING, epicBoxTop, DIGITAL_WIDTH - PADDING, epicBoxTop + epicBoxHeight)
        canvas.drawRoundRect(epicBoxRect, 16f, 16f, paintBoxBg)
        canvas.drawRoundRect(epicBoxRect, 16f, 16f, paintBorder)

        // Left orange indicator bar on EPIC box
        val indicatorRect = RectF(PADDING, epicBoxTop, PADDING + 12f, epicBoxTop + epicBoxHeight)
        canvas.drawRoundRect(indicatorRect, 8f, 8f, paintSaffron)

        paintText.textSize = 30f
        paintText.color = TEXT_SECONDARY
        canvas.drawText("EPIC NUMBER", PADDING + 32f, epicBoxTop + 42f, paintText)

        paintBold.textSize = 44f
        paintBold.color = NAVY_COLOR
        val epicDisplay = slipData.epicNumber.ifEmpty { "NOT AVAILABLE" }
        canvas.drawText(epicDisplay, PADDING + 32f, epicBoxTop + 90f, paintBold)

        currentY += epicBoxHeight + 40f

        // Relative info
        if (!slipData.relativeName.isNullOrBlank()) {
            val relLabel = if (!slipData.relationship.isNullOrBlank()) "${slipData.relationship}'s Name" else "Relative's Name"
            paintText.textSize = 30f
            paintText.color = TEXT_SECONDARY
            canvas.drawText(relLabel, PADDING, currentY, paintText)
            currentY += 40f

            paintBold.textSize = 38f
            paintBold.color = TEXT_PRIMARY
            canvas.drawText(slipData.relativeName, PADDING, currentY, paintBold)
            currentY += 48f
        }

        // Two-Column Grid: Age, Gender, Part No, Serial No
        val gridBoxTop = currentY
        val gridBoxHeight = 220f
        val gridRect = RectF(PADDING, gridBoxTop, DIGITAL_WIDTH - PADDING, gridBoxTop + gridBoxHeight)
        canvas.drawRoundRect(gridRect, 16f, 16f, paintBoxBg)
        canvas.drawRoundRect(gridRect, 16f, 16f, paintBorder)

        val col2X = DIGITAL_WIDTH / 2f + 20f
        val col1X = PADDING + 32f

        // Row 1: Age & Gender
        val row1LabelY = gridBoxTop + 48f
        val row1ValueY = gridBoxTop + 90f
        paintText.textSize = 28f
        paintText.color = TEXT_SECONDARY
        canvas.drawText("AGE", col1X, row1LabelY, paintText)
        canvas.drawText("GENDER", col2X, row1LabelY, paintText)

        paintBold.textSize = 38f
        paintBold.color = TEXT_PRIMARY
        val ageStr = if (slipData.age != null) "${slipData.age} Years" else "-"
        canvas.drawText(ageStr, col1X, row1ValueY, paintBold)
        canvas.drawText(slipData.gender ?: "-", col2X, row1ValueY, paintBold)

        // Row 2: Part No & Serial No
        val row2LabelY = gridBoxTop + 148f
        val row2ValueY = gridBoxTop + 190f
        paintText.textSize = 28f
        paintText.color = TEXT_SECONDARY
        canvas.drawText("PART NUMBER", col1X, row2LabelY, paintText)
        canvas.drawText("SERIAL NUMBER", col2X, row2LabelY, paintText)

        paintBold.textSize = 40f
        paintBold.color = SAFFRON_COLOR
        canvas.drawText(slipData.partNumber ?: "-", col1X, row2ValueY, paintBold)
        canvas.drawText(if (slipData.serialNumber != null) "${slipData.serialNumber}" else "-", col2X, row2ValueY, paintBold)

        currentY += gridBoxHeight + 36f

        // House Number
        if (!slipData.houseNumber.isNullOrBlank()) {
            paintText.textSize = 30f
            paintText.color = TEXT_SECONDARY
            canvas.drawText("HOUSE NUMBER", PADDING, currentY, paintText)
            currentY += 40f

            paintBold.textSize = 36f
            paintBold.color = TEXT_PRIMARY
            canvas.drawText(slipData.houseNumber, PADDING, currentY, paintBold)
            currentY += 44f
        }

        // Polling Station Card
        if (!slipData.pollingStation.isNullOrBlank()) {
            paintText.textSize = 30f
            paintText.color = TEXT_SECONDARY
            canvas.drawText("POLLING STATION", PADDING, currentY, paintText)
            currentY += 40f

            paintBold.textSize = 34f
            paintBold.color = TEXT_PRIMARY
            val wrapped = wrapText(slipData.pollingStation, paintBold, DIGITAL_WIDTH - (PADDING * 2))
            for (line in wrapped) {
                canvas.drawText(line, PADDING, currentY, paintBold)
                currentY += 44f
            }
            currentY += 16f
        }

        // Footer Divider & Timestamp
        canvas.drawLine(PADDING, currentY, DIGITAL_WIDTH - PADDING, currentY, paintBorder)
        currentY += 40f

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        paintText.textSize = 28f
        paintText.color = TEXT_SECONDARY
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText("Verified Voter Record • Generated on ${dateFormat.format(Date())}", DIGITAL_WIDTH / 2f, currentY, paintText)
        paintText.textAlign = Paint.Align.LEFT
        currentY += 56f

        // Outer clean border
        val outerBorder = RectF(6f, 6f, DIGITAL_WIDTH - 6f, measuredHeight - 6f)
        canvas.drawRoundRect(outerBorder, 16f, 16f, paintBorder)

        return bitmap
    }

    /**
     * Saves rendered slip to PNG in cache directory for WhatsApp/sharing.
     */
    fun generateSlipPngFile(slipData: SlipData, outputFile: File): File {
        val bitmap = renderDigitalSlipBitmap(slipData)
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return outputFile
    }

    /**
     * Generates a standard A4/A6 PDF document from the slip data.
     */
    fun generateSlipPdfFile(slipData: SlipData, outputFile: File): File {
        val bitmap = renderDigitalSlipBitmap(slipData)
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        pdfDocument.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        bitmap.recycle()

        return outputFile
    }

    private fun drawDefaultCandidateHeader(
        canvas: Canvas,
        slipData: SlipData,
        startY: Float,
        paintBold: Paint,
        paintText: Paint
    ): Float {
        var y = startY + 24f
        paintBold.textSize = 48f
        paintBold.color = NAVY_COLOR
        paintBold.textAlign = Paint.Align.CENTER
        canvas.drawText(slipData.candidateName.uppercase(Locale.ROOT), DIGITAL_WIDTH / 2f, y, paintBold)
        y += 50f

        paintText.textSize = 32f
        paintText.color = SAFFRON_COLOR
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText(slipData.tagline.uppercase(Locale.ROOT), DIGITAL_WIDTH / 2f, y, paintText)
        y += 40f

        paintBold.textAlign = Paint.Align.LEFT
        paintText.textAlign = Paint.Align.LEFT
        return y
    }

    private fun drawTextOnlyHeader(
        canvas: Canvas,
        slipData: SlipData,
        startY: Float,
        paintBold: Paint,
        paintText: Paint
    ): Float {
        var y = startY + 20f
        paintBold.textSize = 44f
        paintBold.color = NAVY_COLOR
        paintBold.textAlign = Paint.Align.CENTER
        canvas.drawText("VOTER INFORMATION SLIP", DIGITAL_WIDTH / 2f, y, paintBold)
        y += 36f

        paintBold.textAlign = Paint.Align.LEFT
        paintText.textAlign = Paint.Align.LEFT
        return y
    }

    private fun measureRequiredHeight(slipData: SlipData): Int {
        var height = 750
        if (slipData.slipType == SlipType.CUSTOM_IMAGE) {
            val bmp = loadCustomHeaderImage(slipData.headerImageUri)
            if (bmp != null) {
                val availableWidth = (DIGITAL_WIDTH - (PADDING * 2)).toInt()
                val aspect = bmp.height.toFloat() / bmp.width.toFloat()
                height += (availableWidth * aspect).toInt()
                bmp.recycle()
            } else {
                height += 120
            }
        } else {
            height += 80
        }
        if (!slipData.voterNameHindi.isNullOrBlank() && !slipData.voterNameHindi.equals(slipData.voterName, ignoreCase = true)) {
            height += 70
        }
        if (!slipData.relativeName.isNullOrBlank()) height += 90
        if (!slipData.houseNumber.isNullOrBlank()) height += 90
        if (!slipData.pollingStation.isNullOrBlank()) height += 130
        return height.coerceAtLeast(900)
    }

    private fun loadCustomHeaderImage(uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            val stream: InputStream? = context.contentResolver.openInputStream(uri)
            stream?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(candidate)
            if (width > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    lines.add(word)
                }
            } else {
                currentLine = StringBuilder(candidate)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}

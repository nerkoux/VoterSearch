package com.keofi.poonamashishmehta_votergen.data.slip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThermalSlipRenderer(private val context: Context) {

    companion object {
        const val PRINTER_WIDTH_DOTS = 384 // 58mm printer at 203 DPI
        private const val PADDING_X = 12f
    }

    private val regularTypeface: Typeface by lazy {
        com.keofi.poonamashishmehta_votergen.util.DevanagariFontManager.getRegularTypeface(context)
    }

    private val boldTypeface: Typeface by lazy {
        com.keofi.poonamashishmehta_votergen.util.DevanagariFontManager.getBoldTypeface(context)
    }

    /**
     * Renders a complete slip as a 384px wide Bitmap via Android Canvas.
     * All text (English, Hindi/Devanagari, numbers) is rendered natively by Android OS,
     * ensuring 100% correct glyph rendering on thermal printers without font dependencies.
     */
    fun renderThermalBitmap(slipData: SlipData): Bitmap {
        // Measure required height first
        val measuredHeight = measureRequiredHeight(slipData)

        val bitmap = Bitmap.createBitmap(PRINTER_WIDTH_DOTS, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = regularTypeface
        }

        val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = boldTypeface
        }

        val paintLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }

        var currentY = 16f

        // 1. Header Image or Candidate Title (if Custom Image mode)
        if (slipData.slipType == SlipType.CUSTOM_IMAGE) {
            val customBitmap = loadCustomHeaderImage(slipData.headerImageUri)
            if (customBitmap != null) {
                val scaled = ImageConverter.resizeToWidth(customBitmap, PRINTER_WIDTH_DOTS)
                canvas.drawBitmap(scaled, 0f, currentY, null)
                currentY += scaled.height + 12f
                scaled.recycle()
            } else {
                // Fallback default candidate header
                currentY = drawDefaultCandidateHeader(canvas, slipData, currentY, paintBold, paintText)
            }
        } else {
            // Text Only header
            currentY = drawTextOnlyHeader(canvas, slipData, currentY, paintBold, paintText)
        }

        // Horizontal Divider
        canvas.drawLine(PADDING_X, currentY, PRINTER_WIDTH_DOTS - PADDING_X, currentY, paintLine)
        currentY += 18f

        // 2. Voter Name (Large & Prominent)
        paintBold.textSize = 28f
        canvas.drawText(slipData.voterName, PADDING_X, currentY, paintBold)
        currentY += 32f

        // Hindi Name if distinct from primary name
        if (!slipData.voterNameHindi.isNullOrBlank() &&
            !slipData.voterNameHindi.equals(slipData.voterName, ignoreCase = true)
        ) {
            paintText.textSize = 22f
            canvas.drawText(slipData.voterNameHindi, PADDING_X, currentY, paintText)
            currentY += 28f
        }

        // EPIC Number Box / Row
        paintBold.textSize = 22f
        canvas.drawText("EPIC: ${slipData.epicNumber.ifEmpty { "NOT SPECIFIED" }}", PADDING_X, currentY, paintBold)
        currentY += 28f

        // Relative name & Relationship
        if (!slipData.relativeName.isNullOrBlank()) {
            val relLabel = if (!slipData.relationship.isNullOrBlank()) "${slipData.relationship}: " else "Relative: "
            paintText.textSize = 19f
            canvas.drawText("$relLabel${slipData.relativeName}", PADDING_X, currentY, paintText)
            currentY += 26f
        }

        // Subtle divider
        canvas.drawLine(PADDING_X, currentY, PRINTER_WIDTH_DOTS - PADDING_X, currentY, paintLine)
        currentY += 22f

        // Two-column layout for key voting fields
        paintBold.textSize = 19f
        paintText.textSize = 19f

        val col2X = PRINTER_WIDTH_DOTS / 2f

        // Row: Age & Gender
        val ageStr = if (slipData.age != null) "${slipData.age}" else "-"
        val genderStr = slipData.gender ?: "-"
        canvas.drawText("Age: $ageStr", PADDING_X, currentY, paintText)
        canvas.drawText("Gender: $genderStr", col2X, currentY, paintText)
        currentY += 26f

        // Row: Part No & Serial No
        val partStr = slipData.partNumber ?: "-"
        val serialStr = if (slipData.serialNumber != null) "${slipData.serialNumber}" else "-"
        canvas.drawText("Part: $partStr", PADDING_X, currentY, paintBold)
        canvas.drawText("Serial: $serialStr", col2X, currentY, paintBold)
        currentY += 26f

        // Row: House No
        if (!slipData.houseNumber.isNullOrBlank()) {
            canvas.drawText("House No: ${slipData.houseNumber}", PADDING_X, currentY, paintText)
            currentY += 26f
        }

        // Polling Station
        currentY += 4f
        paintBold.textSize = 17f
        canvas.drawText("Polling Station:", PADDING_X, currentY, paintBold)
        currentY += 22f
        paintText.textSize = 17f
        val psText = if (!slipData.pollingStation.isNullOrBlank()) slipData.pollingStation!! else "Not Available"
        val wrapped = wrapText(psText, paintText, PRINTER_WIDTH_DOTS - (PADDING_X * 2))
        for (line in wrapped) {
            canvas.drawText(line, PADDING_X, currentY, paintText)
            currentY += 22f
        }

        // Bottom Divider
        currentY += 10f
        canvas.drawLine(PADDING_X, currentY, PRINTER_WIDTH_DOTS - PADDING_X, currentY, paintLine)
        currentY += 20f

        // Footer: Timestamp
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        paintText.textSize = 14f
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText("Issued: ${dateFormat.format(Date())}", PRINTER_WIDTH_DOTS / 2f, currentY, paintText)
        paintText.textAlign = Paint.Align.LEFT
        currentY += 30f // Feed margin

        return bitmap
    }

    private fun drawDefaultCandidateHeader(
        canvas: Canvas,
        slipData: SlipData,
        startY: Float,
        paintBold: Paint,
        paintText: Paint
    ): Float {
        var y = startY + 12f
        paintBold.textSize = 24f
        paintBold.textAlign = Paint.Align.CENTER
        canvas.drawText(slipData.candidateName.uppercase(Locale.ROOT), PRINTER_WIDTH_DOTS / 2f, y, paintBold)
        y += 26f

        paintText.textSize = 17f
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText(slipData.tagline, PRINTER_WIDTH_DOTS / 2f, y, paintText)
        y += 18f

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
        var y = startY + 10f
        paintBold.textSize = 22f
        paintBold.textAlign = Paint.Align.CENTER
        canvas.drawText("VOTER INFORMATION SLIP", PRINTER_WIDTH_DOTS / 2f, y, paintBold)
        y += 18f

        paintBold.textAlign = Paint.Align.LEFT
        paintText.textAlign = Paint.Align.LEFT
        return y
    }

    private fun measureRequiredHeight(slipData: SlipData): Int {
        var height = 240
        if (slipData.slipType == SlipType.CUSTOM_IMAGE) {
            val bmp = loadCustomHeaderImage(slipData.headerImageUri)
            if (bmp != null) {
                val aspect = bmp.height.toFloat() / bmp.width.toFloat()
                height += (PRINTER_WIDTH_DOTS * aspect).toInt()
                bmp.recycle()
            } else {
                height += 60
            }
        } else {
            height += 40
        }
        if (!slipData.voterNameHindi.isNullOrBlank() && !slipData.voterNameHindi.equals(slipData.voterName, ignoreCase = true)) {
            height += 30
        }
        if (!slipData.relativeName.isNullOrBlank()) height += 30
        if (!slipData.houseNumber.isNullOrBlank()) height += 30
        val psText = if (!slipData.pollingStation.isNullOrBlank()) slipData.pollingStation!! else "Not Available"
        val psLines = ((psText.length / 28) + 1).coerceAtLeast(1)
        height += 24 + (psLines * 22)
        return height.coerceAtLeast(350)
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

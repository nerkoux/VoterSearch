package com.keofi.poonamashishmehta_votergen.data.printer

import android.graphics.Bitmap
import com.keofi.poonamashishmehta_votergen.data.slip.ImageConverter
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

enum class EscPosAlign(val value: Byte) {
    LEFT(0),
    CENTER(1),
    RIGHT(2)
}

class EscPosEncoder {

    private val buffer = ByteArrayOutputStream()

    fun initialize(): EscPosEncoder {
        buffer.write(byteArrayOf(0x1B, 0x40)) // ESC @
        return this
    }

    fun setAlignment(align: EscPosAlign): EscPosEncoder {
        buffer.write(byteArrayOf(0x1B, 0x61, align.value)) // ESC a n
        return this
    }

    fun setBold(enable: Boolean): EscPosEncoder {
        buffer.write(byteArrayOf(0x1B, 0x45, if (enable) 1 else 0)) // ESC E n
        return this
    }

    fun printText(text: String, charset: Charset = Charsets.UTF_8): EscPosEncoder {
        buffer.write(text.toByteArray(charset))
        return this
    }

    fun printTextLine(text: String, charset: Charset = Charsets.UTF_8): EscPosEncoder {
        buffer.write(text.toByteArray(charset))
        buffer.write(byteArrayOf(0x0A))
        return this
    }

    fun feedLines(lines: Int = 3): EscPosEncoder {
        buffer.write(byteArrayOf(0x1B, 0x64, lines.coerceIn(1, 10).toByte())) // ESC d n
        return this
    }

    /**
     * Encodes a bitmap directly as an ESC/POS GS v 0 raster image.
     * Uses Floyd-Steinberg dithering for crisp monochrome thermal reproduction.
     */
    fun printBitmap(bitmap: Bitmap): EscPosEncoder {
        val dithered = ImageConverter.toMonochromeDithered(bitmap)
        val rasterBytes = ImageConverter.toEscPosRasterBytes(dithered)
        buffer.write(rasterBytes)
        dithered.recycle()
        return this
    }

    fun cut(fullCut: Boolean = false): EscPosEncoder {
        // GS V m
        buffer.write(byteArrayOf(0x1D, 0x56, if (fullCut) 0x00 else 0x01))
        return this
    }

    fun toByteArray(): ByteArray = buffer.toByteArray()

    fun reset(): EscPosEncoder {
        buffer.reset()
        return this
    }
}

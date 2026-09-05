package com.keofi.poonamashishmehta_votergen.data.slip

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImageConverter {

    /**
     * Resizes bitmap to target width while preserving aspect ratio.
     */
    fun resizeToWidth(source: Bitmap, targetWidth: Int): Bitmap {
        val aspectRatio = source.height.toFloat() / source.width.toFloat()
        val targetHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    /**
     * Converts a bitmap to a 1-bit monochrome bitmap using Floyd-Steinberg error diffusion dithering.
     * This creates sharp photographic and text reproduction on 203 DPI thermal paper.
     */
    fun toMonochromeDithered(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height

        // Extract grayscale values into a 2D float array
        val gray = Array(height) { FloatArray(width) }
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                // Standard luminance formula
                gray[y][x] = (0.299f * r + 0.587f * g + 0.114f * b)
            }
        }

        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val outPixels = IntArray(width * height)

        // Floyd-Steinberg error diffusion
        for (y in 0 until height) {
            for (x in 0 until width) {
                val oldVal = gray[y][x]
                val newVal = if (oldVal < 128f) 0f else 255f
                val error = oldVal - newVal

                outPixels[y * width + x] = if (newVal == 0f) Color.BLACK else Color.WHITE

                if (x + 1 < width) {
                    gray[y][x + 1] += error * (7f / 16f)
                }
                if (y + 1 < height) {
                    if (x - 1 >= 0) {
                        gray[y + 1][x - 1] += error * (3f / 16f)
                    }
                    gray[y + 1][x] += error * (5f / 16f)
                    if (x + 1 < width) {
                        gray[y + 1][x + 1] += error * (1f / 16f)
                    }
                }
            }
        }

        outBitmap.setPixels(outPixels, 0, width, 0, 0, width, height)
        return outBitmap
    }

    /**
     * Converts a monochrome bitmap into standard ESC/POS GS v 0 raster bit image bytes.
     * Compatible with F2C CX588 and standard 58mm thermal printers.
     */
    fun toEscPosRasterBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // ESC/POS GS v 0 m xL xH yL yH
        // 8 header bytes + (widthBytes * height) data bytes
        val header = byteArrayOf(
            0x1D, 0x76, 0x30, 0x00, // GS v 0 0 (normal mode)
            (widthBytes and 0xFF).toByte(),
            ((widthBytes shr 8) and 0xFF).toByte(),
            (height and 0xFF).toByte(),
            ((height shr 8) and 0xFF).toByte()
        )

        val data = ByteArray(widthBytes * height)
        var offset = 0

        for (y in 0 until height) {
            for (byteX in 0 until widthBytes) {
                var byteVal = 0
                for (bit in 0 until 8) {
                    val x = byteX * 8 + bit
                    if (x < width) {
                        val pixel = pixels[y * width + x]
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF
                        val luminance = (r + g + b) / 3
                        // On thermal paper: 1 is black dot (burn), 0 is white
                        if (luminance < 128) {
                            byteVal = byteVal or (1 shl (7 - bit))
                        }
                    }
                }
                data[offset++] = byteVal.toByte()
            }
        }

        return header + data
    }
}

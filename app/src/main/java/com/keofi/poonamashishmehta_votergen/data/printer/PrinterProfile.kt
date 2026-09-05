package com.keofi.poonamashishmehta_votergen.data.printer

data class PrinterProfile(
    val modelName: String,
    val paperWidthDots: Int,
    val dpi: Int = 203,
    val supportsImage: Boolean = true,
    val supportsCut: Boolean = false,
    val supportsQr: Boolean = true,
    val supportsBold: Boolean = true
) {
    companion object {
        // F2C CX588 58mm portable Bluetooth thermal printer default preset
        val F2C_CX588 = PrinterProfile(
            modelName = "F2C CX588",
            paperWidthDots = 384,
            dpi = 203,
            supportsImage = true,
            supportsCut = false,
            supportsQr = true,
            supportsBold = true
        )

        val GENERIC_58MM = PrinterProfile(
            modelName = "Generic 58mm ESC/POS",
            paperWidthDots = 384,
            dpi = 203,
            supportsImage = true,
            supportsCut = false,
            supportsQr = true,
            supportsBold = true
        )

        val GENERIC_80MM = PrinterProfile(
            modelName = "Generic 80mm ESC/POS",
            paperWidthDots = 576,
            dpi = 203,
            supportsImage = true,
            supportsCut = true,
            supportsQr = true,
            supportsBold = true
        )
    }
}

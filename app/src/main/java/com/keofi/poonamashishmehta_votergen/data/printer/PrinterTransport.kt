package com.keofi.poonamashishmehta_votergen.data.printer

interface PrinterTransport {
    val isConnected: Boolean
    suspend fun connect(address: String): Result<Unit>
    suspend fun send(data: ByteArray): Result<Unit>
    suspend fun disconnect()
}

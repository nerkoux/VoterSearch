package com.keofi.poonamashishmehta_votergen.data.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinterTransport(private val context: Context) : PrinterTransport {

    companion object {
        // Standard Bluetooth Serial Port Profile (SPP) UUID
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val CONNECT_TIMEOUT_MS = 10000L
    }

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override val isConnected: Boolean
        get() = socket?.isConnected == true

    @SuppressLint("MissingPermission")
    override suspend fun connect(address: String): Result<Unit> = withContext(Dispatchers.IO) {
        disconnect()

        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(Exception("This device does not support Bluetooth."))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(Exception("Bluetooth is turned off. Please enable Bluetooth."))
        }

        try {
            // Cancel discovery before connecting for stable connection
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }

            val device: BluetoothDevice = adapter.getRemoteDevice(address)
                ?: return@withContext Result.failure(Exception("Printer device with address $address not found."))

            val newSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            val connectSuccess = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                try {
                    newSocket.connect()
                    true
                } catch (_: Exception) {
                    false
                }
            }

            if (connectSuccess != true || !newSocket.isConnected) {
                try { newSocket.close() } catch (_: Exception) {}
                return@withContext Result.failure(Exception("Could not connect to printer. Ensure it is powered on and in range."))
            }

            socket = newSocket
            outputStream = newSocket.outputStream
            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(Exception("Bluetooth permission is required to connect to the printer."))
        } catch (e: Exception) {
            disconnect()
            Result.failure(Exception(e.localizedMessage ?: "Could not connect to printer."))
        }
    }

    override suspend fun send(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val stream = outputStream
        if (socket?.isConnected != true || stream == null) {
            return@withContext Result.failure(Exception("Printer is not connected."))
        }

        try {
            // Send in chunks of 512 bytes with brief delay to avoid overwhelming portable printer buffers
            val chunkSize = 512
            var offset = 0
            while (offset < data.size) {
                val end = (offset + chunkSize).coerceAtMost(data.size)
                stream.write(data, offset, end - offset)
                stream.flush()
                offset = end
                Thread.sleep(15) // small pacing for thermal head
            }
            Result.success(Unit)
        } catch (e: Exception) {
            disconnect()
            Result.failure(Exception("Print failed: ${e.localizedMessage ?: "Connection lost during printing."}"))
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        try { outputStream?.flush() } catch (_: Exception) {}
        try { outputStream?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        outputStream = null
        socket = null
    }
}

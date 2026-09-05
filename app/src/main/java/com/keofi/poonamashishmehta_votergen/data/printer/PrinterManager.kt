package com.keofi.poonamashishmehta_votergen.data.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.keofi.poonamashishmehta_votergen.data.preferences.AppPreferences
import com.keofi.poonamashishmehta_votergen.data.slip.SlipData
import com.keofi.poonamashishmehta_votergen.data.slip.ThermalSlipRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class DiscoveredPrinter(
    val name: String,
    val address: String,
    val isPaired: Boolean
)

sealed interface PrintStatus {
    data object Idle : PrintStatus
    data class Connecting(val printerName: String) : PrintStatus
    data object Printing : PrintStatus
    data class Success(val message: String) : PrintStatus
    data class Error(val message: String) : PrintStatus
}

class PrinterManager(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val thermalSlipRenderer: ThermalSlipRenderer
) {
    private val transport = BluetoothPrinterTransport(context)
    private var activeProfile = PrinterProfile.F2C_CX588

    private val _printStatus = MutableStateFlow<PrintStatus>(PrintStatus.Idle)
    val printStatus: StateFlow<PrintStatus> = _printStatus.asStateFlow()

    private val _discoveredPrinters = MutableStateFlow<List<DiscoveredPrinter>>(emptyList())
    val discoveredPrinters: StateFlow<List<DiscoveredPrinter>> = _discoveredPrinters.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var discoveryReceiver: BroadcastReceiver? = null

    /**
     * Retrieves all paired Bluetooth devices from the system adapter.
     */
    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<DiscoveredPrinter> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()

        return try {
            val bonded = adapter.bondedDevices ?: emptySet()
            bonded.map { device ->
                DiscoveredPrinter(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    isPaired = true
                )
            }
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    /**
     * Starts Bluetooth discovery for nearby thermal printers.
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        if (!adapter.isEnabled) return

        stopDiscovery()
        _isScanning.value = true

        val initialList = getPairedPrinters().toMutableList()
        _discoveredPrinters.value = initialList

        discoveryReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        if (device != null) {
                            val name = try { device.name } catch (_: SecurityException) { null } ?: "Bluetooth Device"
                            val address = device.address
                            val exists = _discoveredPrinters.value.any { it.address == address }
                            if (!exists) {
                                _discoveredPrinters.value = _discoveredPrinters.value + DiscoveredPrinter(
                                    name = name,
                                    address = address,
                                    isPaired = false
                                )
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        _isScanning.value = false
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        try {
            context.registerReceiver(discoveryReceiver, filter)
            adapter.startDiscovery()
        } catch (_: Exception) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        try {
            if (adapter?.isDiscovering == true) {
                adapter.cancelDiscovery()
            }
        } catch (_: SecurityException) {}

        if (discoveryReceiver != null) {
            try {
                context.unregisterReceiver(discoveryReceiver)
            } catch (_: Exception) {}
            discoveryReceiver = null
        }
        _isScanning.value = false
    }

    /**
     * Prints a voter slip to the selected Bluetooth printer.
     */
    suspend fun printSlip(slipData: SlipData): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = appPreferences.printerSettingsFlow.first()
        val address = prefs.selectedPrinterAddress
        val printerName = prefs.selectedPrinterName ?: "Thermal Printer"

        if (address.isNullOrBlank()) {
            val errorMsg = "No printer selected. Please select or scan for your F2C CX588 printer in the Printer tab."
            _printStatus.value = PrintStatus.Error(errorMsg)
            return@withContext Result.failure(Exception(errorMsg))
        }

        _printStatus.value = PrintStatus.Connecting(printerName)

        val connectResult = transport.connect(address)
        if (connectResult.isFailure) {
            val msg = connectResult.exceptionOrNull()?.localizedMessage ?: "Could not connect to printer."
            _printStatus.value = PrintStatus.Error(msg)
            return@withContext Result.failure(Exception(msg))
        }

        _printStatus.value = PrintStatus.Printing

        try {
            // Render slip as 384-dot monochrome bitmap
            val slipBitmap = thermalSlipRenderer.renderThermalBitmap(slipData)

            val encoder = EscPosEncoder()
                .initialize()
                .setAlignment(EscPosAlign.CENTER)
                .printBitmap(slipBitmap)
                .feedLines(4)

            slipBitmap.recycle()

            val sendResult = transport.send(encoder.toByteArray())
            transport.disconnect()

            if (sendResult.isSuccess) {
                _printStatus.value = PrintStatus.Success("Printed successfully to $printerName")
                Result.success(Unit)
            } else {
                val msg = sendResult.exceptionOrNull()?.localizedMessage ?: "Print failed."
                _printStatus.value = PrintStatus.Error(msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            transport.disconnect()
            val msg = e.localizedMessage ?: "Unable to generate or print slip."
            _printStatus.value = PrintStatus.Error(msg)
            Result.failure(e)
        }
    }

    /**
     * Executes a hardware test print on the configured printer.
     */
    suspend fun testPrint(): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = appPreferences.printerSettingsFlow.first()
        val address = prefs.selectedPrinterAddress
        val printerName = prefs.selectedPrinterName ?: "Thermal Printer"

        if (address.isNullOrBlank()) {
            val errorMsg = "No printer selected. Please connect to your printer first."
            _printStatus.value = PrintStatus.Error(errorMsg)
            return@withContext Result.failure(Exception(errorMsg))
        }

        _printStatus.value = PrintStatus.Connecting(printerName)

        val connectResult = transport.connect(address)
        if (connectResult.isFailure) {
            val msg = connectResult.exceptionOrNull()?.localizedMessage ?: "Could not connect to printer."
            _printStatus.value = PrintStatus.Error(msg)
            return@withContext Result.failure(Exception(msg))
        }

        _printStatus.value = PrintStatus.Printing

        try {
            // Test slip data
            val testSlip = SlipData(
                voterName = "TEST VOTER",
                voterNameHindi = "परीक्षण मतदाता",
                epicNumber = "TEST1234567",
                relativeName = "Election Commission",
                relationship = "Office",
                age = 35,
                gender = "Male",
                partNumber = "001",
                serialNumber = 1,
                houseNumber = "1A",
                pollingStation = "Booth No. 1 - Test Station",
                candidateName = "F2C CX588 58mm Printer Test",
                tagline = "Thermal ESC/POS Hardware Verification"
            )

            val testBitmap = thermalSlipRenderer.renderThermalBitmap(testSlip)

            val encoder = EscPosEncoder()
                .initialize()
                .setAlignment(EscPosAlign.CENTER)
                .printBitmap(testBitmap)
                .feedLines(4)

            testBitmap.recycle()

            val sendResult = transport.send(encoder.toByteArray())
            transport.disconnect()

            if (sendResult.isSuccess) {
                _printStatus.value = PrintStatus.Success("Test print completed!")
                Result.success(Unit)
            } else {
                val msg = sendResult.exceptionOrNull()?.localizedMessage ?: "Test print failed."
                _printStatus.value = PrintStatus.Error(msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            transport.disconnect()
            val msg = e.localizedMessage ?: "Test print failed."
            _printStatus.value = PrintStatus.Error(msg)
            Result.failure(e)
        }
    }

    fun clearStatus() {
        _printStatus.value = PrintStatus.Idle
    }
}

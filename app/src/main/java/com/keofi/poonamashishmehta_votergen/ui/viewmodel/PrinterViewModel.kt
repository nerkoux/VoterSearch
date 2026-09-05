package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.preferences.PrinterSettings
import com.keofi.poonamashishmehta_votergen.data.printer.DiscoveredPrinter
import com.keofi.poonamashishmehta_votergen.data.printer.PrintStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PrinterViewModel : ViewModel() {
    private val app = VoterApp.instance
    private val printerManager = app.printerManager
    private val prefs = app.appPreferences

    val printerSettings: StateFlow<PrinterSettings> = prefs.printerSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrinterSettings())

    val printStatus: StateFlow<PrintStatus> = printerManager.printStatus

    val discoveredPrinters: StateFlow<List<DiscoveredPrinter>> = printerManager.discoveredPrinters

    val isScanning: StateFlow<Boolean> = printerManager.isScanning

    fun scanPrinters() {
        printerManager.startDiscovery()
    }

    fun stopScan() {
        printerManager.stopDiscovery()
    }

    fun selectPrinter(name: String, address: String) {
        viewModelScope.launch {
            prefs.setPrinter(name, address)
        }
    }

    fun forgetPrinter() {
        viewModelScope.launch {
            prefs.setPrinter(null, null)
        }
    }

    fun runTestPrint() {
        viewModelScope.launch {
            printerManager.testPrint()
        }
    }

    fun clearStatus() {
        printerManager.clearStatus()
    }

    override fun onCleared() {
        super.onCleared()
        printerManager.stopDiscovery()
    }
}

package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.preferences.PrinterSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalVoters: Int = 0,
    val totalLists: Int = 0,
    val printerSettings: PrinterSettings = PrinterSettings()
)

class HomeViewModel : ViewModel() {
    private val app = VoterApp.instance
    private val voterRepo = app.voterRepository
    private val listRepo = app.voterListRepository
    private val prefs = app.appPreferences

    val totalVoters: StateFlow<Int> = voterRepo.getTotalVoterCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalLists: StateFlow<Int> = listRepo.getTotalListsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val printerSettings: StateFlow<PrinterSettings> = prefs.printerSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrinterSettings())
}

package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipSettings
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val app = VoterApp.instance
    private val prefs = app.appPreferences
    private val listRepo = app.voterListRepository

    val slipSettings: StateFlow<SlipSettings> = prefs.slipSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SlipSettings())

    val themeMode: StateFlow<String> = prefs.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    fun setSlipType(type: SlipType) {
        viewModelScope.launch {
            prefs.setSlipType(type)
        }
    }

    fun setHeaderImageUri(uriString: String?) {
        viewModelScope.launch {
            prefs.setHeaderImageUri(uriString)
        }
    }

    fun setCandidateName(name: String) {
        viewModelScope.launch {
            prefs.setCandidateName(name)
        }
    }

    fun setTagline(tagline: String) {
        viewModelScope.launch {
            prefs.setTagline(tagline)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            listRepo.deleteAllData()
            onComplete()
        }
    }
}

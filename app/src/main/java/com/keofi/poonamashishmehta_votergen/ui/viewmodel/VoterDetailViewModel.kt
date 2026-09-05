package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.slip.SlipData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VoterDetailViewModel : ViewModel() {
    private val app = VoterApp.instance
    private val voterRepo = app.voterRepository
    private val prefs = app.appPreferences
    private val printerManager = app.printerManager
    private val shareManager = app.slipShareManager

    private val _voter = MutableStateFlow<VoterEntity?>(null)
    val voter: StateFlow<VoterEntity?> = _voter.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadVoter(voterId: Long) {
        viewModelScope.launch {
            _voter.value = voterRepo.getVoterByIdSync(voterId)
        }
    }

    fun printDirectly() {
        val currentVoter = _voter.value ?: return
        viewModelScope.launch {
            val slipSettings = prefs.slipSettingsFlow.first()
            val slipData = SlipData.fromVoter(currentVoter, slipSettings)
            val result = printerManager.printSlip(slipData)
            if (result.isSuccess) {
                _actionMessage.value = "Sent to printer!"
            } else {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Print failed"
            }
        }
    }

    fun shareDirectly() {
        val currentVoter = _voter.value ?: return
        viewModelScope.launch {
            val slipSettings = prefs.slipSettingsFlow.first()
            val slipData = SlipData.fromVoter(currentVoter, slipSettings)
            val result = shareManager.shareImage(slipData)
            if (result.isFailure) {
                _actionMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Sharing failed"
            }
        }
    }

    fun updateVoter(updatedVoter: VoterEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            voterRepo.updateVoter(updatedVoter)
            _voter.value = updatedVoter
            onComplete()
        }
    }

    fun clearMessage() {
        _actionMessage.value = null
    }
}

package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType
import com.keofi.poonamashishmehta_votergen.data.slip.SlipData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SlipPreviewViewModel : ViewModel() {
    private val app = VoterApp.instance
    private val voterRepo = app.voterRepository
    private val prefs = app.appPreferences
    private val printerManager = app.printerManager
    private val shareManager = app.slipShareManager

    private val _voter = MutableStateFlow<VoterEntity?>(null)
    val voter: StateFlow<VoterEntity?> = _voter.asStateFlow()

    private val _slipData = MutableStateFlow<SlipData?>(null)
    val slipData: StateFlow<SlipData?> = _slipData.asStateFlow()

    private val _previewMode = MutableStateFlow(SlipType.CUSTOM_IMAGE)
    val previewMode: StateFlow<SlipType> = _previewMode.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun loadVoter(voterId: Long) {
        viewModelScope.launch {
            val v = voterRepo.getVoterByIdSync(voterId)
            _voter.value = v
            if (v != null) {
                val settings = prefs.slipSettingsFlow.first()
                _previewMode.value = settings.slipType
                _slipData.value = SlipData.fromVoter(v, settings)
            }
        }
    }

    fun setSlipMode(mode: SlipType) {
        _previewMode.value = mode
        val currentSlip = _slipData.value ?: return
        _slipData.value = currentSlip.copy(slipType = mode)
    }

    fun printSlip() {
        val currentSlip = _slipData.value ?: return
        viewModelScope.launch {
            val res = printerManager.printSlip(currentSlip)
            if (res.isSuccess) {
                _statusMessage.value = "Printing slip..."
            } else {
                _statusMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Print failed"
            }
        }
    }

    fun shareImage() {
        val currentSlip = _slipData.value ?: return
        viewModelScope.launch {
            val res = shareManager.shareImage(currentSlip)
            if (res.isFailure) {
                _statusMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Share failed"
            }
        }
    }

    fun sharePdf() {
        val currentSlip = _slipData.value ?: return
        viewModelScope.launch {
            val res = shareManager.sharePdf(currentSlip)
            if (res.isFailure) {
                _statusMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Share failed"
            }
        }
    }

    fun saveImage() {
        val currentSlip = _slipData.value ?: return
        viewModelScope.launch {
            val res = shareManager.saveImageToDevice(currentSlip)
            if (res.isSuccess) {
                _statusMessage.value = res.getOrNull()
            } else {
                _statusMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Save failed"
            }
        }
    }

    fun savePdf() {
        val currentSlip = _slipData.value ?: return
        viewModelScope.launch {
            val res = shareManager.savePdfToDevice(currentSlip)
            if (res.isSuccess) {
                _statusMessage.value = res.getOrNull()
            } else {
                _statusMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Save failed"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}

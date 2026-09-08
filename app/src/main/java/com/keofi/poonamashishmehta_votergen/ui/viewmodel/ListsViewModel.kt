package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterListEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListsViewModel : ViewModel() {
    private val app = VoterApp.instance
    private val listRepo = app.voterListRepository
    private val voterRepo = app.voterRepository

    val allLists: StateFlow<List<VoterListEntity>> = listRepo.getAllLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedList = MutableStateFlow<VoterListEntity?>(null)
    val selectedList: StateFlow<VoterListEntity?> = _selectedList.asStateFlow()

    private val _listVoters = MutableStateFlow<List<VoterEntity>>(emptyList())
    val listVoters: StateFlow<List<VoterEntity>> = _listVoters.asStateFlow()

    private val _reviewVoters = MutableStateFlow<List<VoterEntity>>(emptyList())
    val reviewVoters: StateFlow<List<VoterEntity>> = _reviewVoters.asStateFlow()

    private val _selectedBooth = MutableStateFlow<String?>(null)
    val selectedBooth: StateFlow<String?> = _selectedBooth.asStateFlow()

    private val _listBooths = MutableStateFlow<List<String>>(emptyList())
    val listBooths: StateFlow<List<String>> = _listBooths.asStateFlow()

    private var currentListId: Long = 0
    private var votersJob: kotlinx.coroutines.Job? = null

    fun selectList(listId: Long) {
        currentListId = listId
        _selectedBooth.value = null
        viewModelScope.launch {
            _selectedList.value = listRepo.getListByIdSync(listId)
        }
        loadVotersForList(listId, null)
        viewModelScope.launch {
            voterRepo.getBoothsForList(listId).collect {
                _listBooths.value = it
            }
        }
        viewModelScope.launch {
            voterRepo.getVotersNeedingReview(listId).collect {
                _reviewVoters.value = it
            }
        }
    }

    fun selectBooth(booth: String?) {
        val newBooth = if (_selectedBooth.value == booth) null else booth
        _selectedBooth.value = newBooth
        if (currentListId > 0) {
            loadVotersForList(currentListId, newBooth)
        }
    }

    private fun loadVotersForList(listId: Long, booth: String?) {
        votersJob?.cancel()
        votersJob = viewModelScope.launch {
            if (booth.isNullOrBlank()) {
                voterRepo.getVotersByList(listId, limit = 200).collect {
                    _listVoters.value = it
                }
            } else {
                voterRepo.getVotersByBoothInList(listId, booth, limit = 200).collect {
                    _listVoters.value = it
                }
            }
        }
    }

    fun renameList(listId: Long, newName: String) {
        viewModelScope.launch {
            listRepo.renameList(listId, newName)
            _selectedList.value = listRepo.getListByIdSync(listId)
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            listRepo.deleteList(listId)
            if (_selectedList.value?.id == listId) {
                _selectedList.value = null
            }
        }
    }

    fun updateVoter(voter: VoterEntity) {
        viewModelScope.launch {
            voterRepo.updateVoter(voter)
            _selectedList.value?.id?.let { selectList(it) }
        }
    }
}

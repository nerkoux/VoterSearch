package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel : ViewModel() {
    private val voterRepo = VoterApp.instance.voterRepository

    // Search input states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _relativeQuery = MutableStateFlow("")
    val relativeQuery: StateFlow<String> = _relativeQuery.asStateFlow()

    private val _selectedBooth = MutableStateFlow<String?>(null)
    val selectedBooth: StateFlow<String?> = _selectedBooth.asStateFlow()

    private val _isAdvancedSearch = MutableStateFlow(false)
    val isAdvancedSearch: StateFlow<Boolean> = _isAdvancedSearch.asStateFlow()

    // Distinct booths available in the database
    val availableBooths: StateFlow<List<String>> = voterRepo.getAllBooths()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class CombinedSearchParams(
        val voterQuery: String,
        val relativeQuery: String,
        val booth: String,
        val isAdvanced: Boolean
    )

    val searchResults: StateFlow<List<VoterEntity>> = combine(
        _searchQuery,
        _relativeQuery,
        _selectedBooth,
        _isAdvancedSearch
    ) { voterQ, relQ, booth, isAdvanced ->
        CombinedSearchParams(
            voterQuery = voterQ.trim(),
            relativeQuery = if (isAdvanced) relQ.trim() else "",
            booth = booth ?: "",
            isAdvanced = isAdvanced
        )
    }
        .debounce(150L)
        .distinctUntilChanged()
        .flatMapLatest { params ->
            val hasQuery = params.voterQuery.isNotEmpty() ||
                    params.relativeQuery.isNotEmpty() ||
                    params.booth.isNotEmpty()

            if (!hasQuery) {
                flowOf(emptyList())
            } else {
                voterRepo.searchVotersAdvanced(
                    voterQuery = params.voterQuery,
                    relativeQuery = params.relativeQuery,
                    booth = params.booth,
                    limit = 150
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onRelativeQueryChange(newRelative: String) {
        _relativeQuery.value = newRelative
    }

    fun selectBooth(booth: String?) {
        _selectedBooth.value = if (_selectedBooth.value == booth) null else booth
    }

    fun toggleAdvancedSearch() {
        val next = !_isAdvancedSearch.value
        _isAdvancedSearch.value = next
        if (!next) {
            _relativeQuery.value = ""
        }
    }

    fun setAdvancedSearch(enabled: Boolean) {
        _isAdvancedSearch.value = enabled
        if (!enabled) {
            _relativeQuery.value = ""
        }
    }

    fun clearQuery() {
        _searchQuery.value = ""
    }

    fun clearRelativeQuery() {
        _relativeQuery.value = ""
    }

    fun clearAll() {
        _searchQuery.value = ""
        _relativeQuery.value = ""
        _selectedBooth.value = null
    }
}

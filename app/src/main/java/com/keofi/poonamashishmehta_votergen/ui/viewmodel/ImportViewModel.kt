package com.keofi.poonamashishmehta_votergen.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.importer.ImportState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class SelectedFileInfo(
    val uri: Uri,
    val fileName: String,
    val count: Int,
    val isSpreadsheet: Boolean
)

class ImportViewModel : ViewModel() {
    private val pdfManager = VoterApp.instance.pdfImportManager
    private val spreadsheetManager = VoterApp.instance.spreadsheetImportManager

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _selectedFile = MutableStateFlow<SelectedFileInfo?>(null)
    val selectedFile: StateFlow<SelectedFileInfo?> = _selectedFile.asStateFlow()

    private val _inspectError = MutableStateFlow<String?>(null)
    val inspectError: StateFlow<String?> = _inspectError.asStateFlow()

    private var importJob: Job? = null

    init {
        // Collect state from both managers
        viewModelScope.launch {
            pdfManager.importState.collect { state ->
                if (_selectedFile.value?.isSpreadsheet == false || state !is ImportState.Idle) {
                    _importState.value = state
                }
            }
        }
        viewModelScope.launch {
            spreadsheetManager.importState.collect { state ->
                if (_selectedFile.value?.isSpreadsheet == true || state !is ImportState.Idle) {
                    _importState.value = state
                }
            }
        }
    }

    fun onPdfSelected(uri: Uri) {
        _inspectError.value = null
        viewModelScope.launch {
            val res = pdfManager.inspectPdf(uri)
            if (res.isSuccess) {
                val (name, pages) = res.getOrThrow()
                _selectedFile.value = SelectedFileInfo(uri, name, pages, isSpreadsheet = false)
            } else {
                _inspectError.value = res.exceptionOrNull()?.localizedMessage ?: "Unable to read selected PDF."
            }
        }
    }

    fun onSpreadsheetSelected(uri: Uri) {
        _inspectError.value = null
        viewModelScope.launch {
            val res = spreadsheetManager.inspectFile(uri)
            if (res.isSuccess) {
                val (name, rows) = res.getOrThrow()
                _selectedFile.value = SelectedFileInfo(uri, name, rows, isSpreadsheet = true)
            } else {
                _inspectError.value = res.exceptionOrNull()?.localizedMessage ?: "Unable to read selected spreadsheet."
            }
        }
    }

    fun startImport(customName: String? = null) {
        val file = _selectedFile.value ?: return
        importJob?.cancel()
        importJob = viewModelScope.launch {
            if (file.isSpreadsheet) {
                spreadsheetManager.importSpreadsheet(file.uri, customName)
            } else {
                pdfManager.importPdf(file.uri, customName)
            }
        }
    }

    fun cancelImport() {
        importJob?.cancel()
        pdfManager.resetState()
        spreadsheetManager.resetState()
        _importState.value = ImportState.Idle
    }

    fun reset() {
        _selectedFile.value = null
        _inspectError.value = null
        pdfManager.resetState()
        spreadsheetManager.resetState()
        _importState.value = ImportState.Idle
    }

    fun checkStateValidity() {
        val current = importState.value
        if (current is ImportState.Success) {
            viewModelScope.launch {
                val list = VoterApp.instance.voterListRepository.getListByIdSync(current.listId)
                if (list == null) {
                    reset()
                }
            }
        }
    }

    fun shareTemplate(context: Context) {
        try {
            val templateDir = File(context.cacheDir, "templates")
            if (!templateDir.exists()) templateDir.mkdirs()
            val templateFile = File(templateDir, "voter_list_template.csv")

            context.assets.open("voter_list_template.csv").use { input ->
                FileOutputStream(templateFile).use { output ->
                    input.copyTo(output)
                }
            }

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                templateFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Voter List Import Template")
                putExtra(Intent.EXTRA_TEXT, "Use this CSV template to fill and import voter lists into the app with 100% accuracy.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share or Save CSV Template").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Handle error or fallback
        }
    }
}

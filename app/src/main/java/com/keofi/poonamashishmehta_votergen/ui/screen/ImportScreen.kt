package com.keofi.poonamashishmehta_votergen.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.data.importer.ImportState
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.ErrorRed
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SuccessGreen
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.ImportViewModel

@Composable
fun ImportScreen(
    onBack: () -> Unit,
    onViewList: (Long) -> Unit,
    viewModel: ImportViewModel = viewModel()
) {
    val context = LocalContext.current
    val importState by viewModel.importState.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val inspectError by viewModel.inspectError.collectAsState()

    var customName by remember { mutableStateOf("") }
    var showPdfWarningDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkStateValidity()
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPdfSelected(uri)
        }
    }

    val spreadsheetPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onSpreadsheetSelected(uri)
        }
    }

    // PDF Accuracy Warning Dialog
    if (showPdfWarningDialog) {
        AlertDialog(
            onDismissRequest = { showPdfWarningDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = SaffronOrange,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "PDF Accuracy Advisory",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "PDF electoral rolls (especially scanned Hindi lists) may contain optical character recognition (OCR) errors, misspelled names, or missing details due to complex matras and conjuncts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "⭐ Recommendation: For 100% clean and verified names, use CSV or Excel (.xlsx) file import instead.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = DarkNavy
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPdfWarningDialog = false
                        spreadsheetPickerLauncher.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "*/*"
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                ) {
                    Text("Use CSV / Excel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPdfWarningDialog = false
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    }
                ) {
                    Text("Continue with PDF", color = SecondaryText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (importState is ImportState.Success || importState is ImportState.Error) {
                    viewModel.reset()
                }
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Import Voter List",
                style = MaterialTheme.typography.titleLarge,
                color = DarkNavy
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            when (val state = importState) {
                is ImportState.Idle -> {
                    // Option 1: Excel / CSV (Recommended)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        color = SuccessGreen.copy(alpha = 0.05f)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Excel / CSV File",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DarkNavy
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SuccessGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "RECOMMENDED",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = SuccessGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "100% accurate voter names and data with zero OCR spelling errors. Supports .xlsx and .csv formats.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    spreadsheetPickerLauncher.launch(
                                        arrayOf(
                                            "text/csv",
                                            "text/comma-separated-values",
                                            "application/vnd.ms-excel",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "*/*"
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select Excel / CSV File",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { viewModel.shareTemplate(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Download / Share CSV Template",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option 2: PDF Electoral Roll (Legacy/OCR)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                        color = LightSurface
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Electoral Roll PDF",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkNavy
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracts voter cards from digital or scanned PDFs via on-device OCR. May contain occasional spelling or matra errors.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showPdfWarningDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkNavy)
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = SaffronOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select PDF Roll",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
                                )
                            }
                        }
                    }

                    if (inspectError != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = inspectError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                    }

                    // Selected File Card
                    if (selectedFile != null) {
                        val file = selectedFile!!
                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                            color = Color.White
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.fileName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = PrimaryText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (file.isSpreadsheet) {
                                                "${file.count} voters detected in spreadsheet"
                                            } else {
                                                "${file.count} pages detected in PDF"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SecondaryText
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (file.isSpreadsheet) SuccessGreen.copy(alpha = 0.15f) else SaffronOrange.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (file.isSpreadsheet) "EXCEL / CSV" else "PDF",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (file.isSpreadsheet) SuccessGreen else SaffronOrange
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text("List Name (e.g. Ward 123)") },
                                    placeholder = { Text(file.fileName.substringBeforeLast(".")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = PrimaryText,
                                        fontSize = 16.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = PrimaryText,
                                        unfocusedTextColor = PrimaryText,
                                        focusedLabelColor = SaffronOrange,
                                        unfocusedLabelColor = SecondaryText,
                                        focusedPlaceholderColor = SecondaryText,
                                        unfocusedPlaceholderColor = SecondaryText,
                                        focusedBorderColor = SaffronOrange,
                                        unfocusedBorderColor = DividerGray,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = SaffronOrange
                                    )
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { viewModel.startImport(customName.ifBlank { null }) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SaffronOrange,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Import List",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                is ImportState.Processing -> {
                    Text(
                        text = if (state.isSpreadsheet) "Importing spreadsheet data" else "Processing voter list",
                        style = MaterialTheme.typography.titleLarge,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (state.isSpreadsheet) {
                            "Importing clean records from spreadsheet..."
                        } else if (state.isOcrMode) {
                            "Running on-device OCR..."
                        } else {
                            "Extracting selectable text..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        color = LightSurface
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (state.isSpreadsheet) {
                                        if (state.totalRecords > 0) {
                                            "Record ${java.text.NumberFormat.getNumberInstance().format(state.currentRecord)} / ${java.text.NumberFormat.getNumberInstance().format(state.totalRecords)}"
                                        } else {
                                            "${java.text.NumberFormat.getNumberInstance().format(state.votersDetected)} records processed"
                                        }
                                    } else {
                                        "Page ${state.currentPage} / ${state.totalPages}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = PrimaryText
                                )
                                Text(
                                    text = "${(state.progressPercent * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SaffronOrange
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { state.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = SaffronOrange,
                                trackColor = DividerGray
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Voters detected",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = java.text.NumberFormat.getNumberInstance().format(state.votersDetected),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { viewModel.cancelImport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                    ) {
                        Text("Cancel Import", style = MaterialTheme.typography.labelLarge)
                    }
                }

                is ImportState.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Import Complete",
                        style = MaterialTheme.typography.titleLarge,
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.listName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        color = LightSurface
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (state.isSpreadsheet) {
                                ReportRow("Source format", "Excel / CSV Spreadsheet")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)
                            } else {
                                ReportRow("Pages processed", "${state.totalPages}")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)
                            }
                            ReportRow("Total voters imported", java.text.NumberFormat.getNumberInstance().format(state.totalVoters), isBold = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)
                            ReportRow("High confidence", java.text.NumberFormat.getNumberInstance().format(state.highConfidence), valueColor = SuccessGreen)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)
                            ReportRow("Needs review", "${state.needsReview}", valueColor = if (state.needsReview > 0) SaffronOrange else PrimaryText)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)
                            ReportRow("EPIC detected", java.text.NumberFormat.getNumberInstance().format(state.epicDetected))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onViewList(state.listId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaffronOrange,
                            contentColor = Color.White
                        )
                    ) {
                        Text("View Report & Records", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Text("Import Another List", style = MaterialTheme.typography.labelLarge)
                    }
                }

                is ImportState.Error -> {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Import Failed",
                        style = MaterialTheme.typography.titleLarge,
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaffronOrange,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Try Again", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = PrimaryText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
            ),
            color = valueColor
        )
    }
}

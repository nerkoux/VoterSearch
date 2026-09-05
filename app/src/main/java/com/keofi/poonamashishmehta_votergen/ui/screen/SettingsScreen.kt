package com.keofi.poonamashishmehta_votergen.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import coil.compose.AsyncImage
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.ErrorRed
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToPrinter: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val slipSettings by viewModel.slipSettings.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setHeaderImageUri(uri.toString())
            Toast.makeText(context, "Header image updated", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configure appearance, slip layout, and preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Section 1: Slip Configuration
        SectionHeader("Slip")

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            color = LightSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Slip Type",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSlipType(SlipType.CUSTOM_IMAGE) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = slipSettings.slipType == SlipType.CUSTOM_IMAGE,
                        onClick = { viewModel.setSlipType(SlipType.CUSTOM_IMAGE) },
                        colors = RadioButtonDefaults.colors(selectedColor = SaffronOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Custom Image", style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSlipType(SlipType.TEXT_ONLY) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = slipSettings.slipType == SlipType.TEXT_ONLY,
                        onClick = { viewModel.setSlipType(SlipType.TEXT_ONLY) },
                        colors = RadioButtonDefaults.colors(selectedColor = SaffronOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Text Only", style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
                }

                if (slipSettings.slipType == SlipType.CUSTOM_IMAGE) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), thickness = 0.8.dp, color = DividerGray)

                    Text(
                        text = "Header Image",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!slipSettings.headerImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = slipSettings.headerImageUri,
                            contentDescription = "Custom Slip Header Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Replace")
                            }
                            OutlinedButton(
                                onClick = { viewModel.setHeaderImageUri(null) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                            ) {
                                Text("Remove")
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Header Image")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2: Candidate Info
        SectionHeader("Candidate & Tagline")

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            color = LightSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var candName by remember(slipSettings.candidateName) { mutableStateOf(slipSettings.candidateName) }
                OutlinedTextField(
                    value = candName,
                    onValueChange = {
                        candName = it
                        viewModel.setCandidateName(it)
                    },
                    label = { Text("Candidate / Party Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = PrimaryText,
                        fontSize = 16.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedLabelColor = SaffronOrange,
                        unfocusedLabelColor = SecondaryText,
                        focusedBorderColor = SaffronOrange,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = SaffronOrange
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                var tagline by remember(slipSettings.tagline) { mutableStateOf(slipSettings.tagline) }
                OutlinedTextField(
                    value = tagline,
                    onValueChange = {
                        tagline = it
                        viewModel.setTagline(it)
                    },
                    label = { Text("Slip Subtitle / Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = PrimaryText,
                        fontSize = 16.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedLabelColor = SaffronOrange,
                        unfocusedLabelColor = SecondaryText,
                        focusedBorderColor = SaffronOrange,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = SaffronOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 3: Printer
        SectionHeader("Printer")

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .clickable { onNavigateToPrinter() },
            color = LightSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Thermal Printer Settings", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = PrimaryText)
                    Text("58mm ESC/POS • 203 DPI", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 4: Data Management
        SectionHeader("Data")

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            color = LightSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Local Storage",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = PrimaryText
                )
                Text(
                    text = "All voter records and imported lists are stored completely offline on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All Imported Data")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 5: About
        SectionHeader("About")

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            color = LightSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Voter Search & Slip Generator", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = PrimaryText)
                Text("Version 1.0 (Native Kotlin • On-Device OCR • ESC/POS)", style = MaterialTheme.typography.bodySmall, color = SecondaryText)

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Developed by",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                        Text(
                            text = "Akshat Mehta",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkNavy
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://akshatmehta.com"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronOrange),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, SaffronOrange)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SaffronOrange
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Portfolio",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete All Voter Data?", color = DarkNavy) },
            text = {
                Text("Are you sure you want to delete all imported voter lists and records? This cannot be undone.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData {
                            Toast.makeText(context, "All voter records deleted.", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

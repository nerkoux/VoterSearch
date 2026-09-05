package com.keofi.poonamashishmehta_votergen.ui.screen

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SuccessGreen
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.VoterDetailViewModel
import com.keofi.poonamashishmehta_votergen.util.TextNormalizer

@Composable
fun OcrReviewScreen(
    voterId: Long,
    onBack: () -> Unit,
    onAccepted: () -> Unit,
    viewModel: VoterDetailViewModel = viewModel()
) {
    val voter by viewModel.voter.collectAsState()

    var editName by remember { mutableStateOf("") }
    var editEpic by remember { mutableStateOf("") }
    var editRelative by remember { mutableStateOf("") }
    var editAge by remember { mutableStateOf("") }
    var editPart by remember { mutableStateOf("") }
    var editSerial by remember { mutableStateOf("") }

    LaunchedEffect(voterId) {
        viewModel.loadVoter(voterId)
    }

    LaunchedEffect(voter) {
        voter?.let { v ->
            editName = v.name
            editEpic = v.epicNumber
            editRelative = v.relativeName ?: ""
            editAge = if (v.age != null) "${v.age}" else ""
            editPart = v.partNumber ?: ""
            editSerial = if (v.serialNumber != null) "${v.serialNumber}" else ""
        }
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Review Record",
                style = MaterialTheme.typography.titleLarge,
                color = DarkNavy
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)

        if (voter == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SaffronOrange)
            }
        } else {
            val v = voter!!

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Source: ${v.sourcePdf.ifEmpty { "Voter Roll" }} • Page ${v.sourcePage}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Raw OCR Text excerpt
                if (v.rawOcrText.isNotBlank()) {
                    Text(
                        text = "Raw OCR Text",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(6.dp)),
                        color = LightSurface
                    ) {
                        Text(
                            text = v.rawOcrText,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = PrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                    text = "Detected Record Fields (Edit to correct)",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkNavy
                )

                Spacer(modifier = Modifier.height(14.dp))

                val reviewColors = OutlinedTextFieldDefaults.colors(
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
                val reviewTextStyle = androidx.compose.ui.text.TextStyle(
                    color = PrimaryText,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Voter Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = reviewTextStyle,
                    colors = reviewColors
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editEpic,
                    onValueChange = { editEpic = it },
                    label = { Text("EPIC / Voter ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = reviewTextStyle,
                    colors = reviewColors
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editRelative,
                    onValueChange = { editRelative = it },
                    label = { Text("Relative Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = reviewTextStyle,
                    colors = reviewColors
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editAge,
                        onValueChange = { editAge = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = reviewTextStyle,
                        colors = reviewColors
                    )

                    OutlinedTextField(
                        value = editSerial,
                        onValueChange = { editSerial = it },
                        label = { Text("Serial No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = reviewTextStyle,
                        colors = reviewColors
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editPart,
                    onValueChange = { editPart = it },
                    label = { Text("Part Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = reviewTextStyle,
                    colors = reviewColors
                )
            }

            // Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = {
                        val cleanEpic = editEpic.trim().uppercase()
                        val updated = v.copy(
                            name = editName.trim(),
                            normalizedName = TextNormalizer.normalizeName(editName),
                            epicNumber = cleanEpic,
                            normalizedEpic = TextNormalizer.normalizeEpic(cleanEpic),
                            relativeName = editRelative.trim().ifEmpty { null },
                            age = editAge.trim().toIntOrNull(),
                            serialNumber = editSerial.trim().toIntOrNull(),
                            partNumber = editPart.trim().ifEmpty { null },
                            confidence = 1.0f // Marked reviewed and accepted
                        )
                        viewModel.updateVoter(updated) {
                            onAccepted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accept & Save Record", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
                }
            }
        }
    }
}

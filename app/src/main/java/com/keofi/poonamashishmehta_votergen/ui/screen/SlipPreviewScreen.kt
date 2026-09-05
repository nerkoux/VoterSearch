package com.keofi.poonamashishmehta_votergen.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.VoterApp
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.SlipPreviewViewModel

@Composable
fun SlipPreviewScreen(
    voterId: Long,
    onBack: () -> Unit,
    viewModel: SlipPreviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val voter by viewModel.voter.collectAsState()
    val slipData by viewModel.slipData.collectAsState()
    val previewMode by viewModel.previewMode.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    LaunchedEffect(voterId) {
        viewModel.loadVoter(voterId)
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
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
                text = "Slip Preview",
                style = MaterialTheme.typography.titleLarge,
                color = DarkNavy
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)

        if (slipData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SaffronOrange)
            }
        } else {
            val currentSlip = slipData!!

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var activeTab by remember { mutableStateOf(0) } // 0 = Digital Slip (WhatsApp/PDF), 1 = Thermal Printer Slip

                // Header Mode Selector Chips (Custom Image vs Text Only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = previewMode == SlipType.CUSTOM_IMAGE,
                        onClick = { viewModel.setSlipMode(SlipType.CUSTOM_IMAGE) },
                        label = { Text("Custom Image") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronOrange,
                            selectedLabelColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FilterChip(
                        selected = previewMode == SlipType.TEXT_ONLY,
                        onClick = { viewModel.setSlipMode(SlipType.TEXT_ONLY) },
                        label = { Text("Text Only") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // View Toggle: Digital Slip (Phone/Share) vs Thermal Print (Receipt)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == 0) SaffronOrange else Color.Transparent)
                            .clickable { activeTab = 0 }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Digital Slip",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 0) Color.White else PrimaryText
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == 1) DarkNavy else Color.Transparent)
                            .clickable { activeTab = 1 }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Thermal Slip (58mm)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 1) Color.White else PrimaryText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High-fidelity Preview Render
                val previewBitmap = remember(currentSlip, activeTab) {
                    if (activeTab == 0) {
                        VoterApp.instance.digitalSlipRenderer.renderDigitalSlipBitmap(currentSlip)
                    } else {
                        VoterApp.instance.thermalSlipRenderer.renderThermalBitmap(currentSlip)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = if (activeTab == 0) "Digital Slip Preview" else "Thermal Slip Preview",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Bottom Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Primary Action: Print
                Button(
                    onClick = { viewModel.printSlip() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronOrange,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Print Slip",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sharing & Saving Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.shareImage() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavy)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Image", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    }

                    OutlinedButton(
                        onClick = { viewModel.sharePdf() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavy)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "PDF", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    }

                    OutlinedButton(
                        onClick = { viewModel.saveImage() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavy)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Save", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}

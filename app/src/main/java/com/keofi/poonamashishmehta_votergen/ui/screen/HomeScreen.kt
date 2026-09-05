package com.keofi.poonamashishmehta_votergen.ui.screen

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToPrinter: () -> Unit,
    onNavigateToLists: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val totalVoters by viewModel.totalVoters.collectAsState()
    val totalLists by viewModel.totalLists.collectAsState()
    val printerSettings by viewModel.printerSettings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // App / Screen Header
        Text(
            text = "Voter List",
            style = MaterialTheme.typography.titleLarge,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Search, view and print voter details",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Hero Action 1: Search Voter (Primary Saffron)
        Button(
            onClick = onNavigateToSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SaffronOrange,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search Voter",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Action 2: Import Voter List (Dark Navy / Clean Outline)
        OutlinedButton(
            onClick = onNavigateToImport,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DarkNavy
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkNavy)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DarkNavy
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Import Voter List",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Medium),
                    color = DarkNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Compact Statistics Section
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .clickable { onNavigateToLists() },
            color = LightSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Imported Lists",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Text(
                        text = "$totalLists",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryText
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = DividerGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Voters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Text(
                        text = java.text.NumberFormat.getNumberInstance().format(totalVoters),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronOrange
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Printer Status Section
        Text(
            text = "Printer Status",
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(12.dp))

        val isConfigured = !printerSettings.selectedPrinterAddress.isNullOrBlank()

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = if (isConfigured) SaffronOrange else SecondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = printerSettings.selectedPrinterName ?: "F2C CX588 (58mm)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isConfigured) SuccessGreen else SecondaryText,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isConfigured) "Configured / Ready" else "Not Connected (Tap to setup)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isConfigured) SuccessGreen else SecondaryText
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Printer Settings",
                    tint = SecondaryText
                )
            }
        }
    }
}

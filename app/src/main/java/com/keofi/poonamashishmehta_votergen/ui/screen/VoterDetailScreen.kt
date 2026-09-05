package com.keofi.poonamashishmehta_votergen.ui.screen

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.VoterDetailViewModel

@Composable
fun VoterDetailScreen(
    voterId: Long,
    onBack: () -> Unit,
    onGenerateSlip: (Long) -> Unit,
    viewModel: VoterDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val voter by viewModel.voter.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(voterId) {
        viewModel.loadVoter(voterId)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
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
                text = "Voter Details",
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
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Name prominent
                Text(
                    text = v.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
                    color = PrimaryText
                )

                if (!v.nameHindi.isNullOrBlank() && !v.nameHindi.equals(v.name, ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = v.nameHindi,
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Information sheet container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                    color = LightSurface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // EPIC Number
                        DetailItem(label = "EPIC NUMBER", value = v.epicNumber.ifEmpty { "Not Available" }, isHighlight = true)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)

                        // Relative Name & Relation
                        val relLabel = if (!v.relationship.isNullOrBlank()) "${v.relationship.uppercase()} NAME" else "RELATIVE NAME"
                        DetailItem(label = relLabel, value = v.relativeName ?: "-")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)

                        // Two-column row: Age & Gender
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(label = "AGE", value = if (v.age != null) "${v.age} Years" else "-")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(label = "GENDER", value = v.gender ?: "-")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)

                        // Two-column row: Part & Serial
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(label = "PART NUMBER", value = v.partNumber ?: "-")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(label = "SERIAL NUMBER", value = if (v.serialNumber != null) "${v.serialNumber}" else "-")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)

                        // House Number & Status
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(label = "HOUSE NUMBER", value = v.houseNumber ?: "-")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DetailItem(label = "STATUS", value = v.status)
                            }
                        }

                        if (!v.pollingStation.isNullOrBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)
                            DetailItem(label = "POLLING STATION", value = v.pollingStation)
                        }

                        // Source PDF / Page info
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)
                        DetailItem(
                            label = "SOURCE",
                            value = "${v.sourcePdf.ifEmpty { "Voter List" }} • Page ${v.sourcePage}"
                        )
                    }
                }
            }

            // Bottom Action Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Primary Action: Generate Slip (Saffron)
                Button(
                    onClick = { onGenerateSlip(v.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronOrange,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Slip",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary actions: Print and Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.printDirectly() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavy)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Print", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = { viewModel.shareDirectly() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavy)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Share", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = if (isHighlight) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkNavy)
            } else {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = PrimaryText)
            }
        )
    }
}

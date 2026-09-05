package com.keofi.poonamashishmehta_votergen.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.data.printer.PrintStatus
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.ErrorRed
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SuccessGreen
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.PrinterViewModel

@Composable
fun PrinterScreen(
    viewModel: PrinterViewModel = viewModel()
) {
    val context = LocalContext.current
    val printerSettings by viewModel.printerSettings.collectAsState()
    val printStatus by viewModel.printStatus.collectAsState()
    val discoveredPrinters by viewModel.discoveredPrinters.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val bluetoothPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.scanPrinters()
        } else {
            Toast.makeText(context, "Bluetooth permissions are needed to search for printers.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(printStatus) {
        when (val status = printStatus) {
            is PrintStatus.Success -> {
                Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
            is PrintStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                viewModel.clearStatus()
            }
            else -> {}
        }
    }

    fun checkAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val scanGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            if (connectGranted && scanGranted) {
                viewModel.scanPrinters()
            } else {
                bluetoothPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        } else {
            viewModel.scanPrinters()
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
            text = "Printer",
            style = MaterialTheme.typography.titleLarge,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configure 58mm Bluetooth ESC/POS thermal printer",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selected Printer Card as specified in Requirement #22
        Text(
            text = "Selected Printer",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        val isConfigured = !printerSettings.selectedPrinterAddress.isNullOrBlank()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
            color = LightSurface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = printerSettings.selectedPrinterName ?: "F2C CX588",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isConfigured) SuccessGreen else SecondaryText, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConfigured) "Configured" else "Not Configured",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isConfigured) SuccessGreen else SecondaryText
                        )
                    }
                }

                if (!printerSettings.selectedPrinterAddress.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Address: ${printerSettings.selectedPrinterAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.8.dp, color = DividerGray)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paper", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                    Text("${printerSettings.paperWidthMm} mm", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = PrimaryText)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Protocol", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                    Text("ESC/POS", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = PrimaryText)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Actions: Test Print & Forget
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { viewModel.runTestPrint() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConfigured) SaffronOrange else SecondaryText,
                    contentColor = Color.White
                ),
                enabled = isConfigured && printStatus !is PrintStatus.Connecting && printStatus !is PrintStatus.Printing
            ) {
                if (printStatus is PrintStatus.Connecting || printStatus is PrintStatus.Printing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Print")
                }
            }

            if (isConfigured) {
                OutlinedButton(
                    onClick = { viewModel.forgetPrinter() },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                ) {
                    Text("Forget")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Scanner Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nearby Printers",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )

            Button(
                onClick = {
                    if (isScanning) {
                        viewModel.stopScan()
                    } else {
                        checkAndScan()
                    }
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stop Scan", style = MaterialTheme.typography.bodySmall)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Printers", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (discoveredPrinters.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                color = LightSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap 'Scan Printers' to discover paired and nearby Bluetooth printers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                discoveredPrinters.forEachIndexed { index, dev ->
                    val isSelected = dev.address == printerSettings.selectedPrinterAddress
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPrinter(dev.name, dev.address) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = if (isSelected) SaffronOrange else SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = dev.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = PrimaryText
                                )
                                Text(
                                    text = "${dev.address} • ${if (dev.isPaired) "Paired" else "Discovered"}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = SecondaryText
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = SuccessGreen
                            )
                        } else {
                            Text(
                                text = "Select",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = DarkNavy
                            )
                        }
                    }

                    if (index < discoveredPrinters.size - 1) {
                        HorizontalDivider(thickness = 0.8.dp, color = DividerGray)
                    }
                }
            }
        }
    }
}

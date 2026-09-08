package com.keofi.poonamashishmehta_votergen.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.ErrorRed
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SuccessGreen
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.ListsViewModel

@Composable
fun ListDetailScreen(
    listId: Long,
    onBack: () -> Unit,
    onVoterClick: (Long) -> Unit,
    onStartReview: (Long) -> Unit,
    viewModel: ListsViewModel = viewModel()
) {
    val selectedList by viewModel.selectedList.collectAsState()
    val listVoters by viewModel.listVoters.collectAsState()
    val reviewVoters by viewModel.reviewVoters.collectAsState()
    val selectedBooth by viewModel.selectedBooth.collectAsState()
    val listBooths by viewModel.listBooths.collectAsState()

    LaunchedEffect(listId) {
        viewModel.selectList(listId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        var showDeleteDialog by remember { mutableStateOf(false) }

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
                text = selectedList?.name ?: "Voter List",
                style = MaterialTheme.typography.titleLarge,
                color = DarkNavy,
                modifier = Modifier.weight(1f)
            )
            if (selectedList != null) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Delete List",
                        tint = ErrorRed
                    )
                }
            }
        }

        if (showDeleteDialog && selectedList != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Voter List?") },
                text = { Text("Are you sure you want to delete '${selectedList!!.name}' and all associated voters? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteList(listId)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)

        if (selectedList == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SaffronOrange)
            }
        } else {
            val list = selectedList!!

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Stat Summary Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                            color = LightSurface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Voters", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                    Text(
                                        java.text.NumberFormat.getNumberInstance().format(list.totalVoters),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DarkNavy
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("High Confidence", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                    Text("${list.highConfidence}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = SuccessGreen)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp, color = DividerGray)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Needs Review", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                                    Text(
                                        "${list.needsReview}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (list.needsReview > 0) SaffronOrange else PrimaryText
                                    )
                                }
                            }
                        }

                        // Review Low-Confidence Button if any exist
                        if (reviewVoters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onStartReview(reviewVoters.first().id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SaffronOrange,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Review ${reviewVoters.size} Uncertain Records")
                            }
                        }

                        // Booth Filter Chips if list has booths
                        if (listBooths.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Booth:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SecondaryText
                                )

                                val isAllSelected = selectedBooth == null
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.selectBooth(null) },
                                    color = if (isAllSelected) SaffronOrange.copy(alpha = 0.12f) else LightSurface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isAllSelected) SaffronOrange else DividerGray
                                    )
                                ) {
                                    Text(
                                        text = "All Booths",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isAllSelected) SaffronOrange else PrimaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                listBooths.forEach { booth ->
                                    val isSelected = selectedBooth == booth
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.selectBooth(booth) },
                                        color = if (isSelected) SaffronOrange.copy(alpha = 0.12f) else LightSurface,
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) SaffronOrange else DividerGray
                                        )
                                    ) {
                                        Text(
                                            text = booth,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) SaffronOrange else PrimaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedBooth != null) "Voters in $selectedBooth" else "Voters in this List",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryText,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${listVoters.size} shown",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                    }
                }

                items(listVoters, key = { it.id }) { voter ->
                    VoterSearchResultRow(
                        voter = voter,
                        onClick = { onVoterClick(voter.id) }
                    )
                    HorizontalDivider(thickness = 0.8.dp, color = DividerGray)
                }
            }
        }
    }
}

package com.keofi.poonamashishmehta_votergen.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.ui.theme.DarkNavy
import com.keofi.poonamashishmehta_votergen.ui.theme.DividerGray
import com.keofi.poonamashishmehta_votergen.ui.theme.LightSurface
import com.keofi.poonamashishmehta_votergen.ui.theme.PrimaryText
import com.keofi.poonamashishmehta_votergen.ui.theme.SaffronOrange
import com.keofi.poonamashishmehta_votergen.ui.theme.SecondaryText
import com.keofi.poonamashishmehta_votergen.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onVoterClick: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)) {
            Text(
                text = "Search voter",
                style = MaterialTheme.typography.titleLarge,
                color = DarkNavy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = {
                    Text(
                        text = "Search name or EPIC number",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (searchQuery.isNotEmpty()) SaffronOrange else SecondaryText,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearQuery() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = SecondaryText
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedPlaceholderColor = SecondaryText,
                    unfocusedPlaceholderColor = SecondaryText,
                    focusedBorderColor = SaffronOrange,
                    unfocusedBorderColor = DividerGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = LightSurface,
                    cursorColor = SaffronOrange
                )
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)

        // Search Results List
        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Enter a name, EPIC or serial number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "e.g. Ramesh or ABC1234567",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching voters found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults, key = { it.id }) { voter ->
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

@Composable
fun VoterSearchResultRow(
    voter: VoterEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = voter.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = PrimaryText
            )

            if (voter.age != null) {
                Text(
                    text = "Age ${voter.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // EPIC Number - Prominently styled
        Text(
            text = "EPIC: ${voter.epicNumber.ifEmpty { "N/A" }}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = DarkNavy
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Part & Serial
        val partStr = voter.partNumber ?: "-"
        val serialStr = if (voter.serialNumber != null) "${voter.serialNumber}" else "-"
        Text(
            text = "Part $partStr • Serial $serialStr",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText
        )

        if (!voter.pollingStation.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Booth: ${voter.pollingStation}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

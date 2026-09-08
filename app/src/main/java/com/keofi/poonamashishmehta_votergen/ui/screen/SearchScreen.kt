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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.ui.theme.CardBorder
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
    val relativeQuery by viewModel.relativeQuery.collectAsState()
    val selectedBooth by viewModel.selectedBooth.collectAsState()
    val isAdvancedSearch by viewModel.isAdvancedSearch.collectAsState()
    val availableBooths by viewModel.availableBooths.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val hasActiveFilter = searchQuery.isNotBlank() || relativeQuery.isNotBlank() || selectedBooth != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search voter",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkNavy
                )

                if (hasActiveFilter) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SaffronOrange,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.clearAll() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Toggle: Quick Search vs Side-by-Side (Relative) Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightSurface)
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!isAdvancedSearch) Color.White else Color.Transparent)
                        .clickable { viewModel.setAdvancedSearch(false) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Quick Search",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (!isAdvancedSearch) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (!isAdvancedSearch) DarkNavy else SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAdvancedSearch) Color.White else Color.Transparent)
                        .clickable { viewModel.setAdvancedSearch(true) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Voter + Relative Name",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isAdvancedSearch) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isAdvancedSearch) DarkNavy else SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isAdvancedSearch) {
                // Single Quick Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onQueryChange(it) },
                    placeholder = {
                        Text(
                            text = "Search name, EPIC or serial...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = PrimaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (searchQuery.isNotEmpty()) SaffronOrange else SecondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearQuery() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = SecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
            } else {
                // Side-by-Side Dual Search Fields: Voter Name & Relative Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = {
                            Text(
                                text = "Voter Name",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = PrimaryText,
                            fontSize = 14.sp
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearQuery() }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SecondaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedBorderColor = SaffronOrange,
                            unfocusedBorderColor = DividerGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = LightSurface,
                            cursorColor = SaffronOrange
                        )
                    )

                    OutlinedTextField(
                        value = relativeQuery,
                        onValueChange = { viewModel.onRelativeQueryChange(it) },
                        placeholder = {
                            Text(
                                text = "Relative Name",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = PrimaryText,
                            fontSize = 14.sp
                        ),
                        trailingIcon = {
                            if (relativeQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearRelativeQuery() }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SecondaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedBorderColor = SaffronOrange,
                            unfocusedBorderColor = DividerGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = LightSurface,
                            cursorColor = SaffronOrange
                        )
                    )
                }
            }

            // Booth Filter Chips
            if (availableBooths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
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

                    // All Booths Chip
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

                    // Individual Booth Chips
                    availableBooths.forEach { booth ->
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
        }

        HorizontalDivider(thickness = 1.dp, color = DividerGray)

        // Search Results List
        if (!hasActiveFilter) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isAdvancedSearch) "Enter voter name and/or relative name" else "Enter a name, EPIC or serial number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAdvancedSearch) "e.g. Voter: Ramesh  |  Relative: Suresh" else "e.g. Ramesh or ABC1234567, or select a booth above",
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
        // Name & Age
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = voter.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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

        // Relative Name & Relationship Side-by-Side
        if (!voter.relativeName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(3.dp))
            val relPrefix = when (voter.relationship?.lowercase()?.trim()) {
                "father", "पिता" -> "s/o (पिता)"
                "husband", "पति" -> "w/o (पति)"
                "mother", "माता" -> "d/o (माता)"
                "other", "अन्य" -> "rel (संबंधी)"
                else -> if (!voter.relationship.isNullOrBlank()) "${voter.relationship}:" else "Relative:"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$relPrefix ",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = SaffronOrange
                )
                Text(
                    text = voter.relativeName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = DarkNavy
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
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Booth: ${voter.pollingStation}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

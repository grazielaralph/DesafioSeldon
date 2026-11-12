package com.example.seldon_fleet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seldon_fleet.domain.Decision
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.DeviceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    viewModel: FleetViewModel,
    onDeviceClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seldon Fleet", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is FleetUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FleetUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is FleetUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Barra de Filtros por Status com Contadores (REQ-05)
                        StatusFilterRow(
                            selectedFilter = state.selectedFilter,
                            statusCounts = state.statusCounts,
                            totalCount = state.totalCount,
                            onFilterSelect = { viewModel.selectFilter(it) }
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.devices) { (device, decision) ->
                                DeviceCard(
                                    device = device,
                                    decision = decision,
                                    onClick = { onDeviceClick(device.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(device.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterRow(
    selectedFilter: DeviceStatus?,
    statusCounts: Map<DeviceStatus, Int>,
    totalCount: Int,
    onFilterSelect: (DeviceStatus?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelect(null) },
                label = { Text("Todos ($totalCount)") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == DeviceStatus.OK,
                onClick = { onFilterSelect(if (selectedFilter == DeviceStatus.OK) null else DeviceStatus.OK) },
                label = { Text("OK (${statusCounts[DeviceStatus.OK] ?: 0})") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == DeviceStatus.ATENCAO,
                onClick = { onFilterSelect(if (selectedFilter == DeviceStatus.ATENCAO) null else DeviceStatus.ATENCAO) },
                label = { Text("Atenção (${statusCounts[DeviceStatus.ATENCAO] ?: 0})") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == DeviceStatus.CRITICO,
                onClick = { onFilterSelect(if (selectedFilter == DeviceStatus.CRITICO) null else DeviceStatus.CRITICO) },
                label = { Text("Crítico (${statusCounts[DeviceStatus.CRITICO] ?: 0})") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == DeviceStatus.INDETERMINADO,
                onClick = { onFilterSelect(if (selectedFilter == DeviceStatus.INDETERMINADO) null else DeviceStatus.INDETERMINADO) },
                label = { Text("Sem Dados (${statusCounts[DeviceStatus.INDETERMINADO] ?: 0})") }
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    decision: Decision,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val statusColor = when (decision.status) {
        DeviceStatus.OK -> Color(0xFF4CAF50)
        DeviceStatus.ATENCAO -> Color(0xFFFFC107)
        DeviceStatus.CRITICO -> Color(0xFFF44336)
        DeviceStatus.INDETERMINADO -> Color(0xFF9E9E9E)
    }

    val statusLabel = when (decision.status) {
        DeviceStatus.OK -> "OK"
        DeviceStatus.ATENCAO -> "ATENÇÃO"
        DeviceStatus.CRITICO -> "CRÍTICO"
        DeviceStatus.INDETERMINADO -> "SEM DADOS"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${device.id} • SoC: ${device.soc}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Bateria: ${device.batteryLevelPct}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (device.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (device.isFavorite) Color(0xFFFFC107) else Color.Gray
                )
            }
        }
    }
}
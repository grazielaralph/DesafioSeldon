package com.example.seldon_fleet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onDeviceClick: (String) -> Unit // Recebe o ID do dispositivo para a futura Tela 2
) {
    // Observa o estado emitido pelo ViewModel
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.devices) { (device, decision) ->
                            DeviceCard(
                                device = device,
                                decision = decision,
                                onClick = { onDeviceClick(device.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    decision: Decision,
    onClick: () -> Unit
) {
    // Mapeia o status para uma cor visual
    val statusColor = when (decision.status) {
        DeviceStatus.OK -> Color(0xFF4CAF50) // Verde
        DeviceStatus.ATENCAO -> Color(0xFFFFC107) // Amarelo/Âmbar
        DeviceStatus.CRITICO -> Color(0xFFF44336) // Vermelho
        DeviceStatus.INDETERMINADO -> Color(0xFF9E9E9E) // Cinzento
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
            // Indicador de cor
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Informação do dispositivo
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
            }

            // Etiqueta de status
            Text(
                text = decision.status.name,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
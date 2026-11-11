package com.example.seldon_fleet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun DeviceDetailScreen(
    device: Device,
    decision: Decision,
    onBack: () -> Unit
) {
    val statusColor = when (decision.status) {
        DeviceStatus.OK -> Color(0xFF4CAF50)
        DeviceStatus.ATENCAO -> Color(0xFFFFC107)
        DeviceStatus.CRITICO -> Color(0xFFF44336)
        DeviceStatus.INDETERMINADO -> Color(0xFF9E9E9E)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Dispositivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cartão de Classificação (O destaque da lógica de negócio)
            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = decision.status.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = decision.reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Cartão de Identificação
            DetailCard(title = "Identificação") {
                DetailRow("ID", device.id)
                DetailRow("Modelo", device.model)
                DetailRow("SoC", device.soc)
                DetailRow("Tier", device.tier.uppercase())
                DetailRow("RAM", "${device.ramMb} MB")
                DetailRow("Android API", "${device.androidApi}")
                DetailRow("Último Registo", device.lastSeen)
            }

            // Cartão de Bateria e Uso
            DetailCard(title = "Bateria e Utilização (Sessão)") {
                DetailRow("Capacidade", "${device.batteryCapacityMah ?: "N/A"} mAh")
                DetailRow("Nível Atual", "${device.batteryLevelPct}%")
                DetailRow("Tempo de Ecrã", "${device.screenOnMinutes ?: "N/A"} min")
                DetailRow("Energia Consumida", "${device.energyMah ?: "N/A"} mAh")
            }

            // Cartão de Estabilidade
            DetailCard(title = "Estabilidade e Falhas") {
                DetailRow("Crash Count (7 dias)", "${device.crashCount7d ?: 0}")
                DetailRow("Jank Frames", "${device.jankFramesPct ?: 0.0}%")
            }
        }
    }
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
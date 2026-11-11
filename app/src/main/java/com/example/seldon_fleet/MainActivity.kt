package com.example.seldon_fleet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.seldon_fleet.data.repository.DeviceRepositoryImpl
import com.example.seldon_fleet.domain.ClassifyDevice
import com.example.seldon_fleet.presentation.FleetScreen
import com.example.seldon_fleet.presentation.FleetViewModel
import com.example.seldon_fleet.ui.theme.SeldonfleetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instancia as dependências manualmente (Injeção de Dependência simples)
        val repository = DeviceRepositoryImpl(context = applicationContext)
        val classifyDevice = ClassifyDevice()

        // 2. Factory para criar o ViewModel com as dependências
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FleetViewModel(repository, classifyDevice) as T
            }
        }

        setContent {
            SeldonFleetTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val viewModel: FleetViewModel = viewModel(factory = viewModelFactory)
                    val uiState by viewModel.uiState.collectAsState()

                    // 1. Variável que controla qual ecrã mostrar
                    var selectedDeviceId by remember { mutableStateOf<String?>(null) }

                    // 2. Lógica de navegação
                    if (selectedDeviceId == null) {
                        // Se for null, mostra a Lista
                        FleetScreen(
                            viewModel = viewModel,
                            onDeviceClick = { idClicado ->
                                selectedDeviceId = idClicado // Atualiza o ID ao clicar!
                            }
                        )
                    } else {
                        // Se tiver um ID, procura o telemóvel e mostra os Detalhes
                        if (uiState is FleetUiState.Success) {
                            val devicePair = (uiState as FleetUiState.Success).devices.find { it.first.id == selectedDeviceId }

                            if (devicePair != null) {
                                DeviceDetailScreen(
                                    device = devicePair.first,
                                    decision = devicePair.second,
                                    onBack = { selectedDeviceId = null } // Botão de voltar limpa o ID
                                )
                            } else {
                                selectedDeviceId = null
                            }
                        }
                    }
                }
            }
        }
    }
}
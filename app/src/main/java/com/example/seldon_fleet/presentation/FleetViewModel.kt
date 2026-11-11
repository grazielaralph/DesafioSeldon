package com.example.seldon_fleet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seldon_fleet.domain.ClassifyDevice
import com.example.seldon_fleet.domain.Decision
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Definir os estados possíveis do ecrã
sealed class FleetUiState {
    object Loading : FleetUiState()
    data class Success(val devices: List<Pair<Device, Decision>>) : FleetUiState()
    data class Error(val message: String) : FleetUiState()
}

// 2. Criar o ViewModel
class FleetViewModel(
    private val repository: DeviceRepository,
    private val classifyDevice: ClassifyDevice
) : ViewModel() {

    private val _uiState = MutableStateFlow<FleetUiState>(FleetUiState.Loading)
    val uiState: StateFlow<FleetUiState> = _uiState.asStateFlow()

    init {
        loadFleet()
    }

    private fun loadFleet() {
        viewModelScope.launch {
            _uiState.value = FleetUiState.Loading
            try {
                // Pede os dados ao repositório
                val rawDevices = repository.load()

                // Passa cada dispositivo pela regra de negócio do Domínio
                val classifiedList = rawDevices.map { device ->
                    Pair(device, classifyDevice(device)) // Une o dispositivo ao seu Status/Justificativa
                }

                // Atualiza a UI com sucesso
                _uiState.value = FleetUiState.Success(classifiedList)
            } catch (e: Exception) {
                _uiState.value = FleetUiState.Error("Erro ao carregar dados: ${e.message}")
            }
        }
    }
}
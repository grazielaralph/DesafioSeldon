package com.example.seldon_fleet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.Decision
import com.example.seldon_fleet.domain.DeviceRepository
import com.example.seldon_fleet.domain.ClassifyDevice // Ajuste o import se a sua regra estiver noutra pasta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FleetViewModel(
    private val repository: DeviceRepository,
    private val classifyDevice: ClassifyDevice = ClassifyDevice() // A sua regra de negócio
) : ViewModel() {

    // Agora o compilador já reconhece o FleetUiState
    private val _uiState = MutableStateFlow<FleetUiState>(FleetUiState.Loading)
    val uiState: StateFlow<FleetUiState> = _uiState

    init {
        viewModelScope.launch {
            // Fica à escuta do DataStore continuamente
            repository.observeDevices().collect { devices ->
                // Aplica a regra de negócio a cada dispositivo para criar o Par (Device, Decision)
                val devicesWithDecisions = devices.map { device ->
                    Pair(device, classifyDevice(device))
                }

                // Envia para o ecrã o estado de Sucesso
                _uiState.value = FleetUiState.Success(devicesWithDecisions)
            }
        }
    }

    fun toggleFavorite(deviceId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(deviceId)
        }
    }
}

// === ADICIONADO AQUI: A definição dos estados que a UI espera ===
sealed interface FleetUiState {
    object Loading : FleetUiState
    data class Error(val message: String) : FleetUiState
    data class Success(val devices: List<Pair<Device, Decision>>) : FleetUiState
}
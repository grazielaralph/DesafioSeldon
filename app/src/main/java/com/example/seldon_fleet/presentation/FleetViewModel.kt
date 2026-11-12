package com.example.seldon_fleet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seldon_fleet.domain.ClassifyDevice
import com.example.seldon_fleet.domain.Decision
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.DeviceRepository
import com.example.seldon_fleet.domain.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FleetViewModel(
    private val repository: DeviceRepository,
    private val classifyDevice: ClassifyDevice = ClassifyDevice()
) : ViewModel() {

    private val _rawDevices = MutableStateFlow<List<Pair<Device, Decision>>>(emptyList())
    private val _selectedFilter = MutableStateFlow<DeviceStatus?>(null)
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<FleetUiState> = combine(_rawDevices, _selectedFilter, _isLoading) { devices, filter, loading ->
        if (loading) {
            FleetUiState.Loading
        } else {
            val counts = DeviceStatus.entries.associateWith { status ->
                devices.count { it.second.status == status }
            }
            val filtered = if (filter == null) devices else devices.filter { it.second.status == filter }
            FleetUiState.Success(
                allDevices = devices,
                devices = filtered,
                selectedFilter = filter,
                statusCounts = counts,
                totalCount = devices.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FleetUiState.Loading)

    init {
        viewModelScope.launch {
            repository.observeDevices().collect { devices ->
                val devicesWithDecisions = devices.map { device ->
                    Pair(device, classifyDevice(device))
                }
                _rawDevices.value = devicesWithDecisions
                _isLoading.value = false
            }
        }
    }

    fun selectFilter(status: DeviceStatus?) {
        _selectedFilter.value = status
    }

    fun toggleFavorite(deviceId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(deviceId)
        }
    }
}

sealed interface FleetUiState {
    object Loading : FleetUiState
    data class Error(val message: String) : FleetUiState
    data class Success(
        val allDevices: List<Pair<Device, Decision>>,
        val devices: List<Pair<Device, Decision>>,
        val selectedFilter: DeviceStatus?,
        val statusCounts: Map<DeviceStatus, Int>,
        val totalCount: Int
    ) : FleetUiState
}
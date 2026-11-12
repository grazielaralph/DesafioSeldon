package com.example.seldon_fleet.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.seldon_fleet.data.dto.FleetResponseDto
import com.example.seldon_fleet.data.mapper.toDomain
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.DeviceRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStreamReader

// Cria o DataStore uma única vez
val Context.dataStore by preferencesDataStore(name = "fleet_preferences")

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {

    // Chave para guardar a lista de IDs favoritos no DataStore
    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_devices")

    // 1. IMPLEMENTAÇÃO OBRIGATÓRIA DO CONTRATO
    override fun observeDevices(): Flow<List<Device>> {
        return context.dataStore.data.map { preferences ->
            val favoriteIds = preferences[FAVORITES_KEY] ?: emptySet()
            val allDevices = loadJson() // Chama a sua função de leitura abaixo

            // Cruza os dados: se o ID do JSON estiver salvo no DataStore, marca como true
            allDevices.map { device ->
                device.copy(isFavorite = favoriteIds.contains(device.id))
            }
        }
    }

    // 2. IMPLEMENTAÇÃO OBRIGATÓRIA DO CONTRATO
    override suspend fun toggleFavorite(deviceId: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            if (currentFavorites.contains(deviceId)) {
                preferences[FAVORITES_KEY] = currentFavorites - deviceId
            } else {
                preferences[FAVORITES_KEY] = currentFavorites + deviceId
            }
        }
    }

    // 3. A SUA FUNÇÃO ANTIGA DE LEITURA (agora privada e renomeada)
    private fun loadJson(): List<Device> {
        return try {
            val inputStream = context.assets.open("frota.json")
            val reader = InputStreamReader(inputStream)

            val response: FleetResponseDto = Gson().fromJson(reader, FleetResponseDto::class.java)
            reader.close()

            response.devices.mapNotNull { it.toDomain() }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
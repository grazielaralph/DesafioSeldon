package com.example.seldon_fleet.data.repository

import android.content.Context
import com.example.seldon_fleet.data.dto.FleetResponseDto
import com.example.seldon_fleet.data.mapper.toDomain
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.DeviceRepository
import com.google.gson.Gson
import java.io.InputStreamReader

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {

    override fun load(): List<Device> {
        return try {
            val inputStream = context.assets.open("frota.json")
            val reader = InputStreamReader(inputStream)

            // 1. Em vez de ler uma lista, lê o envelope (FleetResponseDto)
            val response: FleetResponseDto = Gson().fromJson(reader, FleetResponseDto::class.java)

            reader.close()

            // 2. Extrai a lista de dispositivos (response.devices) e mapeia para o Domínio
            response.devices.mapNotNull { it.toDomain() }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
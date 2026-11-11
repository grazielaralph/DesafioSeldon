package com.example.seldon_fleet.data.mapper

import com.example.seldon_fleet.data.dto.DeviceDto
import com.example.seldon_fleet.domain.Device

fun DeviceDto.toDomain(): Device? {
    // Ignorar dispositivos sem ID, pois não podem ser identificados
    val validId = this.id ?: return null

    val normalizedEnergy = EnergyNormalizer.toMah(
        energy = this.energyConsumed,
        unit = this.energyUnit,
        voltage = this.nominalVoltage
    )

    return Device(
        id = validId,
        model = this.model ?: "Desconhecido",
        soc = this.soc ?: "Desconhecido",
        tier = this.tier ?: "unknown",
        ramMb = this.ramMb ?: 0,
        androidApi = this.androidApi ?: 0,
        batteryCapacityMah = this.batteryCapacityMah,
        batteryLevelPct = this.batteryLevelPct ?: 0,
        screenOnMinutes = this.screenOnMinutes,
        energyMah = normalizedEnergy,
        jankFramesPct = this.jankFramesPct,
        crashCount7d = this.crashCount7d,
        lastSeen = this.lastSeen ?: ""
    )
}
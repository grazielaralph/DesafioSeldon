package com.example.seldon_fleet.data.dto

import com.google.gson.annotations.SerializedName

data class DeviceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("soc") val soc: String?,
    @SerializedName("tier") val tier: String?,
    @SerializedName("ramMb") val ramMb: Int?,
    @SerializedName("androidApi") val androidApi: Int?,
    @SerializedName("batteryCapacityMah") val batteryCapacityMah: Int?,
    @SerializedName("batteryLevelPct") val batteryLevelPct: Int?,
    @SerializedName("screenOnMinutes") val screenOnMinutes: Int?,
    @SerializedName("energyConsumed") val energyConsumed: Double?,
    @SerializedName("energyUnit") val energyUnit: String?,
    @SerializedName("nominalVoltage") val nominalVoltage: Double?,
    @SerializedName("jankFramesPct") val jankFramesPct: Double?,
    @SerializedName("crashCount7d") val crashCount7d: Int?,
    @SerializedName("lastSeen") val lastSeen: String?
)

data class FleetResponseDto(
    @SerializedName("devices") val devices: List<DeviceDto>
)
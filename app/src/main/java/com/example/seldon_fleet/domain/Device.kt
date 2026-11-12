package com.example.seldon_fleet.domain

import kotlinx.coroutines.flow.Flow

data class Device(
    val id: String, val model: String, val soc: String, val tier: String, val ramMb: Int,
    val androidApi: Int, val batteryCapacityMah: Int?, val batteryLevelPct: Int,
    val screenOnMinutes: Int?, val energyMah: Double?, val jankFramesPct: Double?,
    val crashCount7d: Int?, val lastSeen: String, val isFavorite: Boolean = false
)

interface DeviceRepository {

    fun observeDevices(): Flow<List<Device>>
    suspend fun toggleFavorite(deviceId: String)
}
interface FavoriteRepository { fun load(): Set<String>; fun save(ids: Set<String>) }

enum class DeviceStatus { INDETERMINADO, CRITICO, ATENCAO, OK }

data class Decision(
    val status: DeviceStatus,
    val energyMah: Double? = null,
    val drainRelative: Double? = null,
    val reason: String
)

class ClassifyDevice {
    operator fun invoke(device: Device): Decision {
        // 1. Verificação de dados insuficientes / indisponíveis (Defeito 2)
        if (device.batteryCapacityMah == null || device.batteryCapacityMah <= 0) {
            return Decision(DeviceStatus.INDETERMINADO, reason = "capacidade da bateria ausente ou igual a zero")
        }
        if (device.screenOnMinutes == null || device.screenOnMinutes <= 0) {
            return Decision(DeviceStatus.INDETERMINADO, reason = "tempo de tela ausente ou igual a zero")
        }
        val energy = device.energyMah ?: return Decision(DeviceStatus.INDETERMINADO, reason = "consumo normalizado indisponível")

        // 2. Cálculo das derivadas
        val drain = energy / (device.screenOnMinutes / 60.0)
        val relative = (drain / device.batteryCapacityMah) * 100.0

        val crashes = device.crashCount7d ?: 0
        val jank = device.jankFramesPct ?: 0.0

        // 3. Determinação do Status (Ordem de precedência)
        val status = when {
            relative >= 12.0 || crashes >= 3 || jank >= 15.0 -> DeviceStatus.CRITICO
            relative >= 7.0 || crashes >= 1 || jank >= 8.0 -> DeviceStatus.ATENCAO
            else -> DeviceStatus.OK
        }

        // 4. Determinação da Justificativa (Garantindo que o motivo bate com a ordem de urgência do Status)
        val reason = when {
            // Motivos Críticos primeiro
            relative >= 12.0 -> "dreno de %.1f %%/h, acima do limiar de 12 %%/h".format(relative)
            crashes >= 3 -> "%d falhas nos últimos 7 dias, acima do limite de 3".format(crashes)
            jank >= 15.0 -> "%.1f%% de frames perdidos, acima do limiar de 15%%".format(jank)

            // Motivos de Atenção em seguida
            relative >= 7.0 -> "dreno de %.1f %%/h, acima do limiar de 7 %%/h".format(relative)
            crashes >= 1 -> "%d falha(s) nos últimos 7 dias".format(crashes)
            jank >= 8.0 -> "%.1f%% de frames perdidos, acima do limiar de 8%%".format(jank)

            else -> "nenhum sinal ultrapassou os limiares"
        }

        return Decision(status, energy, relative, reason)
    }
}
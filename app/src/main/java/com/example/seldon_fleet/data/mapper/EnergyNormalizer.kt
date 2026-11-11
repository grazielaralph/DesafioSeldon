package com.example.seldon_fleet.data.mapper

object EnergyNormalizer {
    fun toMah(energy: Double?, unit: String?, voltage: Double?): Double? {
        if (energy == null || energy <= 0.0) return null

        return when (unit?.lowercase()) {
            "mwh" -> {
                if (voltage != null && voltage > 0.0) {
                    energy / voltage
                } else {
                    null // Sem tensão válida, não podemos converter mWh para mAh
                }
            }
            else -> energy // Assume que já é mAh ou unidade desconhecida
        }
    }
}
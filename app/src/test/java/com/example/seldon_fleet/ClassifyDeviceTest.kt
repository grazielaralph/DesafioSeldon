package com.example.seldon_fleet

import com.example.seldon_fleet.domain.ClassifyDevice
import com.example.seldon_fleet.domain.Device
import com.example.seldon_fleet.domain.DeviceStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClassifyDeviceTest {
    private lateinit var classifyDevice: ClassifyDevice

    @Before
    fun setUp() {
        classifyDevice = ClassifyDevice()
    }

    // Função auxiliar para criar um dispositivo "base" limpo e OK
    private fun createBaseDevice(): Device {
        return Device(
            id = "DEV-0001",
            model = "Pixel 6a",
            soc = "Tensor G1",
            tier = "mid",
            ramMb = 6144, // Certifique-se de que no seu Device.kt também está ramMB (maiúsculas)
            androidApi = 34,
            batteryCapacityMah = 5000,
            batteryLevelPct = 80,
            screenOnMinutes = 120,
            energyMah = 200.0,
            jankFramesPct = 1.0,
            crashCount7d = 0,
            lastSeen = "2026-10-20T02:58:10Z"
        )
    }

    @Test
    fun `quando dispositivo tem dados normais deve retornar OK`() {
        val device = createBaseDevice()
        val decision = classifyDevice(device)
        assertEquals(DeviceStatus.OK, decision.status)
    }

    @Test
    fun `quando capacidade da bateria e nula deve retornar INDETERMINADO`() {
        val device = createBaseDevice().copy(batteryCapacityMah = null)
        val decision = classifyDevice(device)
        assertEquals(DeviceStatus.INDETERMINADO, decision.status)
    }

    @Test
    fun `quando tempo de tela e zero deve retornar INDETERMINADO`() {
        val device = createBaseDevice().copy(screenOnMinutes = 0)
        val decision = classifyDevice(device) // Corrigido aqui
        assertEquals(DeviceStatus.INDETERMINADO, decision.status)
    }

    @Test
    fun `fronteira CRITICO dreno relativo exatamente em 12 pct por h deve retornar CRITICO`() { // Removido o ":"
        val device = createBaseDevice().copy(
            screenOnMinutes = 60,
            energyMah = 600.0
        )
        val decision = classifyDevice(device)
        assertEquals(DeviceStatus.CRITICO, decision.status)
    }

    @Test
    fun `fronteira ATENCAO dreno relativo em 11,9 pct por h deve retornar ATENCAO`() { // Removido o ":"
        val device = createBaseDevice().copy(
            screenOnMinutes = 60,
            energyMah = 595.0
        )
        val decision = classifyDevice(device)
        assertEquals(DeviceStatus.ATENCAO, decision.status)
    }

    @Test
    fun `precedencia sinal critico de falhas deve prevalecer sobre dreno de atencao`() { // Removido o ":"
        val device = createBaseDevice().copy(
            screenOnMinutes = 60,
            energyMah = 400.0,
            crashCount7d = 3
        )
        val decision = classifyDevice(device)

        assertEquals(DeviceStatus.CRITICO, decision.status)
        assertEquals("3 falhas nos últimos 7 dias, acima do limite de 3", decision.reason)
    }
}
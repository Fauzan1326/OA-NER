package com.example

import com.example.core.config.AppConfig
import com.example.core.config.ProfileType
import com.example.core.contract.DeviceInfo
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.QualityInfo
import com.example.core.contract.SamplingInfo
import com.example.core.contract.SchemaVersion
import com.example.core.contract.SensorPacket
import com.example.core.contract.SignalQualityStatus
import com.example.core.contract.TimestampInfo
import com.example.core.error.ArthroscanException
import com.example.core.logging.LogLevel
import com.example.core.logging.StructuredLogger
import com.example.hardware.SimulatorSensorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalDataContractTest {

    @Test
    fun testSchemaVersionIsV1() {
        assertEquals("1.0", SchemaVersion.CURRENT)
    }

    @Test
    fun testSensorPacketValidation() {
        val validPacket = SensorPacket(
            schemaVersion = "1.0",
            subjectId = "SUBJ_001",
            sessionId = "SESS_001",
            sensorId = "IMU_01",
            modality = Modality.IMU,
            timestamp = TimestampInfo(deviceTimeMs = 1000L, sequenceNumber = 1L),
            sampling = SamplingInfo(rateHz = 100.0),
            channels = listOf("acc_x", "acc_y", "acc_z"),
            values = listOf(0.0, 1.0, 0.0),
            units = "g",
            device = DeviceInfo(status = DeviceStatus.STREAMING),
            quality = QualityInfo(status = SignalQualityStatus.PASS, score = 0.99)
        )
        assertTrue(validPacket.isValid())

        val nanPacket = validPacket.copy(values = listOf(Double.NaN, 1.0, 0.0))
        assertFalse(nanPacket.isValid())

        val mismatchedPacket = validPacket.copy(values = listOf(1.0))
        assertFalse(mismatchedPacket.isValid())
    }

    @Test
    fun testSimulatorAdaptersAllModalities() {
        val modalities = listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF)
        for (mod in modalities) {
            val adapter = SimulatorSensorAdapter(mod)
            assertEquals(DeviceStatus.DISCONNECTED, adapter.getStatus())
            assertTrue(adapter.connect())
            assertEquals(DeviceStatus.CONNECTED, adapter.getStatus())
            assertTrue(adapter.startStream())
            assertEquals(DeviceStatus.STREAMING, adapter.getStatus())

            val sample = adapter.readSample()
            assertNotNull(sample)
            assertTrue(sample!!.isValid())
            assertEquals(mod, sample.modality)
            assertEquals(SchemaVersion.CURRENT, sample.schemaVersion)
            assertTrue(sample.values.isNotEmpty())

            assertTrue(adapter.stopStream())
            assertTrue(adapter.disconnect())
        }
    }

    @Test
    fun testConfigurationProfiles() {
        val demoConfig = AppConfig.defaultFor(ProfileType.DEMO)
        assertEquals(ProfileType.DEMO, demoConfig.profile)
        assertEquals(100.0, demoConfig.samplingRatesHz[Modality.IMU] ?: 0.0, 0.01)

        val researchConfig = AppConfig.defaultFor(ProfileType.RESEARCH)
        assertEquals(ProfileType.RESEARCH, researchConfig.profile)
        assertEquals(200.0, researchConfig.samplingRatesHz[Modality.IMU] ?: 0.0, 0.01)
    }

    @Test
    fun testStructuredLogger() {
        StructuredLogger.log(LogLevel.INFO, "TEST_TAG", "Test module 1 foundation log")
        val latest = StructuredLogger.logs.value.lastOrNull()
        assertNotNull(latest)
        assertEquals("TEST_TAG", latest?.tag)
        assertEquals("Test module 1 foundation log", latest?.message)
    }

    @Test
    fun testArthroscanExceptions() {
        val ex = ArthroscanException.ContractException.InvalidPacketException("Missing channels")
        assertEquals("ERR_INVALID_PACKET", ex.errorCode)
        assertTrue(ex.message.contains("Missing channels"))
    }
}

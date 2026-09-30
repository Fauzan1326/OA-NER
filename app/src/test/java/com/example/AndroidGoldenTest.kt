package com.example

import com.example.ai.ModelMetadata
import com.example.ai.ModelStatus
import com.example.ai.RuntimeEngine
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
import com.example.hardware.SimulatorSensorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Cross-Platform Golden Test & Acceptance Suite for Module 1
 * SIH 2026 Problem SIH26004 - Team GOD'S PLAN
 */
class AndroidGoldenTest {

    private val numericalToleranceEpsilon = 0.00001

    @Test
    fun testUniversalDataContractValidation() {
        val validPacket = SensorPacket(
            schemaVersion = "1.0",
            subjectId = "SUBJ_GOLDEN_26004",
            sessionId = "SESSION_GOLDEN_001",
            sensorId = "IMU_TIBIA_01",
            modality = Modality.IMU,
            timestamp = TimestampInfo(deviceTimeMs = 1774000000000L, sequenceNumber = 1024L),
            sampling = SamplingInfo(rateHz = 100.0),
            channels = listOf("acc_x", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z"),
            values = listOf(0.012, 0.981, -0.045, 0.001, -0.004, 0.008),
            units = "g,rad/s",
            device = DeviceInfo(status = DeviceStatus.STREAMING, firmwareVersion = "sim-1.0"),
            quality = QualityInfo(status = SignalQualityStatus.PASS, score = 0.99)
        )
        assertTrue(validPacket.isValid())

        // Negative check: NaN value must be rejected
        val nanPacket = validPacket.copy(values = listOf(Double.NaN, 0.981, -0.045, 0.001, -0.004, 0.008))
        assertFalse(nanPacket.isValid())

        // Negative check: Infinite value must be rejected
        val infPacket = validPacket.copy(values = listOf(Double.POSITIVE_INFINITY, 0.981, -0.045, 0.001, -0.004, 0.008))
        assertFalse(infPacket.isValid())

        // Negative check: Channel and value length mismatch must be rejected
        val mismatchPacket = validPacket.copy(values = listOf(0.012, 0.981))
        assertFalse(mismatchPacket.isValid())

        // Negative check: Invalid sampling rate must be rejected
        val invalidRatePacket = validPacket.copy(sampling = SamplingInfo(rateHz = 0.0))
        assertFalse(invalidRatePacket.isValid())

        // Negative check: Schema version mismatch must be rejected
        val invalidVersionPacket = validPacket.copy(schemaVersion = "99.0")
        assertFalse(invalidVersionPacket.isValid())
    }

    @Test
    fun testDeterministicSimulatorOutputPrecision() {
        // Verify deterministic equations match python equations for seed 26004
        val imuAdapter = SimulatorSensorAdapter(Modality.IMU, 100.0)
        imuAdapter.connect()
        imuAdapter.startStream()

        val sample1 = imuAdapter.readSample()
        assertNotNull(sample1)
        assertTrue(sample1!!.isValid())
        assertEquals("1.0", sample1.schemaVersion)
        assertEquals("SUBJ_SIM_26004", sample1.subjectId)
        assertEquals(6, sample1.values.size)

        // Verify that sample 1 values are within expected mathematical bounds
        // ax at t = 1/100s = 0.04 * sin(2*pi*1.2*0.01) = 0.04 * sin(0.075398) ~= 0.003014
        val expectedAx = 0.04 * kotlin.math.sin(2.0 * Math.PI * 1.2 * 0.01)
        assertTrue(abs(sample1.values[0] - expectedAx) <= numericalToleranceEpsilon)
    }

    @Test(expected = ArthroscanException.ModelException.ModelCompatibilityException::class)
    fun testModelCompatibilityCheckBlocksMismatch() {
        val metadata = ModelMetadata(
            modelId = "arthroscan_imu_v1",
            modelVersion = "1.0.0",
            featureSchemaVersion = "1.0",
            preprocessingVersion = "1.0",
            canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
            sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            status = ModelStatus.VALIDATED
        )
        // Expects 1.0, received 1.1 -> must block inference and throw exception
        metadata.verifyCompatibility("1.1")
    }

    @Test(expected = ArthroscanException.ModelException.ModelIntegrityException::class)
    fun testModelIntegrityCheckBlocksCorruptSha() {
        val metadata = ModelMetadata(
            modelId = "arthroscan_imu_v1",
            modelVersion = "1.0.0",
            featureSchemaVersion = "1.0",
            preprocessingVersion = "1.0",
            canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
            sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            status = ModelStatus.VALIDATED
        )
        metadata.verifyIntegrity("corrupt_hash_00000000000000000000000000000000")
    }

    @Test
    fun testModalityAdaptersAllModalities() {
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
}

package com.example

import com.example.core.contract.DeviceInfo
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.QualityInfo
import com.example.core.contract.SamplingInfo
import com.example.core.contract.SchemaVersion
import com.example.core.contract.SensorPacket
import com.example.core.contract.SignalQualityStatus
import com.example.core.contract.TimestampInfo
import com.example.preprocessing.ArtifactType
import com.example.preprocessing.BiquadFilter
import com.example.preprocessing.ImuPreprocessor
import com.example.preprocessing.MovingAverageFilter
import com.example.preprocessing.PreprocessingPipeline
import com.example.preprocessing.RmsEnvelopeFilter
import com.example.preprocessing.SignalQualityGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Module3PreprocessingTest {

    @Test
    fun testBiquadLowpassFilter() {
        val lp = BiquadFilter.createLowpass(cutoffHz = 20.0, sampleRateHz = 100.0)
        var y = 0.0
        for (i in 0 until 50) {
            y = lp.process(1.0)
        }
        assertEquals(1.0, y, 0.05)
    }

    @Test
    fun testMovingAverageAndRms() {
        val ma = MovingAverageFilter(windowSize = 4)
        var maOut = 0.0
        listOf(2.0, 4.0, 6.0, 8.0).forEach { maOut = ma.process(it) }
        assertEquals(5.0, maOut, 0.001)

        val rms = RmsEnvelopeFilter(windowSize = 2)
        rms.process(3.0)
        val out = rms.process(4.0)
        assertEquals(3.5355, out, 0.01)
    }

    @Test
    fun testImuPreprocessor() {
        val imu = ImuPreprocessor()
        val vals = listOf(0.0, 1.0, 0.0, 0.1, 0.0, 0.0)
        val out = imu.processSample(vals)
        assertEquals(6, out.cleanedValues.size)
        assertTrue(out.accMagnitude > 0.0)
    }

    @Test
    fun testQualityGateClippingAndSpike() {
        val gate = SignalQualityGate()

        // Clean normal IMU window with natural variance
        val cleanWindow = (0 until 10).map { i ->
            listOf(0.01 + 0.005 * i, 0.98 - 0.003 * i, -0.05, 0.0, 0.0, 0.0)
        }
        val cleanQa = gate.assessImu(cleanWindow)
        assertEquals(SignalQualityStatus.PASS, cleanQa.status)
        assertTrue(cleanQa.sqiScore >= 0.70)

        // Clipping window (> 15.8g)
        val clipWindow = (0 until 5).map { listOf(0.0, 16.0, 0.0) }
        val clipQa = gate.assessImu(clipWindow)
        assertTrue(clipQa.detectedArtifacts.contains(ArtifactType.CLIPPING_SATURATION))
        assertTrue(clipQa.sqiScore < 0.70)

        // Motion spike window (> 4.0g)
        val spikeWindow = (0 until 5).map { listOf(0.0, 4.5, 0.0) }
        val spikeQa = gate.assessImu(spikeWindow)
        assertTrue(spikeQa.detectedArtifacts.contains(ArtifactType.MOTION_SPIKE))
    }

    @Test
    fun testQualityGateFlatline() {
        val gate = SignalQualityGate()
        val flatlineWindow = List(10) { 0.0 }
        val flatQa = gate.assessVag(flatlineWindow)
        assertTrue(flatQa.detectedArtifacts.contains(ArtifactType.FLATLINE_DROPOUT))
        assertEquals(SignalQualityStatus.FAIL, flatQa.status)
    }

    @Test
    fun testUnifiedPreprocessingPipeline() {
        val pipeline = PreprocessingPipeline()
        val pkt = SensorPacket(
            schemaVersion = SchemaVersion.CURRENT,
            subjectId = "S_TEST",
            sessionId = "SESS_01",
            sensorId = "DEV_IMU",
            modality = Modality.IMU,
            timestamp = TimestampInfo(deviceTimeMs = 1000L, sequenceNumber = 1L),
            sampling = SamplingInfo(rateHz = 100.0),
            channels = listOf("ax", "ay", "az", "gx", "gy", "gz"),
            values = listOf(0.02, 0.98, -0.04, 0.01, -0.01, 0.0),
            units = "g,rad/s",
            device = DeviceInfo(status = DeviceStatus.STREAMING),
            quality = QualityInfo(status = SignalQualityStatus.PASS, score = 1.0)
        )
        val frame = pipeline.processPacket(pkt)
        assertEquals(Modality.IMU, frame.modality)
        assertEquals(6, frame.cleanedValues.size)
        assertTrue(frame.derivedMetrics.containsKey("acc_magnitude"))
        assertNotNull(frame.quality)
    }
}

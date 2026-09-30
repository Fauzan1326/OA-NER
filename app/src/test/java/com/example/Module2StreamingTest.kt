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
import com.example.hardware.CsvReplaySensorAdapter
import com.example.hardware.GatewayPacketFramer
import com.example.hardware.GatewaySensorAdapter
import com.example.hardware.session.SessionRecorder
import com.example.hardware.streaming.CircularBuffer
import com.example.hardware.streaming.PacketSequenceTracker
import com.example.hardware.streaming.StreamIngestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Module2StreamingTest {

    @Test
    fun testCsvReplayAdapter() {
        val adapter = CsvReplaySensorAdapter(
            modality = Modality.IMU,
            csvContent = CsvReplaySensorAdapter.DEFAULT_SAMPLE_IMU,
            samplingRateHz = 100.0,
            loop = false
        )
        assertTrue(adapter.connect())
        assertTrue(adapter.startStream())

        val sample = adapter.readSample()
        assertNotNull(sample)
        assertEquals(Modality.IMU, sample!!.modality)
        assertEquals(SchemaVersion.CURRENT, sample.schemaVersion)
        assertEquals(6, sample.values.size)
        assertTrue(sample.isValid())

        val calib = adapter.calibrate()
        assertEquals("CALIBRATED", calib["status"])
        adapter.disconnect()
    }

    @Test
    fun testGatewayPacketFramerChecksum() {
        val values = listOf(0.12f, -0.45f, 1.02f)
        val frame = GatewayPacketFramer.encodeFrame(0, 105, values)
        assertEquals(GatewayPacketFramer.SOF, frame.first())
        assertEquals(GatewayPacketFramer.EOF, frame.last())

        val decoded = GatewayPacketFramer.decodeFrame(frame)
        assertNotNull(decoded)
        assertEquals(0, decoded!!.modalityId)
        assertEquals(105, decoded.sequence)
        assertEquals(3, decoded.values.size)
        assertEquals(0.12f, decoded.values[0], 0.001f)

        // Test checksum corruption
        val corrupt = frame.copyOf()
        corrupt[corrupt.size - 2] = (corrupt[corrupt.size - 2].toInt() xor 0xFF).toByte()
        val corruptDecoded = GatewayPacketFramer.decodeFrame(corrupt)
        assertNull(corruptDecoded)
    }

    @Test
    fun testCircularBufferWindow() {
        val cb = CircularBuffer(capacity = 5)
        for (i in 0 until 10) {
            val pkt = SensorPacket(
                schemaVersion = SchemaVersion.CURRENT,
                subjectId = "S1",
                sessionId = "SESS1",
                sensorId = "DEV1",
                modality = Modality.IMU,
                timestamp = TimestampInfo(deviceTimeMs = 1000L + i * 10, sequenceNumber = i.toLong()),
                sampling = SamplingInfo(rateHz = 100.0),
                channels = listOf("x"),
                values = listOf(i.toDouble()),
                units = "g",
                device = DeviceInfo(status = DeviceStatus.STREAMING),
                quality = QualityInfo(status = SignalQualityStatus.PASS, score = 1.0)
            )
            cb.append(pkt)
        }

        assertEquals(5, cb.size())
        assertEquals(100.0, cb.utilizationPercent(), 0.01)
        val window = cb.getWindow(3)
        assertEquals(3, window.size)
        assertEquals(9L, window.last().timestamp.sequenceNumber)
    }

    @Test
    fun testSequenceTrackerGapsAndLoss() {
        val tracker = PacketSequenceTracker(Modality.IMU)
        tracker.setSamplingRate(100.0)

        // Packet 1
        val p1 = SensorPacket(
            schemaVersion = SchemaVersion.CURRENT, subjectId = "S", sessionId = "S", sensorId = "D",
            modality = Modality.IMU,
            timestamp = TimestampInfo(deviceTimeMs = 1000L, sequenceNumber = 1L),
            sampling = SamplingInfo(rateHz = 100.0), channels = listOf("x"), values = listOf(1.0), units = "g",
            device = DeviceInfo(status = DeviceStatus.STREAMING), quality = QualityInfo(status = SignalQualityStatus.PASS, score = 1.0)
        )
        tracker.trackPacket(p1)
        assertEquals(1L, tracker.getTelemetry().totalPacketsReceived)
        assertEquals(0L, tracker.getTelemetry().sequenceGapsDetected)

        // Packet 4 (missed 2 and 3 -> 2 gap packets)
        val p4 = SensorPacket(
            schemaVersion = SchemaVersion.CURRENT, subjectId = "S", sessionId = "S", sensorId = "D",
            modality = Modality.IMU,
            timestamp = TimestampInfo(deviceTimeMs = 1030L, sequenceNumber = 4L),
            sampling = SamplingInfo(rateHz = 100.0), channels = listOf("x"), values = listOf(4.0), units = "g",
            device = DeviceInfo(status = DeviceStatus.STREAMING), quality = QualityInfo(status = SignalQualityStatus.PASS, score = 1.0)
        )
        tracker.trackPacket(p4)
        assertEquals(2L, tracker.getTelemetry().sequenceGapsDetected)
        assertTrue(tracker.getTelemetry().packetLossRatePercent > 0.0)
    }

    @Test
    fun testStreamIngestionEngineBoundaryRejection() {
        val engine = StreamIngestionEngine(bufferCapacity = 100)

        // Valid packet
        val validPkt = SensorPacket(
            schemaVersion = SchemaVersion.CURRENT, subjectId = "S", sessionId = "S", sensorId = "D",
            modality = Modality.VAG,
            timestamp = TimestampInfo(deviceTimeMs = 1000L, sequenceNumber = 1L),
            sampling = SamplingInfo(rateHz = 2000.0), channels = listOf("ch1"), values = listOf(0.05), units = "mV",
            device = DeviceInfo(status = DeviceStatus.STREAMING), quality = QualityInfo(status = SignalQualityStatus.PASS, score = 1.0)
        )
        assertTrue(engine.ingestPacket(validPkt))

        // Invalid packet (bad schema version)
        val invalidPkt = validPkt.copy(schemaVersion = "0.9")
        val ingested = engine.ingestPacket(invalidPkt)
        assertEquals(false, ingested)
        assertEquals(1L, engine.getTelemetry(Modality.VAG).totalPacketsDropped)
    }

    @Test
    fun testSessionRecorder() {
        val recorder = SessionRecorder()
        recorder.startRecording("TEST_SESSION")
        val pkt = SensorPacket(
            schemaVersion = SchemaVersion.CURRENT, subjectId = "S", sessionId = "TEST_SESSION", sensorId = "D",
            modality = Modality.SEMG,
            timestamp = TimestampInfo(deviceTimeMs = 1000L, sequenceNumber = 1L),
            sampling = SamplingInfo(rateHz = 1000.0), channels = listOf("vm"), values = listOf(0.1), units = "mV",
            device = DeviceInfo(status = DeviceStatus.STREAMING), quality = QualityInfo(status = SignalQualityStatus.PASS, score = 1.0)
        )
        recorder.recordPacket(pkt)
        val count = recorder.stopRecording()
        assertEquals(1, count)
        assertEquals(1, recorder.recordedPackets.size)
    }
}

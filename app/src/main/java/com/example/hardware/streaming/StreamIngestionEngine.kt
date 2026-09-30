package com.example.hardware.streaming

import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Clean Real-Time Stream Ingestion Engine:
 * Sensor -> Adapter -> Boundary Validation -> Bounded Queue -> Circular Buffer -> Telemetry
 */
class StreamIngestionEngine(
    val bufferCapacity: Int = 1000,
    val epochWindowMs: Long = 100L
) {
    val buffers = ConcurrentHashMap<Modality, CircularBuffer>().apply {
        Modality.values().forEach { put(it, CircularBuffer(bufferCapacity)) }
    }

    val trackers = ConcurrentHashMap<Modality, PacketSequenceTracker>().apply {
        Modality.values().forEach { put(it, PacketSequenceTracker(it)) }
    }

    val synchronizer = TimestampSynchronizer(epochWindowMs)

    private val _telemetryState = MutableStateFlow<Map<Modality, StreamHealthTelemetry>>(emptyMap())
    val telemetryState: StateFlow<Map<Modality, StreamHealthTelemetry>> = _telemetryState.asStateFlow()

    private var totalPacketsIngested = 0L
    private var totalPacketsDropped = 0L

    fun ingestPacket(packet: SensorPacket): Boolean {
        // 1. Boundary Contract Validation
        if (!packet.isValid()) {
            totalPacketsDropped++
            trackers[packet.modality]?.recordDroppedPacket()
            updateTelemetryState()
            return false
        }

        // 2. Sequence & Loss Tracking
        val tracker = trackers[packet.modality] ?: PacketSequenceTracker(packet.modality).also {
            trackers[packet.modality] = it
        }
        tracker.trackPacket(packet)

        // 3. Circular Ring Buffer
        val buf = buffers[packet.modality] ?: CircularBuffer(bufferCapacity).also {
            buffers[packet.modality] = it
        }
        buf.append(packet)
        tracker.updateBufferUtilization(buf.utilizationPercent())

        // 4. Multi-Rate Timestamp Synchronization
        synchronizer.ingest(packet)

        totalPacketsIngested++
        updateTelemetryState()
        return true
    }

    fun getTelemetry(modality: Modality): StreamHealthTelemetry {
        return trackers[modality]?.getTelemetry() ?: StreamHealthTelemetry(modality = modality)
    }

    fun getAllTelemetry(): Map<Modality, StreamHealthTelemetry> {
        return trackers.mapValues { it.value.getTelemetry() }
    }

    fun getCircularBuffer(modality: Modality): CircularBuffer? {
        return buffers[modality]
    }

    private fun updateTelemetryState() {
        _telemetryState.value = getAllTelemetry()
    }

    fun clear() {
        buffers.values.forEach { it.clear() }
        synchronizer.clear()
        updateTelemetryState()
    }
}

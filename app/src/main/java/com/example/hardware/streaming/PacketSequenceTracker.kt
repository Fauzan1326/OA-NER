package com.example.hardware.streaming

import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import java.util.ArrayDeque
import kotlin.math.abs

data class StreamHealthTelemetry(
    val modality: Modality,
    val totalPacketsReceived: Long = 0L,
    val totalPacketsDropped: Long = 0L,
    val sequenceGapsDetected: Long = 0L,
    val duplicatesDetected: Long = 0L,
    val outOfOrderDetected: Long = 0L,
    val bufferUtilizationPercent: Double = 0.0,
    val packetLossRatePercent: Double = 0.0,
    val averageJitterMs: Double = 0.0,
    val lastSequenceNumber: Long = -1L,
    val lastDeviceTimeMs: Long = -1L
)

class PacketSequenceTracker(val modality: Modality) {
    private var telemetry = StreamHealthTelemetry(modality = modality)
    private var expectedIntervalMs = 10.0
    private val jitterHistory = ArrayDeque<Double>()
    private val lock = Any()

    fun setSamplingRate(rateHz: Double) {
        synchronized(lock) {
            if (rateHz > 0) {
                expectedIntervalMs = 1000.0 / rateHz
            }
        }
    }

    fun trackPacket(packet: SensorPacket): StreamHealthTelemetry {
        synchronized(lock) {
            val totalRx = telemetry.totalPacketsReceived + 1
            val seq = packet.timestamp.sequenceNumber
            val devTime = packet.timestamp.deviceTimeMs

            var gaps = telemetry.sequenceGapsDetected
            var dups = telemetry.duplicatesDetected
            var outOfOrder = telemetry.outOfOrderDetected
            var avgJitter = telemetry.averageJitterMs

            if (telemetry.lastSequenceNumber >= 0) {
                val seqDiff = seq - telemetry.lastSequenceNumber
                when {
                    seqDiff == 0L -> dups++
                    seqDiff < 0L -> outOfOrder++
                    seqDiff > 1L -> gaps += (seqDiff - 1L)
                }

                if (telemetry.lastDeviceTimeMs > 0) {
                    val actualInterval = (devTime - telemetry.lastDeviceTimeMs).toDouble()
                    val jitter = abs(actualInterval - expectedIntervalMs)
                    if (jitterHistory.size >= 50) {
                        jitterHistory.removeFirst()
                    }
                    jitterHistory.addLast(jitter)
                    avgJitter = jitterHistory.average()
                }
            }

            val totalExpected = totalRx + gaps
            val lossPercent = if (totalExpected > 0) (gaps.toDouble() / totalExpected.toDouble()) * 100.0 else 0.0

            telemetry = telemetry.copy(
                totalPacketsReceived = totalRx,
                sequenceGapsDetected = gaps,
                duplicatesDetected = dups,
                outOfOrderDetected = outOfOrder,
                packetLossRatePercent = lossPercent,
                averageJitterMs = avgJitter,
                lastSequenceNumber = seq,
                lastDeviceTimeMs = devTime
            )
            return telemetry
        }
    }

    fun recordDroppedPacket() {
        synchronized(lock) {
            telemetry = telemetry.copy(totalPacketsDropped = telemetry.totalPacketsDropped + 1)
        }
    }

    fun updateBufferUtilization(percent: Double) {
        synchronized(lock) {
            telemetry = telemetry.copy(bufferUtilizationPercent = percent)
        }
    }

    fun getTelemetry(): StreamHealthTelemetry {
        synchronized(lock) {
            return telemetry
        }
    }
}

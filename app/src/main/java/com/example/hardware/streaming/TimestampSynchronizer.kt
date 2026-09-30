package com.example.hardware.streaming

import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import java.util.concurrent.ConcurrentHashMap

data class SynchronizedFrame(
    val epochMs: Long,
    val packetsByModality: Map<Modality, List<SensorPacket>>
)

class TimestampSynchronizer(val epochWindowMs: Long = 100L) {
    private val epochBuckets = ConcurrentHashMap<Long, MutableMap<Modality, MutableList<SensorPacket>>>()

    fun ingest(packet: SensorPacket) {
        val epoch = (packet.timestamp.deviceTimeMs / epochWindowMs) * epochWindowMs
        val bucket = epochBuckets.computeIfAbsent(epoch) {
            ConcurrentHashMap<Modality, MutableList<SensorPacket>>().apply {
                Modality.values().forEach { put(it, mutableListOf()) }
            }
        }
        bucket[packet.modality]?.add(packet)

        // Prune old epochs to maintain a bounded window
        if (epochBuckets.size > 30) {
            val oldest = epochBuckets.keys.minOrNull()
            if (oldest != null) {
                epochBuckets.remove(oldest)
            }
        }
    }

    fun getSynchronizedFrame(epochMs: Long): SynchronizedFrame? {
        val bucket = epochBuckets[epochMs] ?: return null
        return SynchronizedFrame(
            epochMs = epochMs,
            packetsByModality = bucket.toMap()
        )
    }

    fun getLatestCompletedEpoch(): Long? {
        val sorted = epochBuckets.keys.sorted()
        return if (sorted.size >= 2) sorted[sorted.size - 2] else null
    }

    fun clear() {
        epochBuckets.clear()
    }
}

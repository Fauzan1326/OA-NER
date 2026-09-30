package com.example.hardware.streaming

import com.example.core.contract.SensorPacket

/**
 * Thread-safe fixed-capacity circular ring buffer for fast window extraction
 * without continuous memory re-allocation.
 */
class CircularBuffer(val capacity: Int) {
    init {
        require(capacity > 0) { "Capacity must be greater than 0" }
    }

    private val lock = Any()
    private val buffer = arrayOfNulls<SensorPacket>(capacity)
    private var head = 0
    private var count = 0

    fun append(packet: SensorPacket) {
        synchronized(lock) {
            buffer[head] = packet
            head = (head + 1) % capacity
            if (count < capacity) {
                count++
            }
        }
    }

    fun getWindow(size: Int): List<SensorPacket> {
        synchronized(lock) {
            if (size <= 0 || count == 0) return emptyList()
            val windowSize = minOf(size, count)
            val result = ArrayList<SensorPacket>(windowSize)
            var startIdx = (head - windowSize + capacity) % capacity
            for (i in 0 until windowSize) {
                buffer[(startIdx + i) % capacity]?.let { result.add(it) }
            }
            return result
        }
    }

    fun getAll(): List<SensorPacket> {
        synchronized(lock) {
            return getWindow(count)
        }
    }

    fun size(): Int {
        synchronized(lock) {
            return count
        }
    }

    fun utilizationPercent(): Double {
        synchronized(lock) {
            return (count.toDouble() / capacity.toDouble()) * 100.0
        }
    }

    fun clear() {
        synchronized(lock) {
            buffer.fill(null)
            head = 0
            count = 0
        }
    }
}

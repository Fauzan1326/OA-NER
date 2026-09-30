package com.example.hardware.session

import com.example.core.contract.SensorPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory and local session recorder for ARTHROSCAN-NER.
 * Records validated streaming SensorPacket sequences for audit, replay, and calibration.
 */
class SessionRecorder {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordedPackets = CopyOnWriteArrayList<SensorPacket>()
    val recordedPackets: List<SensorPacket> get() = _recordedPackets

    private var activeSessionId: String? = null

    fun startRecording(sessionId: String): Boolean {
        activeSessionId = sessionId
        _recordedPackets.clear()
        _isRecording.value = true
        return true
    }

    fun recordPacket(packet: SensorPacket) {
        if (_isRecording.value) {
            _recordedPackets.add(packet)
        }
    }

    fun stopRecording(): Int {
        _isRecording.value = false
        val count = _recordedPackets.size
        activeSessionId = null
        return count
    }

    fun clear() {
        _recordedPackets.clear()
        _isRecording.value = false
        activeSessionId = null
    }
}

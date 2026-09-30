package com.example.domain.repository

import com.example.core.config.AppConfig
import com.example.core.config.ProfileType
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import com.example.hardware.streaming.StreamHealthTelemetry
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sensor Repository Interface (Clean Architecture Domain Boundary)
 */
interface SensorRepository {
    val packetStream: SharedFlow<SensorPacket>
    val sensorStatuses: StateFlow<Map<Modality, DeviceStatus>>
    val isStreaming: StateFlow<Boolean>
    val activeConfig: StateFlow<AppConfig>
    val telemetryState: StateFlow<Map<Modality, StreamHealthTelemetry>>
    val isRecording: StateFlow<Boolean>

    suspend fun connectAll(): Boolean
    suspend fun disconnectAll(): Boolean
    suspend fun startStreaming(): Boolean
    suspend fun stopStreaming(): Boolean
    fun setProfile(profileType: ProfileType)
    suspend fun calibrateAll(): Map<Modality, Map<String, Any>>
    fun startRecording(sessionId: String): Boolean
    fun stopRecording(): Int
    fun getRecordedPackets(): List<SensorPacket>
}

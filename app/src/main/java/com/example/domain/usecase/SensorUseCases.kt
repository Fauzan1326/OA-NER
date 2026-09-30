package com.example.domain.usecase

import com.example.core.config.ProfileType
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import com.example.domain.repository.SensorRepository
import com.example.hardware.streaming.StreamHealthTelemetry
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class GetSensorTelemetryUseCase(private val repository: SensorRepository) {
    val packetStream: SharedFlow<SensorPacket> = repository.packetStream
    val sensorStatuses: StateFlow<Map<Modality, DeviceStatus>> = repository.sensorStatuses
    val isStreaming: StateFlow<Boolean> = repository.isStreaming
    val telemetryState: StateFlow<Map<Modality, StreamHealthTelemetry>> = repository.telemetryState
    val isRecording: StateFlow<Boolean> = repository.isRecording
}

class ConnectSensorsUseCase(private val repository: SensorRepository) {
    suspend fun connect(): Boolean = repository.connectAll()
    suspend fun disconnect(): Boolean = repository.disconnectAll()
}

class ToggleStreamingUseCase(private val repository: SensorRepository) {
    suspend fun start(): Boolean = repository.startStreaming()
    suspend fun stop(): Boolean = repository.stopStreaming()
}

class SelectProfileUseCase(private val repository: SensorRepository) {
    fun execute(profileType: ProfileType) {
        repository.setProfile(profileType)
    }
}

class ValidatePacketUseCase {
    fun execute(packet: SensorPacket): Boolean = packet.isValid()
}

class CalibrateSensorsUseCase(private val repository: SensorRepository) {
    suspend fun execute(): Map<Modality, Map<String, Any>> = repository.calibrateAll()
}

class RecordSessionUseCase(private val repository: SensorRepository) {
    fun start(sessionId: String): Boolean = repository.startRecording(sessionId)
    fun stop(): Int = repository.stopRecording()
    fun getRecorded(): List<SensorPacket> = repository.getRecordedPackets()
}

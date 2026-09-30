package com.example.data.repository

import com.example.core.config.AppConfig
import com.example.core.config.ProfileType
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import com.example.domain.repository.SensorRepository
import com.example.hardware.SensorPluginRegistry
import com.example.hardware.streaming.StreamHealthTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorRepositoryImpl(
    private val pluginRegistry: SensorPluginRegistry = SensorPluginRegistry()
) : SensorRepository {

    private val _activeConfig = MutableStateFlow(AppConfig.defaultFor(ProfileType.DEMO))
    override val activeConfig: StateFlow<AppConfig> = _activeConfig.asStateFlow()

    override val packetStream: SharedFlow<SensorPacket> = pluginRegistry.packetFlow
    override val sensorStatuses: StateFlow<Map<Modality, DeviceStatus>> = pluginRegistry.sensorStatuses
    override val isStreaming: StateFlow<Boolean> = pluginRegistry.isStreaming
    override val telemetryState: StateFlow<Map<Modality, StreamHealthTelemetry>> = pluginRegistry.telemetry
    override val isRecording: StateFlow<Boolean> = pluginRegistry.sessionRecorder.isRecording

    override suspend fun connectAll(): Boolean {
        return pluginRegistry.connectAll()
    }

    override suspend fun disconnectAll(): Boolean {
        return pluginRegistry.disconnectAll()
    }

    override suspend fun startStreaming(): Boolean {
        return pluginRegistry.startAllStreams()
    }

    override suspend fun stopStreaming(): Boolean {
        return pluginRegistry.stopAllStreams()
    }

    override fun setProfile(profileType: ProfileType) {
        _activeConfig.value = AppConfig.defaultFor(profileType)
        pluginRegistry.configureProfile(profileType)
    }

    override suspend fun calibrateAll(): Map<Modality, Map<String, Any>> {
        return pluginRegistry.calibrateAll()
    }

    override fun startRecording(sessionId: String): Boolean {
        return pluginRegistry.sessionRecorder.startRecording(sessionId)
    }

    override fun stopRecording(): Int {
        return pluginRegistry.sessionRecorder.stopRecording()
    }

    override fun getRecordedPackets(): List<SensorPacket> {
        return pluginRegistry.sessionRecorder.recordedPackets
    }
}

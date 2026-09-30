package com.example.hardware

import com.example.core.config.ProfileType
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket
import com.example.core.logging.StructuredLogger
import com.example.hardware.session.SessionRecorder
import com.example.hardware.streaming.StreamHealthTelemetry
import com.example.hardware.streaming.StreamIngestionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ModalityState(
    val modality: Modality,
    val adapterType: String,
    val status: DeviceStatus,
    val packetsReceived: Long = 0L,
    val lastQualityScore: Double = 1.0,
    val lastSampleRate: Double = 0.0,
    val latestPacket: SensorPacket? = null
)

class SensorPluginRegistry(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val ingestionEngine: StreamIngestionEngine = StreamIngestionEngine(),
    val sessionRecorder: SessionRecorder = SessionRecorder()
) {
    private val adapters = mutableMapOf<Modality, SensorAdapter>()

    private val _states = MutableStateFlow<Map<Modality, ModalityState>>(emptyMap())
    val states: StateFlow<Map<Modality, ModalityState>> = _states.asStateFlow()

    private val _sensorStatuses = MutableStateFlow<Map<Modality, DeviceStatus>>(emptyMap())
    val sensorStatuses: StateFlow<Map<Modality, DeviceStatus>> = _sensorStatuses.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _packetStream = MutableSharedFlow<SensorPacket>(replay = 10, extraBufferCapacity = 128)
    val packetFlow: SharedFlow<SensorPacket> = _packetStream.asSharedFlow()

    val telemetry: StateFlow<Map<Modality, StreamHealthTelemetry>> = ingestionEngine.telemetryState

    private var streamingJob: Job? = null
    private var currentProfile: ProfileType = ProfileType.DEMO

    init {
        configureProfile(ProfileType.DEMO)
    }

    fun configureProfile(profile: ProfileType) {
        stopAllStreams()
        disconnectAll()
        adapters.clear()
        currentProfile = profile

        when (profile) {
            ProfileType.RESEARCH -> {
                register(CsvReplaySensorAdapter(Modality.IMU, CsvReplaySensorAdapter.DEFAULT_SAMPLE_IMU, 100.0))
                register(CsvReplaySensorAdapter(Modality.VAG, CsvReplaySensorAdapter.DEFAULT_SAMPLE_VAG, 2000.0))
                register(CsvReplaySensorAdapter(Modality.SEMG, CsvReplaySensorAdapter.DEFAULT_SAMPLE_SEMG, 1000.0))
                register(CsvReplaySensorAdapter(Modality.RF, CsvReplaySensorAdapter.DEFAULT_SAMPLE_RF, 10.0))
            }
            ProfileType.HARDWARE -> {
                register(GatewaySensorAdapter(Modality.IMU, "COM3", "USB_CDC", 100.0))
                register(GatewaySensorAdapter(Modality.VAG, "BLE:AA:BB:CC:01", "BLE", 2000.0))
                register(GatewaySensorAdapter(Modality.SEMG, "COM4", "USB_CDC", 1000.0))
                register(GatewaySensorAdapter(Modality.RF, "COM5", "USB_CDC", 10.0))
            }
            ProfileType.DEMO -> {
                register(SimulatorSensorAdapter(Modality.IMU, 100.0))
                register(SimulatorSensorAdapter(Modality.VAG, 2000.0))
                register(SimulatorSensorAdapter(Modality.SEMG, 1000.0))
                register(SimulatorSensorAdapter(Modality.RF, 10.0))
            }
        }
        StructuredLogger.i("SENSOR_REGISTRY", "Configured hardware adapters for profile: ${profile.name}")
    }

    fun register(adapter: SensorAdapter) {
        adapters[adapter.modality] = adapter
        updateState(adapter.modality) {
            ModalityState(
                modality = adapter.modality,
                adapterType = adapter.getMetadata()["sensor_type"] as? String ?: "ADAPTER",
                status = adapter.getStatus(),
                lastSampleRate = (adapter.getMetadata()["rate_hz"] as? Double) ?: 0.0
            )
        }
        updateStatuses()
        StructuredLogger.i("SENSOR_REGISTRY", "Registered adapter for ${adapter.modality.name}")
    }

    fun connectAll(): Boolean {
        var allSuccess = true
        adapters.values.forEach { adapter ->
            val ok = adapter.connect()
            if (!ok) allSuccess = false
            updateState(adapter.modality) { it.copy(status = adapter.getStatus()) }
        }
        updateStatuses()
        StructuredLogger.i("SENSOR_REGISTRY", "Connect all sensors: success=$allSuccess")
        return allSuccess
    }

    fun startAllStreams(): Boolean {
        adapters.values.forEach { it.startStream() }
        adapters.values.forEach { adapter ->
            updateState(adapter.modality) { it.copy(status = adapter.getStatus()) }
        }
        updateStatuses()

        streamingJob?.cancel()
        _isStreaming.value = true
        streamingJob = scope.launch {
            StructuredLogger.i("SENSOR_REGISTRY", "Starting real-time sensor ingestion loop")
            while (isActive) {
                adapters.values.forEach { adapter ->
                    val packet = adapter.readSample()
                    if (packet != null) {
                        // Ingest into Real-Time Stream Ingestion Engine (Validates, tracks gaps, ring-buffers)
                        val ingested = ingestionEngine.ingestPacket(packet)
                        if (ingested) {
                            sessionRecorder.recordPacket(packet)
                            _packetStream.tryEmit(packet)
                            updateState(adapter.modality) { curr ->
                                curr.copy(
                                    packetsReceived = curr.packetsReceived + 1,
                                    lastQualityScore = packet.quality.score,
                                    latestPacket = packet,
                                    status = adapter.getStatus()
                                )
                            }
                        }
                    }
                }
                delay(100) // Deterministic streaming cadence tick
            }
        }
        return true
    }

    fun stopAllStreams(): Boolean {
        streamingJob?.cancel()
        streamingJob = null
        _isStreaming.value = false
        adapters.values.forEach { it.stopStream() }
        adapters.values.forEach { adapter ->
            updateState(adapter.modality) { it.copy(status = adapter.getStatus()) }
        }
        updateStatuses()
        StructuredLogger.i("SENSOR_REGISTRY", "Stopped sensor streaming loop")
        return true
    }

    fun disconnectAll(): Boolean {
        stopAllStreams()
        adapters.values.forEach { it.disconnect() }
        adapters.values.forEach { adapter ->
            updateState(adapter.modality) { it.copy(status = adapter.getStatus()) }
        }
        updateStatuses()
        StructuredLogger.i("SENSOR_REGISTRY", "Disconnected all sensors")
        return true
    }

    fun calibrateAll(): Map<Modality, Map<String, Any>> {
        val results = mutableMapOf<Modality, Map<String, Any>>()
        adapters.forEach { (modality, adapter) ->
            val res = adapter.calibrate()
            results[modality] = res
            StructuredLogger.i("SENSOR_REGISTRY", "Calibrated ${modality.name}: $res")
        }
        return results
    }

    fun getAdapter(modality: Modality): SensorAdapter? = adapters[modality]

    private fun updateStatuses() {
        _sensorStatuses.value = adapters.mapValues { it.value.getStatus() }
    }

    private fun updateState(modality: Modality, transform: (ModalityState) -> ModalityState) {
        val currentMap = _states.value.toMutableMap()
        val existing = currentMap[modality] ?: ModalityState(
            modality = modality,
            adapterType = "UNKNOWN",
            status = DeviceStatus.DISCONNECTED
        )
        currentMap[modality] = transform(existing)
        _states.value = currentMap
    }
}

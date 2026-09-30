package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DemoScenarioType
import com.example.ai.DeterministicDemoScenarios
import com.example.ai.InferenceTraceStep
import com.example.ai.ModalityValidationResult
import com.example.ai.ModelMetadata
import com.example.ai.MultimodalScreeningDecision
import com.example.ai.RichModalityPrediction
import com.example.core.config.AppConfig
import com.example.core.config.ProfileType
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.SchemaVersion
import com.example.core.contract.SensorPacket
import com.example.core.logging.LogEntry
import com.example.core.logging.StructuredLogger
import com.example.data.repository.SensorRepositoryImpl
import com.example.domain.repository.SensorRepository
import com.example.domain.usecase.CalibrateSensorsUseCase
import com.example.domain.usecase.ConnectSensorsUseCase
import com.example.domain.usecase.ExtractFeaturesUseCase
import com.example.domain.usecase.GetModelRegistryUseCase
import com.example.domain.usecase.GetSensorTelemetryUseCase
import com.example.domain.usecase.PreprocessPacketUseCase
import com.example.domain.usecase.QueryFeatureStoreUseCase
import com.example.domain.usecase.RecordSessionUseCase
import com.example.domain.usecase.RunModalityInferenceUseCase
import com.example.domain.usecase.RunMultimodalFusionUseCase
import com.example.domain.usecase.SelectProfileUseCase
import com.example.domain.usecase.ToggleStreamingUseCase
import com.example.domain.usecase.ValidateInferenceInputsUseCase
import com.example.domain.usecase.ValidatePacketUseCase
import com.example.features.ModalityFeatureVector
import com.example.hardware.ModalityState
import com.example.hardware.SensorPluginRegistry
import com.example.hardware.streaming.StreamHealthTelemetry
import com.example.preprocessing.ArtifactType
import com.example.preprocessing.PreprocessedFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val profile: ProfileType = ProfileType.DEMO,
    val config: AppConfig = AppConfig.defaultFor(ProfileType.DEMO),
    val isStreaming: Boolean = false,
    val isConnected: Boolean = false,
    val totalPacketsReceived: Long = 0L,
    val validPacketsCount: Long = 0L,
    val droppedPacketsCount: Long = 0L,
    val modalityStates: Map<Modality, ModalityState> = emptyMap(),
    val telemetryByModality: Map<Modality, StreamHealthTelemetry> = emptyMap(),
    val sqiScores: Map<Modality, Double> = emptyMap(),
    val detectedArtifacts: Map<Modality, List<ArtifactType>> = emptyMap(),
    val derivedMetrics: Map<Modality, Map<String, Double>> = emptyMap(),
    val latestFeatureVectors: Map<Modality, ModalityFeatureVector> = emptyMap(),
    val featureStoreCount: Int = 0,
    val featureStoreCountByModality: Map<Modality, Int> = emptyMap(),
    val latestPredictions: Map<Modality, RichModalityPrediction> = emptyMap(),
    val registeredModels: List<ModelMetadata> = emptyList(),
    val modalityValidations: Map<Modality, ModalityValidationResult> = emptyMap(),
    val screeningDecision: MultimodalScreeningDecision? = null,
    val isAnalyzing: Boolean = false,
    val analysisStepDescription: String? = null,
    val screeningSessionHistory: List<MultimodalScreeningDecision> = emptyList(),
    val selectedDemoScenario: DemoScenarioType = DemoScenarioType.CLEAN_MULTIMODAL,
    val isRecording: Boolean = false,
    val calibrationMessage: String? = null,
    val latestPacket: SensorPacket? = null,
    val latestFrame: PreprocessedFrame? = null,
    val logs: List<LogEntry> = emptyList(),
    val schemaVersion: String = SchemaVersion.CURRENT
)

/**
 * Clean Architecture ViewModel: Orchestrates domain use cases
 * UI observes state only; no direct BLE or ONNX interactions from UI.
 */
class ArthroscanViewModel(
    private val pluginRegistry: SensorPluginRegistry = SensorPluginRegistry(),
    private val sensorRepository: SensorRepository = SensorRepositoryImpl(pluginRegistry),
    private val getSensorTelemetryUseCase: GetSensorTelemetryUseCase = GetSensorTelemetryUseCase(sensorRepository),
    private val connectSensorsUseCase: ConnectSensorsUseCase = ConnectSensorsUseCase(sensorRepository),
    private val toggleStreamingUseCase: ToggleStreamingUseCase = ToggleStreamingUseCase(sensorRepository),
    private val selectProfileUseCase: SelectProfileUseCase = SelectProfileUseCase(sensorRepository),
    private val validatePacketUseCase: ValidatePacketUseCase = ValidatePacketUseCase(),
    private val calibrateSensorsUseCase: CalibrateSensorsUseCase = CalibrateSensorsUseCase(sensorRepository),
    private val recordSessionUseCase: RecordSessionUseCase = RecordSessionUseCase(sensorRepository),
    private val preprocessPacketUseCase: PreprocessPacketUseCase = PreprocessPacketUseCase(),
    private val extractFeaturesUseCase: ExtractFeaturesUseCase = ExtractFeaturesUseCase(),
    val queryFeatureStoreUseCase: QueryFeatureStoreUseCase = QueryFeatureStoreUseCase(extractFeaturesUseCase.pipeline.featureStore),
    private val runModalityInferenceUseCase: RunModalityInferenceUseCase = RunModalityInferenceUseCase(),
    private val getModelRegistryUseCase: GetModelRegistryUseCase = GetModelRegistryUseCase(),
    private val validateInferenceInputsUseCase: ValidateInferenceInputsUseCase = ValidateInferenceInputsUseCase(),
    private val runMultimodalFusionUseCase: RunMultimodalFusionUseCase = RunMultimodalFusionUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = combine(
        _uiState,
        pluginRegistry.states,
        getSensorTelemetryUseCase.telemetryState,
        getSensorTelemetryUseCase.isRecording,
        StructuredLogger.logs
    ) { current, modalityStates, telemetry, isRec, logs ->
        val isAnyStreaming = modalityStates.values.any { it.status == DeviceStatus.STREAMING }
        val isAllConnected = modalityStates.values.isNotEmpty() && modalityStates.values.all {
            it.status == DeviceStatus.CONNECTED || it.status == DeviceStatus.STREAMING
        }
        val totalPackets = modalityStates.values.sumOf { it.packetsReceived }
        val totalDropped = telemetry.values.sumOf { it.totalPacketsDropped }

        current.copy(
            isStreaming = isAnyStreaming,
            isConnected = isAllConnected,
            modalityStates = modalityStates,
            telemetryByModality = telemetry,
            isRecording = isRec,
            totalPacketsReceived = totalPackets,
            validPacketsCount = totalPackets,
            droppedPacketsCount = totalDropped,
            logs = logs.takeLast(20)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        StructuredLogger.i("VIEWMODEL", "ARTHROSCAN Foundation initialized with Canonical Schema v${SchemaVersion.CURRENT}")
        val registered = getModelRegistryUseCase.listModels()
        _uiState.value = _uiState.value.copy(
            registeredModels = registered,
            modalityValidations = validateInferenceInputsUseCase.execute(emptyMap(), emptyMap())
        )

        viewModelScope.launch {
            getSensorTelemetryUseCase.packetStream.collect { packet ->
                if (validatePacketUseCase.execute(packet)) {
                    val frame = preprocessPacketUseCase.execute(packet)
                    val featureVector = extractFeaturesUseCase.execute(frame, packet)

                    val newSqi = _uiState.value.sqiScores.toMutableMap()
                    val newArtifacts = _uiState.value.detectedArtifacts.toMutableMap()
                    val newMetrics = _uiState.value.derivedMetrics.toMutableMap()
                    val newFeatureVectors = _uiState.value.latestFeatureVectors.toMutableMap()
                    val newPredictions = _uiState.value.latestPredictions.toMutableMap()

                    newSqi[packet.modality] = frame.quality.sqiScore
                    newArtifacts[packet.modality] = frame.quality.detectedArtifacts
                    newMetrics[packet.modality] = frame.derivedMetrics
                    if (featureVector != null) {
                        newFeatureVectors[packet.modality] = featureVector
                        // Module 5: Independent Modality AI Model Inference
                        val prediction = runModalityInferenceUseCase.execute(featureVector)
                        newPredictions[packet.modality] = prediction
                    } else {
                        // Quality gate failed or window incomplete: remove stale feature vector for this modality
                        newFeatureVectors.remove(packet.modality)
                    }

                    val totalStoreCount = queryFeatureStoreUseCase.getCount()
                    val countsByMod = Modality.values().associateWith { queryFeatureStoreUseCase.getCount(it) }
                    val validations = validateInferenceInputsUseCase.execute(newFeatureVectors, newSqi)

                    _uiState.value = _uiState.value.copy(
                        latestPacket = packet,
                        latestFrame = frame,
                        sqiScores = newSqi,
                        detectedArtifacts = newArtifacts,
                        derivedMetrics = newMetrics,
                        latestFeatureVectors = newFeatureVectors,
                        featureStoreCount = totalStoreCount,
                        featureStoreCountByModality = countsByMod,
                        latestPredictions = newPredictions,
                        modalityValidations = validations
                    )
                }
            }
        }
    }

    fun switchProfile(profile: ProfileType) {
        selectProfileUseCase.execute(profile)
        preprocessPacketUseCase.reset()
        extractFeaturesUseCase.reset()
        val newConfig = AppConfig.defaultFor(profile)
        _uiState.value = _uiState.value.copy(
            profile = profile,
            config = newConfig,
            calibrationMessage = null,
            latestFeatureVectors = emptyMap(),
            featureStoreCount = 0,
            featureStoreCountByModality = emptyMap(),
            screeningDecision = null,
            modalityValidations = validateInferenceInputsUseCase.execute(emptyMap(), emptyMap())
        )
        StructuredLogger.i("VIEWMODEL", "Switched profile to ${profile.name}: ${profile.label}")
    }

    fun setDemoScenario(scenario: DemoScenarioType) {
        _uiState.value = _uiState.value.copy(selectedDemoScenario = scenario)
        StructuredLogger.i("VIEWMODEL", "Selected demo scenario: ${scenario.title}")
    }

    fun loadDemoScenario(scenario: DemoScenarioType) {
        val (vectors, sqiScores) = DeterministicDemoScenarios.generateScenarioVectors(
            scenario = scenario,
            sessionId = "DEMO_SESS_26004_${System.currentTimeMillis() % 1000}"
        )
        val predictions = mutableMapOf<Modality, RichModalityPrediction>()
        vectors.forEach { (mod, vec) ->
            predictions[mod] = runModalityInferenceUseCase.execute(vec)
        }
        val validations = validateInferenceInputsUseCase.execute(vectors, sqiScores)

        _uiState.value = _uiState.value.copy(
            selectedDemoScenario = scenario,
            latestFeatureVectors = vectors,
            sqiScores = sqiScores,
            latestPredictions = predictions,
            modalityValidations = validations
        )
        StructuredLogger.i("VIEWMODEL", "Loaded scenario data: ${scenario.title} (Seed 26004)")
    }

    fun runMultimodalInference() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = _uiState.value.copy(isAnalyzing = true, analysisStepDescription = "VALIDATING FEATURES...")

            delay(160)
            _uiState.value = _uiState.value.copy(analysisStepDescription = "NORMALIZING (LOCKED PARAMETERS)...")

            delay(160)
            _uiState.value = _uiState.value.copy(analysisStepDescription = "RUNNING MODALITY MODELS...")

            delay(180)
            _uiState.value = _uiState.value.copy(analysisStepDescription = "ALIGNING FEATURES & EVALUATING COVERAGE...")

            delay(160)
            _uiState.value = _uiState.value.copy(analysisStepDescription = "MULTIMODAL FUSION...")

            delay(160)
            _uiState.value = _uiState.value.copy(analysisStepDescription = "ESTIMATING UNCERTAINTY...")

            delay(160)
            _uiState.value = _uiState.value.copy(analysisStepDescription = "GENERATING SCREENING TIER...")

            delay(140)
            val sessId = "SESS_${System.currentTimeMillis()}"
            val subjId = "SUBJ_ANON_26004"
            val decision = runMultimodalFusionUseCase.execute(
                sessionId = sessId,
                subjectId = subjId,
                vectors = state.latestFeatureVectors,
                sqiScores = state.sqiScores,
                isDemoSimulation = state.profile == ProfileType.DEMO
            )

            val updatedHistory = listOf(decision) + state.screeningSessionHistory.take(4)

            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                analysisStepDescription = null,
                screeningDecision = decision,
                screeningSessionHistory = updatedHistory
            )
            StructuredLogger.i("VIEWMODEL", "Inference complete: Tier=${decision.screeningRiskTier.label}, Uncertainty=${decision.uncertaintyResult.tier.label}")
        }
    }

    fun clearScreeningDecision() {
        _uiState.value = _uiState.value.copy(screeningDecision = null)
    }

    fun connectSensors() {
        viewModelScope.launch {
            val ok = connectSensorsUseCase.connect()
            if (ok) {
                StructuredLogger.i("VIEWMODEL", "All sensor adapters connected to universal bus")
            }
        }
    }

    fun toggleStreaming() {
        viewModelScope.launch {
            val state = uiState.value
            if (state.isStreaming) {
                toggleStreamingUseCase.stop()
            } else {
                if (!state.isConnected) {
                    connectSensorsUseCase.connect()
                }
                toggleStreamingUseCase.start()
            }
        }
    }

    fun disconnectSensors() {
        viewModelScope.launch {
            connectSensorsUseCase.disconnect()
        }
    }

    fun calibrateSensors() {
        viewModelScope.launch {
            val results = calibrateSensorsUseCase.execute()
            val msg = "Calibrated ${results.size} sensors: Zero-point offset locked."
            _uiState.value = _uiState.value.copy(calibrationMessage = msg)
            StructuredLogger.i("VIEWMODEL", msg)
        }
    }

    fun toggleRecording() {
        val state = uiState.value
        if (state.isRecording) {
            val count = recordSessionUseCase.stop()
            StructuredLogger.i("VIEWMODEL", "Session recording stopped. Stored $count packets.")
        } else {
            val sessId = "SESS_${System.currentTimeMillis()}"
            recordSessionUseCase.start(sessId)
            StructuredLogger.i("VIEWMODEL", "Session recording started ($sessId).")
        }
    }
}

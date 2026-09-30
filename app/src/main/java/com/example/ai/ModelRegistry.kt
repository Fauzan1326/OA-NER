package com.example.ai

import com.example.core.contract.Modality
import com.example.features.ModalityFeatureVector
import java.util.concurrent.ConcurrentHashMap

class ModelRegistry {
    private val runners = ConcurrentHashMap<Modality, ModalityInferenceRunner>()

    init {
        registerRunner(ImuModalityModelRunner())
        registerRunner(VagModalityModelRunner())
        registerRunner(SemgModalityModelRunner())
        registerRunner(RfExperimentalModelRunner())
    }

    fun registerRunner(runner: ModalityInferenceRunner) {
        runners[runner.modality] = runner
    }

    fun getRunner(modality: Modality): ModalityInferenceRunner? = runners[modality]

    fun listModels(): List<ModelMetadata> = runners.values.map { it.metadata }

    fun unloadAll() {
        runners.values.forEach { it.unload() }
        runners.clear()
    }
}

class ModalityInferenceOrchestrator(
    private val registry: ModelRegistry = ModelRegistry()
) {
    fun runInference(vector: ModalityFeatureVector): RichModalityPrediction {
        val modality = vector.provenance.modality
        val runner = registry.getRunner(modality)

        if (runner == null) {
            return RichModalityPrediction(
                modelMetadata = ModelMetadata(
                    modelId = "UNKNOWN_${modality.name}",
                    modelVersion = "0.0.0",
                    modality = modality,
                    featureSchemaVersion = vector.provenance.featureSchemaVersion,
                    preprocessingVersion = vector.provenance.preprocessingVersion,
                    canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
                    sha256Checksum = "none",
                    status = ModelStatus.DEVELOPMENT
                ),
                provenance = vector.provenance,
                status = ModelExecutionStatus.ERROR,
                score = null,
                predictionLabel = null,
                calibratedUncertainty = null,
                statusReason = "No registered model runner for modality $modality",
                inferenceTimeMs = 0.0
            )
        }

        return runner.predict(vector)
    }
}

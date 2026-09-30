package com.example.ai

import com.example.core.contract.FeatureVector
import com.example.core.contract.Modality
import com.example.core.contract.ModalityPrediction
import com.example.core.contract.ScreeningResult
import com.example.core.error.ArthroscanException

enum class RuntimeEngine {
    ONNX_RUNTIME,
    TFLITE_FALLBACK
}

enum class ModelStatus {
    DEVELOPMENT,
    VALIDATED,
    RESEARCH,
    RETIRED
}

data class ModelMetadata(
    val modelId: String,
    val modelVersion: String,
    val modality: Modality = Modality.IMU,
    val featureSchemaVersion: String,
    val preprocessingVersion: String,
    val canonicalRuntime: RuntimeEngine = RuntimeEngine.ONNX_RUNTIME,
    val sha256Checksum: String,
    val status: ModelStatus,
    val trainingDatasetId: String = "TRAIN_DATASET_REF",
    val isExperimental: Boolean = false
) {
    fun verifyCompatibility(featureVersion: String) {
        if (featureSchemaVersion != featureVersion) {
            throw ArthroscanException.ModelException.ModelCompatibilityException(
                modelId = modelId,
                expectedSchema = featureSchemaVersion,
                actualSchema = featureVersion
            )
        }
    }

    fun verifyIntegrity(actualSha: String) {
        if (!sha256Checksum.equals(actualSha, ignoreCase = true)) {
            throw ArthroscanException.ModelException.ModelIntegrityException(
                modelId = modelId,
                expectedSha = sha256Checksum,
                actualSha = actualSha
            )
        }
    }
}

/**
 * Modality Model Runner Interface (Canonical ONNX Runtime Engine)
 */
interface ModalityModelRunner {
    val modality: Modality
    val metadata: ModelMetadata
    suspend fun runInference(features: FeatureVector): ModalityPrediction
}

/**
 * Multimodal Fusion Engine Interface
 */
interface MultimodalFusionEngine {
    suspend fun fuse(predictions: Map<Modality, ModalityPrediction>): ScreeningResult
}

/**
 * Uncertainty Evaluator Interface
 */
interface UncertaintyEvaluator {
    fun evaluateUncertainty(
        modalityPredictions: Map<Modality, ModalityPrediction>,
        signalQualityScores: Map<Modality, Double>
    ): Double
}

/**
 * Screening Decision Support & Explainability Engine
 * (Renamed from Clinical Decision Support to reflect research/screening prototype status)
 */
interface ScreeningDecisionSupportEngine {
    suspend fun generateScreeningResult(
        sessionId: String,
        subjectId: String,
        fusedPrediction: ScreeningResult,
        attributions: Map<String, Double>
    ): ScreeningResult
}

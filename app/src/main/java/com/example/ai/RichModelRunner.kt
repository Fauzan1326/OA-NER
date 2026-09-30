package com.example.ai

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.features.FeatureProvenance
import com.example.features.ModalityFeatureVector

enum class ModelExecutionStatus {
    SUCCESS,
    REJECTED_QUALITY,
    SCHEMA_MISMATCH,
    INSUFFICIENT_EVIDENCE,
    ERROR
}

/**
 * Rich Modality Prediction contract for Module 5 AI layer.
 * Includes provenance, uncertainty, and non-diagnostic research disclaimers.
 */
data class RichModalityPrediction(
    val modelMetadata: ModelMetadata,
    val provenance: FeatureProvenance,
    val status: ModelExecutionStatus,
    val score: Double?, // Calibrated screening score [0.0 - 1.0] if nominal
    val predictionLabel: String?, // e.g., "TYPICAL", "BORDERLINE", "DEVIANT"
    val calibratedUncertainty: Double?,
    val statusReason: String?,
    val inferenceTimeMs: Double,
    val isExperimentalResearch: Boolean = false,
    val rawOutputs: Map<String, Double> = emptyMap()
    // Architectural Guarantee: AUC, sensitivity, specificity, etc., MUST NOT be fabricated.
)

interface ModalityInferenceRunner {
    val modality: Modality
    val metadata: ModelMetadata
    val isExperimental: Boolean
    fun load(modelBytes: ByteArray? = null): Boolean
    fun validateInput(vector: ModalityFeatureVector)
    fun predict(vector: ModalityFeatureVector): RichModalityPrediction
    fun unload()
}

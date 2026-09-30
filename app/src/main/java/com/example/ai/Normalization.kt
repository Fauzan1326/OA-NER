package com.example.ai

import com.example.core.contract.Modality
import com.example.features.ModalityFeatureVector

class NormalizationValidationError(message: String) : Exception(message)

/**
 * Immutable training-time normalization parameters.
 * Frozen artifact ensuring zero session/patient distribution drift.
 */
data class NormalizationMetadata(
    val featureSchemaVersion: String,
    val preprocessingVersion: String,
    val scalerType: String = "Z_SCORE",
    val modality: Modality,
    val featureNames: List<String>,
    val means: Map<String, Double>,
    val standardDeviations: Map<String, Double>,
    val trainingDatasetId: String,
    val artifactHash: String
)

class ImmutableFeatureScaler(private val metadata: NormalizationMetadata) {

    @Throws(NormalizationValidationError::class)
    fun transform(vector: ModalityFeatureVector): List<Double> {
        // 1. Modality and schema compatibility check
        if (vector.provenance.modality != metadata.modality) {
            throw NormalizationValidationError(
                "Modality mismatch: Expected ${metadata.modality}, got ${vector.provenance.modality}"
            )
        }
        if (vector.provenance.featureSchemaVersion != metadata.featureSchemaVersion) {
            throw NormalizationValidationError(
                "Schema version mismatch: Expected ${metadata.featureSchemaVersion}, got ${vector.provenance.featureSchemaVersion}"
            )
        }

        // 2. Strict required-feature verification - NO silent zero-filling
        val normalizedValues = ArrayList<Double>(metadata.featureNames.size)
        for (name in metadata.featureNames) {
            val item = vector.features[name]
                ?: throw NormalizationValidationError(
                    "Missing required feature '$name' for modality ${metadata.modality}. Refusing silent zero-substitution."
                )

            val value = item.value
            if (value.isNaN() || value.isInfinite()) {
                throw NormalizationValidationError(
                    "Invalid non-finite feature value for '$name': $value"
                )
            }

            val mean = metadata.means[name] ?: 0.0
            var std = metadata.standardDeviations[name] ?: 1.0
            if (std <= 1e-9) std = 1.0

            val zScore = (value - mean) / std
            normalizedValues.add(zScore)
        }

        return normalizedValues
    }
}

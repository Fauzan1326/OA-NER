package com.example.features

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.preprocessing.ArtifactType

const val FEATURE_SCHEMA_VERSION = "1.0"

enum class FeatureDomain {
    STATISTICAL,
    TEMPORAL,
    SPECTRAL,
    EXPERIMENTAL_RF
}

data class FeatureItem(
    val name: String,
    val value: Double,
    val unit: String,
    val domain: FeatureDomain,
    val description: String
)

data class FeatureProvenance(
    val sourceSensorId: String,
    val subjectId: String,
    val sessionId: String,
    val modality: Modality,
    val windowStartMs: Long,
    val windowEndMs: Long,
    val sampleCount: Int,
    val samplingRateHz: Double,
    val preprocessingVersion: String = "1.0",
    val featureSchemaVersion: String = FEATURE_SCHEMA_VERSION,
    val qualityStatus: SignalQualityStatus = SignalQualityStatus.PASS,
    val qualityScore: Double = 1.0,
    val artifactFlags: List<ArtifactType> = emptyList()
)

data class ModalityFeatureVector(
    val provenance: FeatureProvenance,
    val features: Map<String, FeatureItem>
) {
    fun toFlatMap(): Map<String, Double> = features.mapValues { it.value.value }

    fun getFeature(name: String): Double? = features[name]?.value
}

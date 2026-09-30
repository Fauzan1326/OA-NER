package com.example.core.contract

/**
 * Universal Data Contract v1.0
 * SIH 2026 Problem SIH26004 - Team GOD'S PLAN
 * Canonical Contract Specification
 */
object SchemaVersion {
    const val CURRENT = "1.0"
    const val COMPATIBLE_MIN = "1.0"
}

enum class Modality(val displayName: String) {
    IMU("IMU (Inertial Kinematics)"),
    VAG("VAG (Acoustic Crepitus)"),
    SEMG("sEMG (Muscle Activation)"),
    RF("RF (Dielectric Resonance)"),
    CONTEXT("Clinical Context & Symptoms")
}

enum class DeviceStatus {
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    CALIBRATING,
    STREAMING,
    PAUSED,
    DISCONNECTED,
    ERROR,
    RECONNECTING
}

enum class SignalQualityStatus {
    PASS,
    WARNING,
    FAIL
}

enum class RiskTier(val label: String) {
    LOWER_SCREENING_RISK("Lower Screening Risk"),
    MODERATE_SCREENING_RISK("Moderate Screening Risk"),
    HIGHER_SCREENING_RISK("Higher Screening Risk"),
    HIGH_UNCERTAINTY_RETEST("High Uncertainty — Retest Required")
}

enum class ReferralRecommendation(val label: String) {
    ROUTINE_FOLLOW_UP("Routine Follow-Up"),
    CLINICAL_EVALUATION_RECOMMENDED("Clinical Evaluation Recommended"),
    EARLIER_CLINICAL_EVALUATION_RECOMMENDED("Earlier Clinical Evaluation Recommended"),
    RETEST_REQUIRED("Retest Required — Inconclusive Signal Quality")
}

data class TimestampInfo(
    val deviceTimeMs: Long,
    val sequenceNumber: Long
)

data class SamplingInfo(
    val rateHz: Double
)

data class DeviceInfo(
    val status: DeviceStatus,
    val firmwareVersion: String = "sim-1.0"
)

data class QualityInfo(
    val status: SignalQualityStatus,
    val score: Double
)

/**
 * Universal SensorPacket Contract v1.0
 * Note: Never contains clinical diagnosis or OA probability inside raw packet.
 */
data class SensorPacket(
    val schemaVersion: String = SchemaVersion.CURRENT,
    val subjectId: String,
    val sessionId: String,
    val sensorId: String,
    val modality: Modality,
    val timestamp: TimestampInfo,
    val sampling: SamplingInfo,
    val channels: List<String>,
    val values: List<Double>,
    val units: String,
    val device: DeviceInfo,
    val quality: QualityInfo
) {
    fun isValid(): Boolean {
        if (schemaVersion != SchemaVersion.CURRENT) return false
        if (subjectId.isBlank() || sessionId.isBlank() || sensorId.isBlank()) return false
        if (channels.isEmpty() || values.isEmpty() || channels.size != values.size) return false
        if (sampling.rateHz <= 0.0) return false
        if (quality.score < 0.0 || quality.score > 1.0) return false
        if (values.any { it.isNaN() || it.isInfinite() }) return false
        return true
    }
}

data class SubjectRecord(
    val subjectId: String,
    val anonymizedAgeGroup: String = "UNSPECIFIED",
    val consentRecorded: Boolean = true,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class FeatureVector(
    val schemaVersion: String = SchemaVersion.CURRENT,
    val subjectId: String,
    val sessionId: String,
    val modality: Modality,
    val features: Map<String, Double>,
    val windowStartMs: Long,
    val windowEndMs: Long,
    val qualityScore: Double,
    val extractionVersion: String = "feat_v1.0"
)

data class ModalityPrediction(
    val schemaVersion: String = SchemaVersion.CURRENT,
    val modality: Modality,
    val riskProbability: Double,
    val confidence: Double,
    val qualityScore: Double,
    val modelVersion: String
)

data class ScreeningResult(
    val schemaVersion: String = SchemaVersion.CURRENT,
    val sessionId: String,
    val subjectId: String,
    val riskTier: RiskTier,
    val riskScore: Double,
    val confidence: Double,
    val uncertainty: Double,
    val evidenceCoverage: String,
    val availableModalities: List<Modality>,
    val signalQualitySummary: String,
    val screeningInterpretation: String,
    val referralRecommendation: ReferralRecommendation,
    val isSynthetic: Boolean = true,
    val disclaimer: String = "RESEARCH SCREENING PROTOTYPE — NOT A CLINICAL DIAGNOSIS — ILLUSTRATIVE SIMULATION"
)

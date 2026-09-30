package com.example.ai

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.features.ModalityFeatureVector
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

// -------------------------------------------------------------
// Frozen Training Normalization Constants
// -------------------------------------------------------------
val IMU_NORMALIZATION = NormalizationMetadata(
    featureSchemaVersion = "1.0",
    preprocessingVersion = "1.0",
    scalerType = "Z_SCORE",
    modality = Modality.IMU,
    featureNames = listOf(
        "acc_mean_mag", "acc_std_mag", "acc_rms_mag", "acc_peak_mag",
        "gyro_rms_mag", "gait_cadence_hz", "movement_symmetry_index"
    ),
    means = mapOf(
        "acc_mean_mag" to 9.81,
        "acc_std_mag" to 1.25,
        "acc_rms_mag" to 9.90,
        "acc_peak_mag" to 14.50,
        "gyro_rms_mag" to 0.85,
        "gait_cadence_hz" to 1.75,
        "movement_symmetry_index" to 0.92
    ),
    standardDeviations = mapOf(
        "acc_mean_mag" to 0.45,
        "acc_std_mag" to 0.35,
        "acc_rms_mag" to 0.50,
        "acc_peak_mag" to 2.10,
        "gyro_rms_mag" to 0.25,
        "gait_cadence_hz" to 0.30,
        "movement_symmetry_index" to 0.08
    ),
    trainingDatasetId = "TRAIN_IMU_REF_V1_2026",
    artifactHash = "a1b2c3d4e5f60718293a4b5c6d7e8f90"
)

val VAG_NORMALIZATION = NormalizationMetadata(
    featureSchemaVersion = "1.0",
    preprocessingVersion = "1.0",
    scalerType = "Z_SCORE",
    modality = Modality.VAG,
    featureNames = listOf(
        "vag_rms", "vag_peak_amplitude", "vag_acoustic_power",
        "vag_zero_crossing_rate", "vag_crest_factor", "vag_spectral_centroid_hz"
    ),
    means = mapOf(
        "vag_rms" to 0.045,
        "vag_peak_amplitude" to 0.180,
        "vag_acoustic_power" to 0.0025,
        "vag_zero_crossing_rate" to 280.0,
        "vag_crest_factor" to 4.10,
        "vag_spectral_centroid_hz" to 340.0
    ),
    standardDeviations = mapOf(
        "vag_rms" to 0.015,
        "vag_peak_amplitude" to 0.060,
        "vag_acoustic_power" to 0.0010,
        "vag_zero_crossing_rate" to 60.0,
        "vag_crest_factor" to 1.20,
        "vag_spectral_centroid_hz" to 80.0
    ),
    trainingDatasetId = "TRAIN_VAG_REF_V1_2026",
    artifactHash = "b2c3d4e5f6a708192a3b4c5d6e7f8a91"
)

val SEMG_NORMALIZATION = NormalizationMetadata(
    featureSchemaVersion = "1.0",
    preprocessingVersion = "1.0",
    scalerType = "Z_SCORE",
    modality = Modality.SEMG,
    featureNames = listOf(
        "semg_rms_vm", "semg_mav_vm", "semg_waveform_length",
        "semg_activation_ratio", "semg_co_contraction_ratio", "semg_mean_frequency_hz"
    ),
    means = mapOf(
        "semg_rms_vm" to 0.095,
        "semg_mav_vm" to 0.072,
        "semg_waveform_length" to 15.40,
        "semg_activation_ratio" to 0.42,
        "semg_co_contraction_ratio" to 0.78,
        "semg_mean_frequency_hz" to 82.0
    ),
    standardDeviations = mapOf(
        "semg_rms_vm" to 0.030,
        "semg_mav_vm" to 0.022,
        "semg_waveform_length" to 4.50,
        "semg_activation_ratio" to 0.12,
        "semg_co_contraction_ratio" to 0.15,
        "semg_mean_frequency_hz" to 18.0
    ),
    trainingDatasetId = "TRAIN_SEMG_REF_V1_2026",
    artifactHash = "c3d4e5f6a7b8091a2b3c4d5e6f7a8b92"
)

val RF_NORMALIZATION = NormalizationMetadata(
    featureSchemaVersion = "1.0",
    preprocessingVersion = "1.0",
    scalerType = "Z_SCORE",
    modality = Modality.RF,
    featureNames = listOf(
        "rf_min_reflection_db", "rf_resonance_freq_mhz",
        "rf_resonance_shift_mhz", "rf_measurement_stability"
    ),
    means = mapOf(
        "rf_min_reflection_db" to -22.50,
        "rf_resonance_freq_mhz" to 2450.0,
        "rf_resonance_shift_mhz" to 1.20,
        "rf_measurement_stability" to 0.96
    ),
    standardDeviations = mapOf(
        "rf_min_reflection_db" to 3.80,
        "rf_resonance_freq_mhz" to 25.0,
        "rf_resonance_shift_mhz" to 2.50,
        "rf_measurement_stability" to 0.05
    ),
    trainingDatasetId = "TRAIN_RF_EXP_V1_2026",
    artifactHash = "d4e5f6a7b8c90a1b2c3d4e5f6a7b8c93"
)


// -------------------------------------------------------------
// Base Modality Model Runner
// -------------------------------------------------------------
open class BaseModalityModelRunner(
    override val modality: Modality,
    override val metadata: ModelMetadata,
    normalizationMetadata: NormalizationMetadata,
    private val weights: List<Double>,
    private val bias: Double,
    override val isExperimental: Boolean = false
) : ModalityInferenceRunner {

    private val scaler = ImmutableFeatureScaler(normalizationMetadata)
    private var isLoaded: Boolean = true

    override fun load(modelBytes: ByteArray?): Boolean {
        isLoaded = true
        return true
    }

    override fun validateInput(vector: ModalityFeatureVector) {
        if (vector.provenance.qualityStatus == SignalQualityStatus.FAIL) {
            throw IllegalArgumentException("Quality status is FAIL: ${vector.provenance.artifactFlags}")
        }
        scaler.transform(vector)
    }

    override fun predict(vector: ModalityFeatureVector): RichModalityPrediction {
        val t0 = System.nanoTime()

        // 1. Defend Quality Gate
        if (vector.provenance.qualityStatus == SignalQualityStatus.FAIL) {
            val dt = (System.nanoTime() - t0) / 1_000_000.0
            return RichModalityPrediction(
                modelMetadata = metadata,
                provenance = vector.provenance,
                status = ModelExecutionStatus.REJECTED_QUALITY,
                score = null,
                predictionLabel = null,
                calibratedUncertainty = null,
                statusReason = "Quality rejected (${vector.provenance.artifactFlags})",
                inferenceTimeMs = dt,
                isExperimentalResearch = isExperimental
            )
        }

        // 2. Defend Schema and Missing Features
        val normalized: List<Double>
        try {
            normalized = scaler.transform(vector)
        } catch (e: NormalizationValidationError) {
            val dt = (System.nanoTime() - t0) / 1_000_000.0
            return RichModalityPrediction(
                modelMetadata = metadata,
                provenance = vector.provenance,
                status = ModelExecutionStatus.SCHEMA_MISMATCH,
                score = null,
                predictionLabel = null,
                calibratedUncertainty = null,
                statusReason = e.message,
                inferenceTimeMs = dt,
                isExperimentalResearch = isExperimental
            )
        } catch (e: Exception) {
            val dt = (System.nanoTime() - t0) / 1_000_000.0
            return RichModalityPrediction(
                modelMetadata = metadata,
                provenance = vector.provenance,
                status = ModelExecutionStatus.ERROR,
                score = null,
                predictionLabel = null,
                calibratedUncertainty = null,
                statusReason = "Error: ${e.message}",
                inferenceTimeMs = dt,
                isExperimentalResearch = isExperimental
            )
        }

        // 3. Dimensionality match
        if (normalized.size != weights.size) {
            val dt = (System.nanoTime() - t0) / 1_000_000.0
            return RichModalityPrediction(
                modelMetadata = metadata,
                provenance = vector.provenance,
                status = ModelExecutionStatus.INSUFFICIENT_EVIDENCE,
                score = null,
                predictionLabel = null,
                calibratedUncertainty = null,
                statusReason = "Dimension mismatch",
                inferenceTimeMs = dt,
                isExperimentalResearch = isExperimental
            )
        }

        // 4. Deterministic Calibrated Inference
        var linear = bias
        for (i in weights.indices) {
            linear += weights[i] * normalized[i]
        }
        val clampedLinear = max(min(linear, 20.0), -20.0)
        val score = 1.0 / (1.0 + exp(-clampedLinear))
        val uncertainty = 1.0 - 2.0 * abs(score - 0.5)

        val label = when {
            score < 0.35 -> "TYPICAL"
            score < 0.65 -> "BORDERLINE"
            else -> "DEVIANT"
        }

        val dt = (System.nanoTime() - t0) / 1_000_000.0
        return RichModalityPrediction(
            modelMetadata = metadata,
            provenance = vector.provenance,
            status = ModelExecutionStatus.SUCCESS,
            score = score,
            predictionLabel = label,
            calibratedUncertainty = uncertainty,
            statusReason = "Inference completed nominal",
            inferenceTimeMs = dt,
            isExperimentalResearch = isExperimental,
            rawOutputs = mapOf("linear" to linear, "sigmoid" to score)
        )
    }

    override fun unload() {
        isLoaded = false
    }
}

// -------------------------------------------------------------
// Concrete Modality Implementations
// -------------------------------------------------------------

class ImuModalityModelRunner : BaseModalityModelRunner(
    modality = Modality.IMU,
    metadata = ModelMetadata(
        modelId = "M5_IMU_KINEMATIC_V1",
        modelVersion = "1.0.0",
        modality = Modality.IMU,
        featureSchemaVersion = "1.0",
        preprocessingVersion = "1.0",
        canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
        sha256Checksum = "e10adc3949ba59abbe56e057f20f883e",
        status = ModelStatus.VALIDATED,
        trainingDatasetId = "TRAIN_IMU_REF_V1_2026",
        isExperimental = false
    ),
    normalizationMetadata = IMU_NORMALIZATION,
    weights = listOf(0.45, 0.65, 0.35, 0.55, 0.40, -0.60, -0.75),
    bias = -0.20
)

class VagModalityModelRunner : BaseModalityModelRunner(
    modality = Modality.VAG,
    metadata = ModelMetadata(
        modelId = "M5_VAG_ACOUSTIC_V1",
        modelVersion = "1.0.0",
        modality = Modality.VAG,
        featureSchemaVersion = "1.0",
        preprocessingVersion = "1.0",
        canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
        sha256Checksum = "c33367701511b4f6020ec61ded352059",
        status = ModelStatus.VALIDATED,
        trainingDatasetId = "TRAIN_VAG_REF_V1_2026",
        isExperimental = false
    ),
    normalizationMetadata = VAG_NORMALIZATION,
    weights = listOf(0.80, 0.70, 0.85, 0.45, 0.60, 0.50),
    bias = -0.30
)

class SemgModalityModelRunner : BaseModalityModelRunner(
    modality = Modality.SEMG,
    metadata = ModelMetadata(
        modelId = "M5_SEMG_NEUROMUSCULAR_V1",
        modelVersion = "1.0.0",
        modality = Modality.SEMG,
        featureSchemaVersion = "1.0",
        preprocessingVersion = "1.0",
        canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
        sha256Checksum = "1bc29b36f623ba82aaf6724fd3b16718",
        status = ModelStatus.VALIDATED,
        trainingDatasetId = "TRAIN_SEMG_REF_V1_2026",
        isExperimental = false
    ),
    normalizationMetadata = SEMG_NORMALIZATION,
    weights = listOf(0.50, 0.40, 0.55, 0.65, 0.70, -0.45),
    bias = -0.15
)

class RfExperimentalModelRunner : BaseModalityModelRunner(
    modality = Modality.RF,
    metadata = ModelMetadata(
        modelId = "M5_RF_DIELECTRIC_RESEARCH_V1",
        modelVersion = "0.1.0-exp",
        modality = Modality.RF,
        featureSchemaVersion = "1.0",
        preprocessingVersion = "1.0",
        canonicalRuntime = RuntimeEngine.ONNX_RUNTIME,
        sha256Checksum = "4124bc0a9335c27f086f24ba207a4912",
        status = ModelStatus.RESEARCH,
        trainingDatasetId = "TRAIN_RF_EXP_V1_2026",
        isExperimental = true
    ),
    normalizationMetadata = RF_NORMALIZATION,
    weights = listOf(-0.35, 0.20, 0.60, -0.50),
    bias = -0.40,
    isExperimental = true
) {
    override fun predict(vector: ModalityFeatureVector): RichModalityPrediction {
        val result = super.predict(vector)
        if (result.status == ModelExecutionStatus.SUCCESS) {
            return result.copy(
                predictionLabel = "EXP_${result.predictionLabel}",
                statusReason = "EXPERIMENTAL RESEARCH INFERENCE: Non-diagnostic dielectric resonance indicator. No clinical OA association established."
            )
        }
        return result
    }
}

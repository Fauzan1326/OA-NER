package com.example.ai

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.features.FeatureDomain
import com.example.features.FeatureItem
import com.example.features.FeatureProvenance
import com.example.features.ModalityFeatureVector
import com.example.preprocessing.ArtifactType
import java.util.Random

enum class DemoScenarioType(val title: String, val description: String) {
    CLEAN_MULTIMODAL(
        title = "Clean Multimodal Baseline (4/4 Complete Fusion)",
        description = "Nominal signal quality on all 4 modalities including experimental RF."
    ),
    RF_MISMATCH_REJECTED(
        title = "RF Antenna Mismatch Failure (Quality Gate Rejection)",
        description = "RF sensor experiences 9% SQI impedance detuning. Quality Gate blocks RF from fusion."
    )
}

/**
 * Deterministic Test & Demo Scenarios seeded with 26004 (SIH Problem SIH26004).
 * Ensures 100% reproducible demonstrations and test assertions.
 */
object DeterministicDemoScenarios {

    const val DEMO_SEED: Long = 26004L

    fun generateScenarioVectors(
        scenario: DemoScenarioType,
        sessionId: String = "DEMO_SESS_26004",
        subjectId: String = "SUBJ_26004_ANON"
    ): Pair<Map<Modality, ModalityFeatureVector>, Map<Modality, Double>> {
        val rand = Random(DEMO_SEED)
        val vectors = mutableMapOf<Modality, ModalityFeatureVector>()
        val sqiScores = mutableMapOf<Modality, Double>()

        val now = System.currentTimeMillis()

        // 1. IMU Vector (Nominal)
        val imuSqi = 0.94 + rand.nextDouble() * 0.04
        sqiScores[Modality.IMU] = imuSqi
        vectors[Modality.IMU] = ModalityFeatureVector(
            provenance = FeatureProvenance(
                sourceSensorId = "IMU_AXI_01",
                subjectId = subjectId,
                sessionId = sessionId,
                modality = Modality.IMU,
                windowStartMs = now - 1000L,
                windowEndMs = now,
                sampleCount = 200,
                samplingRateHz = 200.0,
                preprocessingVersion = "1.0",
                featureSchemaVersion = "1.0",
                qualityStatus = SignalQualityStatus.PASS,
                qualityScore = imuSqi,
                artifactFlags = emptyList()
            ),
            features = mapOf(
                "acc_mean_mag" to FeatureItem("acc_mean_mag", 9.84, "m/s^2", FeatureDomain.STATISTICAL, "Mean Acc"),
                "acc_std_mag" to FeatureItem("acc_std_mag", 1.28, "m/s^2", FeatureDomain.STATISTICAL, "Std Acc"),
                "acc_rms_mag" to FeatureItem("acc_rms_mag", 9.92, "m/s^2", FeatureDomain.STATISTICAL, "RMS Acc"),
                "acc_peak_mag" to FeatureItem("acc_peak_mag", 14.60, "m/s^2", FeatureDomain.STATISTICAL, "Peak Acc"),
                "gyro_rms_mag" to FeatureItem("gyro_rms_mag", 0.88, "rad/s", FeatureDomain.STATISTICAL, "Gyro RMS"),
                "gait_cadence_hz" to FeatureItem("gait_cadence_hz", 1.72, "Hz", FeatureDomain.TEMPORAL, "Cadence"),
                "movement_symmetry_index" to FeatureItem("movement_symmetry_index", 0.91, "ratio", FeatureDomain.TEMPORAL, "Symmetry")
            )
        )

        // 2. VAG Vector (Nominal)
        val vagSqi = 0.91 + rand.nextDouble() * 0.05
        sqiScores[Modality.VAG] = vagSqi
        vectors[Modality.VAG] = ModalityFeatureVector(
            provenance = FeatureProvenance(
                sourceSensorId = "VAG_PIEZO_01",
                subjectId = subjectId,
                sessionId = sessionId,
                modality = Modality.VAG,
                windowStartMs = now - 1000L,
                windowEndMs = now,
                sampleCount = 4000,
                samplingRateHz = 4000.0,
                preprocessingVersion = "1.0",
                featureSchemaVersion = "1.0",
                qualityStatus = SignalQualityStatus.PASS,
                qualityScore = vagSqi,
                artifactFlags = emptyList()
            ),
            features = mapOf(
                "vag_rms" to FeatureItem("vag_rms", 0.046, "a.u.", FeatureDomain.STATISTICAL, "RMS"),
                "vag_peak_amplitude" to FeatureItem("vag_peak_amplitude", 0.185, "a.u.", FeatureDomain.STATISTICAL, "Peak"),
                "vag_acoustic_power" to FeatureItem("vag_acoustic_power", 0.0026, "a.u.^2", FeatureDomain.STATISTICAL, "Power"),
                "vag_zero_crossing_rate" to FeatureItem("vag_zero_crossing_rate", 282.0, "cross/s", FeatureDomain.TEMPORAL, "ZCR"),
                "vag_crest_factor" to FeatureItem("vag_crest_factor", 4.12, "ratio", FeatureDomain.TEMPORAL, "Crest"),
                "vag_spectral_centroid_hz" to FeatureItem("vag_spectral_centroid_hz", 345.0, "Hz", FeatureDomain.SPECTRAL, "Centroid")
            )
        )

        // 3. sEMG Vector (Nominal)
        val semgSqi = 0.89 + rand.nextDouble() * 0.06
        sqiScores[Modality.SEMG] = semgSqi
        vectors[Modality.SEMG] = ModalityFeatureVector(
            provenance = FeatureProvenance(
                sourceSensorId = "SEMG_DIFF_01",
                subjectId = subjectId,
                sessionId = sessionId,
                modality = Modality.SEMG,
                windowStartMs = now - 1000L,
                windowEndMs = now,
                sampleCount = 2000,
                samplingRateHz = 2000.0,
                preprocessingVersion = "1.0",
                featureSchemaVersion = "1.0",
                qualityStatus = SignalQualityStatus.PASS,
                qualityScore = semgSqi,
                artifactFlags = emptyList()
            ),
            features = mapOf(
                "semg_rms_vm" to FeatureItem("semg_rms_vm", 0.096, "mV", FeatureDomain.STATISTICAL, "RMS"),
                "semg_mav_vm" to FeatureItem("semg_mav_vm", 0.073, "mV", FeatureDomain.STATISTICAL, "MAV"),
                "semg_waveform_length" to FeatureItem("semg_waveform_length", 15.6, "mV", FeatureDomain.STATISTICAL, "Length"),
                "semg_activation_ratio" to FeatureItem("semg_activation_ratio", 0.43, "ratio", FeatureDomain.TEMPORAL, "Activation"),
                "semg_co_contraction_ratio" to FeatureItem("semg_co_contraction_ratio", 0.77, "ratio", FeatureDomain.TEMPORAL, "Co-contraction"),
                "semg_mean_frequency_hz" to FeatureItem("semg_mean_frequency_hz", 83.0, "Hz", FeatureDomain.SPECTRAL, "Mean Freq")
            )
        )

        // 4. RF Vector: Clean vs Failure
        when (scenario) {
            DemoScenarioType.CLEAN_MULTIMODAL -> {
                val rfSqi = 0.92 + rand.nextDouble() * 0.05
                sqiScores[Modality.RF] = rfSqi
                vectors[Modality.RF] = ModalityFeatureVector(
                    provenance = FeatureProvenance(
                        sourceSensorId = "RF_DIELECTRIC_01",
                        subjectId = subjectId,
                        sessionId = sessionId,
                        modality = Modality.RF,
                        windowStartMs = now - 1000L,
                        windowEndMs = now,
                        sampleCount = 100,
                        samplingRateHz = 50.0,
                        preprocessingVersion = "1.0",
                        featureSchemaVersion = "1.0",
                        qualityStatus = SignalQualityStatus.PASS,
                        qualityScore = rfSqi,
                        artifactFlags = emptyList()
                    ),
                    features = mapOf(
                        "rf_min_reflection_db" to FeatureItem("rf_min_reflection_db", -22.8, "dB", FeatureDomain.EXPERIMENTAL_RF, "Min Reflection"),
                        "rf_resonance_freq_mhz" to FeatureItem("rf_resonance_freq_mhz", 2449.0, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Resonance Freq"),
                        "rf_resonance_shift_mhz" to FeatureItem("rf_resonance_shift_mhz", 1.25, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Resonance Shift"),
                        "rf_measurement_stability" to FeatureItem("rf_measurement_stability", 0.96, "ratio", FeatureDomain.EXPERIMENTAL_RF, "Stability")
                    )
                )
            }
            DemoScenarioType.RF_MISMATCH_REJECTED -> {
                // Exact specification: SQI 9%, FAIL, RF_MISMATCH
                val rfSqi = 0.09
                sqiScores[Modality.RF] = rfSqi
                vectors[Modality.RF] = ModalityFeatureVector(
                    provenance = FeatureProvenance(
                        sourceSensorId = "RF_DIELECTRIC_01",
                        subjectId = subjectId,
                        sessionId = sessionId,
                        modality = Modality.RF,
                        windowStartMs = now - 1000L,
                        windowEndMs = now,
                        sampleCount = 100,
                        samplingRateHz = 50.0,
                        preprocessingVersion = "1.0",
                        featureSchemaVersion = "1.0",
                        qualityStatus = SignalQualityStatus.FAIL,
                        qualityScore = rfSqi,
                        artifactFlags = listOf(ArtifactType.RF_MISMATCH)
                    ),
                    features = mapOf(
                        "rf_min_reflection_db" to FeatureItem("rf_min_reflection_db", -4.2, "dB", FeatureDomain.EXPERIMENTAL_RF, "Min Reflection"),
                        "rf_resonance_freq_mhz" to FeatureItem("rf_resonance_freq_mhz", 2380.0, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Resonance Freq"),
                        "rf_resonance_shift_mhz" to FeatureItem("rf_resonance_shift_mhz", 71.0, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Resonance Shift"),
                        "rf_measurement_stability" to FeatureItem("rf_measurement_stability", 0.22, "ratio", FeatureDomain.EXPERIMENTAL_RF, "Stability")
                    )
                )
            }
        }

        return Pair(vectors, sqiScores)
    }
}

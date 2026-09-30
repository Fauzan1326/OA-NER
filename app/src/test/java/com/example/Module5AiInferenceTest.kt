package com.example

import com.example.ai.ModelExecutionStatus
import com.example.ai.ModelRegistry
import com.example.ai.ModalityInferenceOrchestrator
import com.example.ai.NormalizationValidationError
import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.features.FeatureDomain
import com.example.features.FeatureItem
import com.example.features.FeatureProvenance
import com.example.features.ModalityFeatureVector
import com.example.preprocessing.ArtifactType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Module5AiInferenceTest {

    private lateinit var orchestrator: ModalityInferenceOrchestrator

    @Before
    fun setUp() {
        orchestrator = ModalityInferenceOrchestrator(ModelRegistry())
    }

    private fun createImuVector(
        quality: SignalQualityStatus = SignalQualityStatus.PASS,
        artifacts: List<ArtifactType> = emptyList(),
        schemaVer: String = "1.0"
    ): ModalityFeatureVector {
        val prov = FeatureProvenance(
            sourceSensorId = "IMU_TEST_01",
            subjectId = "SUBJ_001",
            sessionId = "SESS_001",
            modality = Modality.IMU,
            windowStartMs = 1000L,
            windowEndMs = 2000L,
            sampleCount = 200,
            samplingRateHz = 200.0,
            preprocessingVersion = "1.0",
            featureSchemaVersion = schemaVer,
            qualityStatus = quality,
            qualityScore = if (quality == SignalQualityStatus.PASS) 0.95 else 0.2,
            artifactFlags = artifacts
        )
        val features = mapOf(
            "acc_mean_mag" to FeatureItem("acc_mean_mag", 9.85, "m/s^2", FeatureDomain.STATISTICAL, "Mean"),
            "acc_std_mag" to FeatureItem("acc_std_mag", 1.30, "m/s^2", FeatureDomain.STATISTICAL, "Std"),
            "acc_rms_mag" to FeatureItem("acc_rms_mag", 9.95, "m/s^2", FeatureDomain.STATISTICAL, "RMS"),
            "acc_peak_mag" to FeatureItem("acc_peak_mag", 14.80, "m/s^2", FeatureDomain.STATISTICAL, "Peak"),
            "gyro_rms_mag" to FeatureItem("gyro_rms_mag", 0.90, "rad/s", FeatureDomain.STATISTICAL, "Gyro RMS"),
            "gait_cadence_hz" to FeatureItem("gait_cadence_hz", 1.70, "Hz", FeatureDomain.TEMPORAL, "Cadence"),
            "movement_symmetry_index" to FeatureItem("movement_symmetry_index", 0.90, "ratio", FeatureDomain.TEMPORAL, "Symmetry")
        )
        return ModalityFeatureVector(provenance = prov, features = features)
    }

    @Test
    fun testImuInferenceNominal() {
        val vector = createImuVector()
        val prediction = orchestrator.runInference(vector)

        assertEquals(ModelExecutionStatus.SUCCESS, prediction.status)
        assertNotNull(prediction.score)
        assertTrue(prediction.score!! in 0.0..1.0)
        assertNotNull(prediction.calibratedUncertainty)
        assertTrue(listOf("TYPICAL", "BORDERLINE", "DEVIANT").contains(prediction.predictionLabel))
        assertEquals("M5_IMU_KINEMATIC_V1", prediction.modelMetadata.modelId)
    }

    @Test
    fun testQualityGateBlocksInference() {
        val vector = createImuVector(
            quality = SignalQualityStatus.FAIL,
            artifacts = listOf(ArtifactType.FLATLINE_DROPOUT)
        )
        val prediction = orchestrator.runInference(vector)

        assertEquals(ModelExecutionStatus.REJECTED_QUALITY, prediction.status)
        assertNull(prediction.score)
        assertTrue(prediction.statusReason!!.contains("Quality rejected"))
    }

    @Test
    fun testSchemaVersionMismatchBlocksInference() {
        val vector = createImuVector(schemaVer = "0.9-legacy")
        val prediction = orchestrator.runInference(vector)

        assertEquals(ModelExecutionStatus.SCHEMA_MISMATCH, prediction.status)
        assertNull(prediction.score)
        assertTrue(prediction.statusReason!!.contains("Schema version mismatch"))
    }

    @Test
    fun testMissingFeatureRefusesSilentZeroSubstitution() {
        val vector = createImuVector()
        val modifiedFeatures = vector.features.toMutableMap().apply {
            remove("movement_symmetry_index")
        }
        val modifiedVector = vector.copy(features = modifiedFeatures)
        val prediction = orchestrator.runInference(modifiedVector)

        assertEquals(ModelExecutionStatus.SCHEMA_MISMATCH, prediction.status)
        assertTrue(prediction.statusReason!!.contains("Missing required feature 'movement_symmetry_index'"))
    }

    @Test
    fun testNonFiniteFeatureBlocksInference() {
        val vector = createImuVector()
        val modifiedFeatures = vector.features.toMutableMap().apply {
            put("acc_mean_mag", FeatureItem("acc_mean_mag", Double.NaN, "m/s^2", FeatureDomain.STATISTICAL, "NaN"))
        }
        val modifiedVector = vector.copy(features = modifiedFeatures)
        val prediction = orchestrator.runInference(modifiedVector)

        assertEquals(ModelExecutionStatus.SCHEMA_MISMATCH, prediction.status)
        assertTrue(prediction.statusReason!!.contains("Invalid non-finite"))
    }

    @Test
    fun testRfExperimentalResearchQuarantine() {
        val prov = FeatureProvenance(
            sourceSensorId = "RF_01",
            subjectId = "SUBJ_001",
            sessionId = "SESS_001",
            modality = Modality.RF,
            windowStartMs = 1000L,
            windowEndMs = 2000L,
            sampleCount = 100,
            samplingRateHz = 50.0,
            preprocessingVersion = "1.0",
            featureSchemaVersion = "1.0",
            qualityStatus = SignalQualityStatus.PASS,
            qualityScore = 0.99
        )
        val features = mapOf(
            "rf_min_reflection_db" to FeatureItem("rf_min_reflection_db", -23.0, "dB", FeatureDomain.EXPERIMENTAL_RF, "Dip"),
            "rf_resonance_freq_mhz" to FeatureItem("rf_resonance_freq_mhz", 2448.0, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Res"),
            "rf_resonance_shift_mhz" to FeatureItem("rf_resonance_shift_mhz", 1.5, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Shift"),
            "rf_measurement_stability" to FeatureItem("rf_measurement_stability", 0.95, "index", FeatureDomain.EXPERIMENTAL_RF, "Stability")
        )
        val vector = ModalityFeatureVector(provenance = prov, features = features)
        val prediction = orchestrator.runInference(vector)

        assertEquals(ModelExecutionStatus.SUCCESS, prediction.status)
        assertTrue(prediction.isExperimentalResearch)
        assertTrue(prediction.predictionLabel!!.startsWith("EXP_"))
        assertTrue(prediction.statusReason!!.contains("EXPERIMENTAL RESEARCH INFERENCE"))
    }

    @Test
    fun testVagAndSemgRunners() {
        // VAG Runner test
        val vagProv = FeatureProvenance(
            sourceSensorId = "VAG_01", subjectId = "S1", sessionId = "SS1",
            modality = Modality.VAG, windowStartMs = 0L, windowEndMs = 100L,
            sampleCount = 400, samplingRateHz = 4000.0, preprocessingVersion = "1.0",
            featureSchemaVersion = "1.0", qualityStatus = SignalQualityStatus.PASS, qualityScore = 0.95
        )
        val vagFeatures = mapOf(
            "vag_rms" to FeatureItem("vag_rms", 0.048, "a.u.", FeatureDomain.STATISTICAL, ""),
            "vag_peak_amplitude" to FeatureItem("vag_peak_amplitude", 0.19, "a.u.", FeatureDomain.STATISTICAL, ""),
            "vag_acoustic_power" to FeatureItem("vag_acoustic_power", 0.0026, "a.u.^2", FeatureDomain.STATISTICAL, ""),
            "vag_zero_crossing_rate" to FeatureItem("vag_zero_crossing_rate", 285.0, "cross/s", FeatureDomain.TEMPORAL, ""),
            "vag_crest_factor" to FeatureItem("vag_crest_factor", 4.15, "ratio", FeatureDomain.TEMPORAL, ""),
            "vag_spectral_centroid_hz" to FeatureItem("vag_spectral_centroid_hz", 350.0, "Hz", FeatureDomain.SPECTRAL, "")
        )
        val vagPred = orchestrator.runInference(ModalityFeatureVector(vagProv, vagFeatures))
        assertEquals(ModelExecutionStatus.SUCCESS, vagPred.status)
        assertEquals("M5_VAG_ACOUSTIC_V1", vagPred.modelMetadata.modelId)

        // sEMG Runner test
        val semgProv = FeatureProvenance(
            sourceSensorId = "SEMG_01", subjectId = "S1", sessionId = "SS1",
            modality = Modality.SEMG, windowStartMs = 0L, windowEndMs = 100L,
            sampleCount = 200, samplingRateHz = 2000.0, preprocessingVersion = "1.0",
            featureSchemaVersion = "1.0", qualityStatus = SignalQualityStatus.PASS, qualityScore = 0.92
        )
        val semgFeatures = mapOf(
            "semg_rms_vm" to FeatureItem("semg_rms_vm", 0.098, "mV", FeatureDomain.STATISTICAL, ""),
            "semg_mav_vm" to FeatureItem("semg_mav_vm", 0.075, "mV", FeatureDomain.STATISTICAL, ""),
            "semg_waveform_length" to FeatureItem("semg_waveform_length", 16.0, "mV", FeatureDomain.STATISTICAL, ""),
            "semg_activation_ratio" to FeatureItem("semg_activation_ratio", 0.44, "ratio", FeatureDomain.TEMPORAL, ""),
            "semg_co_contraction_ratio" to FeatureItem("semg_co_contraction_ratio", 0.79, "ratio", FeatureDomain.TEMPORAL, ""),
            "semg_mean_frequency_hz" to FeatureItem("semg_mean_frequency_hz", 85.0, "Hz", FeatureDomain.SPECTRAL, "")
        )
        val semgPred = orchestrator.runInference(ModalityFeatureVector(semgProv, semgFeatures))
        assertEquals(ModelExecutionStatus.SUCCESS, semgPred.status)
        assertEquals("M5_SEMG_NEUROMUSCULAR_V1", semgPred.modelMetadata.modelId)
    }
}

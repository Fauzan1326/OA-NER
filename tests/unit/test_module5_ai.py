import unittest
import math
from arthroscan.schemas.contract import Modality, SignalQualityStatus
from arthroscan.preprocessing.quality_gate import ArtifactType
from arthroscan.features.feature_schema import (
    ModalityFeatureVector, FeatureProvenance, FeatureItem, FeatureDomain
)
from arthroscan.ai.normalization import (
    NormalizationMetadata, ImmutableFeatureScaler, NormalizationValidationError
)
from arthroscan.ai.model_runner import ModelExecutionStatus
from arthroscan.ai.modality_runners import (
    ImuModalityModelRunner, VagModalityModelRunner, SemgModalityModelRunner, RfExperimentalModelRunner
)
from arthroscan.ai.model_registry import ModelRegistry, ModalityInferenceOrchestrator

class TestModule5ModalityAI(unittest.TestCase):

    def setUp(self):
        self.orchestrator = ModalityInferenceOrchestrator()

    def create_imu_vector(self, quality=SignalQualityStatus.PASS, artifacts=None, schema_ver="1.0"):
        prov = FeatureProvenance(
            source_sensor_id="IMU_TEST_01",
            subject_id="SUBJ_001",
            session_id="SESS_001",
            modality=Modality.IMU,
            window_start_ms=1000,
            window_end_ms=2000,
            sample_count=200,
            sampling_rate_hz=200.0,
            preprocessing_version="1.0",
            feature_schema_version=schema_ver,
            quality_status=quality,
            quality_score=0.95 if quality == SignalQualityStatus.PASS else 0.2,
            artifact_flags=artifacts or []
        )
        features = {
            "acc_mean_mag": FeatureItem("acc_mean_mag", 9.85, "m/s^2", FeatureDomain.STATISTICAL, "Mean"),
            "acc_std_mag": FeatureItem("acc_std_mag", 1.30, "m/s^2", FeatureDomain.STATISTICAL, "Std"),
            "acc_rms_mag": FeatureItem("acc_rms_mag", 9.95, "m/s^2", FeatureDomain.STATISTICAL, "RMS"),
            "acc_peak_mag": FeatureItem("acc_peak_mag", 14.80, "m/s^2", FeatureDomain.STATISTICAL, "Peak"),
            "gyro_rms_mag": FeatureItem("gyro_rms_mag", 0.90, "rad/s", FeatureDomain.STATISTICAL, "Gyro RMS"),
            "gait_cadence_hz": FeatureItem("gait_cadence_hz", 1.70, "Hz", FeatureDomain.TEMPORAL, "Cadence"),
            "movement_symmetry_index": FeatureItem("movement_symmetry_index", 0.90, "ratio", FeatureDomain.TEMPORAL, "Symmetry")
        }
        return ModalityFeatureVector(provenance=prov, features=features)

    def test_immutable_normalization_and_predict_nominal(self):
        vec = self.create_imu_vector()
        pred = self.orchestrator.run_inference(vec)

        self.assertEqual(pred.status, ModelExecutionStatus.SUCCESS)
        self.assertIsNotNone(pred.score)
        self.assertTrue(0.0 <= pred.score <= 1.0)
        self.assertIsNotNone(pred.calibrated_uncertainty)
        self.assertIn(pred.prediction_label, ["TYPICAL", "BORDERLINE", "DEVIANT"])
        self.assertEqual(pred.model_metadata.model_id, "M5_IMU_KINEMATIC_V1")

    def test_quality_gate_rejection(self):
        # When signal quality fails, inference is blocked defensively
        vec = self.create_imu_vector(
            quality=SignalQualityStatus.FAIL,
            artifacts=[ArtifactType.FLATLINE_DROPOUT]
        )
        pred = self.orchestrator.run_inference(vec)
        self.assertEqual(pred.status, ModelExecutionStatus.REJECTED_QUALITY)
        self.assertIsNone(pred.score)
        self.assertIn("Rejected", pred.status_reason)

    def test_schema_mismatch_rejection(self):
        # Outdated or mismatched feature schema version is rejected
        vec = self.create_imu_vector(schema_ver="0.9-legacy")
        pred = self.orchestrator.run_inference(vec)
        self.assertEqual(pred.status, ModelExecutionStatus.SCHEMA_MISMATCH)
        self.assertIsNone(pred.score)

    def test_missing_required_feature_refuses_silent_substitution(self):
        # A missing required feature MUST NOT be silently filled with zero
        vec = self.create_imu_vector()
        del vec.features["movement_symmetry_index"]
        pred = self.orchestrator.run_inference(vec)
        self.assertEqual(pred.status, ModelExecutionStatus.SCHEMA_MISMATCH)
        self.assertIn("Missing required feature 'movement_symmetry_index'", pred.status_reason)

    def test_nan_or_inf_rejection(self):
        # Non-finite values are caught and blocked
        vec = self.create_imu_vector()
        vec.features["acc_mean_mag"] = FeatureItem(
            "acc_mean_mag", float("nan"), "m/s^2", FeatureDomain.STATISTICAL, "NaN"
        )
        pred = self.orchestrator.run_inference(vec)
        self.assertEqual(pred.status, ModelExecutionStatus.SCHEMA_MISMATCH)
        self.assertIn("non-finite", pred.status_reason)

    def test_rf_experimental_quarantine(self):
        # RF model execution must be strictly isolated and marked experimental
        prov = FeatureProvenance(
            source_sensor_id="RF_TEST_01",
            subject_id="SUBJ_001",
            session_id="SESS_001",
            modality=Modality.RF,
            window_start_ms=1000,
            window_end_ms=2000,
            sample_count=100,
            sampling_rate_hz=50.0,
            preprocessing_version="1.0",
            feature_schema_version="1.0",
            quality_status=SignalQualityStatus.PASS,
            quality_score=0.98,
            artifact_flags=[]
        )
        features = {
            "rf_min_reflection_db": FeatureItem("rf_min_reflection_db", -23.0, "dB", FeatureDomain.EXPERIMENTAL_RF, "Dip"),
            "rf_resonance_freq_mhz": FeatureItem("rf_resonance_freq_mhz", 2448.0, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Res"),
            "rf_resonance_shift_mhz": FeatureItem("rf_resonance_shift_mhz", 1.5, "MHz", FeatureDomain.EXPERIMENTAL_RF, "Shift"),
            "rf_measurement_stability": FeatureItem("rf_measurement_stability", 0.95, "index", FeatureDomain.EXPERIMENTAL_RF, "Stability")
        }
        vec = ModalityFeatureVector(provenance=prov, features=features)
        pred = self.orchestrator.run_inference(vec)

        self.assertEqual(pred.status, ModelExecutionStatus.SUCCESS)
        self.assertTrue(pred.model_metadata.is_experimental)
        self.assertTrue(pred.prediction_label.startswith("EXP_"))
        self.assertIn("EXPERIMENTAL RESEARCH INFERENCE", pred.status_reason)

    def test_vag_and_semg_runners(self):
        # Test VAG Runner
        vag_prov = FeatureProvenance(
            source_sensor_id="VAG_01", subject_id="S1", session_id="SS1",
            modality=Modality.VAG, window_start_ms=0, window_end_ms=100,
            sample_count=400, sampling_rate_hz=4000.0, preprocessing_version="1.0",
            feature_schema_version="1.0", quality_status=SignalQualityStatus.PASS, quality_score=0.95
        )
        vag_features = {
            "vag_rms": FeatureItem("vag_rms", 0.048, "a.u.", FeatureDomain.STATISTICAL, ""),
            "vag_peak_amplitude": FeatureItem("vag_peak_amplitude", 0.19, "a.u.", FeatureDomain.STATISTICAL, ""),
            "vag_acoustic_power": FeatureItem("vag_acoustic_power", 0.0026, "a.u.^2", FeatureDomain.STATISTICAL, ""),
            "vag_zero_crossing_rate": FeatureItem("vag_zero_crossing_rate", 285.0, "cross/s", FeatureDomain.TEMPORAL, ""),
            "vag_crest_factor": FeatureItem("vag_crest_factor", 4.15, "ratio", FeatureDomain.TEMPORAL, ""),
            "vag_spectral_centroid_hz": FeatureItem("vag_spectral_centroid_hz", 350.0, "Hz", FeatureDomain.SPECTRAL, "")
        }
        vag_pred = self.orchestrator.run_inference(ModalityFeatureVector(vag_prov, vag_features))
        self.assertEqual(vag_pred.status, ModelExecutionStatus.SUCCESS)
        self.assertEqual(vag_pred.model_metadata.model_id, "M5_VAG_ACOUSTIC_V1")

        # Test sEMG Runner
        semg_prov = FeatureProvenance(
            source_sensor_id="SEMG_01", subject_id="S1", session_id="SS1",
            modality=Modality.SEMG, window_start_ms=0, window_end_ms=100,
            sample_count=200, sampling_rate_hz=2000.0, preprocessing_version="1.0",
            feature_schema_version="1.0", quality_status=SignalQualityStatus.PASS, quality_score=0.92
        )
        semg_features = {
            "semg_rms_vm": FeatureItem("semg_rms_vm", 0.098, "mV", FeatureDomain.STATISTICAL, ""),
            "semg_mav_vm": FeatureItem("semg_mav_vm", 0.075, "mV", FeatureDomain.STATISTICAL, ""),
            "semg_waveform_length": FeatureItem("semg_waveform_length", 16.0, "mV", FeatureDomain.STATISTICAL, ""),
            "semg_activation_ratio": FeatureItem("semg_activation_ratio", 0.44, "ratio", FeatureDomain.TEMPORAL, ""),
            "semg_co_contraction_ratio": FeatureItem("semg_co_contraction_ratio", 0.79, "ratio", FeatureDomain.TEMPORAL, ""),
            "semg_mean_frequency_hz": FeatureItem("semg_mean_frequency_hz", 85.0, "Hz", FeatureDomain.SPECTRAL, "")
        }
        semg_pred = self.orchestrator.run_inference(ModalityFeatureVector(semg_prov, semg_features))
        self.assertEqual(semg_pred.status, ModelExecutionStatus.SUCCESS)
        self.assertEqual(semg_pred.model_metadata.model_id, "M5_SEMG_NEUROMUSCULAR_V1")

if __name__ == "__main__":
    unittest.main()

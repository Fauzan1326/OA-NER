"""
Unit tests for Module 4: Multimodal Feature Extraction & Feature Store.
"""

import unittest
import math
from arthroscan.schemas.contract import Modality, SignalQualityStatus
from arthroscan.preprocessing.quality_gate import QualityAssessment, ArtifactType
from arthroscan.features.feature_schema import (
    FEATURE_SCHEMA_VERSION, FeatureDomain, FeatureProvenance, ModalityFeatureVector
)
from arthroscan.features.temporal_spectral import (
    mean, root_mean_square, zero_crossing_rate, compute_dft_magnitude, spectral_centroid_and_spread
)
from arthroscan.features.extractors import (
    ImuFeatureExtractor, VagFeatureExtractor, SemgFeatureExtractor, RfFeatureExtractor
)
from arthroscan.features.feature_store import (
    FeatureStore, FeatureStoreValidationError
)

class TestModule4Features(unittest.TestCase):

    def test_math_primitives(self):
        vals = [1.0, -1.0, 1.0, -1.0]
        self.assertEqual(mean(vals), 0.0)
        self.assertEqual(root_mean_square(vals), 1.0)
        self.assertAlmostEqual(zero_crossing_rate(vals), 1.0, places=4)

        # Spectral Centroid test with 10 Hz sine
        fs = 100.0
        n = 64
        sine = [math.sin(2.0 * math.pi * 10.0 * t / fs) for t in range(n)]
        freqs, mags = compute_dft_magnitude(sine, fs)
        centroid, spread, dom = spectral_centroid_and_spread(freqs, mags)
        self.assertAlmostEqual(dom, 10.0, delta=2.0)

    def test_imu_feature_extractor(self):
        extractor = ImuFeatureExtractor()
        # 10 samples of 1g on Z, 0 on others
        window = [[0.0, 0.0, 1.0, 0.0, 0.0, 0.0] for _ in range(20)]
        qa = QualityAssessment(
            modality=Modality.IMU,
            status=SignalQualityStatus.PASS,
            sqi_score=0.95,
            detected_artifacts=[]
        )
        vec = extractor.extract(
            window=window,
            sampling_rate_hz=100.0,
            qa=qa,
            start_time_ms=0,
            end_time_ms=200
        )
        self.assertEqual(vec.provenance.modality, Modality.IMU)
        self.assertEqual(vec.provenance.quality_status, SignalQualityStatus.PASS)
        self.assertAlmostEqual(vec.get_feature("acc_mean_mag"), 1.0, places=4)
        self.assertAlmostEqual(vec.get_feature("acc_rms_mag"), 1.0, places=4)
        self.assertAlmostEqual(vec.get_feature("acc_std_mag"), 0.0, places=4)
        self.assertEqual(vec.provenance.feature_schema_version, FEATURE_SCHEMA_VERSION)

    def test_vag_feature_extractor(self):
        extractor = VagFeatureExtractor()
        # Simulated crepitus acoustic burst
        window = [0.1 * math.sin(i * 0.5) for i in range(50)]
        qa = QualityAssessment(
            modality=Modality.VAG,
            status=SignalQualityStatus.PASS,
            sqi_score=0.90,
            detected_artifacts=[]
        )
        vec = extractor.extract(
            window=window,
            sampling_rate_hz=2000.0,
            qa=qa,
            start_time_ms=1000,
            end_time_ms=1025
        )
        self.assertEqual(vec.provenance.modality, Modality.VAG)
        self.assertIn("vag_rms", vec.features)
        self.assertIn("vag_spectral_centroid_hz", vec.features)
        self.assertGreater(vec.get_feature("vag_rms"), 0.0)

    def test_semg_feature_extractor(self):
        extractor = SemgFeatureExtractor()
        # Multi-channel activation [VM, VL]
        window = [[0.2, 0.1] for _ in range(40)]
        qa = QualityAssessment(
            modality=Modality.SEMG,
            status=SignalQualityStatus.PASS,
            sqi_score=0.88,
            detected_artifacts=[]
        )
        vec = extractor.extract(
            window=window,
            sampling_rate_hz=1000.0,
            qa=qa,
            start_time_ms=500,
            end_time_ms=540
        )
        self.assertEqual(vec.provenance.modality, Modality.SEMG)
        self.assertAlmostEqual(vec.get_feature("semg_rms_vm"), 0.2, places=3)
        # VM=0.2, VL=0.1 -> ratio = 0.2 / 0.3 = 0.6667
        self.assertAlmostEqual(vec.get_feature("semg_co_contraction_ratio"), 0.6667, places=2)

    def test_rf_feature_extractor(self):
        extractor = RfFeatureExtractor(baseline_res_freq_mhz=2450.0)
        # S11 magnitude, phase, freq
        window = [[-18.5, 0.2, 2448.0] for _ in range(10)]
        qa = QualityAssessment(
            modality=Modality.RF,
            status=SignalQualityStatus.PASS,
            sqi_score=0.92,
            detected_artifacts=[]
        )
        vec = extractor.extract(
            window=window,
            sampling_rate_hz=50.0,
            qa=qa,
            start_time_ms=0,
            end_time_ms=200
        )
        self.assertEqual(vec.provenance.modality, Modality.RF)
        self.assertEqual(vec.features["rf_resonance_shift_mhz"].domain, FeatureDomain.EXPERIMENTAL_RF)
        self.assertAlmostEqual(vec.get_feature("rf_resonance_shift_mhz"), -2.0, places=3)
        self.assertAlmostEqual(vec.get_feature("rf_min_reflection_db"), -18.5, places=3)

    def test_feature_store_validation_and_query(self):
        store = FeatureStore(reject_quality_failures=True)
        imu_extractor = ImuFeatureExtractor()
        window = [[0.0, 0.0, 1.0, 0.0, 0.0, 0.0] for _ in range(10)]
        qa_pass = QualityAssessment(Modality.IMU, 0.95, SignalQualityStatus.PASS, [])
        qa_fail = QualityAssessment(Modality.IMU, 0.30, SignalQualityStatus.FAIL, [ArtifactType.CLIPPING_SATURATION])

        vec_pass = imu_extractor.extract(window, 100.0, qa_pass, start_time_ms=0, end_time_ms=100)
        vec_fail = imu_extractor.extract(window, 100.0, qa_fail, start_time_ms=100, end_time_ms=200)

        # vec_pass should insert successfully
        self.assertTrue(store.insert(vec_pass))
        # vec_fail should be rejected due to reject_quality_failures=True
        self.assertFalse(store.insert(vec_fail))

        self.assertEqual(store.count(), 1)
        self.assertEqual(store.count(Modality.IMU), 1)
        self.assertEqual(store.count(Modality.VAG), 0)

        # Query
        res = store.query(modality=Modality.IMU, start_ms=0, end_ms=100)
        self.assertEqual(len(res), 1)

        # Normalization stats
        stats = store.compute_normalization_stats(Modality.IMU)
        self.assertIn("acc_mean_mag", stats)
        self.assertAlmostEqual(stats["acc_mean_mag"]["mean"], 1.0, places=4)

if __name__ == "__main__":
    unittest.main()

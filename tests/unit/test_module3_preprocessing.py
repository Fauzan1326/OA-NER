"""
Unit Tests for Module 3: Signal Quality, Preprocessing & Artifact Detection (Python)
"""

import unittest
from arthroscan.schemas.contract import (
    SensorPacket, Modality, DeviceStatus, SignalQualityStatus,
    TimestampInfo, SamplingInfo, DeviceInfo, QualityInfo, SchemaVersion
)
from arthroscan.preprocessing.filters import (
    BiquadFilter, MovingAverageFilter, RmsEnvelopeFilter
)
from arthroscan.preprocessing.modality_preprocessors import (
    ImuPreprocessor, VagPreprocessor, SemgPreprocessor, RfPreprocessor
)
from arthroscan.preprocessing.quality_gate import (
    SignalQualityGate, ArtifactType
)
from arthroscan.preprocessing.pipeline import PreprocessingPipeline

class TestModule3Preprocessing(unittest.TestCase):

    def test_biquad_lowpass_filter(self):
        lp = BiquadFilter.create_lowpass(cutoff_hz=20.0, sample_rate_hz=100.0)
        # DC gain should be approx 1.0
        dc_val = 1.0
        y = 0.0
        for _ in range(50):
            y = lp.process(dc_val)
        self.assertAlmostEqual(y, 1.0, places=2)

    def test_moving_average_and_rms(self):
        ma = MovingAverageFilter(window_size=4)
        for v in [2.0, 4.0, 6.0, 8.0]:
            out = ma.process(v)
        self.assertEqual(out, 5.0)

        rms = RmsEnvelopeFilter(window_size=2)
        rms.process(3.0)
        out2 = rms.process(4.0)
        # sqrt((9 + 16) / 2) = sqrt(12.5) = 3.5355
        self.assertAlmostEqual(out2, 3.5355, places=3)

    def test_imu_preprocessing(self):
        imu = ImuPreprocessor()
        vals = [0.0, 1.0, 0.0, 0.1, 0.0, 0.0]
        filtered, acc_mag, gyro_mag = imu.process_sample(vals)
        self.assertEqual(len(filtered), 6)
        self.assertGreater(acc_mag, 0.0)

    def test_artifact_detection_clipping_and_spikes(self):
        gate = SignalQualityGate()
        
        # Clean normal IMU window with natural variation
        clean_window = [[0.01 + 0.005 * i, 0.98 - 0.003 * i, -0.05, 0.0, 0.0, 0.0] for i in range(10)]
        clean_qa = gate.assess_imu(clean_window)
        self.assertEqual(clean_qa.status, SignalQualityStatus.PASS)
        self.assertGreaterEqual(clean_qa.sqi_score, 0.70)

        # Clipping window (sensor hitting dynamic range limit > 15.8g)
        clip_window = [[0.0, 16.0, 0.0] for _ in range(5)]
        clip_qa = gate.assess_imu(clip_window)
        self.assertIn(ArtifactType.CLIPPING_SATURATION, clip_qa.detected_artifacts)
        self.assertLess(clip_qa.sqi_score, 0.70)

        # Motion spike window (> 4.0g)
        spike_window = [[0.0, 4.5, 0.0] for _ in range(5)]
        spike_qa = gate.assess_imu(spike_window)
        self.assertIn(ArtifactType.MOTION_SPIKE, spike_qa.detected_artifacts)

    def test_artifact_detection_flatline(self):
        gate = SignalQualityGate()
        # Zero variance flatline
        flatline_window = [0.0 for _ in range(10)]
        flat_qa = gate.assess_vag(flatline_window)
        self.assertIn(ArtifactType.FLATLINE_DROPOUT, flat_qa.detected_artifacts)
        self.assertEqual(flat_qa.status, SignalQualityStatus.FAIL)

    def test_unified_preprocessing_pipeline(self):
        pipeline = PreprocessingPipeline()
        pkt = SensorPacket(
            schema_version="1.0",
            subject_id="S_TEST",
            session_id="SESS_01",
            sensor_id="DEV_IMU",
            modality=Modality.IMU,
            timestamp=TimestampInfo(device_time_ms=1000, sequence_number=1),
            sampling=SamplingInfo(rate_hz=100.0),
            channels=["ax", "ay", "az", "gx", "gy", "gz"],
            values=[0.02, 0.98, -0.04, 0.01, -0.01, 0.0],
            units="g,rad/s",
            device=DeviceInfo(status=DeviceStatus.STREAMING),
            quality=QualityInfo(status=SignalQualityStatus.PASS, score=1.0)
        )
        frame = pipeline.process_packet(pkt)
        self.assertEqual(frame.modality, Modality.IMU)
        self.assertEqual(len(frame.cleaned_values), 6)
        self.assertIn("acc_magnitude", frame.derived_metrics)
        self.assertIsNotNone(frame.quality)

if __name__ == "__main__":
    unittest.main()

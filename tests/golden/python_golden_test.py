import json
import os
import unittest
import math

class PythonGoldenTest(unittest.TestCase):
    def setUp(self):
        self.curr_dir = os.path.dirname(os.path.abspath(__file__))

    def test_golden_session_packets(self):
        session_path = os.path.join(self.curr_dir, "golden_session.json")
        with open(session_path, "r") as f:
            data = json.load(f)

        self.assertEqual(data["schema_version"], "1.0")
        self.assertEqual(data["subject_id"], "SUBJ_GOLDEN_26004")
        self.assertEqual(len(data["sensor_packets"]), 4)

        seen_modalities = set()
        for packet in data["sensor_packets"]:
            self.assertEqual(packet["schema_version"], "1.0")
            self.assertEqual(packet["subject_id"], "SUBJ_GOLDEN_26004")
            self.assertIn(packet["modality"], ["IMU", "VAG", "sEMG", "RF"])
            seen_modalities.add(packet["modality"])
            self.assertGreater(packet["sampling"]["rate_hz"], 0.0)
            self.assertEqual(len(packet["channels"]), len(packet["values"]))
            self.assertGreaterEqual(packet["quality"]["score"], 0.0)
            self.assertLessEqual(packet["quality"]["score"], 1.0)
            for v in packet["values"]:
                self.assertFalse(math.isnan(v))
                self.assertFalse(math.isinf(v))

        self.assertEqual(seen_modalities, {"IMU", "VAG", "sEMG", "RF"})

    def test_golden_features_and_tolerance(self):
        feat_path = os.path.join(self.curr_dir, "golden_features.json")
        with open(feat_path, "r") as f:
            data = json.load(f)

        self.assertEqual(data["feature_schema_version"], "1.0")
        epsilon = data["numerical_tolerance_epsilon"]
        self.assertLessEqual(epsilon, 1e-4)

        feats = data["modality_features"]
        self.assertIn("IMU", feats)
        self.assertIn("VAG", feats)
        self.assertIn("sEMG", feats)
        self.assertIn("RF", feats)
        # Check IMU mean acc magnitude is close to 0.98215
        self.assertAlmostEqual(feats["IMU"]["mean_acc_mag"], 0.98215, delta=epsilon)

    def test_golden_screening_result_safety(self):
        result_path = os.path.join(self.curr_dir, "golden_screening_result.json")
        with open(result_path, "r") as f:
            res = json.load(f)

        self.assertEqual(res["schema_version"], "1.0")
        self.assertEqual(res["risk_tier"], "MODERATE_SCREENING_RISK")
        self.assertIn("CLINICAL_EVALUATION_RECOMMENDED", res["referral_recommendation"])
        self.assertTrue(res["is_synthetic"])
        self.assertIn("NOT A CLINICAL DIAGNOSIS", res["disclaimer"])
        self.assertIn("ILLUSTRATIVE SIMULATION", res["disclaimer"])

if __name__ == "__main__":
    unittest.main()

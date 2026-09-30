import json
import os
import unittest

class GoldenContractTest(unittest.TestCase):
    def test_golden_session_contract(self):
        curr_dir = os.path.dirname(os.path.abspath(__file__))
        golden_file = os.path.join(curr_dir, "golden_session.json")
        with open(golden_file, "r") as f:
            data = json.load(f)

        self.assertEqual(data["schema_version"], "v1.0")
        self.assertEqual(len(data["sensor_packets"]), 3)
        modalities = [p["modality"] for p in data["sensor_packets"]]
        self.assertIn("IMU", modalities)
        self.assertIn("VAG", modalities)
        self.assertIn("RF", modalities)

        for p in data["sensor_packets"]:
            self.assertEqual(p["schema_version"], "v1.0")
            self.assertIn(p["device_status"], ["STREAMING", "CONNECTED"])
            self.assertGreater(p["quality_score"], 0.0)

if __name__ == "__main__":
    unittest.main()

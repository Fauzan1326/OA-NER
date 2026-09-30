"""
Module 2 Unit Test Suite:
Testing CSV Replay, Gateway framing, Sequence Tracking, Circular Buffer,
Timestamp Synchronization, and Session Recording/Replay.
"""

import unittest
import os
import tempfile
from arthroscan.schemas.contract import (
    SensorPacket, Modality, DeviceStatus, SignalQualityStatus,
    TimestampInfo, SamplingInfo, DeviceInfo, QualityInfo, SchemaVersion
)
from arthroscan.hardware.csv_replay import CSVReplaySensorAdapter
from arthroscan.hardware.gateway import HardwareGatewayAdapter, GatewayPacketFramer
from arthroscan.hardware.streaming import (
    CircularBuffer, PacketSequenceTracker, TimestampSynchronizer, StreamIngestionEngine
)
from arthroscan.hardware.recorder import SessionRecorder, SessionReplayReader

class TestModule2Streaming(unittest.TestCase):

    def test_csv_replay_adapter(self):
        adapter = CSVReplaySensorAdapter(
            modality=Modality.IMU,
            csv_file_path="datasets/raw/sample_gait_imu.csv",
            loop=False
        )
        self.assertTrue(adapter.connect())
        self.assertTrue(adapter.start_stream())
        
        sample1 = adapter.read_sample()
        self.assertNotNull = self.assertIsNotNone(sample1)
        self.assertEqual(sample1.modality, Modality.IMU)
        self.assertEqual(sample1.schema_version, SchemaVersion.CURRENT)
        self.assertEqual(len(sample1.values), 6)
        self.assertTrue(sample1.is_valid())

        calib = adapter.calibrate()
        self.assertEqual(calib["status"], "CALIBRATED")
        adapter.disconnect()

    def test_gateway_framing_and_checksum(self):
        values = [0.12, -0.45, 1.02]
        frame = GatewayPacketFramer.encode_frame(0, 105, values)
        self.assertEqual(frame[0], GatewayPacketFramer.SOF)
        self.assertEqual(frame[-1], GatewayPacketFramer.EOF)

        decoded = GatewayPacketFramer.decode_frame(frame)
        self.assertIsNotNone(decoded)
        mod_id, rx_seq, rx_vals = decoded
        self.assertEqual(mod_id, 0)
        self.assertEqual(rx_seq, 105)
        self.assertEqual(len(rx_vals), 3)
        self.assertAlmostEqual(rx_vals[0], 0.12, places=5)

        # Corrupt checksum test
        corrupt_frame = bytearray(frame)
        corrupt_frame[-2] ^= 0xFF
        corrupt_decoded = GatewayPacketFramer.decode_frame(bytes(corrupt_frame))
        self.assertIsNone(corrupt_decoded)

    def test_gateway_adapter_lifecycle_and_reconnect(self):
        gw = HardwareGatewayAdapter(Modality.IMU, port_or_mac="TEST_PORT", gateway_type="USB_CDC")
        self.assertTrue(gw.connect())
        self.assertTrue(gw.start_stream())
        sample = gw.read_sample()
        self.assertIsNotNone(sample)
        self.assertTrue(sample.is_valid())

        # Test graceful degradation on disconnect
        gw.simulate_hardware_disconnect()
        self.assertEqual(gw.get_status(), DeviceStatus.DISCONNECTED)
        
        # Auto-reconnect on read_sample
        reconnected_sample = gw.read_sample()
        self.assertIsNotNone(reconnected_sample)
        self.assertEqual(gw.get_status(), DeviceStatus.STREAMING)

    def test_circular_buffer_and_window(self):
        cb = CircularBuffer(capacity=5)
        for i in range(10):
            pkt = SensorPacket(
                schema_version=SchemaVersion.CURRENT,
                subject_id="S1",
                session_id="SESS1",
                sensor_id="DEV1",
                modality=Modality.IMU,
                timestamp=TimestampInfo(device_time_ms=1000 + i * 10, sequence_number=i),
                sampling=SamplingInfo(rate_hz=100.0),
                channels=["x"],
                values=[float(i)],
                units="g",
                device=DeviceInfo(status=DeviceStatus.STREAMING),
                quality=QualityInfo(status=SignalQualityStatus.PASS, score=1.0)
            )
            cb.append(pkt)

        self.assertEqual(cb.size(), 5)
        self.assertEqual(cb.utilization_percent(), 100.0)
        window = cb.get_window(3)
        self.assertEqual(len(window), 3)
        self.assertEqual(window[-1].timestamp.sequence_number, 9)

    def test_packet_sequence_tracking_gaps_and_loss(self):
        tracker = PacketSequenceTracker(Modality.IMU)
        tracker.set_sampling_rate(100.0)

        # Packet 1
        p1 = SensorPacket(
            schema_version="1.0", subject_id="S", session_id="S", sensor_id="D",
            modality=Modality.IMU,
            timestamp=TimestampInfo(device_time_ms=1000, sequence_number=1),
            sampling=SamplingInfo(rate_hz=100.0), channels=["x"], values=[1.0], units="g",
            device=DeviceInfo(status=DeviceStatus.STREAMING), quality=QualityInfo(status=SignalQualityStatus.PASS, score=1.0)
        )
        tracker.track_packet(p1)
        self.assertEqual(tracker.telemetry.total_packets_received, 1)
        self.assertEqual(tracker.telemetry.sequence_gaps_detected, 0)

        # Gap: Packet 5 arrives next (missing 2, 3, 4 -> 3 packets lost)
        p5 = SensorPacket(
            schema_version="1.0", subject_id="S", session_id="S", sensor_id="D",
            modality=Modality.IMU,
            timestamp=TimestampInfo(device_time_ms=1040, sequence_number=5),
            sampling=SamplingInfo(rate_hz=100.0), channels=["x"], values=[5.0], units="g",
            device=DeviceInfo(status=DeviceStatus.STREAMING), quality=QualityInfo(status=SignalQualityStatus.PASS, score=1.0)
        )
        tracker.track_packet(p5)
        self.assertEqual(tracker.telemetry.sequence_gaps_detected, 3)
        self.assertGreater(tracker.telemetry.packet_loss_rate_percent, 0.0)

        # Duplicate: Packet 5 arrives again
        tracker.track_packet(p5)
        self.assertEqual(tracker.telemetry.duplicates_detected, 1)

    def test_session_recorder_and_replay(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            recorder = SessionRecorder(output_dir=tmpdir)
            rec_file = recorder.start_recording("TEST_SESS")
            
            p = SensorPacket(
                schema_version="1.0", subject_id="S_REC", session_id="TEST_SESS", sensor_id="DEV_REC",
                modality=Modality.VAG,
                timestamp=TimestampInfo(device_time_ms=1774000000000, sequence_number=1),
                sampling=SamplingInfo(rate_hz=2000.0), channels=["vag_ch1"], values=[0.123], units="mV",
                device=DeviceInfo(status=DeviceStatus.STREAMING), quality=QualityInfo(status=SignalQualityStatus.PASS, score=1.0)
            )
            recorder.record_packet(p)
            count = recorder.stop_recording()
            self.assertEqual(count, 1)

            # Replay back
            replayed = SessionReplayReader.read_session_file(rec_file)
            self.assertEqual(len(replayed), 1)
            self.assertEqual(replayed[0].subject_id, "S_REC")
            self.assertEqual(replayed[0].values[0], 0.123)

if __name__ == "__main__":
    unittest.main()

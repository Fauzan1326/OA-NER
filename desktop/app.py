"""
ARTHROSCAN-NER Desktop Research Application Shell
Module 2: Universal Sensor Simulation, Hardware-Abstraction & Real-Time Streaming Engine
SIH 2026 - Problem Statement: SIH26004 - Team GOD'S PLAN
"""
import sys
import os
import time
from arthroscan.schemas.contract import Modality, SCHEMA_VERSION, DeviceStatus
from arthroscan.hardware.simulator import SimulatorSensorAdapter
from arthroscan.hardware.csv_replay import CSVReplaySensorAdapter
from arthroscan.hardware.gateway import HardwareGatewayAdapter
from arthroscan.hardware.streaming import StreamIngestionEngine
from arthroscan.hardware.recorder import SessionRecorder

def run_streaming_demo(profile: str = "DEMO"):
    print("=" * 72)
    print("  ARTHROSCAN-NER — RESEARCH & SCREENING PLATFORM (MODULE 2)")
    print(f"  Profile: {profile} | Schema: v{SCHEMA_VERSION} | Seed: 26004")
    print("  Disclaimers: SCREENING DECISION SUPPORT & RESEARCH — NOT CLINICAL DIAGNOSIS")
    print("=" * 72)

    # Initialize adapters based on selected profile
    if profile == "RESEARCH":
        print("[MODE] Initializing CSV Replay Adapters for Research Benchmarks...")
        adapters = {
            Modality.IMU: CSVReplaySensorAdapter(Modality.IMU, "datasets/raw/sample_gait_imu.csv", 100.0),
            Modality.VAG: CSVReplaySensorAdapter(Modality.VAG, "datasets/raw/sample_acoustic_vag.csv", 2000.0),
            Modality.SEMG: CSVReplaySensorAdapter(Modality.SEMG, "datasets/raw/sample_muscle_semg.csv", 1000.0),
            Modality.RF: CSVReplaySensorAdapter(Modality.RF, "datasets/raw/sample_resonance_rf.csv", 10.0),
        }
    elif profile == "HARDWARE":
        print("[MODE] Initializing Hardware Gateway Adapters (BLE / USB Serial)...")
        adapters = {
            Modality.IMU: HardwareGatewayAdapter(Modality.IMU, "COM3", "USB_CDC", 100.0),
            Modality.VAG: HardwareGatewayAdapter(Modality.VAG, "BLE:AA:BB:CC:01", "BLE", 2000.0),
            Modality.SEMG: HardwareGatewayAdapter(Modality.SEMG, "COM4", "USB_CDC", 100.0),
            Modality.RF: HardwareGatewayAdapter(Modality.RF, "COM5", "USB_CDC", 10.0),
        }
    else:  # DEMO
        print("[MODE] Initializing Deterministic Multi-Sensor Simulators (Seed: 26004)...")
        adapters = {
            Modality.IMU: SimulatorSensorAdapter(Modality.IMU, 100.0),
            Modality.VAG: SimulatorSensorAdapter(Modality.VAG, 2000.0),
            Modality.SEMG: SimulatorSensorAdapter(Modality.SEMG, 1000.0),
            Modality.RF: SimulatorSensorAdapter(Modality.RF, 10.0),
        }

    engine = StreamIngestionEngine(buffer_capacity=500)
    recorder = SessionRecorder(output_dir="recordings")
    rec_path = recorder.start_recording(f"DESKTOP_{profile}")

    print("\n[LIFECYCLE] Connecting and starting sensor streams...")
    for mod, adapter in adapters.items():
        adapter.connect()
        adapter.start_stream()
        print(f"  ✓ {mod.name:<6} Adapter: {adapter.get_metadata().get('sensor_type', 'ADAPTER')} -> STATUS: {adapter.get_status().name}")

    print("\n[STREAMING] Streaming ticks into Ingestion Engine & Circular Ring Buffer...")
    for tick in range(15):
        for mod, adapter in adapters.items():
            packet = adapter.read_sample()
            if packet:
                ok = engine.ingest_packet(packet)
                if ok:
                    recorder.record_packet(packet)

    recorded_count = recorder.stop_recording()
    print(f"\n[RECORDING] Recorded {recorded_count} packets to session file: {rec_path}")

    print("\n[TELEMETRY & HARDWARE HEALTH REPORT]")
    for mod in [Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF]:
        telem = engine.get_telemetry(mod)
        buf = engine.buffers[mod]
        print(
            f"  Modality: {mod.name:<6} | Rx: {telem.total_packets_received:<3} | "
            f"Gaps: {telem.sequence_gaps_detected} | Loss: {telem.packet_loss_rate_percent:.1f}% | "
            f"Jitter: {telem.average_jitter_ms:.2f}ms | Ring Buffer: {buf.size()}/{buf.capacity} ({buf.utilization_percent():.1f}%)"
        )

    # Disconnect
    for adapter in adapters.values():
        adapter.disconnect()

    print("\n[COMPLETED] Module 2 Real-Time Streaming Engine execution finished successfully.")
    return 0

def main():
    profile = sys.argv[1] if len(sys.argv) > 1 else "DEMO"
    return run_streaming_demo(profile)

if __name__ == "__main__":
    sys.exit(main())

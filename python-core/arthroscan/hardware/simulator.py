import time
import math
from typing import Dict, Any, Optional
from arthroscan.hardware.adapter import SensorAdapter
from arthroscan.schemas.contract import (
    SensorPacket,
    Modality,
    DeviceStatus,
    SignalQualityStatus,
    TimestampInfo,
    SamplingInfo,
    DeviceInfo,
    QualityInfo,
    SCHEMA_VERSION
)

# Deterministic Seed for SIH 2026 Problem SIH26004
SIMULATOR_SEED = 26004

class SimulatorSensorAdapter(SensorAdapter):
    def __init__(self, modality: Modality, sampling_rate_hz: float = 100.0):
        self.modality = modality
        self.sampling_rate = sampling_rate_hz
        self.status = DeviceStatus.DISCONNECTED
        self.seq = 0
        self.subject_id = f"SUBJ_SIM_{SIMULATOR_SEED}"
        self.session_id = "SESSION_DEMO_26004"
        self.is_streaming = False
        self.firmware_version = "sim-v1.0-sih26004"

    def connect(self) -> bool:
        self.status = DeviceStatus.CONNECTED
        return True

    def disconnect(self) -> bool:
        self.is_streaming = False
        self.status = DeviceStatus.DISCONNECTED
        return True

    def start_stream(self) -> bool:
        if self.status in (DeviceStatus.CONNECTED, DeviceStatus.PAUSED):
            self.status = DeviceStatus.STREAMING
            self.is_streaming = True
            return True
        return False

    def stop_stream(self) -> bool:
        if self.is_streaming:
            self.is_streaming = False
            self.status = DeviceStatus.CONNECTED
            return True
        return False

    def read_sample(self) -> Optional[SensorPacket]:
        if not self.is_streaming:
            return None
        self.seq += 1
        now_ms = 1774000000000 + int(self.seq * (1000.0 / self.sampling_rate))
        t = self.seq / self.sampling_rate

        # Deterministic generation identical across Python and Android
        if self.modality == Modality.IMU:
            channels = ["acc_x", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z"]
            ax = 0.04 * math.sin(2.0 * math.pi * 1.2 * t)
            ay = 0.98 + 0.05 * math.cos(2.0 * math.pi * 1.2 * t)
            az = 0.02 * math.sin(4.0 * math.pi * 1.2 * t)
            gx = 0.15 * math.sin(2.0 * math.pi * 1.2 * t)
            gy = 0.05 * math.cos(2.0 * math.pi * 1.2 * t)
            gz = 0.02 * math.sin(math.pi * t)
            values = [ax, ay, az, gx, gy, gz]
            units = "g,rad/s"
            q_score = 0.99
        elif self.modality == Modality.VAG:
            channels = ["vag_acoustic"]
            v1 = 0.035 * math.sin(2.0 * math.pi * 140.0 * t) + 0.008 * math.sin(2.0 * math.pi * 320.0 * t)
            values = [v1]
            units = "mV"
            q_score = 0.96
        elif self.modality == Modality.SEMG:
            channels = ["rectus_femoris", "vastus_medialis"]
            env = 0.085 * (math.sin(2.0 * math.pi * 0.8 * t) ** 2) + 0.015
            values = [env, env * 0.85]
            units = "uV"
            q_score = 0.94
        elif self.modality == Modality.RF:
            channels = ["s11_mag_db", "resonance_ghz", "phase_deg"]
            s11_mag = -18.42 + 0.3 * math.sin(2.0 * math.pi * 0.2 * t)
            res_ghz = 2.449 + 0.002 * math.cos(2.0 * math.pi * 0.2 * t)
            values = [s11_mag, res_ghz, -45.2]
            units = "dB,GHz,deg"
            q_score = 0.92
        else:
            channels = ["ch1"]
            values = [0.0]
            units = "arb"
            q_score = 1.0

        return SensorPacket(
            schema_version=SCHEMA_VERSION,
            subject_id=self.subject_id,
            session_id=self.session_id,
            sensor_id=f"SIM_{self.modality.value}_01",
            modality=self.modality,
            timestamp=TimestampInfo(device_time_ms=now_ms, sequence_number=self.seq),
            sampling=SamplingInfo(rate_hz=self.sampling_rate),
            channels=channels,
            values=values,
            units=units,
            device=DeviceInfo(status=self.status, firmware_version=self.firmware_version),
            quality=QualityInfo(status=SignalQualityStatus.PASS, score=q_score)
        )

    def get_status(self) -> DeviceStatus:
        return self.status

    def calibrate(self) -> Dict[str, Any]:
        return {"calibrated": True, "reference_seed": SIMULATOR_SEED, "timestamp": int(time.time() * 1000)}

    def get_metadata(self) -> Dict[str, Any]:
        return {
            "sensor_type": "DETERMINISTIC_SIMULATOR",
            "seed": SIMULATOR_SEED,
            "modality": self.modality.value,
            "rate_hz": self.sampling_rate
        }

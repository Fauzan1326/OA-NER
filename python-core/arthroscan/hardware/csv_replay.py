"""
CSV Replay Sensor Adapter for ARTHROSCAN-NER
Implements SensorAdapter to stream canonical SensorPacket v1.0 from research benchmark files.
"""

import os
import csv
import time
from typing import Dict, Any, Optional, List
from arthroscan.schemas.contract import (
    SensorPacket, Modality, DeviceStatus, SignalQualityStatus,
    TimestampInfo, SamplingInfo, DeviceInfo, QualityInfo, SchemaVersion
)
from arthroscan.hardware.adapter import SensorAdapter

class CSVReplaySensorAdapter(SensorAdapter):
    def __init__(
        self,
        modality: Modality,
        csv_file_path: str,
        sampling_rate_hz: Optional[float] = None,
        loop: bool = True
    ):
        self.modality = modality
        self.csv_file_path = csv_file_path
        self._status = DeviceStatus.DISCONNECTED
        self._loop = loop
        self._rows: List[Dict[str, str]] = []
        self._row_index = 0
        self._seq = 0
        self._channels: List[str] = []
        self._units: str = ""
        self._sampling_rate_hz = sampling_rate_hz or self._default_rate(modality)
        self._calibration_offset: List[float] = []

    def _default_rate(self, modality: Modality) -> float:
        if modality == Modality.IMU:
            return 100.0
        elif modality == Modality.VAG:
            return 2000.0
        elif modality == Modality.SEMG:
            return 1000.0
        elif modality == Modality.RF:
            return 10.0
        return 50.0

    def connect(self) -> bool:
        if not os.path.exists(self.csv_file_path):
            self._status = DeviceStatus.ERROR
            return False

        try:
            with open(self.csv_file_path, "r", encoding="utf-8") as f:
                reader = csv.reader(f)
                header = None
                self._rows = []
                for row in reader:
                    if not row or row[0].startswith("#"):
                        continue
                    if header is None:
                        header = [col.strip() for col in row]
                        continue
                    if len(row) == len(header):
                        row_dict = dict(zip(header, [col.strip() for col in row]))
                        self._rows.append(row_dict)

            self._determine_channels_and_units()
            self._row_index = 0
            self._status = DeviceStatus.CONNECTED
            return True
        except Exception:
            self._status = DeviceStatus.ERROR
            return False

    def _determine_channels_and_units(self):
        if self.modality == Modality.IMU:
            self._channels = ["acc_x", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z"]
            self._units = "g,rad/s"
        elif self.modality == Modality.VAG:
            self._channels = ["vag_ch1", "vag_ch2"]
            self._units = "mV"
        elif self.modality == Modality.SEMG:
            self._channels = ["semg_vm", "semg_vl", "semg_rf"]
            self._units = "mV"
        elif self.modality == Modality.RF:
            self._channels = ["s11_mag_db", "s11_phase_rad", "center_freq_mhz"]
            self._units = "dB,rad,MHz"
        else:
            self._channels = ["val_0"]
            self._units = "arb"
        self._calibration_offset = [0.0] * len(self._channels)

    def disconnect(self) -> bool:
        self.stop_stream()
        self._status = DeviceStatus.DISCONNECTED
        return True

    def start_stream(self) -> bool:
        if self._status != DeviceStatus.CONNECTED and self._status != DeviceStatus.STREAMING:
            if not self.connect():
                return False
        self._status = DeviceStatus.STREAMING
        return True

    def stop_stream(self) -> bool:
        if self._status == DeviceStatus.STREAMING:
            self._status = DeviceStatus.CONNECTED
        return True

    def read_sample(self) -> Optional[SensorPacket]:
        if self._status != DeviceStatus.STREAMING or not self._rows:
            return None

        if self._row_index >= len(self._rows):
            if self._loop:
                self._row_index = 0
            else:
                self._status = DeviceStatus.CONNECTED
                return None

        row = self._rows[self._row_index]
        self._row_index += 1
        self._seq += 1

        device_time_ms = int(row.get("device_time_ms", int(time.time() * 1000)))
        subject_id = row.get("subject_id", "SUBJ_CSV_REPLAY")
        session_id = row.get("session_id", "SESS_CSV_REPLAY")

        values: List[float] = []
        for i, ch in enumerate(self._channels):
            raw_val = float(row.get(ch, 0.0))
            offset = self._calibration_offset[i] if i < len(self._calibration_offset) else 0.0
            values.append(raw_val - offset)

        return SensorPacket(
            schema_version=SchemaVersion.CURRENT,
            subject_id=subject_id,
            session_id=session_id,
            sensor_id=f"CSV_{self.modality.name}_01",
            modality=self.modality,
            timestamp=TimestampInfo(
                device_time_ms=device_time_ms,
                sequence_number=self._seq
            ),
            sampling=SamplingInfo(
                rate_hz=self._sampling_rate_hz
            ),
            channels=self._channels,
            values=values,
            units=self._units,
            device=DeviceInfo(
                status=self._status,
                firmware_version="csv-replay-1.0"
            ),
            quality=QualityInfo(
                status=SignalQualityStatus.PASS,
                score=0.99
            )
        )

    def get_status(self) -> DeviceStatus:
        return self._status

    def calibrate(self) -> Dict[str, Any]:
        """Calculates zero-point calibration offsets from the first batch of rows."""
        if not self._rows:
            return {"status": "FAILED", "reason": "No data available for calibration"}
        
        sample_count = min(10, len(self._rows))
        sums = [0.0] * len(self._channels)
        for i in range(sample_count):
            row = self._rows[i]
            for c_idx, ch in enumerate(self._channels):
                sums[c_idx] += float(row.get(ch, 0.0))
        
        self._calibration_offset = [s / sample_count for s in sums]
        return {
            "status": "CALIBRATED",
            "channels": self._channels,
            "offsets": self._calibration_offset,
            "sample_count": sample_count
        }

    def get_metadata(self) -> Dict[str, Any]:
        return {
            "sensor_type": "CSV_REPLAY",
            "file_path": self.csv_file_path,
            "modality": self.modality.name,
            "rate_hz": self._sampling_rate_hz,
            "total_rows": len(self._rows),
            "loop": self._loop
        }

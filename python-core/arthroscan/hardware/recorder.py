"""
Session Recording and Replay Engine for ARTHROSCAN-NER
Records validated streaming SensorPacket streams to replay bundles and replays them.
"""

import json
import os
import time
from typing import List, Optional
from arthroscan.schemas.contract import (
    SensorPacket, Modality, DeviceStatus, SignalQualityStatus,
    TimestampInfo, SamplingInfo, DeviceInfo, QualityInfo, SchemaVersion
)

class SessionRecorder:
    def __init__(self, output_dir: str = "recordings"):
        self.output_dir = output_dir
        self._current_file: Optional[str] = None
        self._file_handle = None
        self._recorded_count = 0
        os.makedirs(self.output_dir, exist_ok=True)

    def start_recording(self, session_id: str) -> str:
        self.stop_recording()
        filename = f"session_{session_id}_{int(time.time())}.jsonl"
        self._current_file = os.path.join(self.output_dir, filename)
        self._file_handle = open(self._current_file, "w", encoding="utf-8")
        self._recorded_count = 0
        return self._current_file

    def record_packet(self, packet: SensorPacket):
        if self._file_handle is not None and not self._file_handle.closed:
            data = {
                "schema_version": packet.schema_version,
                "subject_id": packet.subject_id,
                "session_id": packet.session_id,
                "sensor_id": packet.sensor_id,
                "modality": packet.modality.name,
                "device_time_ms": packet.timestamp.device_time_ms,
                "sequence_number": packet.timestamp.sequence_number,
                "rate_hz": packet.sampling.rate_hz,
                "channels": packet.channels,
                "values": packet.values,
                "units": packet.units,
                "device_status": packet.device.status.name,
                "firmware_version": packet.device.firmware_version,
                "quality_status": packet.quality.status.name,
                "quality_score": packet.quality.score
            }
            self._file_handle.write(json.dumps(data) + "\n")
            self._recorded_count += 1

    def stop_recording(self) -> int:
        count = self._recorded_count
        if self._file_handle is not None and not self._file_handle.closed:
            self._file_handle.flush()
            self._file_handle.close()
            self._file_handle = None
        self._current_file = None
        self._recorded_count = 0
        return count

class SessionReplayReader:
    @classmethod
    def read_session_file(cls, filepath: str) -> List[SensorPacket]:
        packets: List[SensorPacket] = []
        if not os.path.exists(filepath):
            return packets

        with open(filepath, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                d = json.loads(line)
                pkt = SensorPacket(
                    schema_version=d["schema_version"],
                    subject_id=d["subject_id"],
                    session_id=d["session_id"],
                    sensor_id=d["sensor_id"],
                    modality=Modality[d["modality"]],
                    timestamp=TimestampInfo(
                        device_time_ms=d["device_time_ms"],
                        sequence_number=d["sequence_number"]
                    ),
                    sampling=SamplingInfo(
                        rate_hz=d["rate_hz"]
                    ),
                    channels=d["channels"],
                    values=d["values"],
                    units=d["units"],
                    device=DeviceInfo(
                        status=DeviceStatus[d["device_status"]],
                        firmware_version=d["firmware_version"]
                    ),
                    quality=QualityInfo(
                        status=SignalQualityStatus[d["quality_status"]],
                        score=d["quality_score"]
                    )
                )
                packets.append(pkt)
        return packets

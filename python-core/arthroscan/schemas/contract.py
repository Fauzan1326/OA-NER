from enum import Enum
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field
import math

SCHEMA_VERSION = "1.0"

class SchemaVersion:
    CURRENT = SCHEMA_VERSION

class Modality(str, Enum):
    IMU = "IMU"
    VAG = "VAG"
    SEMG = "sEMG"
    RF = "RF"
    CONTEXT = "CONTEXT"

class DeviceStatus(str, Enum):
    DISCOVERING = "DISCOVERING"
    CONNECTING = "CONNECTING"
    CONNECTED = "CONNECTED"
    CALIBRATING = "CALIBRATING"
    STREAMING = "STREAMING"
    PAUSED = "PAUSED"
    DISCONNECTED = "DISCONNECTED"
    ERROR = "ERROR"
    RECONNECTING = "RECONNECTING"

class SignalQualityStatus(str, Enum):
    PASS = "PASS"
    WARNING = "WARNING"
    FAIL = "FAIL"

@dataclass
class TimestampInfo:
    device_time_ms: int
    sequence_number: int

@dataclass
class SamplingInfo:
    rate_hz: float

@dataclass
class DeviceInfo:
    status: DeviceStatus
    firmware_version: str = "sim-1.0"

@dataclass
class QualityInfo:
    status: SignalQualityStatus
    score: float

@dataclass
class SensorPacket:
    schema_version: str
    subject_id: str
    session_id: str
    sensor_id: str
    modality: Modality
    timestamp: TimestampInfo
    sampling: SamplingInfo
    channels: List[str]
    values: List[float]
    units: str
    device: DeviceInfo
    quality: QualityInfo

    def is_valid(self) -> bool:
        if self.schema_version != SCHEMA_VERSION:
            return False
        if not self.subject_id or not self.session_id or not self.sensor_id:
            return False
        if not self.channels or not self.values or len(self.channels) != len(self.values):
            return False
        if self.sampling.rate_hz <= 0.0:
            return False
        for v in self.values:
            if math.isnan(v) or math.isinf(v):
                return False
        return True

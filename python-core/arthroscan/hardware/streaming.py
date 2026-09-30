"""
Real-Time Streaming Engine for ARTHROSCAN-NER
Implements bounded queues, circular buffers, packet sequence tracking,
loss detection, and multi-modality timestamp synchronization.
"""

import time
import collections
from typing import Dict, Any, Optional, List, Deque
from dataclasses import dataclass, field
from arthroscan.schemas.contract import SensorPacket, Modality, DeviceStatus

@dataclass
class StreamHealthTelemetry:
    modality: Modality
    total_packets_received: int = 0
    total_packets_dropped: int = 0
    sequence_gaps_detected: int = 0
    duplicates_detected: int = 0
    out_of_order_detected: int = 0
    buffer_utilization_percent: float = 0.0
    packet_loss_rate_percent: float = 0.0
    average_jitter_ms: float = 0.0
    last_sequence_number: int = -1
    last_device_time_ms: int = -1

class PacketSequenceTracker:
    def __init__(self, modality: Modality):
        self.modality = modality
        self.telemetry = StreamHealthTelemetry(modality=modality)
        self._expected_interval_ms = 10.0
        self._jitter_history: Deque[float] = collections.deque(maxlen=50)

    def set_sampling_rate(self, rate_hz: float):
        if rate_hz > 0:
            self._expected_interval_ms = 1000.0 / rate_hz

    def track_packet(self, packet: SensorPacket):
        self.telemetry.total_packets_received += 1
        seq = packet.timestamp.sequence_number
        dev_time = packet.timestamp.device_time_ms

        if self.telemetry.last_sequence_number >= 0:
            seq_diff = seq - self.telemetry.last_sequence_number
            if seq_diff == 0:
                self.telemetry.duplicates_detected += 1
            elif seq_diff < 0:
                self.telemetry.out_of_order_detected += 1
            elif seq_diff > 1:
                gaps = seq_diff - 1
                self.telemetry.sequence_gaps_detected += gaps

            if self.telemetry.last_device_time_ms > 0:
                actual_interval = dev_time - self.telemetry.last_device_time_ms
                jitter = abs(actual_interval - self._expected_interval_ms)
                self._jitter_history.append(jitter)
                if self._jitter_history:
                    self.telemetry.average_jitter_ms = sum(self._jitter_history) / len(self._jitter_history)

        self.telemetry.last_sequence_number = seq
        self.telemetry.last_device_time_ms = dev_time

        # Compute packet loss percentage
        total_expected = self.telemetry.total_packets_received + self.telemetry.sequence_gaps_detected
        if total_expected > 0:
            self.telemetry.packet_loss_rate_percent = (
                self.telemetry.sequence_gaps_detected / total_expected
            ) * 100.0

class CircularBuffer:
    """Fixed-capacity thread-safe circular ring buffer for fast window extraction."""
    def __init__(self, capacity: int):
        self.capacity = max(1, capacity)
        self._buffer: Deque[SensorPacket] = collections.deque(maxlen=self.capacity)

    def append(self, packet: SensorPacket):
        self._buffer.append(packet)

    def get_window(self, size: int) -> List[SensorPacket]:
        if size <= 0:
            return []
        items = list(self._buffer)
        return items[-size:] if len(items) >= size else items

    def get_all(self) -> List[SensorPacket]:
        return list(self._buffer)

    def size(self) -> int:
        return len(self._buffer)

    def utilization_percent(self) -> float:
        return (len(self._buffer) / self.capacity) * 100.0

    def clear(self):
        self._buffer.clear()

class TimestampSynchronizer:
    """
    Synchronizes heterogeneous rate streams (IMU 100Hz, VAG 2000Hz, sEMG 1000Hz, RF 10Hz)
    into aligned time frames.
    """
    def __init__(self, epoch_window_ms: int = 100):
        self.epoch_window_ms = epoch_window_ms
        self._epoch_buckets: Dict[int, Dict[Modality, List[SensorPacket]]] = {}

    def ingest(self, packet: SensorPacket):
        epoch = (packet.timestamp.device_time_ms // self.epoch_window_ms) * self.epoch_window_ms
        if epoch not in self._epoch_buckets:
            self._epoch_buckets[epoch] = {m: [] for m in Modality}
        self._epoch_buckets[epoch][packet.modality].append(packet)

        # Retain only the most recent 20 epochs in memory
        if len(self._epoch_buckets) > 20:
            oldest_epoch = min(self._epoch_buckets.keys())
            del self._epoch_buckets[oldest_epoch]

    def get_synchronized_frame(self, epoch: int) -> Optional[Dict[Modality, List[SensorPacket]]]:
        return self._epoch_buckets.get(epoch)

    def get_latest_completed_epoch(self) -> Optional[int]:
        if not self._epoch_buckets:
            return None
        sorted_epochs = sorted(self._epoch_buckets.keys())
        if len(sorted_epochs) > 1:
            return sorted_epochs[-2]
        return None

class StreamIngestionEngine:
    """
    Central real-time ingestion coordinator:
    Sensor -> Adapter -> Boundary Validation -> Bounded Queue -> Circular Buffer -> Telemetry
    """
    def __init__(self, buffer_capacity: int = 1000, queue_max_size: int = 500):
        self.buffer_capacity = buffer_capacity
        self.queue_max_size = queue_max_size
        self.buffers: Dict[Modality, CircularBuffer] = {
            m: CircularBuffer(buffer_capacity) for m in Modality
        }
        self.trackers: Dict[Modality, PacketSequenceTracker] = {
            m: PacketSequenceTracker(m) for m in Modality
        }
        self.synchronizer = TimestampSynchronizer(epoch_window_ms=100)
        self.total_dropped = 0

    def ingest_packet(self, packet: SensorPacket) -> bool:
        # Step 1: Boundary validation
        if not packet.is_valid():
            self.total_dropped += 1
            self.trackers[packet.modality].telemetry.total_packets_dropped += 1
            return False

        # Step 2: Sequence and loss tracking
        tracker = self.trackers[packet.modality]
        tracker.track_packet(packet)

        # Step 3: Bounded circular buffer insertion
        buf = self.buffers[packet.modality]
        buf.append(packet)
        tracker.telemetry.buffer_utilization_percent = buf.utilization_percent()

        # Step 4: Timestamp synchronization
        self.synchronizer.ingest(packet)

        return True

    def get_telemetry(self, modality: Modality) -> StreamHealthTelemetry:
        return self.trackers[modality].telemetry

    def get_all_telemetry(self) -> Dict[Modality, StreamHealthTelemetry]:
        return {m: self.trackers[m].telemetry for m in Modality}

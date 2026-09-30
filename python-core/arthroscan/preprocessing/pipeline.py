"""
Unified Signal Preprocessing & Quality Pipeline for ARTHROSCAN-NER (Module 3).
Orchestrates digital filtering, envelope computation, artifact detection,
and quality gate scoring across all incoming SensorPacket streams.
"""

from typing import Dict, List, Any, Optional
from dataclasses import dataclass
from arthroscan.schemas.contract import SensorPacket, Modality, SignalQualityStatus
from arthroscan.preprocessing.modality_preprocessors import (
    ImuPreprocessor, VagPreprocessor, SemgPreprocessor, RfPreprocessor
)
from arthroscan.preprocessing.quality_gate import SignalQualityGate, QualityAssessment, ArtifactType

@dataclass
class PreprocessedFrame:
    modality: Modality
    cleaned_values: List[float]
    derived_metrics: Dict[str, float]
    quality: QualityAssessment

class PreprocessingPipeline:
    def __init__(self):
        self.imu_prep = ImuPreprocessor()
        self.vag_prep = VagPreprocessor()
        self.semg_prep = SemgPreprocessor()
        self.rf_prep = RfPreprocessor()
        self.quality_gate = SignalQualityGate()

        # Sliding window histories for artifact detection
        self.history_imu: List[List[float]] = []
        self.history_vag: List[float] = []
        self.history_semg: List[List[float]] = []
        self.history_rf: List[List[float]] = []
        self.window_max_len = 50

    def process_packet(self, packet: SensorPacket) -> PreprocessedFrame:
        mod = packet.modality
        vals = packet.values

        if mod == Modality.IMU:
            cleaned, acc_mag, gyro_mag = self.imu_prep.process_sample(vals)
            self.history_imu.append(cleaned)
            if len(self.history_imu) > self.window_max_len:
                self.history_imu.pop(0)
            qa = self.quality_gate.assess_imu(self.history_imu)
            return PreprocessedFrame(
                modality=mod,
                cleaned_values=cleaned,
                derived_metrics={"acc_magnitude": acc_mag, "gyro_magnitude": gyro_mag},
                quality=qa
            )

        elif mod == Modality.VAG:
            cleaned, envelope = self.vag_prep.process_sample(vals)
            self.history_vag.append(cleaned[0])
            if len(self.history_vag) > self.window_max_len:
                self.history_vag.pop(0)
            qa = self.quality_gate.assess_vag(self.history_vag)
            return PreprocessedFrame(
                modality=mod,
                cleaned_values=cleaned,
                derived_metrics={"acoustic_envelope": envelope, "acoustic_power": cleaned[0]**2},
                quality=qa
            )

        elif mod == Modality.SEMG:
            cleaned, envelopes = self.semg_prep.process_sample(vals)
            self.history_semg.append(cleaned)
            if len(self.history_semg) > self.window_max_len:
                self.history_semg.pop(0)
            qa = self.quality_gate.assess_semg(self.history_semg)
            rms_total = sum(envelopes) / max(1, len(envelopes))
            return PreprocessedFrame(
                modality=mod,
                cleaned_values=cleaned,
                derived_metrics={"mean_rms_envelope": rms_total},
                quality=qa
            )

        elif mod == Modality.RF:
            cleaned, shift_mhz = self.rf_prep.process_sample(vals)
            self.history_rf.append(cleaned)
            if len(self.history_rf) > self.window_max_len:
                self.history_rf.pop(0)
            qa = self.quality_gate.assess_rf(self.history_rf)
            return PreprocessedFrame(
                modality=mod,
                cleaned_values=cleaned,
                derived_metrics={"freq_shift_mhz": shift_mhz},
                quality=qa
            )

        else:
            qa = QualityAssessment(mod, 1.0, SignalQualityStatus.PASS, [ArtifactType.NONE])
            return PreprocessedFrame(modality=mod, cleaned_values=vals, derived_metrics={}, quality=qa)

    def reset(self):
        self.imu_prep.reset()
        self.vag_prep.reset()
        self.semg_prep.reset()
        self.rf_prep.reset()
        self.history_imu.clear()
        self.history_vag.clear()
        self.history_semg.clear()
        self.history_rf.clear()

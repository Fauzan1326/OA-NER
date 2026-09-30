"""
Signal Quality Index (SQI) and Artifact Detection Engine for ARTHROSCAN-NER.
Implements multi-criteria quality scoring and artifact classification:
- Saturation / Clipping
- Flatline / Sensor Dropout
- Motion / Mechanical Spikes
- Electrode Liftoff / High Impedance
- RF Detuning / Mismatch
"""

from enum import Enum
from typing import List, Dict, Tuple, Optional
from dataclasses import dataclass
from arthroscan.schemas.contract import Modality, SignalQualityStatus

class ArtifactType(str, Enum):
    NONE = "NONE"
    CLIPPING_SATURATION = "CLIPPING_SATURATION"
    FLATLINE_DROPOUT = "FLATLINE_DROPOUT"
    MOTION_SPIKE = "MOTION_SPIKE"
    ELECTRODE_LIFTOFF = "ELECTRODE_LIFTOFF"
    RF_MISMATCH = "RF_MISMATCH"

@dataclass
class QualityAssessment:
    modality: Modality
    sqi_score: float  # 0.0 to 1.0
    status: SignalQualityStatus
    detected_artifacts: List[ArtifactType]
    rejection_reason: Optional[str] = None

class SignalQualityGate:
    """
    Evaluates sensor signal windows for artifacts and computes Signal Quality Index (SQI).
    Thresholds:
    - PASS: SQI >= 0.70
    - WARNING: 0.50 <= SQI < 0.70
    - FAIL: SQI < 0.50
    """
    def __init__(self):
        self.pass_threshold = 0.70
        self.warning_threshold = 0.50

    def assess_imu(self, window_acc: List[List[float]]) -> QualityAssessment:
        artifacts = []
        if not window_acc:
            return QualityAssessment(Modality.IMU, 0.0, SignalQualityStatus.FAIL, [ArtifactType.FLATLINE_DROPOUT], "Empty window")

        # Check for clipping (exceeding 16g dynamic range)
        has_clipping = any(any(abs(v) > 15.8 for v in sample[:3]) for sample in window_acc)
        if has_clipping:
            artifacts.append(ArtifactType.CLIPPING_SATURATION)

        # Check for motion spikes (acc magnitude > 4.0g)
        has_spike = any(math_mag(sample[:3]) > 4.0 for sample in window_acc)
        if has_spike:
            artifacts.append(ArtifactType.MOTION_SPIKE)

        # Check for flatline (variance < 1e-5)
        variances = [variance([s[i] for s in window_acc]) for i in range(min(3, len(window_acc[0])))]
        if all(v < 1e-6 for v in variances):
            artifacts.append(ArtifactType.FLATLINE_DROPOUT)

        sqi = 1.0
        if ArtifactType.CLIPPING_SATURATION in artifacts:
            sqi -= 0.40
        if ArtifactType.MOTION_SPIKE in artifacts:
            sqi -= 0.25
        if ArtifactType.FLATLINE_DROPOUT in artifacts:
            sqi -= 0.60
        sqi = max(0.0, min(1.0, sqi))

        status = self._get_status(sqi)
        reason = artifacts[0].value if artifacts else None
        return QualityAssessment(Modality.IMU, sqi, status, artifacts, reason)

    def assess_vag(self, window_vag: List[float]) -> QualityAssessment:
        artifacts = []
        if not window_vag:
            return QualityAssessment(Modality.VAG, 0.0, SignalQualityStatus.FAIL, [ArtifactType.FLATLINE_DROPOUT], "Empty window")

        # Saturation check (> 2.5 mV)
        if any(abs(v) > 2.45 for v in window_vag):
            artifacts.append(ArtifactType.CLIPPING_SATURATION)

        # Flatline check (stddev < 1e-4)
        var = variance(window_vag)
        if var < 1e-7:
            artifacts.append(ArtifactType.FLATLINE_DROPOUT)

        sqi = 1.0
        if ArtifactType.CLIPPING_SATURATION in artifacts:
            sqi -= 0.45
        if ArtifactType.FLATLINE_DROPOUT in artifacts:
            sqi -= 0.60
        sqi = max(0.0, min(1.0, sqi))

        status = self._get_status(sqi)
        reason = artifacts[0].value if artifacts else None
        return QualityAssessment(Modality.VAG, sqi, status, artifacts, reason)

    def assess_semg(self, window_semg: List[List[float]]) -> QualityAssessment:
        artifacts = []
        if not window_semg:
            return QualityAssessment(Modality.SEMG, 0.0, SignalQualityStatus.FAIL, [ArtifactType.FLATLINE_DROPOUT], "Empty window")

        # Electrode liftoff: huge amplitude spike (> 4.8 mV) or flatline
        has_liftoff = any(any(abs(v) > 4.5 for v in sample) for sample in window_semg)
        if has_liftoff:
            artifacts.append(ArtifactType.ELECTRODE_LIFTOFF)

        var = variance([s[0] for s in window_semg])
        if var < 1e-6:
            artifacts.append(ArtifactType.FLATLINE_DROPOUT)

        sqi = 1.0
        if ArtifactType.ELECTRODE_LIFTOFF in artifacts:
            sqi -= 0.50
        if ArtifactType.FLATLINE_DROPOUT in artifacts:
            sqi -= 0.50
        sqi = max(0.0, min(1.0, sqi))

        status = self._get_status(sqi)
        reason = artifacts[0].value if artifacts else None
        return QualityAssessment(Modality.SEMG, sqi, status, artifacts, reason)

    def assess_rf(self, window_rf: List[List[float]]) -> QualityAssessment:
        artifacts = []
        if not window_rf:
            return QualityAssessment(Modality.RF, 0.0, SignalQualityStatus.FAIL, [ArtifactType.FLATLINE_DROPOUT], "Empty window")

        # RF Mismatch check: S11 > -3 dB means poor matching / antenna liftoff
        s11_values = [s[0] for s in window_rf]
        if any(v > -3.0 for v in s11_values):
            artifacts.append(ArtifactType.RF_MISMATCH)

        var = variance(s11_values)
        if var < 1e-7:
            artifacts.append(ArtifactType.FLATLINE_DROPOUT)

        sqi = 1.0
        if ArtifactType.RF_MISMATCH in artifacts:
            sqi -= 0.40
        if ArtifactType.FLATLINE_DROPOUT in artifacts:
            sqi -= 0.50
        sqi = max(0.0, min(1.0, sqi))

        status = self._get_status(sqi)
        reason = artifacts[0].value if artifacts else None
        return QualityAssessment(Modality.RF, sqi, status, artifacts, reason)

    def _get_status(self, sqi: float) -> SignalQualityStatus:
        if sqi >= self.pass_threshold:
            return SignalQualityStatus.PASS
        elif sqi >= self.warning_threshold:
            return SignalQualityStatus.WARNING
        else:
            return SignalQualityStatus.FAIL

def math_mag(vec: List[float]) -> float:
    return (sum(x**2 for x in vec)) ** 0.5

def variance(data: List[float]) -> float:
    if len(data) < 2:
        return 0.0
    mean = sum(data) / len(data)
    return sum((x - mean) ** 2 for x in data) / len(data)

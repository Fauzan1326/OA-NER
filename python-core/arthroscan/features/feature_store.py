"""
Feature Store v1 for ARTHROSCAN-NER (Module 4).
Manages validated, traceable multi-modal feature vectors, windowed queries,
and normalization parameters for downstream AI models (Module 5).
"""

import threading
import math
from typing import List, Dict, Optional, Any
from arthroscan.schemas.contract import Modality, SignalQualityStatus
from arthroscan.features.feature_schema import ModalityFeatureVector

class FeatureStoreValidationError(Exception):
    pass

class FeatureStore:
    """
    In-memory, thread-safe Feature Store v1.
    Maintains chronological feature vectors indexed by modality, subject, and session.
    """
    def __init__(self, reject_quality_failures: bool = False):
        self._lock = threading.Lock()
        self._records: List[ModalityFeatureVector] = []
        self.reject_quality_failures = reject_quality_failures

    def insert(self, vector: ModalityFeatureVector) -> bool:
        """
        Validates and inserts a feature vector.
        Rejects NaNs, Infs, or optionally quality FAILs.
        """
        self._validate(vector)
        if self.reject_quality_failures and vector.provenance.quality_status == SignalQualityStatus.FAIL:
            return False

        with self._lock:
            self._records.append(vector)
        return True

    def _validate(self, vector: ModalityFeatureVector) -> None:
        if not vector.provenance.source_sensor_id:
            raise FeatureStoreValidationError("Missing source_sensor_id in feature provenance.")
        
        for name, item in vector.features.items():
            if math.isnan(item.value) or math.isinf(item.value):
                raise FeatureStoreValidationError(f"Invalid non-finite feature value for '{name}': {item.value}")

    def query(
        self,
        modality: Optional[Modality] = None,
        subject_id: Optional[str] = None,
        session_id: Optional[str] = None,
        start_ms: Optional[int] = None,
        end_ms: Optional[int] = None
    ) -> List[ModalityFeatureVector]:
        with self._lock:
            results = self._records[:]

        if modality is not None:
            results = [r for r in results if r.provenance.modality == modality]
        if subject_id is not None:
            results = [r for r in results if r.provenance.subject_id == subject_id]
        if session_id is not None:
            results = [r for r in results if r.provenance.session_id == session_id]
        if start_ms is not None:
            results = [r for r in results if r.provenance.window_start_ms >= start_ms]
        if end_ms is not None:
            results = [r for r in results if r.provenance.window_end_ms <= end_ms]

        return results

    def get_latest(self, modality: Modality) -> Optional[ModalityFeatureVector]:
        with self._lock:
            for r in reversed(self._records):
                if r.provenance.modality == modality:
                    return r
        return None

    def count(self, modality: Optional[Modality] = None) -> int:
        with self._lock:
            if modality is None:
                return len(self._records)
            return sum(1 for r in self._records if r.provenance.modality == modality)

    def compute_normalization_stats(self, modality: Modality) -> Dict[str, Dict[str, float]]:
        """
        Computes mean and standard deviation for all features of a given modality
        to calibrate downstream z-score scalers.
        """
        vectors = self.query(modality=modality)
        if not vectors:
            return {}

        feature_values: Dict[str, List[float]] = {}
        for vec in vectors:
            for name, item in vec.features.items():
                if name not in feature_values:
                    feature_values[name] = []
                feature_values[name].append(item.value)

        stats: Dict[str, Dict[str, float]] = {}
        for name, vals in feature_values.items():
            m = sum(vals) / len(vals)
            variance = sum((x - m) ** 2 for x in vals) / len(vals)
            s = math.sqrt(variance)
            stats[name] = {"mean": m, "std": s if s > 1e-9 else 1.0}
        return stats

    def clear(self) -> None:
        with self._lock:
            self._records.clear()

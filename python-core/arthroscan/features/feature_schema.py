"""
Canonical Feature Schema and Provenance Contracts for ARTHROSCAN-NER (Module 4).
Ensures end-to-end traceability from raw sensor timestamps and quality tags
to downstream clinical decision models.
"""

from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, List, Optional, Any
from arthroscan.schemas.contract import Modality, SignalQualityStatus
from arthroscan.preprocessing.quality_gate import ArtifactType

FEATURE_SCHEMA_VERSION = "1.0"

class FeatureDomain(str, Enum):
    STATISTICAL = "STATISTICAL"
    TEMPORAL = "TEMPORAL"
    SPECTRAL = "SPECTRAL"
    EXPERIMENTAL_RF = "EXPERIMENTAL_RF"

@dataclass
class FeatureItem:
    name: str
    value: float
    unit: str
    domain: FeatureDomain
    description: str

@dataclass
class FeatureProvenance:
    source_sensor_id: str
    subject_id: str
    session_id: str
    modality: Modality
    window_start_ms: int
    window_end_ms: int
    sample_count: int
    sampling_rate_hz: float
    preprocessing_version: str = "1.0"
    feature_schema_version: str = FEATURE_SCHEMA_VERSION
    quality_status: SignalQualityStatus = SignalQualityStatus.PASS
    quality_score: float = 1.0
    artifact_flags: List[ArtifactType] = field(default_factory=list)

@dataclass
class ModalityFeatureVector:
    provenance: FeatureProvenance
    features: Dict[str, FeatureItem]

    def to_flat_dict(self) -> Dict[str, float]:
        return {name: item.value for name, item in self.features.items()}

    def get_feature(self, name: str) -> Optional[float]:
        item = self.features.get(name)
        return item.value if item else None

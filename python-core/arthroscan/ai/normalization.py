"""
Module 5: Immutable Normalization Metadata & Defensive Pre-Inference Scaler.
Enforces fixed, versioned training-time normalization parameters to guarantee
zero runtime distribution drift.
"""

from dataclasses import dataclass, field
from typing import Dict, List, Optional
import math
from arthroscan.schemas.contract import Modality
from arthroscan.features.feature_schema import ModalityFeatureVector

class NormalizationValidationError(Exception):
    pass

@dataclass(frozen=True)
class NormalizationMetadata:
    feature_schema_version: str
    preprocessing_version: str
    scaler_type: str  # e.g., "Z_SCORE"
    modality: Modality
    feature_names: List[str]
    means: Dict[str, float]
    standard_deviations: Dict[str, float]
    training_dataset_id: str
    artifact_hash: str

class ImmutableFeatureScaler:
    """
    Applies fixed, frozen training normalization parameters to incoming FeatureVectors.
    Strictly validates:
      1. Feature schema version compatibility.
      2. Absence of missing required features (no silent zero-filling).
      3. Finite values (no NaN or Inf).
    """
    def __init__(self, metadata: NormalizationMetadata):
        self.metadata = metadata

    def transform(self, vector: ModalityFeatureVector) -> List[float]:
        # 1. Validate modality and schema version
        if vector.provenance.modality != self.metadata.modality:
            raise NormalizationValidationError(
                f"Modality mismatch: Expected {self.metadata.modality}, got {vector.provenance.modality}"
            )
        if vector.provenance.feature_schema_version != self.metadata.feature_schema_version:
            raise NormalizationValidationError(
                f"Feature schema mismatch: Expected {self.metadata.feature_schema_version}, "
                f"got {vector.provenance.feature_schema_version}"
            )

        # 2. Check all required features are present (no silent substitution)
        normalized_values: List[float] = []
        for name in self.metadata.feature_names:
            if name not in vector.features:
                raise NormalizationValidationError(
                    f"Missing required feature '{name}' for modality {self.metadata.modality}. "
                    f"Refusing silent zero-substitution."
                )
            
            val = vector.features[name].value
            if math.isnan(val) or math.isinf(val):
                raise NormalizationValidationError(
                    f"Invalid non-finite feature value for '{name}': {val}"
                )

            mean = self.metadata.means[name]
            std = self.metadata.standard_deviations[name]
            if std <= 1e-9:
                std = 1.0  # Avoid division by zero

            z_score = (val - mean) / std
            normalized_values.append(z_score)

        return normalized_values

"""
Module 5: Modality Model Runner, Model Registry Contracts, and Prediction Output Schema.
Defines execution wrappers and strict boundaries for modality-specific AI models.
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from enum import Enum
from typing import Dict, List, Optional
import time
import hashlib

from arthroscan.schemas.contract import Modality, SignalQualityStatus
from arthroscan.features.feature_schema import ModalityFeatureVector, FeatureProvenance
from arthroscan.ai.normalization import NormalizationMetadata, ImmutableFeatureScaler, NormalizationValidationError

class ModelExecutionStatus(str, Enum):
    SUCCESS = "SUCCESS"
    REJECTED_QUALITY = "REJECTED_QUALITY"
    SCHEMA_MISMATCH = "SCHEMA_MISMATCH"
    INSUFFICIENT_EVIDENCE = "INSUFFICIENT_EVIDENCE"
    ERROR = "ERROR"

@dataclass(frozen=True)
class ModelMetadata:
    model_id: str
    model_version: str
    modality: Modality
    feature_schema_version: str
    preprocessing_version: str
    input_schema_version: str
    output_schema_version: str
    training_dataset_id: str
    artifact_hash: str
    runtime_version: str
    is_experimental: bool = False

@dataclass
class ModalityPrediction:
    model_metadata: ModelMetadata
    provenance: FeatureProvenance
    status: ModelExecutionStatus
    score: Optional[float]  # Raw calibrated model score / probability [0.0 - 1.0] if successful
    prediction_label: Optional[str]  # e.g., "MILD_RISK", "ELEVATED_RISK", "NORMAL"
    calibrated_uncertainty: Optional[float]  # Estimated uncertainty or variance
    status_reason: Optional[str]
    inference_time_ms: float
    raw_output: Dict[str, float] = field(default_factory=dict)

    # Architectural Guarantee: No manufactured clinical metrics
    # AUC, sensitivity, specificity, etc., must NOT be fabricated here.

class ModalityModelRunner(ABC):
    """
    Abstract Model Runner contract adhering to lifecycle:
    load -> validate -> predict -> get_metadata -> unload
    """
    @abstractmethod
    def load(self, model_path_or_bytes: bytes) -> bool:
        pass

    @abstractmethod
    def validate_input(self, vector: ModalityFeatureVector) -> None:
        """Raises NormalizationValidationError or ValueError on failure"""
        pass

    @abstractmethod
    def predict(self, vector: ModalityFeatureVector) -> ModalityPrediction:
        pass

    @abstractmethod
    def get_metadata(self) -> ModelMetadata:
        pass

    @abstractmethod
    def unload(self) -> None:
        pass

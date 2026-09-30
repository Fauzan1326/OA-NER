"""
ARTHROSCAN-NER Module 4: Multimodal Feature Extraction & Feature Store.
"""

from arthroscan.features.feature_schema import (
    FEATURE_SCHEMA_VERSION,
    FeatureDomain,
    FeatureItem,
    FeatureProvenance,
    ModalityFeatureVector
)
from arthroscan.features.extractors import (
    ImuFeatureExtractor,
    VagFeatureExtractor,
    SemgFeatureExtractor,
    RfFeatureExtractor
)
from arthroscan.features.feature_store import (
    FeatureStore,
    FeatureStoreValidationError
)

__all__ = [
    "FEATURE_SCHEMA_VERSION",
    "FeatureDomain",
    "FeatureItem",
    "FeatureProvenance",
    "ModalityFeatureVector",
    "ImuFeatureExtractor",
    "VagFeatureExtractor",
    "SemgFeatureExtractor",
    "RfFeatureExtractor",
    "FeatureStore",
    "FeatureStoreValidationError"
]

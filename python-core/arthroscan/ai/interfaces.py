from abc import ABC, abstractmethod
from typing import Dict, Any, List
from dataclasses import dataclass

@dataclass
class ModelMetadata:
    model_name: str
    model_version: str
    feature_schema_version: str
    preprocessing_version: str
    runtime_engine: str  # e.g., ONNX, TFLite
    status: str  # DEVELOPMENT, VALIDATION, DEMO, RESEARCH, RETIRED

class ModalityModelRunner(ABC):
    @abstractmethod
    def run_inference(self, feature_vector: Dict[str, float]) -> Dict[str, Any]:
        pass

class MultimodalFusionEngine(ABC):
    @abstractmethod
    def fuse(self, modality_outputs: Dict[str, Dict[str, Any]]) -> Dict[str, Any]:
        pass

class UncertaintyEvaluator(ABC):
    @abstractmethod
    def evaluate(self, fusion_result: Dict[str, Any], quality_scores: Dict[str, float]) -> float:
        pass

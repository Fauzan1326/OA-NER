"""
Module 5: AI Model Registry and Inference Orchestrator.
Manages lifecycle of modality runners, enforces version boundaries, and orchestrates execution.
"""

from typing import Dict, List, Optional
from arthroscan.schemas.contract import Modality
from arthroscan.features.feature_schema import ModalityFeatureVector
from arthroscan.ai.model_runner import ModalityModelRunner, ModelMetadata, ModalityPrediction, ModelExecutionStatus
from arthroscan.ai.modality_runners import (
    ImuModalityModelRunner,
    VagModalityModelRunner,
    SemgModalityModelRunner,
    RfExperimentalModelRunner
)

class ModelRegistry:
    """
    Central repository for registered and verified ModalityModelRunners.
    Allows dynamic lookup, version verification, and lifecycle management.
    """
    def __init__(self):
        self._runners: Dict[Modality, ModalityModelRunner] = {}
        # Pre-register default verified runners
        self.register_runner(ImuModalityModelRunner())
        self.register_runner(VagModalityModelRunner())
        self.register_runner(SemgModalityModelRunner())
        self.register_runner(RfExperimentalModelRunner())

    def register_runner(self, runner: ModalityModelRunner):
        meta = runner.get_metadata()
        self._runners[meta.modality] = runner

    def get_runner(self, modality: Modality) -> Optional[ModalityModelRunner]:
        return self._runners.get(modality)

    def list_models(self) -> List[ModelMetadata]:
        return [runner.get_metadata() for runner in self._runners.values()]

    def unload_all(self):
        for runner in self._runners.values():
            runner.unload()
        self._runners.clear()

class ModalityInferenceOrchestrator:
    """
    Module 5 Orchestrator:
    Takes FeatureVectors, queries registry for appropriate ModalityModelRunner,
    and returns verified ModalityPrediction without performing fusion.
    """
    def __init__(self, registry: Optional[ModelRegistry] = None):
        self.registry = registry or ModelRegistry()

    def run_inference(self, vector: ModalityFeatureVector) -> ModalityPrediction:
        modality = vector.provenance.modality
        runner = self.registry.get_runner(modality)

        if runner is None:
            return ModalityPrediction(
                model_metadata=ModelMetadata(
                    model_id=f"UNKNOWN_{modality.value}",
                    model_version="0.0.0",
                    modality=modality,
                    feature_schema_version=vector.provenance.feature_schema_version,
                    preprocessing_version=vector.provenance.preprocessing_version,
                    input_schema_version="none",
                    output_schema_version="none",
                    training_dataset_id="none",
                    artifact_hash="none",
                    runtime_version="none"
                ),
                provenance=vector.provenance,
                status=ModelExecutionStatus.ERROR,
                score=None,
                prediction_label=None,
                calibrated_uncertainty=None,
                status_reason=f"No registered model runner for modality {modality}",
                inference_time_ms=0.0
            )

        return runner.predict(vector)

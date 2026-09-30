package com.example.domain.usecase

import com.example.ai.DefaultMultimodalFusionEngine
import com.example.ai.InferenceInputValidator
import com.example.ai.ModalityInferenceOrchestrator
import com.example.ai.ModalityValidationResult
import com.example.ai.ModelMetadata
import com.example.ai.ModelRegistry
import com.example.ai.MultimodalScreeningDecision
import com.example.ai.RichModalityPrediction
import com.example.core.contract.Modality
import com.example.features.ModalityFeatureVector

class RunModalityInferenceUseCase(
    private val orchestrator: ModalityInferenceOrchestrator = ModalityInferenceOrchestrator()
) {
    fun execute(vector: ModalityFeatureVector): RichModalityPrediction {
        return orchestrator.runInference(vector)
    }
}

class ValidateInferenceInputsUseCase(
    private val validator: InferenceInputValidator = InferenceInputValidator()
) {
    fun execute(
        vectors: Map<Modality, ModalityFeatureVector>,
        sqiScores: Map<Modality, Double>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Map<Modality, ModalityValidationResult> {
        return validator.validateAll(
            vectors = vectors,
            sqiScores = sqiScores,
            currentTimeMs = currentTimeMs
        )
    }
}

class RunMultimodalFusionUseCase(
    private val validator: InferenceInputValidator = InferenceInputValidator(),
    private val orchestrator: ModalityInferenceOrchestrator = ModalityInferenceOrchestrator(),
    private val fusionEngine: DefaultMultimodalFusionEngine = DefaultMultimodalFusionEngine()
) {
    fun execute(
        sessionId: String,
        subjectId: String,
        vectors: Map<Modality, ModalityFeatureVector>,
        sqiScores: Map<Modality, Double>,
        isDemoSimulation: Boolean = true
    ): MultimodalScreeningDecision {
        val validations = validator.validateAll(vectors, sqiScores)
        val predictions = mutableMapOf<Modality, RichModalityPrediction>()

        validations.forEach { (mod, validation) ->
            if (validation.isAvailable && validation.vector != null) {
                predictions[mod] = orchestrator.runInference(validation.vector)
            }
        }

        return fusionEngine.executeFusion(
            sessionId = sessionId,
            subjectId = subjectId,
            validations = validations,
            predictions = predictions,
            sqiScores = sqiScores,
            isDemoSimulation = isDemoSimulation
        )
    }
}

class GetModelRegistryUseCase(
    private val registry: ModelRegistry = ModelRegistry()
) {
    fun listModels(): List<ModelMetadata> = registry.listModels()
}

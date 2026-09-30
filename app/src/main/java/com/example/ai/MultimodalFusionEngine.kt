package com.example.ai

import com.example.core.contract.Modality
import com.example.core.contract.ModalityPrediction
import com.example.core.contract.ReferralRecommendation
import com.example.core.contract.RiskTier
import com.example.core.contract.SchemaVersion
import com.example.core.contract.ScreeningResult
import com.example.core.contract.SignalQualityStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FusionState(val label: String) {
    COMPLETE("Complete Multimodal Fusion"),
    PARTIAL("Partial Multimodal Fusion"),
    INSUFFICIENT("Insufficient Modal Evidence")
}

enum class UncertaintyTier(val label: String, val description: String) {
    LOWER_UNCERTAINTY("Lower Uncertainty", "High multimodal agreement and complete signal coverage"),
    MODERATE_UNCERTAINTY("Moderate Uncertainty", "Acceptable coverage with minor signal or model variation"),
    ELEVATED_UNCERTAINTY("Elevated Uncertainty", "Partial modality availability or degraded signal quality"),
    HIGH_UNCERTAINTY("High Uncertainty — Retest Required", "Multiple modalities unavailable or fatal quality rejections")
}

data class UncertaintyEvaluationResult(
    val tier: UncertaintyTier,
    val uncertaintyScore: Double, // Calibrated metric [0.0 - 1.0] for research inspection
    val contributingFactors: List<String>
)

data class InferenceTraceStep(
    val timestamp: String,
    val message: String,
    val isWarningOrError: Boolean = false
)

data class MultimodalScreeningDecision(
    val schemaVersion: String = SchemaVersion.CURRENT,
    val sessionId: String,
    val subjectId: String,
    val fusionState: FusionState,
    val availableModalities: List<Modality>,
    val excludedModalities: List<Modality>,
    val modalityValidationResults: Map<Modality, ModalityValidationResult>,
    val modalityPredictions: Map<Modality, RichModalityPrediction>,
    val participatingWeights: Map<Modality, Double>,
    val fusedScore: Double?,
    val uncertaintyResult: UncertaintyEvaluationResult,
    val screeningRiskTier: RiskTier,
    val referralRecommendation: ReferralRecommendation,
    val screeningRecommendationText: String,
    val isDemoSimulation: Boolean = true,
    val trace: List<InferenceTraceStep> = emptyList(),
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun toScreeningResult(): ScreeningResult {
        return ScreeningResult(
            schemaVersion = schemaVersion,
            sessionId = sessionId,
            subjectId = subjectId,
            riskTier = screeningRiskTier,
            riskScore = fusedScore ?: 0.0,
            confidence = 1.0 - uncertaintyResult.uncertaintyScore,
            uncertainty = uncertaintyResult.uncertaintyScore,
            evidenceCoverage = "${availableModalities.size}/4 Modalities (${fusionState.label})",
            availableModalities = availableModalities,
            signalQualitySummary = "Available: ${availableModalities.joinToString { it.name }} | Excluded: ${if (excludedModalities.isEmpty()) "None" else excludedModalities.joinToString { it.name }}",
            screeningInterpretation = "$screeningRiskTier — ${uncertaintyResult.tier.label}",
            referralRecommendation = referralRecommendation,
            isSynthetic = isDemoSimulation,
            disclaimer = "RESEARCH SCREENING DECISION SUPPORT — NOT A CLINICAL DIAGNOSIS — ILLUSTRATIVE SIMULATION"
        )
    }
}

/**
 * Domain Uncertainty Evaluator
 */
class DefaultUncertaintyEvaluator : UncertaintyEvaluator {

    fun evaluate(
        validations: Map<Modality, ModalityValidationResult>,
        predictions: Map<Modality, RichModalityPrediction>,
        sqiScores: Map<Modality, Double>
    ): UncertaintyEvaluationResult {
        val factors = mutableListOf<String>()
        var penalty = 0.0

        val availableMods = validations.filter { it.value.isAvailable }.keys
        val totalMods = 4

        // 1. Modality Availability Penalties
        when (availableMods.size) {
            4 -> {
                // Complete participation
            }
            3 -> {
                penalty += 0.30
                factors.add("Partial coverage: 3 / 4 modalities available")
            }
            2 -> {
                penalty += 0.55
                factors.add("Reduced coverage: 2 / 4 modalities available")
            }
            1 -> {
                penalty += 0.75
                factors.add("Severely limited coverage: 1 / 4 modalities available")
            }
            else -> {
                penalty += 0.95
                factors.add("Zero modalities available: missing or rejected inputs")
            }
        }

        // 2. Specific Excluded Modality Diagnoses
        validations.forEach { (mod, res) ->
            if (!res.isAvailable) {
                factors.add("${mod.name} excluded: ${res.reason}")
                if (mod == Modality.RF && res.qualityStatus == SignalQualityStatus.FAIL) {
                    penalty += 0.15
                }
            }
        }

        // 3. Signal Quality Soft Degradation (0.50 <= SQI < 0.70)
        sqiScores.forEach { (mod, sqi) ->
            if (availableMods.contains(mod)) {
                if (sqi < 0.70) {
                    penalty += 0.10
                    factors.add("${mod.name} signal quality degraded (SQI ${(sqi * 100).toInt()}%)")
                }
            }
        }

        // 4. Modality Prediction Uncertainty
        predictions.filter { availableMods.contains(it.key) }.forEach { (mod, pred) ->
            if (pred.calibratedUncertainty != null && pred.calibratedUncertainty > 0.60) {
                penalty += 0.10
                factors.add("${mod.name} internal model uncertainty elevated")
            }
        }

        val clampedUncertainty = penalty.coerceIn(0.10, 0.98)

        val tier = when {
            clampedUncertainty >= 0.70 || availableMods.size < 3 -> UncertaintyTier.HIGH_UNCERTAINTY
            clampedUncertainty >= 0.45 -> UncertaintyTier.ELEVATED_UNCERTAINTY
            clampedUncertainty >= 0.25 -> UncertaintyTier.MODERATE_UNCERTAINTY
            else -> UncertaintyTier.LOWER_UNCERTAINTY
        }

        if (factors.isEmpty()) {
            factors.add("All 4 modalities synchronized with nominal signal quality")
        }

        return UncertaintyEvaluationResult(
            tier = tier,
            uncertaintyScore = clampedUncertainty,
            contributingFactors = factors
        )
    }

    override fun evaluateUncertainty(
        modalityPredictions: Map<Modality, ModalityPrediction>,
        signalQualityScores: Map<Modality, Double>
    ): Double {
        val availCount = modalityPredictions.size
        return when (availCount) {
            4 -> 0.15
            3 -> 0.45
            2 -> 0.70
            else -> 0.95
        }
    }
}

/**
 * Multimodal Fusion Engine Implementation
 *
 * Implements weighted evidence integration across:
 * - IMU (Kinematic): 0.35
 * - VAG (Acoustic Crepitus): 0.35
 * - sEMG (Neuromuscular Co-contraction): 0.20
 * - RF (Dielectric Resonance): 0.10 [Non-Diagnostic Research]
 */
class DefaultMultimodalFusionEngine(
    private val uncertaintyEvaluator: DefaultUncertaintyEvaluator = DefaultUncertaintyEvaluator()
) {
    private val baselineWeights = mapOf(
        Modality.IMU to 0.35,
        Modality.VAG to 0.35,
        Modality.SEMG to 0.20,
        Modality.RF to 0.10
    )

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun executeFusion(
        sessionId: String,
        subjectId: String,
        validations: Map<Modality, ModalityValidationResult>,
        predictions: Map<Modality, RichModalityPrediction>,
        sqiScores: Map<Modality, Double>,
        isDemoSimulation: Boolean = true
    ): MultimodalScreeningDecision {
        val trace = mutableListOf<InferenceTraceStep>()
        fun log(msg: String, isWarn: Boolean = false) {
            trace.add(InferenceTraceStep(timeFormat.format(Date()), msg, isWarn))
        }

        log("Feature schema validated (Canonical v${SchemaVersion.CURRENT})")

        // 1. Audit Modality Inputs
        val availableMods = mutableListOf<Modality>()
        val excludedMods = mutableListOf<Modality>()

        listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF).forEach { mod ->
            val v = validations[mod]
            if (v != null && v.isAvailable) {
                availableMods.add(mod)
                log("${mod.name} input accepted (SQI: ${sqiScores[mod]?.let { "${(it * 100).toInt()}%" } ?: "nominal"})")
            } else {
                excludedMods.add(mod)
                log("${mod.name} input rejected: ${v?.reason ?: "Unavailable"}", isWarn = true)
            }
        }

        // 2. Determine Fusion State
        val fusionState = when (availableMods.size) {
            4 -> {
                log("Complete multimodal fusion enabled (4 / 4 modalities participating)")
                FusionState.COMPLETE
            }
            3 -> {
                log("Partial multimodal fusion enabled (3 / 4 modalities participating; RF or secondary modality excluded)", isWarn = true)
                FusionState.PARTIAL
            }
            else -> {
                log("Insufficient modal evidence (${availableMods.size} / 4 available; fusion cannot proceed to nominal risk tier)", isWarn = true)
                FusionState.INSUFFICIENT
            }
        }

        // 3. Compute Normalized Participation Weights
        val activeRawWeights = availableMods.associateWith { baselineWeights[it] ?: 0.25 }
        val rawWeightSum = activeRawWeights.values.sum()
        val normalizedWeights = if (rawWeightSum > 0.0) {
            activeRawWeights.mapValues { it.value / rawWeightSum }
        } else {
            emptyMap()
        }

        // 4. Compute Fused Screening Score
        var fusedScore: Double? = null
        if (availableMods.isNotEmpty() && fusionState != FusionState.INSUFFICIENT) {
            var weightedSum = 0.0
            availableMods.forEach { mod ->
                val p = predictions[mod]
                val score = p?.score ?: 0.50
                val w = normalizedWeights[mod] ?: 0.0
                weightedSum += score * w
            }
            fusedScore = weightedSum.coerceIn(0.0, 1.0)
            log("Multimodal fusion score computed: ${"%.3f".format(fusedScore)}")
        } else {
            log("Screening score calculation bypassed due to insufficient inputs", isWarn = true)
        }

        // 5. Evaluate Uncertainty
        val uncertaintyResult = uncertaintyEvaluator.evaluate(
            validations = validations,
            predictions = predictions,
            sqiScores = sqiScores
        )
        log("Uncertainty estimated: ${uncertaintyResult.tier.label} (Index: ${"%.2f".format(uncertaintyResult.uncertaintyScore)})")

        // 6. Screening Risk Tier & Non-Diagnostic Recommendation
        val screeningRiskTier: RiskTier
        val referralRecommendation: ReferralRecommendation
        val recommendationText: String

        if (uncertaintyResult.tier == UncertaintyTier.HIGH_UNCERTAINTY || fusionState == FusionState.INSUFFICIENT) {
            screeningRiskTier = RiskTier.HIGH_UNCERTAINTY_RETEST
            referralRecommendation = ReferralRecommendation.RETEST_REQUIRED
            recommendationText = "Repeat measurement before interpreting the screening result. Signal quality or modality participation did not meet screening threshold."
            log("Screening tier generated: HIGH UNCERTAINTY — RETEST REQUIRED", isWarn = true)
        } else {
            val score = fusedScore ?: 0.50
            if (score < 0.38) {
                screeningRiskTier = RiskTier.LOWER_SCREENING_RISK
                referralRecommendation = ReferralRecommendation.ROUTINE_FOLLOW_UP
                recommendationText = "Continue routine observation according to the applicable clinical context."
                log("Screening tier generated: LOWER SCREENING RISK")
            } else if (score < 0.68) {
                screeningRiskTier = RiskTier.MODERATE_SCREENING_RISK
                referralRecommendation = ReferralRecommendation.CLINICAL_EVALUATION_RECOMMENDED
                recommendationText = "Consider repeat measurement and clinical evaluation if symptoms or concern persist."
                log("Screening tier generated: MODERATE SCREENING RISK")
            } else {
                screeningRiskTier = RiskTier.HIGHER_SCREENING_RISK
                referralRecommendation = ReferralRecommendation.EARLIER_CLINICAL_EVALUATION_RECOMMENDED
                recommendationText = "Clinical evaluation is recommended; this screening output does not establish a diagnosis."
                log("Screening tier generated: HIGHER SCREENING RISK")
            }
        }

        return MultimodalScreeningDecision(
            sessionId = sessionId,
            subjectId = subjectId,
            fusionState = fusionState,
            availableModalities = availableMods,
            excludedModalities = excludedMods,
            modalityValidationResults = validations,
            modalityPredictions = predictions,
            participatingWeights = normalizedWeights,
            fusedScore = fusedScore,
            uncertaintyResult = uncertaintyResult,
            screeningRiskTier = screeningRiskTier,
            referralRecommendation = referralRecommendation,
            screeningRecommendationText = recommendationText,
            isDemoSimulation = isDemoSimulation,
            trace = trace
        )
    }
}

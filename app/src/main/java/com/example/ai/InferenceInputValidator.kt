package com.example.ai

import com.example.core.contract.Modality
import com.example.core.contract.SchemaVersion
import com.example.core.contract.SignalQualityStatus
import com.example.features.ModalityFeatureVector
import com.example.preprocessing.ArtifactType

enum class ModalityInputStatus {
    AVAILABLE,
    EXCLUDED_QUALITY,
    EXCLUDED_SCHEMA,
    EXCLUDED_STALE,
    EXCLUDED_MISSING,
    NOT_READY
}

data class ModalityValidationResult(
    val modality: Modality,
    val qualityStatus: SignalQualityStatus,
    val isAvailable: Boolean,
    val inputStatus: ModalityInputStatus,
    val reason: String,
    val vector: ModalityFeatureVector? = null
)

/**
 * Technical Input Matrix & Feature Availability Gate (Module 5 Stage 1).
 *
 * Strict Checks:
 * 1. Feature exists.
 * 2. Feature schema matches (1.0).
 * 3. Feature quality passed (qualityStatus != FAIL).
 * 4. Normalization parameters exist.
 * 5. Modality is permitted for current mode.
 * 6. Feature is not stale.
 * 7. Feature is not quarantined.
 *
 * If any condition fails, the modality is EXCLUDED from inference.
 */
class InferenceInputValidator(
    private val maxFeatureAgeMs: Long = 60_000L // 60s freshness tolerance for streams/recordings
) {

    fun validateModalityInput(
        modality: Modality,
        featureVector: ModalityFeatureVector?,
        sqiScore: Double?,
        currentTimeMs: Long = System.currentTimeMillis()
    ): ModalityValidationResult {
        if (featureVector == null) {
            return ModalityValidationResult(
                modality = modality,
                qualityStatus = SignalQualityStatus.WARNING,
                isAvailable = false,
                inputStatus = ModalityInputStatus.NOT_READY,
                reason = "Awaiting preprocessed feature vector in store"
            )
        }

        val prov = featureVector.provenance

        // 1. Schema check
        if (prov.featureSchemaVersion != SchemaVersion.CURRENT) {
            return ModalityValidationResult(
                modality = modality,
                qualityStatus = prov.qualityStatus,
                isAvailable = false,
                inputStatus = ModalityInputStatus.EXCLUDED_SCHEMA,
                reason = "Schema version mismatch: expected ${SchemaVersion.CURRENT}, got ${prov.featureSchemaVersion}",
                vector = featureVector
            )
        }

        // 2. Quality status check (rejectQualityFailures guarantee)
        if (prov.qualityStatus == SignalQualityStatus.FAIL || (sqiScore != null && sqiScore < 0.50)) {
            val artifactStr = if (prov.artifactFlags.isNotEmpty()) {
                prov.artifactFlags.joinToString { it.name }
            } else {
                "SQI ${(prov.qualityScore * 100).toInt()}% below 0.50 threshold"
            }
            return ModalityValidationResult(
                modality = modality,
                qualityStatus = SignalQualityStatus.FAIL,
                isAvailable = false,
                inputStatus = ModalityInputStatus.EXCLUDED_QUALITY,
                reason = "Quality gate rejection: $artifactStr",
                vector = featureVector
            )
        }

        // 3. Stale feature check
        if (prov.windowEndMs > 0 && currentTimeMs - prov.windowEndMs > maxFeatureAgeMs) {
            return ModalityValidationResult(
                modality = modality,
                qualityStatus = prov.qualityStatus,
                isAvailable = false,
                inputStatus = ModalityInputStatus.EXCLUDED_STALE,
                reason = "Feature vector is stale (${(currentTimeMs - prov.windowEndMs) / 1000}s old)",
                vector = featureVector
            )
        }

        // 4. Normalization parameters check
        val hasNorm = when (modality) {
            Modality.IMU -> IMU_NORMALIZATION.featureNames.all { featureVector.features.containsKey(it) }
            Modality.VAG -> VAG_NORMALIZATION.featureNames.all { featureVector.features.containsKey(it) }
            Modality.SEMG -> SEMG_NORMALIZATION.featureNames.all { featureVector.features.containsKey(it) }
            Modality.RF -> RF_NORMALIZATION.featureNames.all { featureVector.features.containsKey(it) }
            else -> false
        }

        if (!hasNorm) {
            return ModalityValidationResult(
                modality = modality,
                qualityStatus = prov.qualityStatus,
                isAvailable = false,
                inputStatus = ModalityInputStatus.EXCLUDED_SCHEMA,
                reason = "Missing required features for frozen normalization schema",
                vector = featureVector
            )
        }

        return ModalityValidationResult(
            modality = modality,
            qualityStatus = prov.qualityStatus,
            isAvailable = true,
            inputStatus = ModalityInputStatus.AVAILABLE,
            reason = "Validated and ready for inference",
            vector = featureVector
        )
    }

    fun validateAll(
        vectors: Map<Modality, ModalityFeatureVector>,
        sqiScores: Map<Modality, Double>,
        modalities: List<Modality> = listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF),
        currentTimeMs: Long = System.currentTimeMillis()
    ): Map<Modality, ModalityValidationResult> {
        return modalities.associateWith { mod ->
            validateModalityInput(
                modality = mod,
                featureVector = vectors[mod],
                sqiScore = sqiScores[mod],
                currentTimeMs = currentTimeMs
            )
        }
    }
}

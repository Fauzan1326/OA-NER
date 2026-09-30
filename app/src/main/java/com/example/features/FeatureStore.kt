package com.example.features

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import java.util.Collections
import kotlin.math.sqrt

class FeatureStoreValidationError(message: String) : Exception(message)

data class NormalizationParam(
    val mean: Double,
    val std: Double
)

class FeatureStore(
    private val rejectQualityFailures: Boolean = false
) {
    private val records = Collections.synchronizedList(mutableListOf<ModalityFeatureVector>())

    @Throws(FeatureStoreValidationError::class)
    fun insert(vector: ModalityFeatureVector): Boolean {
        validate(vector)
        if (rejectQualityFailures && vector.provenance.qualityStatus == SignalQualityStatus.FAIL) {
            return false
        }
        records.add(vector)
        return true
    }

    private fun validate(vector: ModalityFeatureVector) {
        if (vector.provenance.sourceSensorId.isBlank()) {
            throw FeatureStoreValidationError("Missing sourceSensorId in feature provenance.")
        }
        for ((name, item) in vector.features) {
            if (item.value.isNaN() || item.value.isInfinite()) {
                throw FeatureStoreValidationError("Invalid non-finite feature value for '$name': ${item.value}")
            }
        }
    }

    fun query(
        modality: Modality? = null,
        subjectId: String? = null,
        sessionId: String? = null,
        startMs: Long? = null,
        endMs: Long? = null
    ): List<ModalityFeatureVector> {
        synchronized(records) {
            var list = records.toList()
            if (modality != null) {
                list = list.filter { it.provenance.modality == modality }
            }
            if (subjectId != null) {
                list = list.filter { it.provenance.subjectId == subjectId }
            }
            if (sessionId != null) {
                list = list.filter { it.provenance.sessionId == sessionId }
            }
            if (startMs != null) {
                list = list.filter { it.provenance.windowStartMs >= startMs }
            }
            if (endMs != null) {
                list = list.filter { it.provenance.windowEndMs <= endMs }
            }
            return list
        }
    }

    fun getLatest(modality: Modality): ModalityFeatureVector? {
        synchronized(records) {
            return records.asReversed().firstOrNull { it.provenance.modality == modality }
        }
    }

    fun count(modality: Modality? = null): Int {
        synchronized(records) {
            if (modality == null) return records.size
            return records.count { it.provenance.modality == modality }
        }
    }

    fun computeNormalizationStats(modality: Modality): Map<String, NormalizationParam> {
        val vectors = query(modality = modality)
        if (vectors.isEmpty()) return emptyMap()

        val featureValues = mutableMapOf<String, MutableList<Double>>()
        for (vec in vectors) {
            for ((name, item) in vec.features) {
                featureValues.getOrPut(name) { mutableListOf() }.add(item.value)
            }
        }

        val result = mutableMapOf<String, NormalizationParam>()
        for ((name, vals) in featureValues) {
            val mean = vals.sum() / vals.size
            val variance = vals.sumOf { (it - mean) * (it - mean) } / vals.size
            val std = sqrt(variance)
            result[name] = NormalizationParam(mean, if (std > 1e-9) std else 1.0)
        }
        return result
    }

    fun clear() {
        records.clear()
    }
}

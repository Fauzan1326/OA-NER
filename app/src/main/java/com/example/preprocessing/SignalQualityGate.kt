package com.example.preprocessing

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import kotlin.math.abs
import kotlin.math.sqrt

enum class ArtifactType(val description: String) {
    NONE("Clean Signal"),
    CLIPPING_SATURATION("Dynamic Range Saturation / Clipping"),
    FLATLINE_DROPOUT("Zero Variance Sensor Dropout"),
    MOTION_SPIKE("Mechanical Impact / Acceleration Spike"),
    ELECTRODE_LIFTOFF("High-Impedance Electrode Disconnection"),
    RF_MISMATCH("Severe Antenna Reflection / Detuning")
}

data class QualityAssessment(
    val modality: Modality,
    val sqiScore: Double, // 0.0 to 1.0
    val status: SignalQualityStatus,
    val detectedArtifacts: List<ArtifactType>,
    val rejectionReason: String? = null
)

class SignalQualityGate(
    val passThreshold: Double = 0.70,
    val warningThreshold: Double = 0.50
) {
    fun assessImu(window: List<List<Double>>): QualityAssessment {
        val artifacts = mutableListOf<ArtifactType>()
        if (window.isEmpty()) {
            return QualityAssessment(Modality.IMU, 0.0, SignalQualityStatus.FAIL, listOf(ArtifactType.FLATLINE_DROPOUT), "Empty window")
        }

        // Saturation check (> 15.8g)
        val hasClipping = window.any { sample ->
            sample.take(3).any { abs(it) > 15.8 }
        }
        if (hasClipping) artifacts.add(ArtifactType.CLIPPING_SATURATION)

        // Motion spike check (magnitude > 4.0g)
        val hasSpike = window.any { sample ->
            val mag = sqrt(sample.take(3).sumOf { it * it })
            mag > 4.0
        }
        if (hasSpike) artifacts.add(ArtifactType.MOTION_SPIKE)

        // Flatline check (variance < 1e-6)
        val numCh = minOf(3, window[0].size)
        val variances = (0 until numCh).map { ch ->
            variance(window.map { it[ch] })
        }
        if (variances.all { it < 1e-6 }) {
            artifacts.add(ArtifactType.FLATLINE_DROPOUT)
        }

        var sqi = 1.0
        if (artifacts.contains(ArtifactType.CLIPPING_SATURATION)) sqi -= 0.40
        if (artifacts.contains(ArtifactType.MOTION_SPIKE)) sqi -= 0.25
        if (artifacts.contains(ArtifactType.FLATLINE_DROPOUT)) sqi -= 0.60
        sqi = maxOf(0.0, minOf(1.0, sqi))

        val status = getStatus(sqi)
        val reason = if (artifacts.isNotEmpty()) artifacts.first().name else null
        return QualityAssessment(Modality.IMU, sqi, status, artifacts, reason)
    }

    fun assessVag(window: List<Double>): QualityAssessment {
        val artifacts = mutableListOf<ArtifactType>()
        if (window.isEmpty()) {
            return QualityAssessment(Modality.VAG, 0.0, SignalQualityStatus.FAIL, listOf(ArtifactType.FLATLINE_DROPOUT), "Empty window")
        }

        // Clipping check (> 2.45 mV)
        if (window.any { abs(it) > 2.45 }) {
            artifacts.add(ArtifactType.CLIPPING_SATURATION)
        }

        // Flatline check
        if (variance(window) < 1e-7) {
            artifacts.add(ArtifactType.FLATLINE_DROPOUT)
        }

        var sqi = 1.0
        if (artifacts.contains(ArtifactType.CLIPPING_SATURATION)) sqi -= 0.45
        if (artifacts.contains(ArtifactType.FLATLINE_DROPOUT)) sqi -= 0.60
        sqi = maxOf(0.0, minOf(1.0, sqi))

        val status = getStatus(sqi)
        val reason = if (artifacts.isNotEmpty()) artifacts.first().name else null
        return QualityAssessment(Modality.VAG, sqi, status, artifacts, reason)
    }

    fun assessSemg(window: List<List<Double>>): QualityAssessment {
        val artifacts = mutableListOf<ArtifactType>()
        if (window.isEmpty()) {
            return QualityAssessment(Modality.SEMG, 0.0, SignalQualityStatus.FAIL, listOf(ArtifactType.FLATLINE_DROPOUT), "Empty window")
        }

        // Liftoff check (> 4.5 mV)
        if (window.any { sample -> sample.any { abs(it) > 4.5 } }) {
            artifacts.add(ArtifactType.ELECTRODE_LIFTOFF)
        }

        // Flatline check
        if (variance(window.map { it[0] }) < 1e-6) {
            artifacts.add(ArtifactType.FLATLINE_DROPOUT)
        }

        var sqi = 1.0
        if (artifacts.contains(ArtifactType.ELECTRODE_LIFTOFF)) sqi -= 0.50
        if (artifacts.contains(ArtifactType.FLATLINE_DROPOUT)) sqi -= 0.50
        sqi = maxOf(0.0, minOf(1.0, sqi))

        val status = getStatus(sqi)
        val reason = if (artifacts.isNotEmpty()) artifacts.first().name else null
        return QualityAssessment(Modality.SEMG, sqi, status, artifacts, reason)
    }

    fun assessRf(window: List<List<Double>>): QualityAssessment {
        val artifacts = mutableListOf<ArtifactType>()
        if (window.isEmpty()) {
            return QualityAssessment(Modality.RF, 0.0, SignalQualityStatus.FAIL, listOf(ArtifactType.FLATLINE_DROPOUT), "Empty window")
        }

        val s11Values = window.map { it[0] }
        if (s11Values.any { it > -3.0 }) {
            artifacts.add(ArtifactType.RF_MISMATCH)
        }
        if (variance(s11Values) < 1e-7) {
            artifacts.add(ArtifactType.FLATLINE_DROPOUT)
        }

        var sqi = 1.0
        if (artifacts.contains(ArtifactType.RF_MISMATCH)) sqi -= 0.40
        if (artifacts.contains(ArtifactType.FLATLINE_DROPOUT)) sqi -= 0.50
        sqi = maxOf(0.0, minOf(1.0, sqi))

        val status = getStatus(sqi)
        val reason = if (artifacts.isNotEmpty()) artifacts.first().name else null
        return QualityAssessment(Modality.RF, sqi, status, artifacts, reason)
    }

    private fun getStatus(sqi: Double): SignalQualityStatus {
        return when {
            sqi >= passThreshold -> SignalQualityStatus.PASS
            sqi >= warningThreshold -> SignalQualityStatus.WARNING
            else -> SignalQualityStatus.FAIL
        }
    }

    private fun variance(data: List<Double>): Double {
        if (data.size < 2) return 0.0
        val mean = data.average()
        return data.sumOf { (it - mean) * (it - mean) } / data.size
    }
}

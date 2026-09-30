package com.example.features

import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.preprocessing.QualityAssessment
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class ImuFeatureExtractor {
    fun extract(
        window: List<List<Double>>,
        samplingRateHz: Double,
        qa: QualityAssessment,
        sourceSensorId: String = "IMU_6DOF",
        subjectId: String = "SUBJECT_001",
        sessionId: String = "SESSION_001",
        startTimeMs: Long = 0L,
        endTimeMs: Long = 1000L
    ): ModalityFeatureVector {
        val provenance = FeatureProvenance(
            sourceSensorId = sourceSensorId,
            subjectId = subjectId,
            sessionId = sessionId,
            modality = Modality.IMU,
            windowStartMs = startTimeMs,
            windowEndMs = endTimeMs,
            sampleCount = window.size,
            samplingRateHz = samplingRateHz,
            qualityStatus = qa.status,
            qualityScore = qa.sqiScore,
            artifactFlags = qa.detectedArtifacts
        )

        val features = mutableMapOf<String, FeatureItem>()
        if (window.isEmpty()) return ModalityFeatureVector(provenance, features)

        val accMags = window.map { s -> sqrt(s[0] * s[0] + s[1] * s[1] + s[2] * s[2]) }
        val gyroMags = window.map { s -> sqrt(s[3] * s[3] + s[4] * s[4] + s[5] * s[5]) }

        features["acc_mean_mag"] = FeatureItem(
            name = "acc_mean_mag", value = FeatureMath.mean(accMags), unit = "g",
            domain = FeatureDomain.STATISTICAL, description = "Mean Acceleration Magnitude"
        )
        features["acc_std_mag"] = FeatureItem(
            name = "acc_std_mag", value = FeatureMath.stdDev(accMags), unit = "g",
            domain = FeatureDomain.STATISTICAL, description = "Standard Deviation of Acceleration"
        )
        features["acc_rms_mag"] = FeatureItem(
            name = "acc_rms_mag", value = FeatureMath.rootMeanSquare(accMags), unit = "g",
            domain = FeatureDomain.STATISTICAL, description = "RMS Acceleration Magnitude"
        )
        features["acc_peak_mag"] = FeatureItem(
            name = "acc_peak_mag", value = FeatureMath.peakAmplitude(accMags), unit = "g",
            domain = FeatureDomain.STATISTICAL, description = "Peak Acceleration"
        )
        features["gyro_rms_mag"] = FeatureItem(
            name = "gyro_rms_mag", value = FeatureMath.rootMeanSquare(gyroMags), unit = "rad/s",
            domain = FeatureDomain.STATISTICAL, description = "RMS Angular Velocity"
        )

        // Temporal gait cadence
        val zcr = FeatureMath.zeroCrossingRate(accMags.map { it - 1.0 })
        val cadence = zcr * (samplingRateHz / 2.0)
        features["gait_cadence_hz"] = FeatureItem(
            name = "gait_cadence_hz", value = cadence, unit = "Hz",
            domain = FeatureDomain.TEMPORAL, description = "Estimated Gait Cadence / Stride Rate"
        )

        // Kinematic symmetry
        val varX = FeatureMath.variance(window.map { it[0] })
        val varY = FeatureMath.variance(window.map { it[1] })
        val symRatio = if (varX > 1e-6) (varY / max(1e-5, varX)) else 1.0
        features["movement_symmetry_index"] = FeatureItem(
            name = "movement_symmetry_index", value = min(5.0, symRatio), unit = "ratio",
            domain = FeatureDomain.TEMPORAL, description = "Kinematic Symmetry Index (AP/ML)"
        )

        // Spectral features
        val dftSlice = accMags.take(min(accMags.size, 128))
        val (freqs, mags) = FeatureMath.computeDftMagnitude(dftSlice, samplingRateHz)
        val (centroid, _, domFreq) = FeatureMath.spectralCentroidAndSpread(freqs, mags)

        features["imu_spectral_centroid_hz"] = FeatureItem(
            name = "imu_spectral_centroid_hz", value = centroid, unit = "Hz",
            domain = FeatureDomain.SPECTRAL, description = "Kinematic Spectral Centroid"
        )
        features["imu_dominant_freq_hz"] = FeatureItem(
            name = "imu_dominant_freq_hz", value = domFreq, unit = "Hz",
            domain = FeatureDomain.SPECTRAL, description = "Dominant Movement Frequency"
        )

        return ModalityFeatureVector(provenance, features)
    }
}

class VagFeatureExtractor {
    fun extract(
        window: List<Double>,
        samplingRateHz: Double,
        qa: QualityAssessment,
        sourceSensorId: String = "VAG_ACOUSTIC",
        subjectId: String = "SUBJECT_001",
        sessionId: String = "SESSION_001",
        startTimeMs: Long = 0L,
        endTimeMs: Long = 1000L
    ): ModalityFeatureVector {
        val provenance = FeatureProvenance(
            sourceSensorId = sourceSensorId,
            subjectId = subjectId,
            sessionId = sessionId,
            modality = Modality.VAG,
            windowStartMs = startTimeMs,
            windowEndMs = endTimeMs,
            sampleCount = window.size,
            samplingRateHz = samplingRateHz,
            qualityStatus = qa.status,
            qualityScore = qa.sqiScore,
            artifactFlags = qa.detectedArtifacts
        )

        val features = mutableMapOf<String, FeatureItem>()
        if (window.isEmpty()) return ModalityFeatureVector(provenance, features)

        val rms = FeatureMath.rootMeanSquare(window)
        val peak = FeatureMath.peakAmplitude(window)

        features["vag_rms"] = FeatureItem(
            name = "vag_rms", value = rms, unit = "mV",
            domain = FeatureDomain.STATISTICAL, description = "VAG RMS Acoustic Amplitude"
        )
        features["vag_peak_amplitude"] = FeatureItem(
            name = "vag_peak_amplitude", value = peak, unit = "mV",
            domain = FeatureDomain.STATISTICAL, description = "VAG Peak Acoustic Amplitude"
        )
        features["vag_acoustic_power"] = FeatureItem(
            name = "vag_acoustic_power", value = rms * rms, unit = "mV^2",
            domain = FeatureDomain.STATISTICAL, description = "Acoustic Power"
        )
        features["vag_zero_crossing_rate"] = FeatureItem(
            name = "vag_zero_crossing_rate", value = FeatureMath.zeroCrossingRate(window), unit = "ratio",
            domain = FeatureDomain.TEMPORAL, description = "VAG Zero Crossing Rate"
        )
        features["vag_crest_factor"] = FeatureItem(
            name = "vag_crest_factor", value = FeatureMath.crestFactor(window), unit = "ratio",
            domain = FeatureDomain.TEMPORAL, description = "Acoustic Crest Factor (Peak/RMS)"
        )

        // Spectral features (Crepitus acoustic signature)
        val dftSlice = window.take(min(window.size, 256))
        val (freqs, mags) = FeatureMath.computeDftMagnitude(dftSlice, samplingRateHz)
        val (centroid, spread, domFreq) = FeatureMath.spectralCentroidAndSpread(freqs, mags)

        features["vag_spectral_centroid_hz"] = FeatureItem(
            name = "vag_spectral_centroid_hz", value = centroid, unit = "Hz",
            domain = FeatureDomain.SPECTRAL, description = "VAG Acoustic Spectral Centroid"
        )
        features["vag_dominant_frequency_hz"] = FeatureItem(
            name = "vag_dominant_frequency_hz", value = domFreq, unit = "Hz",
            domain = FeatureDomain.SPECTRAL, description = "Dominant Acoustic Frequency"
        )
        features["vag_spectral_spread_hz"] = FeatureItem(
            name = "vag_spectral_spread_hz", value = spread, unit = "Hz",
            domain = FeatureDomain.SPECTRAL, description = "Acoustic Spectral Bandwidth / Spread"
        )

        return ModalityFeatureVector(provenance, features)
    }
}

class SemgFeatureExtractor {
    fun extract(
        window: List<List<Double>>,
        samplingRateHz: Double,
        qa: QualityAssessment,
        sourceSensorId: String = "SEMG_3CH",
        subjectId: String = "SUBJECT_001",
        sessionId: String = "SESSION_001",
        startTimeMs: Long = 0L,
        endTimeMs: Long = 1000L
    ): ModalityFeatureVector {
        val provenance = FeatureProvenance(
            sourceSensorId = sourceSensorId,
            subjectId = subjectId,
            sessionId = sessionId,
            modality = Modality.SEMG,
            windowStartMs = startTimeMs,
            windowEndMs = endTimeMs,
            sampleCount = window.size,
            samplingRateHz = samplingRateHz,
            qualityStatus = qa.status,
            qualityScore = qa.sqiScore,
            artifactFlags = qa.detectedArtifacts
        )

        val features = mutableMapOf<String, FeatureItem>()
        if (window.isEmpty()) return ModalityFeatureVector(provenance, features)

        val ch0Vm = window.map { it[0] }
        val ch1Vl = if (window[0].size > 1) window.map { it[1] } else ch0Vm

        val rmsVm = FeatureMath.rootMeanSquare(ch0Vm)
        val mavVm = FeatureMath.meanAbsoluteValue(ch0Vm)
        val wlVm = FeatureMath.waveformLength(ch0Vm)

        features["semg_rms_vm"] = FeatureItem(
            name = "semg_rms_vm", value = rmsVm, unit = "mV",
            domain = FeatureDomain.STATISTICAL, description = "Vastus Medialis RMS Activation"
        )
        features["semg_mav_vm"] = FeatureItem(
            name = "semg_mav_vm", value = mavVm, unit = "mV",
            domain = FeatureDomain.STATISTICAL, description = "Vastus Medialis Mean Absolute Value"
        )
        features["semg_waveform_length"] = FeatureItem(
            name = "semg_waveform_length", value = wlVm, unit = "mV",
            domain = FeatureDomain.TEMPORAL, description = "Waveform Length (Muscle Complexity)"
        )

        // Activation duty cycle (> 50uV)
        val activeCount = ch0Vm.count { kotlin.math.abs(it) > 0.05 }
        features["semg_activation_ratio"] = FeatureItem(
            name = "semg_activation_ratio", value = activeCount.toDouble() / ch0Vm.size, unit = "ratio",
            domain = FeatureDomain.TEMPORAL, description = "Muscle Activation Duty Cycle"
        )

        // Co-contraction ratio: VM / (VM + VL)
        val rmsVl = FeatureMath.rootMeanSquare(ch1Vl)
        val cci = if ((rmsVm + rmsVl) > 1e-6) (rmsVm / (rmsVm + rmsVl)) else 0.5
        features["semg_co_contraction_ratio"] = FeatureItem(
            name = "semg_co_contraction_ratio", value = cci, unit = "ratio",
            domain = FeatureDomain.TEMPORAL, description = "VM to VL Co-contraction Ratio"
        )

        // Spectral mean frequency
        val dftSlice = ch0Vm.take(min(ch0Vm.size, 256))
        val (freqs, mags) = FeatureMath.computeDftMagnitude(dftSlice, samplingRateHz)
        val (centroid, _, _) = FeatureMath.spectralCentroidAndSpread(freqs, mags)

        features["semg_mean_frequency_hz"] = FeatureItem(
            name = "semg_mean_frequency_hz", value = centroid, unit = "Hz",
            domain = FeatureDomain.SPECTRAL, description = "sEMG Mean Spectral Frequency"
        )

        return ModalityFeatureVector(provenance, features)
    }
}

class RfFeatureExtractor(
    private val baselineResFreqMhz: Double = 2450.0
) {
    fun extract(
        window: List<List<Double>>,
        samplingRateHz: Double,
        qa: QualityAssessment,
        sourceSensorId: String = "RF_DIELECTRIC_RESONANCE",
        subjectId: String = "SUBJECT_001",
        sessionId: String = "SESSION_001",
        startTimeMs: Long = 0L,
        endTimeMs: Long = 1000L
    ): ModalityFeatureVector {
        val provenance = FeatureProvenance(
            sourceSensorId = sourceSensorId,
            subjectId = subjectId,
            sessionId = sessionId,
            modality = Modality.RF,
            windowStartMs = startTimeMs,
            windowEndMs = endTimeMs,
            sampleCount = window.size,
            samplingRateHz = samplingRateHz,
            qualityStatus = qa.status,
            qualityScore = qa.sqiScore,
            artifactFlags = qa.detectedArtifacts
        )

        val features = mutableMapOf<String, FeatureItem>()
        if (window.isEmpty()) return ModalityFeatureVector(provenance, features)

        val s11Mags = window.map { it[0] }
        val freqs = window.map { if (it.size > 2) it[2] else baselineResFreqMhz }

        val minS11 = s11Mags.minOrNull() ?: -10.0
        features["rf_min_reflection_db"] = FeatureItem(
            name = "rf_min_reflection_db", value = minS11, unit = "dB",
            domain = FeatureDomain.EXPERIMENTAL_RF, description = "Minimum S11 Reflection Return Loss (Dip)"
        )

        val meanFreq = FeatureMath.mean(freqs)
        val freqShift = meanFreq - baselineResFreqMhz
        features["rf_resonance_freq_mhz"] = FeatureItem(
            name = "rf_resonance_freq_mhz", value = meanFreq, unit = "MHz",
            domain = FeatureDomain.EXPERIMENTAL_RF, description = "Estimated Center Resonance Frequency"
        )
        features["rf_resonance_shift_mhz"] = FeatureItem(
            name = "rf_resonance_shift_mhz", value = freqShift, unit = "MHz",
            domain = FeatureDomain.EXPERIMENTAL_RF, description = "Resonance Frequency Shift from Baseline"
        )

        val s11Var = FeatureMath.variance(s11Mags)
        val stability = max(0.0, min(1.0, 1.0 - (s11Var * 10.0)))
        features["rf_measurement_stability"] = FeatureItem(
            name = "rf_measurement_stability", value = stability, unit = "index",
            domain = FeatureDomain.EXPERIMENTAL_RF, description = "Experimental RF Dielectric Measurement Stability"
        )

        return ModalityFeatureVector(provenance, features)
    }
}

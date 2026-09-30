package com.example.preprocessing

import kotlin.math.abs
import kotlin.math.sqrt

data class ImuPreprocessedOutput(
    val cleanedValues: List<Double>,
    val accMagnitude: Double,
    val gyroMagnitude: Double
)

class ImuPreprocessor(
    val sampleRateHz: Double = 100.0,
    val cutoffHz: Double = 20.0
) {
    private val filters = List(6) { BiquadFilter.createLowpass(cutoffHz, sampleRateHz) }

    fun processSample(values: List<Double>): ImuPreprocessedOutput {
        if (values.size < 6) {
            return ImuPreprocessedOutput(values, 0.0, 0.0)
        }
        val filtered = values.mapIndexed { i, v -> filters[i].process(v) }
        val accMag = sqrt(filtered[0] * filtered[0] + filtered[1] * filtered[1] + filtered[2] * filtered[2])
        val gyroMag = sqrt(filtered[3] * filtered[3] + filtered[4] * filtered[4] + filtered[5] * filtered[5])
        return ImuPreprocessedOutput(filtered, accMag, gyroMag)
    }

    fun reset() {
        filters.forEach { it.reset() }
    }
}

data class VagPreprocessedOutput(
    val cleanedValues: List<Double>,
    val acousticEnvelope: Double,
    val acousticPower: Double
)

class VagPreprocessor(
    val sampleRateHz: Double = 2000.0
) {
    private val hp = BiquadFilter.createHighpass(15.0, sampleRateHz)
    private val lp = BiquadFilter.createLowpass(800.0, sampleRateHz)
    private val rms = RmsEnvelopeFilter(windowSize = 20)

    fun processSample(values: List<Double>): VagPreprocessedOutput {
        if (values.isEmpty()) {
            return VagPreprocessedOutput(listOf(0.0), 0.0, 0.0)
        }
        val bandpassed = lp.process(hp.process(values[0]))
        val envelope = rms.process(bandpassed)
        val power = bandpassed * bandpassed
        return VagPreprocessedOutput(listOf(bandpassed), envelope, power)
    }

    fun reset() {
        hp.reset()
        lp.reset()
        rms.reset()
    }
}

data class SemgPreprocessedOutput(
    val cleanedValues: List<Double>,
    val envelopes: List<Double>,
    val meanRms: Double
)

class SemgPreprocessor(
    val sampleRateHz: Double = 1000.0
) {
    private val notchFilters = List(3) { BiquadFilter.createNotch(50.0, sampleRateHz, q = 8.0) }
    private val hpFilters = List(3) { BiquadFilter.createHighpass(20.0, sampleRateHz) }
    private val rmsFilters = List(3) { RmsEnvelopeFilter(windowSize = 50) }

    fun processSample(values: List<Double>): SemgPreprocessedOutput {
        val count = minOf(values.size, 3)
        val cleaned = ArrayList<Double>(count)
        val envs = ArrayList<Double>(count)
        for (i in 0 until count) {
            val notched = notchFilters[i].process(values[i])
            val filtered = hpFilters[i].process(notched)
            cleaned.add(filtered)
            val env = rmsFilters[i].process(abs(filtered))
            envs.add(env)
        }
        val meanRms = if (envs.isNotEmpty()) envs.average() else 0.0
        return SemgPreprocessedOutput(cleaned, envs, meanRms)
    }

    fun reset() {
        notchFilters.forEach { it.reset() }
        hpFilters.forEach { it.reset() }
        rmsFilters.forEach { it.reset() }
    }
}

data class RfPreprocessedOutput(
    val cleanedValues: List<Double>,
    val freqShiftMhz: Double
)

class RfPreprocessor(
    val sampleRateHz: Double = 10.0,
    val baselineFreqMhz: Double = 2450.0
) {
    private val s11Smoother = MovingAverageFilter(windowSize = 5)

    fun processSample(values: List<Double>): RfPreprocessedOutput {
        if (values.size < 3) {
            return RfPreprocessedOutput(values, 0.0)
        }
        val s11Mag = values[0]
        val s11Phase = values[1]
        val freqMhz = values[2]

        val smoothedS11 = s11Smoother.process(s11Mag)
        val freqShift = freqMhz - baselineFreqMhz
        return RfPreprocessedOutput(listOf(smoothedS11, s11Phase, freqMhz), freqShift)
    }

    fun reset() {
        s11Smoother.reset()
    }
}

package com.example.features

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object FeatureMath {

    fun mean(data: List<Double>): Double {
        if (data.isEmpty()) return 0.0
        return data.sum() / data.size
    }

    fun variance(data: List<Double>): Double {
        if (data.size < 2) return 0.0
        val m = mean(data)
        return data.sumOf { (it - m) * (it - m) } / data.size
    }

    fun stdDev(data: List<Double>): Double {
        return sqrt(variance(data))
    }

    fun rootMeanSquare(data: List<Double>): Double {
        if (data.isEmpty()) return 0.0
        return sqrt(data.sumOf { it * it } / data.size)
    }

    fun peakAmplitude(data: List<Double>): Double {
        if (data.isEmpty()) return 0.0
        return data.maxOf { abs(it) }
    }

    fun meanAbsoluteValue(data: List<Double>): Double {
        if (data.isEmpty()) return 0.0
        return data.sumOf { abs(it) } / data.size
    }

    fun waveformLength(data: List<Double>): Double {
        if (data.size < 2) return 0.0
        var sum = 0.0
        for (i in 1 until data.size) {
            sum += abs(data[i] - data[i - 1])
        }
        return sum
    }

    fun zeroCrossingRate(data: List<Double>, threshold: Double = 1e-5): Double {
        if (data.size < 2) return 0.0
        var crossings = 0
        for (i in 1 until data.size) {
            if ((data[i] * data[i - 1] < 0.0) && abs(data[i] - data[i - 1]) > threshold) {
                crossings++
            }
        }
        return crossings.toDouble() / (data.size - 1)
    }

    fun crestFactor(data: List<Double>): Double {
        val rms = rootMeanSquare(data)
        if (rms < 1e-9) return 0.0
        return peakAmplitude(data) / rms
    }

    /**
     * Discrete Fourier Transform for real signals.
     * Returns Pair(frequenciesHz, magnitudes).
     */
    fun computeDftMagnitude(data: List<Double>, samplingRateHz: Double): Pair<List<Double>, List<Double>> {
        val n = data.size
        if (n == 0) return Pair(emptyList(), emptyList())
        val halfN = n / 2
        val freqs = ArrayList<Double>(halfN)
        val mags = ArrayList<Double>(halfN)

        for (k in 0 until halfN) {
            freqs.add(k * samplingRateHz / n)
            var realPart = 0.0
            var imagPart = 0.0
            val w = 2.0 * PI * k / n
            for (t in 0 until n) {
                val angle = w * t
                realPart += data[t] * cos(angle)
                imagPart -= data[t] * sin(angle)
            }
            val mag = sqrt(realPart * realPart + imagPart * imagPart) / n
            mags.add(mag)
        }
        return Pair(freqs, mags)
    }

    /**
     * Spectral Centroid, Spread (Bandwidth), and Dominant Frequency.
     * Returns Triple(centroidHz, spreadHz, dominantFreqHz).
     */
    fun spectralCentroidAndSpread(freqs: List<Double>, mags: List<Double>): Triple<Double, Double, Double> {
        if (freqs.isEmpty() || mags.isEmpty()) return Triple(0.0, 0.0, 0.0)
        val totalEnergy = mags.sum()
        if (totalEnergy < 1e-12) return Triple(0.0, 0.0, 0.0)

        var weightedFreqSum = 0.0
        for (i in freqs.indices) {
            weightedFreqSum += freqs[i] * mags[i]
        }
        val centroid = weightedFreqSum / totalEnergy

        var varianceSum = 0.0
        var maxMag = -1.0
        var domFreq = freqs[0]
        for (i in freqs.indices) {
            val diff = freqs[i] - centroid
            varianceSum += (diff * diff) * mags[i]
            if (mags[i] > maxMag) {
                maxMag = mags[i]
                domFreq = freqs[i]
            }
        }
        val spread = sqrt(varianceSum / totalEnergy)
        return Triple(centroid, spread, domFreq)
    }
}

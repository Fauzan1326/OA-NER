package com.example.preprocessing

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import java.lang.Math.PI

/**
 * Direct Form II Transposed Biquad Filter:
 * y[n] = b0*x[n] + d1
 * d1 = b1*x[n] - a1*y[n] + d2
 * d2 = b2*x[n] - a2*y[n]
 * Matches Python implementation for exact cross-platform parity.
 */
class BiquadFilter(
    b0: Double, b1: Double, b2: Double,
    a0: Double, a1: Double, a2: Double
) {
    private val b0Norm = b0 / a0
    private val b1Norm = b1 / a0
    private val b2Norm = b2 / a0
    private val a1Norm = a1 / a0
    private val a2Norm = a2 / a0
    private var d1 = 0.0
    private var d2 = 0.0

    fun process(x: Double): Double {
        val y = b0Norm * x + d1
        d1 = b1Norm * x - a1Norm * y + d2
        d2 = b2Norm * x - a2Norm * y
        return y
    }

    fun reset() {
        d1 = 0.0
        d2 = 0.0
    }

    companion object {
        fun createLowpass(cutoffHz: Double, sampleRateHz: Double, q: Double = 0.7071): BiquadFilter {
            val w0 = 2.0 * PI * cutoffHz / sampleRateHz
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = sinW0 / (2.0 * q)

            val b0 = (1.0 - cosW0) / 2.0
            val b1 = 1.0 - cosW0
            val b2 = (1.0 - cosW0) / 2.0
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosW0
            val a2 = 1.0 - alpha
            return BiquadFilter(b0, b1, b2, a0, a1, a2)
        }

        fun createHighpass(cutoffHz: Double, sampleRateHz: Double, q: Double = 0.7071): BiquadFilter {
            val w0 = 2.0 * PI * cutoffHz / sampleRateHz
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = sinW0 / (2.0 * q)

            val b0 = (1.0 + cosW0) / 2.0
            val b1 = -(1.0 + cosW0)
            val b2 = (1.0 + cosW0) / 2.0
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosW0
            val a2 = 1.0 - alpha
            return BiquadFilter(b0, b1, b2, a0, a1, a2)
        }

        fun createNotch(notchHz: Double, sampleRateHz: Double, q: Double = 10.0): BiquadFilter {
            val w0 = 2.0 * PI * notchHz / sampleRateHz
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = sinW0 / (2.0 * q)

            val b0 = 1.0
            val b1 = -2.0 * cosW0
            val b2 = 1.0
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosW0
            val a2 = 1.0 - alpha
            return BiquadFilter(b0, b1, b2, a0, a1, a2)
        }
    }
}

class MovingAverageFilter(val windowSize: Int) {
    private val buffer = ArrayList<Double>()
    private var sum = 0.0

    fun process(x: Double): Double {
        buffer.add(x)
        sum += x
        if (buffer.size > windowSize) {
            sum -= buffer.removeAt(0)
        }
        return sum / buffer.size
    }

    fun reset() {
        buffer.clear()
        sum = 0.0
    }
}

class RmsEnvelopeFilter(val windowSize: Int) {
    private val buffer = ArrayList<Double>()
    private var sumSquares = 0.0

    fun process(x: Double): Double {
        val sq = x * x
        buffer.add(sq)
        sumSquares += sq
        if (buffer.size > windowSize) {
            sumSquares -= buffer.removeAt(0)
        }
        val meanSq = maxOf(0.0, sumSquares / buffer.size)
        return sqrt(meanSq)
    }

    fun reset() {
        buffer.clear()
        sumSquares = 0.0
    }
}

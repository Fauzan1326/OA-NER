"""
Standard Digital Signal Processing (DSP) Filters for ARTHROSCAN-NER.
Deterministic IIR Biquad Filters (Low-pass, High-pass, Band-pass, Notch)
implemented with pure arithmetic for exact cross-platform parity between Python and Kotlin.
"""

import math
from typing import List

class BiquadFilter:
    """
    Direct Form II Transposed Biquad Filter:
    y[n] = b0*x[n] + d1
    d1 = b1*x[n] - a1*y[n] + d2
    d2 = b2*x[n] - a2*y[n]
    """
    def __init__(self, b0: float, b1: float, b2: float, a0: float, a1: float, a2: float):
        self.b0 = b0 / a0
        self.b1 = b1 / a0
        self.b2 = b2 / a0
        self.a1 = a1 / a0
        self.a2 = a2 / a0
        self.d1 = 0.0
        self.d2 = 0.0

    def process(self, x: float) -> float:
        y = self.b0 * x + self.d1
        self.d1 = self.b1 * x - self.a1 * y + self.d2
        self.d2 = self.b2 * x - self.a2 * y
        return y

    def reset(self):
        self.d1 = 0.0
        self.d2 = 0.0

    @classmethod
    def create_lowpass(cls, cutoff_hz: float, sample_rate_hz: float, q: float = 0.7071) -> "BiquadFilter":
        w0 = 2.0 * math.pi * cutoff_hz / sample_rate_hz
        cos_w0 = math.cos(w0)
        sin_w0 = math.sin(w0)
        alpha = sin_w0 / (2.0 * q)

        b0 = (1.0 - cos_w0) / 2.0
        b1 = 1.0 - cos_w0
        b2 = (1.0 - cos_w0) / 2.0
        a0 = 1.0 + alpha
        a1 = -2.0 * cos_w0
        a2 = 1.0 - alpha
        return cls(b0, b1, b2, a0, a1, a2)

    @classmethod
    def create_highpass(cls, cutoff_hz: float, sample_rate_hz: float, q: float = 0.7071) -> "BiquadFilter":
        w0 = 2.0 * math.pi * cutoff_hz / sample_rate_hz
        cos_w0 = math.cos(w0)
        sin_w0 = math.sin(w0)
        alpha = sin_w0 / (2.0 * q)

        b0 = (1.0 + cos_w0) / 2.0
        b1 = -(1.0 + cos_w0)
        b2 = (1.0 + cos_w0) / 2.0
        a0 = 1.0 + alpha
        a1 = -2.0 * cos_w0
        a2 = 1.0 - alpha
        return cls(b0, b1, b2, a0, a1, a2)

    @classmethod
    def create_notch(cls, notch_hz: float, sample_rate_hz: float, q: float = 10.0) -> "BiquadFilter":
        w0 = 2.0 * math.pi * notch_hz / sample_rate_hz
        cos_w0 = math.cos(w0)
        sin_w0 = math.sin(w0)
        alpha = sin_w0 / (2.0 * q)

        b0 = 1.0
        b1 = -2.0 * cos_w0
        b2 = 1.0
        a0 = 1.0 + alpha
        a1 = -2.0 * cos_w0
        a2 = 1.0 - alpha
        return cls(b0, b1, b2, a0, a1, a2)

class MovingAverageFilter:
    def __init__(self, window_size: int):
        self.window_size = max(1, window_size)
        self.buffer: List[float] = []
        self.sum = 0.0

    def process(self, x: float) -> float:
        self.buffer.append(x)
        self.sum += x
        if len(self.buffer) > self.window_size:
            self.sum -= self.buffer.pop(0)
        return self.sum / len(self.buffer)

    def reset(self):
        self.buffer.clear()
        self.sum = 0.0

class RmsEnvelopeFilter:
    def __init__(self, window_size: int):
        self.window_size = max(1, window_size)
        self.buffer: List[float] = []
        self.sum_squares = 0.0

    def process(self, x: float) -> float:
        sq = x * x
        self.buffer.append(sq)
        self.sum_squares += sq
        if len(self.buffer) > self.window_size:
            self.sum_squares -= self.buffer.pop(0)
        mean_sq = max(0.0, self.sum_squares / len(self.buffer))
        return math.sqrt(mean_sq)

    def reset(self):
        self.buffer.clear()
        self.sum_squares = 0.0

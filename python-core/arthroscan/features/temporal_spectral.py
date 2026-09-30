"""
Pure arithmetic Statistical, Temporal, and Spectral math routines
for ARTHROSCAN-NER Feature Extraction.
Maintains exact deterministic behavior across Python and Kotlin.
"""

import math
from typing import List, Tuple

def mean(data: List[float]) -> float:
    if not data:
        return 0.0
    return sum(data) / len(data)

def variance(data: List[float]) -> float:
    if len(data) < 2:
        return 0.0
    m = mean(data)
    return sum((x - m) ** 2 for x in data) / len(data)

def std_dev(data: List[float]) -> float:
    return math.sqrt(variance(data))

def root_mean_square(data: List[float]) -> float:
    if not data:
        return 0.0
    return math.sqrt(sum(x * x for x in data) / len(data))

def peak_amplitude(data: List[float]) -> float:
    if not data:
        return 0.0
    return max(abs(x) for x in data)

def mean_absolute_value(data: List[float]) -> float:
    if not data:
        return 0.0
    return sum(abs(x) for x in data) / len(data)

def waveform_length(data: List[float]) -> float:
    if len(data) < 2:
        return 0.0
    return sum(abs(data[i] - data[i - 1]) for i in range(1, len(data)))

def zero_crossing_rate(data: List[float], threshold: float = 1e-5) -> float:
    if len(data) < 2:
        return 0.0
    crossings = 0
    for i in range(1, len(data)):
        if (data[i] * data[i - 1] < 0) and abs(data[i] - data[i - 1]) > threshold:
            crossings += 1
    return crossings / (len(data) - 1)

def crest_factor(data: List[float]) -> float:
    rms = root_mean_square(data)
    if rms < 1e-9:
        return 0.0
    return peak_amplitude(data) / rms

def compute_dft_magnitude(data: List[float], sampling_rate_hz: float) -> Tuple[List[float], List[float]]:
    """
    Computes Discrete Fourier Transform (DFT) magnitude spectrum for real signals.
    Optimized for small windows (e.g. N <= 256) for exact cross-platform portability.
    Returns: (freqs_hz, magnitudes)
    """
    n = len(data)
    if n == 0:
        return [], []
    half_n = n // 2
    freqs = [k * sampling_rate_hz / n for k in range(half_n)]
    mags = []
    for k in range(half_n):
        real_part = 0.0
        imag_part = 0.0
        w = 2.0 * math.pi * k / n
        for t in range(n):
            angle = w * t
            real_part += data[t] * math.cos(angle)
            imag_part -= data[t] * math.sin(angle)
        mag = math.sqrt(real_part * real_part + imag_part * imag_part) / n
        mags.append(mag)
    return freqs, mags

def spectral_centroid_and_spread(freqs: List[float], mags: List[float]) -> Tuple[float, float, float]:
    """
    Returns: (centroid_hz, spread_hz, dominant_freq_hz)
    """
    if not freqs or not mags:
        return 0.0, 0.0, 0.0
    total_energy = sum(mags)
    if total_energy < 1e-12:
        return 0.0, 0.0, 0.0

    centroid = sum(f * m for f, m in zip(freqs, mags)) / total_energy
    spread = math.sqrt(sum(((f - centroid) ** 2) * m for f, m in zip(freqs, mags)) / total_energy)
    
    max_idx = max(range(len(mags)), key=lambda i: mags[i])
    dominant_freq = freqs[max_idx]
    return centroid, spread, dominant_freq

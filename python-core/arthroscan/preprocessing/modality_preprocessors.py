"""
Modality-specific signal preprocessors for ARTHROSCAN-NER.
Provides deterministic filtering, rectification, and envelope extraction
for IMU, VAG, sEMG, and RF modalities.
"""

import math
from typing import List, Dict, Any, Tuple
from arthroscan.preprocessing.filters import (
    BiquadFilter, MovingAverageFilter, RmsEnvelopeFilter
)

class ImuPreprocessor:
    """
    IMU Preprocessing:
    - 2nd-order Butterworth Low-Pass filter (fc = 20Hz, fs = 100Hz)
    - Acceleration magnitude: sqrt(ax^2 + ay^2 + az^2)
    - Angular velocity magnitude: sqrt(gx^2 + gy^2 + gz^2)
    """
    def __init__(self, sample_rate_hz: float = 100.0, cutoff_hz: float = 20.0):
        self.sample_rate_hz = sample_rate_hz
        self.filters = [
            BiquadFilter.create_lowpass(cutoff_hz, sample_rate_hz) for _ in range(6)
        ]

    def process_sample(self, values: List[float]) -> Tuple[List[float], float, float]:
        if len(values) < 6:
            return values, 0.0, 0.0

        filtered = [self.filters[i].process(values[i]) for i in range(6)]
        acc_mag = math.sqrt(filtered[0]**2 + filtered[1]**2 + filtered[2]**2)
        gyro_mag = math.sqrt(filtered[3]**2 + filtered[4]**2 + filtered[5]**2)
        return filtered, acc_mag, gyro_mag

    def reset(self):
        for f in self.filters:
            f.reset()

class VagPreprocessor:
    """
    Vibroarthrography (VAG) Acoustic Preprocessing:
    - Bandpass filter (Highpass fc=15Hz, Lowpass fc=800Hz, fs=2000Hz)
    - RMS envelope extraction (window = 20 samples = 10ms)
    - Acoustic Energy: x^2
    """
    def __init__(self, sample_rate_hz: float = 2000.0):
        self.sample_rate_hz = sample_rate_hz
        self.hp_ch1 = BiquadFilter.create_highpass(15.0, sample_rate_hz)
        self.lp_ch1 = BiquadFilter.create_lowpass(800.0, sample_rate_hz)
        self.rms_ch1 = RmsEnvelopeFilter(window_size=20)

    def process_sample(self, values: List[float]) -> Tuple[List[float], float]:
        if not values:
            return [0.0], 0.0
        val = values[0]
        # Cascade highpass then lowpass
        bandpassed = self.lp_ch1.process(self.hp_ch1.process(val))
        envelope = self.rms_ch1.process(bandpassed)
        return [bandpassed], envelope

    def reset(self):
        self.hp_ch1.reset()
        self.lp_ch1.reset()
        self.rms_ch1.reset()

class SemgPreprocessor:
    """
    sEMG Muscle Electrophysiology Preprocessing:
    - 50Hz Notch filter (Powerline interference rejection)
    - 20Hz High-pass filter (Motion artifact & electrode drift rejection)
    - Full-wave rectification |x|
    - RMS envelope (window = 50 samples = 50ms at 1000Hz)
    """
    def __init__(self, sample_rate_hz: float = 1000.0):
        self.sample_rate_hz = sample_rate_hz
        self.notch_filters = [
            BiquadFilter.create_notch(50.0, sample_rate_hz, q=8.0) for _ in range(3)
        ]
        self.hp_filters = [
            BiquadFilter.create_highpass(20.0, sample_rate_hz) for _ in range(3)
        ]
        self.rms_filters = [
            RmsEnvelopeFilter(window_size=50) for _ in range(3)
        ]

    def process_sample(self, values: List[float]) -> Tuple[List[float], List[float]]:
        count = min(len(values), 3)
        cleaned = []
        envelopes = []
        for i in range(count):
            # Notch then Highpass
            notched = self.notch_filters[i].process(values[i])
            filtered = self.hp_filters[i].process(notched)
            cleaned.append(filtered)
            env = self.rms_filters[i].process(abs(filtered))
            envelopes.append(env)
        return cleaned, envelopes

    def reset(self):
        for f in self.notch_filters: f.reset()
        for f in self.hp_filters: f.reset()
        for f in self.rms_filters: f.reset()

class RfPreprocessor:
    """
    RF Dielectric Joint Resonance Preprocessing:
    - Moving average smoothing (window = 5 samples)
    - Resonance frequency tracking and dielectric shift calculation
    """
    def __init__(self, sample_rate_hz: float = 10.0, baseline_freq_mhz: float = 2450.0):
        self.sample_rate_hz = sample_rate_hz
        self.baseline_freq_mhz = baseline_freq_mhz
        self.s11_smoother = MovingAverageFilter(window_size=5)

    def process_sample(self, values: List[float]) -> Tuple[List[float], float]:
        if len(values) < 3:
            return values, 0.0
        s11_mag = values[0]
        s11_phase = values[1]
        freq_mhz = values[2]

        smoothed_s11 = self.s11_smoother.process(s11_mag)
        freq_shift_mhz = freq_mhz - self.baseline_freq_mhz
        return [smoothed_s11, s11_phase, freq_mhz], freq_shift_mhz

    def reset(self):
        self.s11_smoother.reset()

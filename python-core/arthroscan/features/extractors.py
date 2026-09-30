"""
Modality-specific Feature Extractors for ARTHROSCAN-NER (Module 4).
Translates preprocessed, quality-evaluated signal windows into rich,
traceable ModalityFeatureVectors.
"""

import math
from typing import List, Dict, Tuple
from arthroscan.schemas.contract import Modality, SignalQualityStatus
from arthroscan.preprocessing.quality_gate import QualityAssessment, ArtifactType
from arthroscan.features.feature_schema import (
    FeatureItem, FeatureDomain, FeatureProvenance, ModalityFeatureVector
)
from arthroscan.features.temporal_spectral import (
    mean, variance, std_dev, root_mean_square, peak_amplitude,
    mean_absolute_value, waveform_length, zero_crossing_rate,
    crest_factor, compute_dft_magnitude, spectral_centroid_and_spread
)

class ImuFeatureExtractor:
    """
    Extracts kinematic, statistical, gait temporal, and spectral features from IMU window.
    Window expected format: list of 6-channel samples [ax, ay, az, gx, gy, gz].
    """
    def extract(
        self,
        window: List[List[float]],
        sampling_rate_hz: float,
        qa: QualityAssessment,
        source_sensor_id: str = "IMU_6DOF",
        subject_id: str = "SUBJECT_001",
        session_id: str = "SESSION_001",
        start_time_ms: int = 0,
        end_time_ms: int = 1000
    ) -> ModalityFeatureVector:
        provenance = FeatureProvenance(
            source_sensor_id=source_sensor_id,
            subject_id=subject_id,
            session_id=session_id,
            modality=Modality.IMU,
            window_start_ms=start_time_ms,
            window_end_ms=end_time_ms,
            sample_count=len(window),
            sampling_rate_hz=sampling_rate_hz,
            quality_status=qa.status,
            quality_score=qa.sqi_score,
            artifact_flags=qa.detected_artifacts
        )

        features: Dict[str, FeatureItem] = {}
        if not window:
            return ModalityFeatureVector(provenance, features)

        acc_mags = [math.sqrt(s[0]**2 + s[1]**2 + s[2]**2) for s in window]
        gyro_mags = [math.sqrt(s[3]**2 + s[4]**2 + s[5]**2) for s in window]

        # Statistical kinematics
        features["acc_mean_mag"] = FeatureItem(
            name="acc_mean_mag", value=mean(acc_mags), unit="g",
            domain=FeatureDomain.STATISTICAL, description="Mean Acceleration Magnitude"
        )
        features["acc_std_mag"] = FeatureItem(
            name="acc_std_mag", value=std_dev(acc_mags), unit="g",
            domain=FeatureDomain.STATISTICAL, description="Standard Deviation of Acceleration"
        )
        features["acc_rms_mag"] = FeatureItem(
            name="acc_rms_mag", value=root_mean_square(acc_mags), unit="g",
            domain=FeatureDomain.STATISTICAL, description="RMS Acceleration Magnitude"
        )
        features["acc_peak_mag"] = FeatureItem(
            name="acc_peak_mag", value=peak_amplitude(acc_mags), unit="g",
            domain=FeatureDomain.STATISTICAL, description="Peak Acceleration"
        )
        features["gyro_rms_mag"] = FeatureItem(
            name="gyro_rms_mag", value=root_mean_square(gyro_mags), unit="rad/s",
            domain=FeatureDomain.STATISTICAL, description="RMS Angular Velocity"
        )

        # Temporal gait descriptors
        zcr = zero_crossing_rate([a - 1.0 for a in acc_mags])  # remove 1g static baseline
        cadence_est = zcr * (sampling_rate_hz / 2.0)  # estimated step cadence
        features["gait_cadence_hz"] = FeatureItem(
            name="gait_cadence_hz", value=cadence_est, unit="Hz",
            domain=FeatureDomain.TEMPORAL, description="Estimated Gait Cadence / Stride Rate"
        )

        # Symmetry descriptor (ratio of forward to lateral variance)
        var_x = variance([s[0] for s in window])
        var_y = variance([s[1] for s in window])
        sym_ratio = (var_y / max(1e-5, var_x)) if var_x > 1e-6 else 1.0
        features["movement_symmetry_index"] = FeatureItem(
            name="movement_symmetry_index", value=min(5.0, sym_ratio), unit="ratio",
            domain=FeatureDomain.TEMPORAL, description="Kinematic Symmetry Index (AP/ML)"
        )

        # Spectral characteristics
        dft_window = acc_mags[:min(len(acc_mags), 128)]
        freqs, mags = compute_dft_magnitude(dft_window, sampling_rate_hz)
        centroid, spread, dom_freq = spectral_centroid_and_spread(freqs, mags)
        features["imu_spectral_centroid_hz"] = FeatureItem(
            name="imu_spectral_centroid_hz", value=centroid, unit="Hz",
            domain=FeatureDomain.SPECTRAL, description="Kinematic Spectral Centroid"
        )
        features["imu_dominant_freq_hz"] = FeatureItem(
            name="imu_dominant_freq_hz", value=dom_freq, unit="Hz",
            domain=FeatureDomain.SPECTRAL, description="Dominant Movement Frequency"
        )

        return ModalityFeatureVector(provenance, features)

class VagFeatureExtractor:
    """
    Extracts acoustic, crepitus, and spectral vibration features from VAG window.
    Window expected format: list of scalar acoustic voltage values (mV).
    """
    def extract(
        self,
        window: List[float],
        sampling_rate_hz: float,
        qa: QualityAssessment,
        source_sensor_id: str = "VAG_ACOUSTIC",
        subject_id: str = "SUBJECT_001",
        session_id: str = "SESSION_001",
        start_time_ms: int = 0,
        end_time_ms: int = 1000
    ) -> ModalityFeatureVector:
        provenance = FeatureProvenance(
            source_sensor_id=source_sensor_id,
            subject_id=subject_id,
            session_id=session_id,
            modality=Modality.VAG,
            window_start_ms=start_time_ms,
            window_end_ms=end_time_ms,
            sample_count=len(window),
            sampling_rate_hz=sampling_rate_hz,
            quality_status=qa.status,
            quality_score=qa.sqi_score,
            artifact_flags=qa.detected_artifacts
        )

        features: Dict[str, FeatureItem] = {}
        if not window:
            return ModalityFeatureVector(provenance, features)

        # Temporal & Statistical Acoustic Descriptors
        rms_val = root_mean_square(window)
        peak_val = peak_amplitude(window)
        features["vag_rms"] = FeatureItem(
            name="vag_rms", value=rms_val, unit="mV",
            domain=FeatureDomain.STATISTICAL, description="VAG RMS Acoustic Amplitude"
        )
        features["vag_peak_amplitude"] = FeatureItem(
            name="vag_peak_amplitude", value=peak_val, unit="mV",
            domain=FeatureDomain.STATISTICAL, description="VAG Peak Acoustic Amplitude"
        )
        features["vag_acoustic_power"] = FeatureItem(
            name="vag_acoustic_power", value=rms_val * rms_val, unit="mV^2",
            domain=FeatureDomain.STATISTICAL, description="Acoustic Power"
        )
        features["vag_zero_crossing_rate"] = FeatureItem(
            name="vag_zero_crossing_rate", value=zero_crossing_rate(window), unit="ratio",
            domain=FeatureDomain.TEMPORAL, description="VAG Zero Crossing Rate"
        )
        features["vag_crest_factor"] = FeatureItem(
            name="vag_crest_factor", value=crest_factor(window), unit="ratio",
            domain=FeatureDomain.TEMPORAL, description="Acoustic Crest Factor (Peak/RMS)"
        )

        # Spectral Descriptors (Crepitus acoustic signature)
        dft_slice = window[:min(len(window), 256)]
        freqs, mags = compute_dft_magnitude(dft_slice, sampling_rate_hz)
        centroid, spread, dom_freq = spectral_centroid_and_spread(freqs, mags)

        features["vag_spectral_centroid_hz"] = FeatureItem(
            name="vag_spectral_centroid_hz", value=centroid, unit="Hz",
            domain=FeatureDomain.SPECTRAL, description="VAG Acoustic Spectral Centroid"
        )
        features["vag_dominant_frequency_hz"] = FeatureItem(
            name="vag_dominant_frequency_hz", value=dom_freq, unit="Hz",
            domain=FeatureDomain.SPECTRAL, description="Dominant Acoustic Frequency"
        )
        features["vag_spectral_spread_hz"] = FeatureItem(
            name="vag_spectral_spread_hz", value=spread, unit="Hz",
            domain=FeatureDomain.SPECTRAL, description="Acoustic Spectral Bandwidth / Spread"
        )

        return ModalityFeatureVector(provenance, features)

class SemgFeatureExtractor:
    """
    Extracts muscle activation, timing, and co-contraction descriptors from sEMG window.
    Window expected format: list of multi-channel sEMG samples (e.g. [VM, VL, RF]).
    """
    def extract(
        self,
        window: List[List[float]],
        sampling_rate_hz: float,
        qa: QualityAssessment,
        source_sensor_id: str = "SEMG_3CH",
        subject_id: str = "SUBJECT_001",
        session_id: str = "SESSION_001",
        start_time_ms: int = 0,
        end_time_ms: int = 1000
    ) -> ModalityFeatureVector:
        provenance = FeatureProvenance(
            source_sensor_id=source_sensor_id,
            subject_id=subject_id,
            session_id=session_id,
            modality=Modality.SEMG,
            window_start_ms=start_time_ms,
            window_end_ms=end_time_ms,
            sample_count=len(window),
            sampling_rate_hz=sampling_rate_hz,
            quality_status=qa.status,
            quality_score=qa.sqi_score,
            artifact_flags=qa.detected_artifacts
        )

        features: Dict[str, FeatureItem] = {}
        if not window:
            return ModalityFeatureVector(provenance, features)

        ch0_vm = [s[0] for s in window]
        ch1_vl = [s[1] for s in window] if len(window[0]) > 1 else ch0_vm

        # sEMG Temporal & Waveform Descriptors
        rms_vm = root_mean_square(ch0_vm)
        mav_vm = mean_absolute_value(ch0_vm)
        wl_vm = waveform_length(ch0_vm)

        features["semg_rms_vm"] = FeatureItem(
            name="semg_rms_vm", value=rms_vm, unit="mV",
            domain=FeatureDomain.STATISTICAL, description="Vastus Medialis RMS Activation"
        )
        features["semg_mav_vm"] = FeatureItem(
            name="semg_mav_vm", value=mav_vm, unit="mV",
            domain=FeatureDomain.STATISTICAL, description="Vastus Medialis Mean Absolute Value"
        )
        features["semg_waveform_length"] = FeatureItem(
            name="semg_waveform_length", value=wl_vm, unit="mV",
            domain=FeatureDomain.TEMPORAL, description="Waveform Length (Muscle Complexity)"
        )

        # Activation ratio (proportion of time signal exceeds threshold)
        active_thresh = 0.05  # 50 uV
        active_count = sum(1 for v in ch0_vm if abs(v) > active_thresh)
        features["semg_activation_ratio"] = FeatureItem(
            name="semg_activation_ratio", value=active_count / len(ch0_vm), unit="ratio",
            domain=FeatureDomain.TEMPORAL, description="Muscle Activation Duty Cycle"
        )

        # Co-contraction Ratio: VM / (VM + VL)
        rms_vl = root_mean_square(ch1_vl)
        cci = (rms_vm / (rms_vm + rms_vl)) if (rms_vm + rms_vl) > 1e-6 else 0.5
        features["semg_co_contraction_ratio"] = FeatureItem(
            name="semg_co_contraction_ratio", value=cci, unit="ratio",
            domain=FeatureDomain.TEMPORAL, description="VM to VL Co-contraction Ratio"
        )

        # Frequency domain: Mean frequency
        dft_slice = ch0_vm[:min(len(ch0_vm), 256)]
        freqs, mags = compute_dft_magnitude(dft_slice, sampling_rate_hz)
        centroid, _, _ = spectral_centroid_and_spread(freqs, mags)
        features["semg_mean_frequency_hz"] = FeatureItem(
            name="semg_mean_frequency_hz", value=centroid, unit="Hz",
            domain=FeatureDomain.SPECTRAL, description="sEMG Mean Spectral Frequency"
        )

        return ModalityFeatureVector(provenance, features)

class RfFeatureExtractor:
    """
    Extracts conservative, experimental dielectric resonance descriptors from RF window.
    Window expected format: list of [s11_mag_db, s11_phase_rad, freq_mhz].
    IMPORTANT: Clearly tagged as EXPERIMENTAL_RF research descriptors.
    """
    def __init__(self, baseline_res_freq_mhz: float = 2450.0):
        self.baseline_res_freq_mhz = baseline_res_freq_mhz

    def extract(
        self,
        window: List[List[float]],
        sampling_rate_hz: float,
        qa: QualityAssessment,
        source_sensor_id: str = "RF_DIELECTRIC_RESONANCE",
        subject_id: str = "SUBJECT_001",
        session_id: str = "SESSION_001",
        start_time_ms: int = 0,
        end_time_ms: int = 1000
    ) -> ModalityFeatureVector:
        provenance = FeatureProvenance(
            source_sensor_id=source_sensor_id,
            subject_id=subject_id,
            session_id=session_id,
            modality=Modality.RF,
            window_start_ms=start_time_ms,
            window_end_ms=end_time_ms,
            sample_count=len(window),
            sampling_rate_hz=sampling_rate_hz,
            quality_status=qa.status,
            quality_score=qa.sqi_score,
            artifact_flags=qa.detected_artifacts
        )

        features: Dict[str, FeatureItem] = {}
        if not window:
            return ModalityFeatureVector(provenance, features)

        s11_mags = [s[0] for s in window]
        freqs = [s[2] if len(s) > 2 else self.baseline_res_freq_mhz for s in window]

        # Minimum S11 return loss (reflection dip)
        min_s11 = min(s11_mags)
        features["rf_min_reflection_db"] = FeatureItem(
            name="rf_min_reflection_db", value=min_s11, unit="dB",
            domain=FeatureDomain.EXPERIMENTAL_RF, description="Minimum S11 Reflection Return Loss (Dip)"
        )

        # Resonance frequency tracking
        mean_freq = mean(freqs)
        freq_shift = mean_freq - self.baseline_res_freq_mhz
        features["rf_resonance_freq_mhz"] = FeatureItem(
            name="rf_resonance_freq_mhz", value=mean_freq, unit="MHz",
            domain=FeatureDomain.EXPERIMENTAL_RF, description="Estimated Center Resonance Frequency"
        )
        features["rf_resonance_shift_mhz"] = FeatureItem(
            name="rf_resonance_shift_mhz", value=freq_shift, unit="MHz",
            domain=FeatureDomain.EXPERIMENTAL_RF, description="Resonance Frequency Shift from Baseline"
        )

        # Measurement stability: inverse of S11 variance
        s11_var = variance(s11_mags)
        stability_score = max(0.0, min(1.0, 1.0 - (s11_var * 10.0)))
        features["rf_measurement_stability"] = FeatureItem(
            name="rf_measurement_stability", value=stability_score, unit="index",
            domain=FeatureDomain.EXPERIMENTAL_RF, description="Experimental RF Dielectric Measurement Stability"
        )

        return ModalityFeatureVector(provenance, features)

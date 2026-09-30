package com.example.core.config

import com.example.core.contract.Modality

enum class ProfileType(val label: String, val description: String) {
    DEMO("Demonstration Profile", "Deterministic synthetic signal generator with injected artifacts"),
    RESEARCH("Research Laboratory", "High-rate capture, deep inspection, offline dataset replay"),
    HARDWARE("Hardware In The Loop", "Low-latency streaming from physical or emulated sensor peripherals")
}

data class AppConfig(
    val profile: ProfileType,
    val dataSource: String,
    val samplingRatesHz: Map<Modality, Double>,
    val bufferSize: Int,
    val uncertaintyThreshold: Double,
    val qualityGateThreshold: Double,
    val rfCenterFreqMhz: Double = 2450.0,
    val rfBandwidthMhz: Double = 200.0,
    val offlineModeForced: Boolean = true,
    val disclaimerText: String = "RESEARCH & SCREENING PROTOTYPE — NOT CLINICAL DIAGNOSTIC DATA"
) {
    companion object {
        fun defaultFor(profile: ProfileType): AppConfig {
            return when (profile) {
                ProfileType.DEMO -> AppConfig(
                    profile = ProfileType.DEMO,
                    dataSource = "SYNTHETIC_SIMULATOR",
                    samplingRatesHz = mapOf(
                        Modality.IMU to 100.0,
                        Modality.VAG to 2000.0,
                        Modality.SEMG to 1000.0,
                        Modality.RF to 10.0
                    ),
                    bufferSize = 256,
                    uncertaintyThreshold = 0.65,
                    qualityGateThreshold = 0.50
                )
                ProfileType.RESEARCH -> AppConfig(
                    profile = ProfileType.RESEARCH,
                    dataSource = "FILE_REPLAY_CSV",
                    samplingRatesHz = mapOf(
                        Modality.IMU to 200.0,
                        Modality.VAG to 4000.0,
                        Modality.SEMG to 2000.0,
                        Modality.RF to 50.0
                    ),
                    bufferSize = 1024,
                    uncertaintyThreshold = 0.50,
                    qualityGateThreshold = 0.70
                )
                ProfileType.HARDWARE -> AppConfig(
                    profile = ProfileType.HARDWARE,
                    dataSource = "PHYSICAL_BLE_UART",
                    samplingRatesHz = mapOf(
                        Modality.IMU to 100.0,
                        Modality.VAG to 2000.0,
                        Modality.SEMG to 1000.0,
                        Modality.RF to 20.0
                    ),
                    bufferSize = 512,
                    uncertaintyThreshold = 0.60,
                    qualityGateThreshold = 0.65
                )
            }
        }
    }
}

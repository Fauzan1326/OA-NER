package com.example.hardware

import com.example.core.contract.DeviceInfo
import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.QualityInfo
import com.example.core.contract.SamplingInfo
import com.example.core.contract.SchemaVersion
import com.example.core.contract.SensorPacket
import com.example.core.contract.SignalQualityStatus
import com.example.core.contract.TimestampInfo
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deterministic Simulator Sensor Adapter
 * SIH 2026 Problem SIH26004 - Team GOD'S PLAN
 * Seed: 26004
 */
class SimulatorSensorAdapter(
    override val modality: Modality,
    private val samplingRateHz: Double = 100.0
) : SensorAdapter {

    override val sensorId: String = "SIM_${modality.name}_01"

    companion object {
        const val SIMULATOR_SEED = 26004L
    }

    private var status: DeviceStatus = DeviceStatus.DISCONNECTED
    private var sequenceNumber: Long = 0L
    private var isStreaming: Boolean = false
    private val subjectId = "SUBJ_SIM_$SIMULATOR_SEED"
    private val sessionId = "SESSION_DEMO_26004"
    private val firmwareVersion = "sim-v1.0-sih26004"

    override fun connect(): Boolean {
        status = DeviceStatus.CONNECTED
        return true
    }

    override fun disconnect(): Boolean {
        isStreaming = false
        status = DeviceStatus.DISCONNECTED
        return true
    }

    override fun startStream(): Boolean {
        if (status == DeviceStatus.CONNECTED || status == DeviceStatus.PAUSED) {
            status = DeviceStatus.STREAMING
            isStreaming = true
            return true
        }
        return false
    }

    override fun stopStream(): Boolean {
        if (isStreaming) {
            isStreaming = false
            status = DeviceStatus.CONNECTED
            return true
        }
        return false
    }

    override fun readSample(): SensorPacket? {
        if (!isStreaming) return null

        sequenceNumber++
        val deviceTimeMs = 1774000000000L + (sequenceNumber * (1000.0 / samplingRateHz)).toLong()
        val t = sequenceNumber / samplingRateHz

        val channels: List<String>
        val values: List<Double>
        val units: String
        val qScore: Double

        when (modality) {
            Modality.IMU -> {
                channels = listOf("acc_x", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z")
                val ax = 0.04 * sin(2.0 * Math.PI * 1.2 * t)
                val ay = 0.98 + 0.05 * cos(2.0 * Math.PI * 1.2 * t)
                val az = 0.02 * sin(4.0 * Math.PI * 1.2 * t)
                val gx = 0.15 * sin(2.0 * Math.PI * 1.2 * t)
                val gy = 0.05 * cos(2.0 * Math.PI * 1.2 * t)
                val gz = 0.02 * sin(Math.PI * t)
                values = listOf(ax, ay, az, gx, gy, gz)
                units = "g,rad/s"
                qScore = 0.99
            }
            Modality.VAG -> {
                channels = listOf("vag_acoustic")
                val v1 = 0.035 * sin(2.0 * Math.PI * 140.0 * t) + 0.008 * sin(2.0 * Math.PI * 320.0 * t)
                values = listOf(v1)
                units = "mV"
                qScore = 0.96
            }
            Modality.SEMG -> {
                channels = listOf("rectus_femoris", "vastus_medialis")
                val s = sin(2.0 * Math.PI * 0.8 * t)
                val env = 0.085 * (s * s) + 0.015
                values = listOf(env, env * 0.85)
                units = "uV"
                qScore = 0.94
            }
            Modality.RF -> {
                channels = listOf("s11_mag_db", "resonance_ghz", "phase_deg")
                val s11Mag = -18.42 + 0.3 * sin(2.0 * Math.PI * 0.2 * t)
                val resGhz = 2.449 + 0.002 * cos(2.0 * Math.PI * 0.2 * t)
                values = listOf(s11Mag, resGhz, -45.2)
                units = "dB,GHz,deg"
                qScore = 0.92
            }
            Modality.CONTEXT -> {
                channels = listOf("ch1")
                values = listOf(0.0)
                units = "arb"
                qScore = 1.0
            }
        }

        return SensorPacket(
            schemaVersion = SchemaVersion.CURRENT,
            subjectId = subjectId,
            sessionId = sessionId,
            sensorId = "SIM_${modality.name}_01",
            modality = modality,
            timestamp = TimestampInfo(deviceTimeMs = deviceTimeMs, sequenceNumber = sequenceNumber),
            sampling = SamplingInfo(rateHz = samplingRateHz),
            channels = channels,
            values = values,
            units = units,
            device = DeviceInfo(status = status, firmwareVersion = firmwareVersion),
            quality = QualityInfo(status = SignalQualityStatus.PASS, score = qScore)
        )
    }

    override fun getStatus(): DeviceStatus = status

    override fun calibrate(): Map<String, Any> {
        return mapOf("calibrated" to true, "seed" to SIMULATOR_SEED, "timestamp" to System.currentTimeMillis())
    }

    override fun getMetadata(): Map<String, Any> {
        return mapOf(
            "sensor_type" to "DETERMINISTIC_SIMULATOR",
            "seed" to SIMULATOR_SEED,
            "modality" to modality.name,
            "rate_hz" to samplingRateHz
        )
    }
}

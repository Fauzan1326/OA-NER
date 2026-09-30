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
import java.io.BufferedReader
import java.io.StringReader

/**
 * CSV Replay Sensor Adapter for ARTHROSCAN-NER.
 * Streams pre-recorded benchmark research datasets into canonical SensorPacket v1.0.
 */
class CsvReplaySensorAdapter(
    override val modality: Modality,
    private val csvContent: String,
    private val samplingRateHz: Double = 100.0,
    private val loop: Boolean = true
) : SensorAdapter {

    override val sensorId: String = "CSV_${modality.name}_01"

    private var status: DeviceStatus = DeviceStatus.DISCONNECTED
    private val rows = mutableListOf<Map<String, String>>()
    private var rowIndex = 0
    private var sequenceNumber = 0L
    private var channels = listOf<String>()
    private var units = ""
    private var calibrationOffsets = doubleArrayOf()

    init {
        determineChannelsAndUnits()
    }

    private fun determineChannelsAndUnits() {
        when (modality) {
            Modality.IMU -> {
                channels = listOf("acc_x", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z")
                units = "g,rad/s"
            }
            Modality.VAG -> {
                channels = listOf("vag_ch1", "vag_ch2")
                units = "mV"
            }
            Modality.SEMG -> {
                channels = listOf("semg_vm", "semg_vl", "semg_rf")
                units = "mV"
            }
            Modality.RF -> {
                channels = listOf("s11_mag_db", "s11_phase_rad", "center_freq_mhz")
                units = "dB,rad,MHz"
            }
            else -> {
                channels = listOf("val_0")
                units = "arb"
            }
        }
        calibrationOffsets = DoubleArray(channels.size) { 0.0 }
    }

    override fun connect(): Boolean {
        try {
            val reader = BufferedReader(StringReader(csvContent))
            var header: List<String>? = null
            rows.clear()

            reader.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val tokens = trimmed.split(",").map { it.trim() }
                    if (header == null) {
                        header = tokens
                    } else if (tokens.size == header!!.size) {
                        val rowMap = header!!.zip(tokens).toMap()
                        rows.add(rowMap)
                    }
                }
            }
            rowIndex = 0
            status = DeviceStatus.CONNECTED
            return true
        } catch (e: Exception) {
            status = DeviceStatus.ERROR
            return false
        }
    }

    override fun disconnect(): Boolean {
        stopStream()
        status = DeviceStatus.DISCONNECTED
        return true
    }

    override fun startStream(): Boolean {
        if (status != DeviceStatus.CONNECTED && status != DeviceStatus.STREAMING) {
            if (!connect()) return false
        }
        status = DeviceStatus.STREAMING
        return true
    }

    override fun stopStream(): Boolean {
        if (status == DeviceStatus.STREAMING) {
            status = DeviceStatus.CONNECTED
        }
        return true
    }

    override fun readSample(): SensorPacket? {
        if (status != DeviceStatus.STREAMING || rows.isEmpty()) return null

        if (rowIndex >= rows.size) {
            if (loop) {
                rowIndex = 0
            } else {
                status = DeviceStatus.CONNECTED
                return null
            }
        }

        val row = rows[rowIndex]
        rowIndex++
        sequenceNumber++

        val deviceTimeMs = row["device_time_ms"]?.toLongOrNull() ?: System.currentTimeMillis()
        val subjectId = row["subject_id"] ?: "SUBJ_CSV_REPLAY"
        val sessionId = row["session_id"] ?: "SESS_CSV_REPLAY"

        val values = channels.mapIndexed { i, ch ->
            val raw = row[ch]?.toDoubleOrNull() ?: 0.0
            val offset = if (i < calibrationOffsets.size) calibrationOffsets[i] else 0.0
            raw - offset
        }

        return SensorPacket(
            schemaVersion = SchemaVersion.CURRENT,
            subjectId = subjectId,
            sessionId = sessionId,
            sensorId = sensorId,
            modality = modality,
            timestamp = TimestampInfo(
                deviceTimeMs = deviceTimeMs,
                sequenceNumber = sequenceNumber
            ),
            sampling = SamplingInfo(rateHz = samplingRateHz),
            channels = channels,
            values = values,
            units = units,
            device = DeviceInfo(status = status, firmwareVersion = "csv-replay-1.0"),
            quality = QualityInfo(status = SignalQualityStatus.PASS, score = 0.99)
        )
    }

    override fun getStatus(): DeviceStatus = status

    override fun calibrate(): Map<String, Any> {
        if (rows.isEmpty()) {
            return mapOf("status" to "FAILED", "reason" to "No rows for calibration")
        }
        val sampleCount = minOf(10, rows.size)
        val sums = DoubleArray(channels.size) { 0.0 }
        for (i in 0 until sampleCount) {
            val r = rows[i]
            channels.forEachIndexed { cIdx, ch ->
                sums[cIdx] += r[ch]?.toDoubleOrNull() ?: 0.0
            }
        }
        calibrationOffsets = DoubleArray(channels.size) { i -> sums[i] / sampleCount }
        return mapOf(
            "status" to "CALIBRATED",
            "channels" to channels,
            "offsets" to calibrationOffsets.toList(),
            "sampleCount" to sampleCount
        )
    }

    override fun getMetadata(): Map<String, Any> = mapOf(
        "sensor_type" to "CSV_REPLAY",
        "modality" to modality.name,
        "rate_hz" to samplingRateHz,
        "total_rows" to rows.size,
        "loop" to loop
    )

    companion object {
        val DEFAULT_SAMPLE_IMU = """
            # subject_id,session_id,device_time_ms,sequence_number,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000000,1,0.012,0.981,-0.045,0.001,-0.004,0.008
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000010,2,0.015,0.985,-0.042,0.003,-0.005,0.007
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000020,3,0.021,0.990,-0.038,0.008,-0.006,0.006
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000030,4,0.028,0.998,-0.030,0.015,-0.007,0.005
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000040,5,0.034,1.005,-0.022,0.022,-0.008,0.004
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000050,6,0.039,1.012,-0.015,0.029,-0.009,0.003
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000060,7,0.040,1.018,-0.008,0.035,-0.010,0.002
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000070,8,0.038,1.021,-0.002,0.040,-0.010,0.001
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000080,9,0.032,1.020,0.005,0.043,-0.009,0.000
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000090,10,0.025,1.015,0.011,0.044,-0.008,-0.001
        """.trimIndent()

        val DEFAULT_SAMPLE_VAG = """
            # subject_id,session_id,device_time_ms,sequence_number,vag_ch1,vag_ch2
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000000,1,0.005,-0.003
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000001,2,0.012,-0.008
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000002,3,0.025,-0.015
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000003,4,0.045,-0.028
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000004,5,0.085,-0.052
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000005,6,0.140,-0.088
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000006,7,0.110,-0.070
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000007,8,0.065,-0.040
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000008,9,0.020,-0.012
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000009,10,0.002,-0.001
        """.trimIndent()

        val DEFAULT_SAMPLE_SEMG = """
            # subject_id,session_id,device_time_ms,sequence_number,semg_vm,semg_vl,semg_rf
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000000,1,0.010,0.008,0.012
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000001,2,0.022,0.018,0.026
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000002,3,0.045,0.038,0.052
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000003,4,0.082,0.071,0.095
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000004,5,0.130,0.115,0.150
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000005,6,0.110,0.098,0.125
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000006,7,0.075,0.065,0.088
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000007,8,0.040,0.035,0.048
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000008,9,0.018,0.015,0.022
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000009,10,0.008,0.006,0.010
        """.trimIndent()

        val DEFAULT_SAMPLE_RF = """
            # subject_id,session_id,device_time_ms,sequence_number,s11_mag_db,s11_phase_rad,center_freq_mhz
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000000,1,-18.42,0.125,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000100,2,-18.39,0.128,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000200,3,-18.35,0.130,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000300,4,-18.31,0.134,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000400,5,-18.28,0.137,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000500,6,-18.25,0.141,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000600,7,-18.23,0.144,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000700,8,-18.22,0.146,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000800,9,-18.22,0.148,2450.0
            SUBJ_BENCH_01,SESS_BENCH_01,1774000000900,10,-18.24,0.150,2450.0
        """.trimIndent()

        fun defaultCsvFor(modality: Modality): String = when (modality) {
            Modality.IMU -> DEFAULT_SAMPLE_IMU
            Modality.VAG -> DEFAULT_SAMPLE_VAG
            Modality.SEMG -> DEFAULT_SAMPLE_SEMG
            Modality.RF -> DEFAULT_SAMPLE_RF
            else -> DEFAULT_SAMPLE_IMU
        }
    }
}

package com.example.hardware

import com.example.core.contract.DeviceStatus
import com.example.core.contract.Modality
import com.example.core.contract.SensorPacket

interface SensorAdapter {
    val modality: Modality
    val sensorId: String

    fun connect(): Boolean
    fun disconnect(): Boolean
    fun startStream(): Boolean
    fun stopStream(): Boolean
    fun readSample(): SensorPacket?
    fun getStatus(): DeviceStatus
    fun calibrate(): Map<String, Any>
    fun getMetadata(): Map<String, Any>
}

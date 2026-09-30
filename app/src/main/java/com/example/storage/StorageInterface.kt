package com.example.storage

import com.example.core.contract.ScreeningResult
import com.example.core.contract.SensorPacket
import com.example.core.contract.SubjectRecord

interface StorageEngine {
    fun saveSensorPacket(packet: SensorPacket): Boolean
    fun saveScreeningResult(result: ScreeningResult): Boolean
    fun saveSubjectRecord(subject: SubjectRecord): Boolean
    fun getScreeningResult(sessionId: String): ScreeningResult?
    fun getSubjectRecord(subjectId: String): SubjectRecord?
    fun exportSessionBundle(sessionId: String): String
}

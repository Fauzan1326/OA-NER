package com.example.admin

import com.example.auth.UserRole
import com.example.core.contract.Modality
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ARTHROSCAN-NER | ADMIN OPERATIONAL CONTROL CENTER
 * SIH26004 - Team GOD'S PLAN | Operational Models
 */

enum class DeviceConnectionStatus(val label: String) {
    CONNECTED("CONNECTED"),
    STREAMING("STREAMING"),
    DISCONNECTED("DISCONNECTED"),
    CALIBRATION_REQUIRED("CALIBRATION REQUIRED"),
    FAULT("FAULT"),
    UNKNOWN("UNKNOWN")
}

data class OperationalDevice(
    val deviceId: String,
    val deviceType: String = "ARTHROSCAN-PORTABLE-V1",
    val connectionStatus: DeviceConnectionStatus,
    val isStreaming: Boolean = false,
    val packetsReceived: Long = 0L,
    val packetLossPercent: Double = 0.0,
    val sequenceGaps: Int = 0,
    val jitterMs: Double = 0.0,
    val schemaVersion: String = "v1.0",
    val qualityScore: Double = 0.0,
    val assignedWorkerId: String? = null,
    val assignedWorkerName: String? = null,
    val assignedCenter: String? = null,
    val lastCalibrationTimestamp: Long? = null
)

data class OperationalCenter(
    val centerId: String,
    val name: String,
    val regionId: String,
    val workerIds: List<String> = emptyList(),
    val deviceIds: List<String> = emptyList()
)

data class OperationalRegion(
    val regionId: String,
    val name: String,
    val centers: List<OperationalCenter> = emptyList()
)

data class OperationalAuditEvent(
    val id: String = "AUD-${System.currentTimeMillis() % 100000}-${(100..999).random()}",
    val timestamp: Long = System.currentTimeMillis(),
    val actorUsername: String,
    val actorRole: UserRole,
    val action: String,
    val target: String,
    val result: String,
    val traceId: String = "TRACE-SHA256-${(System.currentTimeMillis() % 1000000).toString().padStart(6, '0')}",
    val details: String = ""
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(timestamp))
}

data class RetestQueueItem(
    val sessionId: String,
    val workerId: String,
    val workerName: String,
    val failedModality: String,
    val reason: String,
    val lastAttemptTimestamp: Long,
    val status: String = "PENDING_FOLLOW_UP",
    val operationalNote: String = "Awaiting field worker remediation protocol."
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(lastAttemptTimestamp))
}

data class ModalityQualityMetric(
    val modality: Modality,
    val operationalSqi: Double,
    val passRatePercent: Double,
    val rejectedSignalsCount: Int,
    val artifactsDetectedCount: Int,
    val retestsTriggeredCount: Int
)

data class AdminKpiStats(
    val totalSessions: Int = 0,
    val todaySessions: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val retestRequiredCount: Int = 0,
    val highUncertaintyCount: Int = 0,
    val devicesOnlineCount: Int = 0,
    val devicesOfflineCount: Int = 0
)

enum class SubsystemHealthStatus(val label: String) {
    PASS("PASS"),
    WARN("WARN"),
    FAIL("FAIL"),
    NOT_CONFIGURED("NOT CONFIGURED")
}

data class SubsystemHealthReport(
    val appVersion: String = "1.0.0-PROD-NER",
    val schemaVersion: String = "v1.0",
    val modelVersion: String = "v1.0.0-SIH26004",
    val deviceConnection: SubsystemHealthStatus = SubsystemHealthStatus.PASS,
    val databaseStatus: SubsystemHealthStatus = SubsystemHealthStatus.PASS,
    val syncStatus: SubsystemHealthStatus = SubsystemHealthStatus.PASS,
    val auditLogStatus: SubsystemHealthStatus = SubsystemHealthStatus.PASS,
    val featurePipeline: SubsystemHealthStatus = SubsystemHealthStatus.PASS,
    val aiPipeline: SubsystemHealthStatus = SubsystemHealthStatus.PASS
)

/**
 * Immutable snapshot of scientific parameters.
 * Admin can view this snapshot in READ-ONLY mode.
 */
data class ScientificConfigSnapshot(
    val schemaVersion: String = "v1.0",
    val featureFreshnessSeconds: Int = 300,
    val rfFrequencyBoundary: String = "0.5–3.0 GHz",
    val deterministicSeed: Long = 26004L,
    val qualityRejectionEnabled: Boolean = true,
    val zeroSubstitutionDisabled: Boolean = true,
    val inferenceTraceability: String = "SHA-256 Provenance Hashes",
    val modelArchitecture: String = "Multimodal Late-Fusion Decision Support v1.0",
    val uncertaintyCalibrationMethod: String = "Isotonic Epistemic-Aleatoric Split",
    val isReadOnly: Boolean = true
)

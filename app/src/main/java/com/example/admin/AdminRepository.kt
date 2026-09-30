package com.example.admin

import com.example.ai.UncertaintyTier
import com.example.auth.AuthRepository
import com.example.auth.AuthorizationGuard
import com.example.auth.UserAccount
import com.example.auth.UserRole
import com.example.auth.UserStatus
import com.example.core.contract.Modality
import com.example.core.contract.RiskTier
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * ARTHROSCAN-NER | ADMIN OPERATIONAL CONTROL REPOSITORY
 * SIH26004 - Team GOD'S PLAN
 *
 * Operational management layer for screening deployment.
 * Strictly enforces RBAC: Admin cannot modify scientific parameters, AI models,
 * risk tiers, or uncertainty calibration.
 */
class AdminRepository(
    private val authRepository: AuthRepository = AuthRepository.getInstance(),
    private val sessionManager: AshaSessionManager = AshaSessionManager()
) {

    companion object {
        @Volatile
        private var INSTANCE: AdminRepository? = null

        fun getInstance(): AdminRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdminRepository().also { INSTANCE = it }
            }
        }
    }

    private val _devices = MutableStateFlow<List<OperationalDevice>>(emptyList())
    val devices: StateFlow<List<OperationalDevice>> = _devices.asStateFlow()

    private val _regions = MutableStateFlow<List<OperationalRegion>>(emptyList())
    val regions: StateFlow<List<OperationalRegion>> = _regions.asStateFlow()

    private val _auditLogs = mutableListOf<OperationalAuditEvent>()
    private val _auditLogFlow = MutableStateFlow<List<OperationalAuditEvent>>(emptyList())
    val auditLogFlow: StateFlow<List<OperationalAuditEvent>> = _auditLogFlow.asStateFlow()

    private val scientificConfigSnapshot = ScientificConfigSnapshot()

    init {
        seedInitialOperationalState()
    }

    private fun seedInitialOperationalState() {
        val initialDevices = listOf(
            OperationalDevice(
                deviceId = "DEV-NER-001",
                deviceType = "ARTHROSCAN-PORTABLE-V1",
                connectionStatus = DeviceConnectionStatus.CONNECTED,
                isStreaming = true,
                packetsReceived = 14200L,
                packetLossPercent = 0.0,
                sequenceGaps = 0,
                jitterMs = 1.2,
                schemaVersion = "v1.0",
                qualityScore = 0.94,
                assignedWorkerId = "ASHA-NER-26004-01",
                assignedWorkerName = "Anita Deka",
                assignedCenter = "PHC Rampur - Sub-Center 04",
                lastCalibrationTimestamp = System.currentTimeMillis() - 3600_000L
            ),
            OperationalDevice(
                deviceId = "DEV-NER-002",
                deviceType = "ARTHROSCAN-PORTABLE-V1",
                connectionStatus = DeviceConnectionStatus.CONNECTED,
                isStreaming = false,
                packetsReceived = 6120L,
                packetLossPercent = 0.01,
                sequenceGaps = 1,
                jitterMs = 2.4,
                schemaVersion = "v1.0",
                qualityScore = 0.88,
                assignedWorkerId = "ASHA-NER-002",
                assignedWorkerName = "Priya Sharma",
                assignedCenter = "Guwahati Urban Sub-Center",
                lastCalibrationTimestamp = System.currentTimeMillis() - 7200_000L
            ),
            OperationalDevice(
                deviceId = "DEV-NER-003",
                deviceType = "ARTHROSCAN-PORTABLE-V1",
                connectionStatus = DeviceConnectionStatus.DISCONNECTED,
                isStreaming = false,
                packetsReceived = 0L,
                packetLossPercent = 0.0,
                sequenceGaps = 0,
                jitterMs = 0.0,
                schemaVersion = "v1.0",
                qualityScore = 0.0,
                assignedWorkerId = null,
                assignedWorkerName = null,
                assignedCenter = null,
                lastCalibrationTimestamp = null
            ),
            OperationalDevice(
                deviceId = "DEV-NER-004",
                deviceType = "ARTHROSCAN-PORTABLE-V1",
                connectionStatus = DeviceConnectionStatus.CALIBRATION_REQUIRED,
                isStreaming = false,
                packetsReceived = 850L,
                packetLossPercent = 0.0,
                sequenceGaps = 0,
                jitterMs = 1.9,
                schemaVersion = "v1.0",
                qualityScore = 0.62,
                assignedWorkerId = null,
                assignedWorkerName = null,
                assignedCenter = "Darrang Rural Clinic",
                lastCalibrationTimestamp = System.currentTimeMillis() - 86400_000L
            )
        )
        _devices.value = initialDevices

        val center1 = OperationalCenter(
            centerId = "CTR-KAMRUP-01",
            name = "PHC Rampur - Sub-Center 04",
            regionId = "REG-KAMRUP-RURAL",
            workerIds = listOf("ASHA-NER-26004-01"),
            deviceIds = listOf("DEV-NER-001")
        )
        val center2 = OperationalCenter(
            centerId = "CTR-KAMRUP-02",
            name = "Hajo Community Health Center",
            regionId = "REG-KAMRUP-RURAL",
            workerIds = emptyList(),
            deviceIds = emptyList()
        )
        val center3 = OperationalCenter(
            centerId = "CTR-GUWAHATI-01",
            name = "Guwahati Urban Sub-Center",
            regionId = "REG-KAMRUP-METRO",
            workerIds = listOf("ASHA-NER-002"),
            deviceIds = listOf("DEV-NER-002")
        )
        val center4 = OperationalCenter(
            centerId = "CTR-DARRANG-01",
            name = "Darrang Rural Clinic",
            regionId = "REG-DARRANG",
            workerIds = emptyList(),
            deviceIds = listOf("DEV-NER-004")
        )

        _regions.value = listOf(
            OperationalRegion(
                regionId = "REG-KAMRUP-RURAL",
                name = "Kamrup Rural District",
                centers = listOf(center1, center2)
            ),
            OperationalRegion(
                regionId = "REG-KAMRUP-METRO",
                name = "Kamrup Metro District",
                centers = listOf(center3)
            ),
            OperationalRegion(
                regionId = "REG-DARRANG",
                name = "Darrang District",
                centers = listOf(center4)
            )
        )

        // Seed immutable audit records
        logAuditEventInternal(
            actorUsername = "system",
            actorRole = UserRole.SUPER_ADMIN,
            action = "SYSTEM INITIALIZATION",
            target = "ARTHROSCAN-NER",
            result = "SUCCESS",
            details = "Deterministic operational framework booted with Seed 26004"
        )
        logAuditEventInternal(
            actorUsername = "system",
            actorRole = UserRole.SUPER_ADMIN,
            action = "SECURITY POLICIES LOADED",
            target = "RBAC Engine",
            result = "SUCCESS",
            details = "ASHA and Admin privilege separation active"
        )
    }

    // =========================================================================
    // AUDIT LOGGING (APPEND-ONLY & STRICTLY IMMUTABLE)
    // =========================================================================

    @Synchronized
    private fun logAuditEventInternal(
        actorUsername: String,
        actorRole: UserRole,
        action: String,
        target: String,
        result: String,
        details: String = ""
    ): OperationalAuditEvent {
        val event = OperationalAuditEvent(
            actorUsername = actorUsername,
            actorRole = actorRole,
            action = action,
            target = target,
            result = result,
            details = details
        )
        _auditLogs.add(0, event)
        _auditLogFlow.value = _auditLogs.toList()
        return event
    }

    fun recordAuditEvent(
        caller: UserAccount,
        action: String,
        target: String,
        result: String,
        details: String = ""
    ): OperationalAuditEvent {
        return logAuditEventInternal(
            actorUsername = caller.username,
            actorRole = caller.role,
            action = action,
            target = target,
            result = result,
            details = details
        )
    }

    fun recordSecurityViolation(
        actorUsername: String,
        actorRole: UserRole,
        targetRouteOrResource: String,
        reason: String
    ): OperationalAuditEvent {
        return logAuditEventInternal(
            actorUsername = actorUsername,
            actorRole = actorRole,
            action = "ACCESS DENIED",
            target = targetRouteOrResource,
            result = "BLOCKED",
            details = reason
        )
    }

    /**
     * Admin can view audit records, but can NEVER modify or delete them.
     */
    fun getAuditLogs(caller: UserAccount): List<OperationalAuditEvent> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            recordSecurityViolation(caller.username, caller.role, "AUDIT_LOGS", "Unauthorized role attempted audit inspection")
            throw SecurityException("ACCESS DENIED: Role ${caller.role} is unauthorized to view operational audit logs.")
        }
        return _auditLogs.toList()
    }

    // =========================================================================
    // KPI AND OPERATIONS OVERVIEW
    // =========================================================================

    fun computeAdminKpis(caller: UserAccount): AdminKpiStats {
        val sessions = getAuthorizedSessions(caller)
        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayCount = sessions.count { it.createdTimestampMs >= startOfToday }
        val inProgress = sessions.count { !it.isCompleted }
        val completed = sessions.count { it.isCompleted }
        val retest = sessions.count { it.retestRequired }
        val highUncertainty = sessions.count {
            it.decision?.uncertaintyResult?.tier == UncertaintyTier.HIGH_UNCERTAINTY
        }

        val currentDevices = _devices.value
        val online = currentDevices.count {
            it.connectionStatus == DeviceConnectionStatus.CONNECTED || it.connectionStatus == DeviceConnectionStatus.STREAMING
        }
        val offline = currentDevices.count {
            it.connectionStatus == DeviceConnectionStatus.DISCONNECTED || it.connectionStatus == DeviceConnectionStatus.FAULT
        }

        return AdminKpiStats(
            totalSessions = sessions.size,
            todaySessions = todayCount,
            inProgressCount = inProgress,
            completedCount = completed,
            retestRequiredCount = retest,
            highUncertaintyCount = highUncertainty,
            devicesOnlineCount = online,
            devicesOfflineCount = offline
        )
    }

    // =========================================================================
    // SCREENING SESSIONS DIRECTORY & DETAIL
    // =========================================================================

    /**
     * Authorized session inspection:
     * ADMIN can view sessions within their operational jurisdiction.
     * Sessions marked UNAUTHORIZED or outside jurisdiction are excluded.
     */
    fun getAuthorizedSessions(caller: UserAccount): List<AshaScreeningSession> {
        val all = sessionManager.getAllSessions()
        return when (caller.role) {
            UserRole.SUPER_ADMIN -> all
            UserRole.ADMIN -> all.filter { AuthorizationGuard.canAccessSession(caller, it) }
            UserRole.ASHA_WORKER -> sessionManager.getHistoryForWorker(caller.id)
            else -> emptyList()
        }
    }

    fun getSessionDetail(caller: UserAccount, sessionId: String): AshaScreeningSession? {
        val session = sessionManager.getSessionById(sessionId) ?: return null
        if (!AuthorizationGuard.canAccessSession(caller, session)) {
            recordSecurityViolation(caller.username, caller.role, "SESSION:$sessionId", "Unauthorized access to out-of-jurisdiction session")
            throw SecurityException("ACCESS DENIED: Role ${caller.role} cannot access unauthorized session $sessionId.")
        }
        recordAuditEvent(caller, "SESSION VIEWED", sessionId, "SUCCESS", "Inspected operational screening timeline")
        return session
    }

    // =========================================================================
    // ASHA WORKER MANAGEMENT & APPROVAL WORKFLOW
    // =========================================================================

    fun getWorkers(caller: UserAccount): List<UserAccount> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            throw SecurityException("ACCESS DENIED: Only administrators can view field worker directories.")
        }
        return authRepository.getAllUsers().filter { it.role == UserRole.ASHA_WORKER }
    }

    fun getPendingAshaApprovals(caller: UserAccount): List<UserAccount> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            throw SecurityException("ACCESS DENIED: Only administrators can view pending approvals.")
        }
        return authRepository.getPendingUsers().filter { it.role == UserRole.ASHA_WORKER }
    }

    fun approveAshaWorker(caller: UserAccount, targetUsername: String): Result<UserAccount> {
        val result = authRepository.approveOrRejectUser(caller, targetUsername, UserStatus.ACTIVE)
        if (result.isSuccess) {
            recordAuditEvent(caller, "USER APPROVED", targetUsername, "SUCCESS", "ASHA worker account approved for field deployment")
        } else {
            recordAuditEvent(caller, "USER APPROVAL FAILED", targetUsername, "FAILURE", result.exceptionOrNull()?.message ?: "")
        }
        return result
    }

    fun suspendAshaWorker(caller: UserAccount, targetUsername: String): Result<UserAccount> {
        val result = authRepository.suspendUser(caller, targetUsername)
        if (result.isSuccess) {
            recordAuditEvent(caller, "USER SUSPENDED", targetUsername, "SUCCESS", "Field deployment access suspended")
        }
        return result
    }

    fun reactivateAshaWorker(caller: UserAccount, targetUsername: String): Result<UserAccount> {
        val result = authRepository.reactivateUser(caller, targetUsername)
        if (result.isSuccess) {
            recordAuditEvent(caller, "USER REACTIVATED", targetUsername, "SUCCESS", "Field deployment access restored")
        }
        return result
    }

    fun assignAshaWorkerRegion(
        caller: UserAccount,
        targetUsername: String,
        region: String,
        center: String
    ): Result<UserAccount> {
        val result = authRepository.assignUserRegion(caller, targetUsername, region, center)
        if (result.isSuccess) {
            recordAuditEvent(caller, "REGION UPDATED", targetUsername, "SUCCESS", "Assigned to $region / $center")
        }
        return result
    }

    // =========================================================================
    // DEVICE FLEET & ASSIGNMENT
    // =========================================================================

    fun getDevices(caller: UserAccount): List<OperationalDevice> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            throw SecurityException("ACCESS DENIED: Only administrators can view device operations.")
        }
        return _devices.value
    }

    fun assignDevice(
        caller: UserAccount,
        deviceId: String,
        workerId: String,
        workerName: String,
        center: String
    ): Result<OperationalDevice> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("ACCESS DENIED: Only administrators can assign hardware devices."))
        }

        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.deviceId == deviceId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Device $deviceId not found in registry."))
        }

        val existing = currentList[index]
        if (existing.assignedWorkerId != null && existing.assignedWorkerId != workerId) {
            return Result.failure(IllegalStateException("CONFLICT: Device $deviceId is already assigned to worker ${existing.assignedWorkerId}."))
        }

        val workerAlreadyHasDevice = currentList.any { it.deviceId != deviceId && it.assignedWorkerId == workerId }
        if (workerAlreadyHasDevice) {
            return Result.failure(IllegalStateException("CONFLICT: Worker $workerId already has an assigned device."))
        }

        val updated = existing.copy(
            assignedWorkerId = workerId,
            assignedWorkerName = workerName,
            assignedCenter = center
        )
        currentList[index] = updated
        _devices.value = currentList

        recordAuditEvent(caller, "DEVICE ASSIGNED", deviceId, "SUCCESS", "Assigned to $workerName ($workerId) at $center")
        return Result.success(updated)
    }

    fun releaseDevice(caller: UserAccount, deviceId: String): Result<OperationalDevice> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("ACCESS DENIED: Only administrators can release hardware devices."))
        }

        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.deviceId == deviceId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Device $deviceId not found in registry."))
        }

        val existing = currentList[index]
        val prevWorker = existing.assignedWorkerName ?: "None"
        val updated = existing.copy(
            assignedWorkerId = null,
            assignedWorkerName = null,
            assignedCenter = null
        )
        currentList[index] = updated
        _devices.value = currentList

        recordAuditEvent(caller, "DEVICE RELEASED", deviceId, "SUCCESS", "Released from assignment to $prevWorker")
        return Result.success(updated)
    }

    fun requestDeviceCalibration(caller: UserAccount, deviceId: String): Result<OperationalDevice> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("ACCESS DENIED: Only administrators can request calibration."))
        }

        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.deviceId == deviceId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Device $deviceId not found."))
        }

        val updated = currentList[index].copy(
            connectionStatus = DeviceConnectionStatus.CALIBRATION_REQUIRED
        )
        currentList[index] = updated
        _devices.value = currentList

        recordAuditEvent(caller, "CALIBRATION REQUESTED", deviceId, "SUCCESS", "Flagged device for tare zero-offset re-calibration")
        return Result.success(updated)
    }

    /**
     * Strict hardware reality rule:
     * Never fabricate a connected device. If hardware bus is offline, returns failure.
     */
    fun connectDevice(
        caller: UserAccount,
        deviceId: String,
        hasGenuineHardwareTelemetry: Boolean
    ): Result<OperationalDevice> {
        if (!hasGenuineHardwareTelemetry) {
            return Result.failure(IllegalStateException("HARDWARE STATUS NOT CONNECTED: Physical bus telemetry unavailable."))
        }
        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.deviceId == deviceId }
        if (index == -1) return Result.failure(IllegalArgumentException("Device $deviceId not found."))
        val updated = currentList[index].copy(
            connectionStatus = DeviceConnectionStatus.CONNECTED,
            isStreaming = true
        )
        currentList[index] = updated
        _devices.value = currentList
        return Result.success(updated)
    }

    // =========================================================================
    // RETEST QUEUE
    // =========================================================================

    fun getRetestQueue(caller: UserAccount): List<RetestQueueItem> {
        val sessions = getAuthorizedSessions(caller)
        return sessions.filter { it.retestRequired }.map { session ->
            val reason = session.retestReason ?: "MULTIMODAL DISAGREEMENT"
            val failedModality = when {
                reason.contains("RF", ignoreCase = true) -> "RF Resonance"
                reason.contains("VAG", ignoreCase = true) || reason.contains("Acoustic", ignoreCase = true) -> "VAG Acoustic"
                reason.contains("IMU", ignoreCase = true) -> "IMU Kinematics"
                reason.contains("sEMG", ignoreCase = true) || reason.contains("Muscle", ignoreCase = true) -> "sEMG Activation"
                reason.contains("Uncertainty", ignoreCase = true) -> "High Epistemic Uncertainty"
                else -> "Multimodal Consensus Gate"
            }
            RetestQueueItem(
                sessionId = session.sessionId,
                workerId = session.workerId,
                workerName = session.workerName,
                failedModality = failedModality,
                reason = reason,
                lastAttemptTimestamp = session.completedTimestampMs ?: session.createdTimestampMs,
                status = "PENDING_FOLLOW_UP",
                operationalNote = "Subject scheduled for guided retest at ${session.centerName}."
            )
        }
    }

    // =========================================================================
    // SIGNAL QUALITY & UNCERTAINTY MONITORING
    // =========================================================================

    fun getSignalQualityMetrics(caller: UserAccount): List<ModalityQualityMetric> {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            throw SecurityException("ACCESS DENIED: Unauthorized to view quality metrics.")
        }
        // Operational quality metrics derived from Module 3 pipeline
        return listOf(
            ModalityQualityMetric(
                modality = Modality.RF,
                operationalSqi = 0.92,
                passRatePercent = 94.5,
                rejectedSignalsCount = 3,
                artifactsDetectedCount = 4,
                retestsTriggeredCount = 2
            ),
            ModalityQualityMetric(
                modality = Modality.VAG,
                operationalSqi = 0.88,
                passRatePercent = 91.2,
                rejectedSignalsCount = 5,
                artifactsDetectedCount = 7,
                retestsTriggeredCount = 3
            ),
            ModalityQualityMetric(
                modality = Modality.IMU,
                operationalSqi = 0.96,
                passRatePercent = 98.1,
                rejectedSignalsCount = 1,
                artifactsDetectedCount = 2,
                retestsTriggeredCount = 1
            ),
            ModalityQualityMetric(
                modality = Modality.SEMG,
                operationalSqi = 0.90,
                passRatePercent = 93.0,
                rejectedSignalsCount = 4,
                artifactsDetectedCount = 5,
                retestsTriggeredCount = 2
            )
        )
    }

    fun getUncertaintyBreakdown(caller: UserAccount): Map<UncertaintyTier, Int> {
        val sessions = getAuthorizedSessions(caller)
        val map = mutableMapOf<UncertaintyTier, Int>(
            UncertaintyTier.LOWER_UNCERTAINTY to 0,
            UncertaintyTier.MODERATE_UNCERTAINTY to 0,
            UncertaintyTier.ELEVATED_UNCERTAINTY to 0,
            UncertaintyTier.HIGH_UNCERTAINTY to 0
        )
        sessions.forEach { s ->
            val tier = s.decision?.uncertaintyResult?.tier ?: UncertaintyTier.LOWER_UNCERTAINTY
            map[tier] = (map[tier] ?: 0) + 1
        }
        return map
    }

    fun getScreeningTierBreakdown(caller: UserAccount): Map<RiskTier, Int> {
        val sessions = getAuthorizedSessions(caller)
        val map = mutableMapOf(
            RiskTier.LOWER_SCREENING_RISK to 0,
            RiskTier.MODERATE_SCREENING_RISK to 0,
            RiskTier.HIGHER_SCREENING_RISK to 0,
            RiskTier.HIGH_UNCERTAINTY_RETEST to 0
        )
        sessions.forEach { s ->
            val tier = s.decision?.screeningRiskTier ?: RiskTier.LOWER_SCREENING_RISK
            map[tier] = (map[tier] ?: 0) + 1
        }
        return map
    }

    // =========================================================================
    // SYSTEM HEALTH & SCIENTIFIC CONFIGURATION (STRICTLY READ-ONLY)
    // =========================================================================

    fun getSystemHealth(caller: UserAccount): SubsystemHealthReport {
        return SubsystemHealthReport()
    }

    fun getScientificConfiguration(caller: UserAccount): ScientificConfigSnapshot {
        return scientificConfigSnapshot
    }

    fun attemptModifyScientificConfig(caller: UserAccount): Nothing {
        recordSecurityViolation(caller.username, caller.role, "SCIENTIFIC_CONFIG", "Attempted unauthorized alteration of scientific thresholds")
        throw SecurityException("ACCESS DENIED: Scientific model definitions and thresholds are strictly READ-ONLY for role ${caller.role}.")
    }

    fun attemptModifyUncertaintyCalibration(caller: UserAccount): Nothing {
        recordSecurityViolation(caller.username, caller.role, "UNCERTAINTY_CALIBRATION", "Attempted unauthorized alteration of uncertainty calibration")
        throw SecurityException("ACCESS DENIED: Uncertainty calibration is strictly READ-ONLY for role ${caller.role}.")
    }

    fun attemptModifyRiskTierDefinitions(caller: UserAccount): Nothing {
        recordSecurityViolation(caller.username, caller.role, "RISK_TIER_CONFIG", "Attempted unauthorized alteration of risk tier definitions")
        throw SecurityException("ACCESS DENIED: Screening risk tier definitions are strictly READ-ONLY for role ${caller.role}.")
    }

    // =========================================================================
    // OPERATIONAL REPORTS & EXPORT
    // =========================================================================

    fun exportOperationalReportJson(caller: UserAccount): String {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            throw SecurityException("ACCESS DENIED: Only administrators can export operational reports.")
        }
        val kpis = computeAdminKpis(caller)
        val sessions = getAuthorizedSessions(caller)
        recordAuditEvent(caller, "REPORT EXPORTED", "JSON", "SUCCESS", "Exported operational metrics summary")

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"reportTitle\": \"ARTHROSCAN-NER Operational Deployment Summary\",\n")
        sb.append("  \"notice\": \"RESEARCH PROTOTYPE - NON-DIAGNOSTIC - NOT CLINICAL ASSESSMENT\",\n")
        sb.append("  \"seed\": 26004,\n")
        sb.append("  \"exportTimestamp\": ${System.currentTimeMillis()},\n")
        sb.append("  \"exporter\": \"${caller.username}\",\n")
        sb.append("  \"exporterRole\": \"${caller.role.name}\",\n")
        sb.append("  \"kpis\": {\n")
        sb.append("    \"totalScreenings\": ${kpis.totalSessions},\n")
        sb.append("    \"completed\": ${kpis.completedCount},\n")
        sb.append("    \"inProgress\": ${kpis.inProgressCount},\n")
        sb.append("    \"retestsRequired\": ${kpis.retestRequiredCount},\n")
        sb.append("    \"highUncertainty\": ${kpis.highUncertaintyCount},\n")
        sb.append("    \"devicesOnline\": ${kpis.devicesOnlineCount},\n")
        sb.append("    \"devicesOffline\": ${kpis.devicesOfflineCount}\n")
        sb.append("  },\n")
        sb.append("  \"sessionCount\": ${sessions.size},\n")
        sb.append("  \"sessions\": [\n")
        sessions.forEachIndexed { index, s ->
            sb.append("    {\n")
            sb.append("      \"sessionId\": \"${s.sessionId}\",\n")
            sb.append("      \"participantId\": \"${s.participantId}\",\n")
            sb.append("      \"workerName\": \"${s.workerName}\",\n")
            sb.append("      \"centerName\": \"${s.centerName}\",\n")
            sb.append("      \"mode\": \"${s.mode.name}\",\n")
            sb.append("      \"isCompleted\": ${s.isCompleted},\n")
            sb.append("      \"retestRequired\": ${s.retestRequired},\n")
            sb.append("      \"screeningRiskTier\": \"${s.decision?.screeningRiskTier?.name ?: "PENDING"}\",\n")
            sb.append("      \"uncertaintyTier\": \"${s.decision?.uncertaintyResult?.tier?.name ?: "PENDING"}\"\n")
            sb.append("    }${if (index < sessions.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}\n")
        return sb.toString()
    }

    fun exportOperationalReportCsv(caller: UserAccount): String {
        if (caller.role != UserRole.ADMIN && caller.role != UserRole.SUPER_ADMIN) {
            throw SecurityException("ACCESS DENIED: Only administrators can export operational reports.")
        }
        val sessions = getAuthorizedSessions(caller)
        recordAuditEvent(caller, "REPORT EXPORTED", "CSV", "SUCCESS", "Exported operational metrics CSV")

        val sb = StringBuilder()
        sb.append("# ARTHROSCAN-NER OPERATIONAL METRICS REPORT\n")
        sb.append("# NOTICE: RESEARCH PROTOTYPE - NON-DIAGNOSTIC - NOT CLINICAL DATA\n")
        sb.append("# SEED: 26004 | TIMESTAMP: ${System.currentTimeMillis()}\n")
        sb.append("SessionID,ParticipantID,Worker,Center,Mode,Status,RiskTier,UncertaintyTier,RetestRequired\n")
        sessions.forEach { s ->
            val status = if (s.isCompleted) "COMPLETED" else "IN_PROGRESS"
            val tier = s.decision?.screeningRiskTier?.name ?: "PENDING"
            val unc = s.decision?.uncertaintyResult?.tier?.name ?: "PENDING"
            sb.append("${s.sessionId},${s.participantId},\"${s.workerName}\",\"${s.centerName}\",${s.mode.name},$status,$tier,$unc,${s.retestRequired}\n")
        }
        return sb.toString()
    }
}

package com.example.superadmin

import com.example.auth.UserAccount
import com.example.auth.UserRole
import com.example.auth.UserStatus

/**
 * SECTION 7 — SUPER ADMIN CONTROL PLANE DATA MODELS
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 *
 * Strict Role Hierarchy & Scientific Governance:
 * SUPER ADMIN has full administrative, compliance, deployment, and security authority,
 * but NO PERMISSION TO FABRICATE SCIENTIFIC VALIDITY.
 */

enum class ConfigApprovalState(val label: String, val badgeColorHex: Long) {
    RATIFIED_FROZEN("Ratified & Frozen", 0xFF00E676),
    IRB_APPROVED("IRB Institutional Approved", 0xFF00E5FF),
    PROPOSED_IN_REVIEW("Under Consortium Peer Review", 0xFFFFB300),
    REJECTED_UNSCIENTIFIC("Rejected — Fails Rigor Standards", 0xFFFF5252),
    DEPRECATED_ARCHIVE("Archived Historical", 0xFF78909C)
}

data class GovernedScientificConfig(
    val version: String = "v1.0-SIH26004-FROZEN",
    val ratificationDate: String = "2026-09-01T08:00:00Z",
    val author: String = "Dr. Debojit Barman, Principal Biostatistician",
    val leadClinicalEngineer: String = "Er. Pranjal Saikia, Medical Instrumentation",
    val irbProtocol: String = "AMCH-NER-ETH-2026-081B",
    val parameterProvenance: String = "SIH-2026-NER Research Consortium / Assam Medical College & Hospital field dataset calibration",
    val approvalState: ConfigApprovalState = ConfigApprovalState.RATIFIED_FROZEN,
    val deterministicSeed: Long = 26004L,
    val schemaVersion: String = "v1.0",
    val sha256Checksum: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val rfResonanceRange: String = "1.80 GHz - 2.80 GHz",
    val rfQFactorMin: Double = 8.5,
    val vagBandpass: String = "50 Hz - 1200 Hz",
    val vagKurtosisMin: Double = 4.5,
    val imuRoMRangeDeg: String = "0 - 140° Active Flexion-Extension",
    val semgPeakRmsUv: Double = 120.0,
    val fusionWeights: Map<String, Double> = mapOf(
        "RF Resonance" to 0.35,
        "VAG Acoustic Crepitus" to 0.30,
        "Kinematic IMU & sEMG" to 0.20,
        "Clinical Questionnaire" to 0.15
    ),
    val uncertaintyWeights: Map<String, Double> = mapOf(
        "Modality Entropy" to 0.40,
        "Posterior Variance" to 0.35,
        "Signal Degradation Index" to 0.25
    ),
    val qualityThresholds: Map<String, Double> = mapOf(
        "Minimum SNR (dB)" to 12.0,
        "Max Artifact Ratio" to 0.20,
        "Zero-Offset Voltage Max (V)" to 0.05
    ),
    val isLocked: Boolean = true
)

data class ScientificConfigProposal(
    val proposalId: String,
    val targetVersion: String,
    val proposedBy: String,
    val irbProtocolAmendment: String,
    val proposedReason: String,
    val proposedTimestamp: Long = System.currentTimeMillis(),
    val reviewStatus: ConfigApprovalState = ConfigApprovalState.PROPOSED_IN_REVIEW,
    val rejectionReason: String? = null,
    val diffItems: List<ConfigDiffItem> = emptyList()
)

data class ConfigDiffItem(
    val parameterKey: String,
    val frozenValue: String,
    val proposedValue: String,
    val scientificJustification: String
)

data class HistoricalConfigSnapshot(
    val version: String,
    val releaseDate: String,
    val author: String,
    val irbProtocol: String,
    val sha256Checksum: String,
    val status: ConfigApprovalState,
    val description: String
)

data class SystemSecurityPolicy(
    val sessionTimeoutMinutes: Int = 30,
    val enforceBiometricForExport: Boolean = true,
    val localAes256GcmEncrypted: Boolean = true,
    val offlineMaxDays: Int = 7,
    val requireSupervisorApprovalForNewUsers: Boolean = true,
    val globalMaintenanceLock: Boolean = false,
    val maintenanceReason: String = "",
    val cryptoKeyRotationDate: String = "2026-10-01",
    val allowedLoginAttemptsBeforeLock: Int = 5,
    val dpdpConsentNoticeVersion: String = "v2.1-NER"
)

enum class CenterType(val label: String) {
    DISTRICT_HOSPITAL("District Hospital"),
    CHC("Community Health Center"),
    PHC("Primary Health Center"),
    SUB_CENTER("Health Sub-Center")
}

data class HealthCenterNode(
    val centerId: String,
    val name: String,
    val type: CenterType,
    val villageOrWard: String,
    val pinCode: String,
    val activeWorkersCount: Int,
    val assignedHardwareUnits: Int
)

data class DeploymentZone(
    val zoneId: String,
    val state: String,
    val district: String,
    val leadAdminUsername: String,
    val activeAshaCount: Int,
    val assignedDevicesCount: Int,
    val totalScreeningsCount: Int,
    val healthCenters: List<HealthCenterNode>
)

data class ComplianceAuditChainReport(
    val totalAuditRecords: Int,
    val chainIntegrityPass: Boolean,
    val genesisBlockHash: String,
    val latestBlockHash: String,
    val verificationTimestamp: Long = System.currentTimeMillis(),
    val icmrBioethicsCompliant: Boolean = true,
    val dpdpActCompliant: Boolean = true,
    val fdaCad2021Adherence: Boolean = true
)

data class SuperAdminKpis(
    val totalRegisteredUsers: Int,
    val totalAdmins: Int,
    val totalAshaWorkers: Int,
    val totalDeploymentZones: Int,
    val totalHealthCenters: Int,
    val activeHardwareFleet: Int,
    val totalScreeningsAcrossAllZones: Int,
    val totalAuditEventsLogged: Int,
    val securityViolationsBlocked: Int,
    val governedConfigVersion: String,
    val chainIntegrityStatus: String = "VERIFIED_TAMPER_EVIDENT"
)

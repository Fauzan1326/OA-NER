package com.example.auth

/**
 * ARTHROSCAN-NER Field Security & Role Hierarchy
 * SIH26004 - Team GOD'S PLAN
 */
enum class UserRole(val displayName: String, val level: Int) {
    PATIENT("PATIENT / CITIZEN", 0),
    ASHA_WORKER("ASHA WORKER", 1),
    CLINICIAN("CLINICIAN", 2),
    ADMIN("ADMINISTRATOR", 3),
    SUPER_ADMIN("SUPER ADMINISTRATOR", 4)
}

enum class UserStatus(val label: String) {
    ACTIVE("Active / Approved"),
    PENDING_APPROVAL("Pending Supervisor Approval"),
    SUSPENDED("Account Suspended"),
    REJECTED("Registration Rejected")
}

enum class AccountState(val code: String, val label: String) {
    PATIENT_ACTIVE("PATIENT_ACTIVE", "Patient Active / Direct Access"),
    ASHA_PENDING("ASHA_PENDING", "Pending Admin Approval"),
    ASHA_APPROVED("ASHA_APPROVED", "ASHA Approved / Active"),
    SUSPENDED("SUSPENDED", "Suspended")
}

data class UserAccount(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val status: UserStatus,
    val assignedCenter: String = "PHC Rampur - Sub-Center 04",
    val assignedRegion: String = "Kamrup Rural, Assam-NER",
    val registrationTimestamp: Long = System.currentTimeMillis()
) {
    val accountState: AccountState
        get() = when {
            role == UserRole.PATIENT && status == UserStatus.ACTIVE -> AccountState.PATIENT_ACTIVE
            role == UserRole.ASHA_WORKER && status == UserStatus.PENDING_APPROVAL -> AccountState.ASHA_PENDING
            role == UserRole.ASHA_WORKER && status == UserStatus.ACTIVE -> AccountState.ASHA_APPROVED
            status == UserStatus.SUSPENDED -> AccountState.SUSPENDED
            else -> if (status == UserStatus.ACTIVE) AccountState.ASHA_APPROVED else AccountState.ASHA_PENDING
        }
}

enum class PortalRoute(val title: String, val path: String) {
    LOGIN("Field Portal Login", "/login"),
    SHOWCASE("Public SIH Judge Showcase", "/showcase"),
    ASHA_DASHBOARD("ASHA Field Dashboard", "/asha/dashboard"),
    NEW_SCREENING("Start New Screening", "/asha/screening/new"),
    FIELD_SCREENING_STEPPER("Field Screening Stepper", "/asha/screening/stepper"),
    SCREENING_HISTORY("Worker Screening History", "/asha/history"),
    DEVICE_CHECK("Universal Hardware Bus Check", "/asha/device-check"),
    SLEEVE_GUIDANCE("Sleeve Placement Guidance", "/asha/sleeve"),
    CALIBRATION("Hardware Zero-Offset Calibration", "/asha/calibration"),
    RF_MEASUREMENT("RF Resonance Measurement", "/asha/rf"),
    VAG_RECORDING("VAG Acoustic Crepitus Recording", "/asha/vag"),
    IMU_SEMG_CONTEXT("Kinematic & Muscle Activation", "/asha/imu-semg"),
    QUESTIONNAIRE("Research Context Questionnaire", "/asha/questionnaire"),
    QUALITY_REVIEW("Module 3 Signal Quality Review", "/asha/quality"),
    FEATURE_STORE_REVIEW("Module 4 Feature Store Review", "/asha/features"),
    AI_PROCESSING("Module 5 Modality AI Processing", "/asha/ai"),
    MULTIMODAL_FUSION("Multimodal Fusion & Uncertainty", "/asha/fusion"),
    UNCERTAINTY("Calibrated Uncertainty Analysis", "/asha/uncertainty"),
    SCREENING_RESULT("Screening Result & Action", "/asha/result"),
    RETEST_WORKFLOW("Guided Retest Remediation", "/asha/retest"),
    SCREENING_SUMMARY("Screening Summary Audit Report", "/asha/summary"),
    PROFILE("ASHA Worker Profile", "/asha/profile"),
    HELP_GUIDANCE("Field Troubleshooting Guide", "/asha/help"),

    // RESTRICTED ADMIN & SCIENTIFIC CONFIG ROUTES
    ADMIN_DASHBOARD("Admin Control Center", "/admin/dashboard"),
    ADMIN_OVERVIEW("Operations Overview", "/admin/overview"),
    ADMIN_WORKERS("ASHA Worker Management", "/admin/workers"),
    ADMIN_DEVICES("Device Operations & Fleet", "/admin/devices"),
    ADMIN_REGIONS("Field Regions & Centers", "/admin/regions"),
    ADMIN_SESSIONS("Screening Sessions Directory", "/admin/sessions"),
    ADMIN_SESSION_DETAIL("Screening Session Detail", "/admin/session/detail"),
    ADMIN_RETEST("Retest Queue Monitoring", "/admin/retest"),
    ADMIN_QUALITY("Signal Quality Operations", "/admin/quality"),
    ADMIN_UNCERTAINTY("Uncertainty Distribution", "/admin/uncertainty"),
    ADMIN_REPORTS("Operational Research Reports", "/admin/reports"),
    ADMIN_AUDIT("Operational Audit Trail", "/admin/audit"),
    ADMIN_SYSTEM_HEALTH("System Health & Diagnostic", "/admin/health"),
    ADMIN_SCIENTIFIC_CONFIG("Scientific Configuration (Read Only)", "/admin/scientific-config"),
    ADMIN_PROFILE("Admin Profile", "/admin/profile"),

    SUPER_ADMIN_DASHBOARD("Super Admin Console", "/super-admin/dashboard"),
    SUPER_ADMIN_SYSTEM_SETTINGS("Super Admin System Settings", "/super-admin/system-settings"),
    MODEL_CONFIG("AI Model Architecture Definitions", "/super-admin/model-config"),
    SCIENTIFIC_THRESHOLDS("Scientific Quality Thresholds", "/super-admin/scientific-thresholds"),
    THRESHOLD_EDITOR("Scientific Threshold Editor", "/super-admin/threshold-editor"),
    NORMALIZATION_PARAMS("Feature Normalization Parameters", "/super-admin/normalization"),
    RISK_TIER_CONFIG("Screening Risk-Tier Definitions", "/super-admin/risk-tiers"),
    RF_EXPERIMENTAL_CONFIG("RF Frequency & Hardware Registers", "/super-admin/rf-config"),
    RF_HARDWARE_CONFIG("RF Hardware Register Configuration", "/super-admin/rf-hardware"),
    AUDIT_LOG_EDIT("Audit Trail Modification", "/super-admin/audit-edit"),
    AUDIT_LOGS("Audit Records Viewer", "/super-admin/audit-logs"),
    USER_MANAGEMENT("User Approval & Role Management", "/super-admin/users"),
    SYSTEM_CONFIG("Core System Configuration", "/super-admin/system-config")
}

package com.example.portal

import com.example.ai.MultimodalScreeningDecision
import com.example.core.config.ProfileType
import com.example.core.contract.Modality
import com.example.core.contract.ReferralRecommendation
import com.example.core.contract.RiskTier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AshaStep(val stepNumber: Int, val code: String, val title: String) {
    DEVICE(1, "01 DEVICE", "Hardware Connection & Bus Check"),
    SLEEVE(2, "02 SLEEVE", "Sleeve Placement & Sensor Seating"),
    CALIBRATION(3, "03 CALIBRATION", "Hardware Zero-Offset Calibration"),
    RF(4, "04 RF", "RF Resonance Measurement (0.5–3.0 GHz)"),
    VAG(5, "05 VAG", "VAG Acoustic Crepitus Recording"),
    IMU_SEMG(6, "06 IMU / sEMG", "Kinematic Motion & Muscle Activation"),
    QUESTIONNAIRE(7, "07 QUESTIONNAIRE", "Research Context Input"),
    QUALITY(8, "08 QUALITY", "Module 3 Signal Quality & Artifact Gate"),
    AI(9, "09 AI", "Module 5 Modality AI Processing"),
    RESULT(10, "10 RESULT", "Screening Result & Action Guidance"),
    SUMMARY(11, "11 SUMMARY", "Screening Summary Audit Report")
}

enum class StepStatus(val label: String) {
    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    PASS("PASS"),
    WARN("WARN"),
    FAIL("FAIL"),
    RETEST("RETEST")
}

data class ResearchQuestionnaireResponse(
    val painContext: String = "MILD_INTERMITTENT", // NONE, MILD_INTERMITTENT, MODERATE_ACTIVITY, PERSISTENT
    val functionalDifficulty: String = "STAIRS_AND_SQUATTING", // NONE, STAIRS_AND_SQUATTING, WALKING_LIMIT, PROLONGED_STANDING
    val occupationalExposure: String = "MANUAL_FIELD_LABOR", // SEDENTARY, MODERATE_WALKING, MANUAL_FIELD_LABOR, HEAVY_LIFTING
    val biomechanicalImpact: String = "PREVIOUS_MINOR_TWIST" // NONE, PREVIOUS_MINOR_TWIST, REPETITIVE_IMPACT, JOINT_STIFFNESS_MORNING
)

data class SleevePlacementCheck(
    val sensorAlignment: String = "PASS", // PASS, WARN, REPOSITION
    val sleeveTension: String = "PASS",
    val contactQuality: String = "PASS",
    val orientation: String = "PASS",
    val isConfirmed: Boolean = false
) {
    val isAllPass: Boolean get() =
        sensorAlignment == "PASS" && sleeveTension == "PASS" && contactQuality == "PASS" && orientation == "PASS"
}

data class AshaScreeningSession(
    val sessionId: String,
    val participantId: String,
    val workerId: String,
    val workerName: String,
    val centerName: String,
    val mode: ProfileType,
    val createdTimestampMs: Long = System.currentTimeMillis(),
    val completedTimestampMs: Long? = null,
    val currentStep: AshaStep = AshaStep.DEVICE,
    val stepStatuses: Map<AshaStep, StepStatus> = AshaStep.values().associateWith {
        if (it == AshaStep.DEVICE) StepStatus.ACTIVE else StepStatus.PENDING
    },
    val questionnaire: ResearchQuestionnaireResponse = ResearchQuestionnaireResponse(),
    val sleevePlacement: SleevePlacementCheck = SleevePlacementCheck(),
    val calibrationPassed: Boolean = false,
    val calibrationMessage: String? = null,
    val decision: MultimodalScreeningDecision? = null,
    val retestRequired: Boolean = false,
    val retestReason: String? = null,
    val retestGuidance: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(createdTimestampMs))
}

data class AshaDashboardStats(
    val totalScreenings: Int = 0,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val retestRequiredCount: Int = 0,
    val highUncertaintyCount: Int = 0
)

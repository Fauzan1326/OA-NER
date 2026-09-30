package com.example.ui.showcase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DemoScenarioType
import com.example.ai.DeterministicDemoScenarios
import com.example.ai.MultimodalScreeningDecision
import com.example.ai.RichModalityPrediction
import com.example.core.contract.Modality
import com.example.core.contract.SignalQualityStatus
import com.example.ai.InferenceInputValidator
import com.example.ai.ModalityInferenceOrchestrator
import com.example.core.config.ProfileType
import com.example.domain.usecase.RunModalityInferenceUseCase
import com.example.domain.usecase.RunMultimodalFusionUseCase
import com.example.domain.usecase.ValidateInferenceInputsUseCase
import com.example.features.FeatureDomain
import com.example.features.FeatureItem
import com.example.features.FeatureProvenance
import com.example.features.ModalityFeatureVector
import com.example.preprocessing.ArtifactType
import com.example.ui.ComprehensiveScreeningSummaryCard
import com.example.ui.theme.*
import java.util.Random

/**
 * SECTION 8 — LIVE SYSTEM DEMONSTRATOR
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 *
 * Scenarios:
 * A: 4/4 Clean Multimodal
 * B: RF Failure / Retest (SQI 9% RF mismatch rejection)
 * C: High Uncertainty (Modality Disagreement -> Retest Required)
 *
 * Deterministic with seed 26004.
 * Reuses existing ComprehensiveScreeningSummaryCard.
 */

enum class ShowcaseScenario(val label: String, val badge: String, val description: String) {
    SCENARIO_A(
        "SCENARIO A: 4/4 Clean Multimodal",
        "4/4 NOMINAL",
        "Nominal signal quality on all 4 channels (IMU, VAG, sEMG, RF). Complete multimodal fusion."
    ),
    SCENARIO_B(
        "SCENARIO B: RF Failure / Retest",
        "QUALITY GATE REJECTION",
        "RF sensor experiences impedance detuning (SQI 9%). Quality Gate rejects RF; 3/4 modalities fused."
    ),
    SCENARIO_C(
        "SCENARIO C: High Uncertainty",
        "ELEVATED UNCERTAINTY",
        "Multiple channels experience sub-threshold signal quality and divergence, triggering mandatory retest."
    )
}

@Composable
fun ShowcaseLiveDemoSection(
    modifier: Modifier = Modifier
) {
    var selectedScenario by remember { mutableStateOf(ShowcaseScenario.SCENARIO_A) }
    var currentStepIndex by remember { mutableStateOf(0) }

    // Use cases for deterministic execution
    val runFusionUseCase = remember { RunMultimodalFusionUseCase() }
    val runModalityInferenceUseCase = remember { RunModalityInferenceUseCase() }
    val validateInputsUseCase = remember { ValidateInferenceInputsUseCase() }

    // Compute vectors & decision deterministically based on scenario
    val demoData = remember(selectedScenario) {
        val sessionId = "SHOWCASE_SESS_26004"
        val subjectId = "SUBJ_26004_DEMO"

        val (vectors, sqiScores) = when (selectedScenario) {
            ShowcaseScenario.SCENARIO_A -> {
                DeterministicDemoScenarios.generateScenarioVectors(
                    scenario = DemoScenarioType.CLEAN_MULTIMODAL,
                    sessionId = sessionId,
                    subjectId = subjectId
                )
            }
            ShowcaseScenario.SCENARIO_B -> {
                DeterministicDemoScenarios.generateScenarioVectors(
                    scenario = DemoScenarioType.RF_MISMATCH_REJECTED,
                    sessionId = sessionId,
                    subjectId = subjectId
                )
            }
            ShowcaseScenario.SCENARIO_C -> {
                // Generate high uncertainty scenario deterministically with seed 26004L
                val rand = Random(ShowcaseData.SEED_VALUE)
                val now = System.currentTimeMillis()
                val vMap = mutableMapOf<Modality, ModalityFeatureVector>()
                val sMap = mutableMapOf<Modality, Double>()

                // IMU is nominal
                sMap[Modality.IMU] = 0.92
                vMap[Modality.IMU] = ModalityFeatureVector(
                    provenance = FeatureProvenance(
                        sourceSensorId = "IMU_AXI_01",
                        subjectId = subjectId,
                        sessionId = sessionId,
                        modality = Modality.IMU,
                        windowStartMs = now - 1000L,
                        windowEndMs = now,
                        sampleCount = 200,
                        samplingRateHz = 200.0,
                        qualityStatus = SignalQualityStatus.PASS,
                        qualityScore = 0.92
                    ),
                    features = mapOf(
                        "acc_mean_mag" to FeatureItem("acc_mean_mag", 9.81, "m/s^2", FeatureDomain.STATISTICAL, "Mean Acc"),
                        "movement_symmetry_index" to FeatureItem("movement_symmetry_index", 0.94, "ratio", FeatureDomain.TEMPORAL, "Symmetry")
                    )
                )

                // VAG is marginal (SQI 0.52)
                sMap[Modality.VAG] = 0.52
                vMap[Modality.VAG] = ModalityFeatureVector(
                    provenance = FeatureProvenance(
                        sourceSensorId = "VAG_PIEZO_01",
                        subjectId = subjectId,
                        sessionId = sessionId,
                        modality = Modality.VAG,
                        windowStartMs = now - 1000L,
                        windowEndMs = now,
                        sampleCount = 4000,
                        samplingRateHz = 4000.0,
                        qualityStatus = SignalQualityStatus.PASS,
                        qualityScore = 0.52,
                        artifactFlags = listOf(ArtifactType.MOTION_SPIKE)
                    ),
                    features = mapOf(
                        "vag_rms" to FeatureItem("vag_rms", 0.12, "a.u.", FeatureDomain.STATISTICAL, "RMS"),
                        "vag_crest_factor" to FeatureItem("vag_crest_factor", 5.8, "ratio", FeatureDomain.TEMPORAL, "Crest")
                    )
                )

                // sEMG is failed (electrode lift, SQI 0.12)
                sMap[Modality.SEMG] = 0.12
                vMap[Modality.SEMG] = ModalityFeatureVector(
                    provenance = FeatureProvenance(
                        sourceSensorId = "SEMG_DIFF_01",
                        subjectId = subjectId,
                        sessionId = sessionId,
                        modality = Modality.SEMG,
                        windowStartMs = now - 1000L,
                        windowEndMs = now,
                        sampleCount = 2000,
                        samplingRateHz = 2000.0,
                        qualityStatus = SignalQualityStatus.FAIL,
                        qualityScore = 0.12,
                        artifactFlags = listOf(ArtifactType.ELECTRODE_LIFTOFF)
                    ),
                    features = mapOf(
                        "semg_rms_vm" to FeatureItem("semg_rms_vm", 0.01, "mV", FeatureDomain.STATISTICAL, "RMS")
                    )
                )

                // RF is rejected (detuning, SQI 0.15)
                sMap[Modality.RF] = 0.15
                vMap[Modality.RF] = ModalityFeatureVector(
                    provenance = FeatureProvenance(
                        sourceSensorId = "RF_DIELECTRIC_01",
                        subjectId = subjectId,
                        sessionId = sessionId,
                        modality = Modality.RF,
                        windowStartMs = now - 1000L,
                        windowEndMs = now,
                        sampleCount = 100,
                        samplingRateHz = 50.0,
                        qualityStatus = SignalQualityStatus.FAIL,
                        qualityScore = 0.15,
                        artifactFlags = listOf(ArtifactType.RF_MISMATCH)
                    ),
                    features = mapOf(
                        "rf_min_reflection_db" to FeatureItem("rf_min_reflection_db", -5.1, "dB", FeatureDomain.EXPERIMENTAL_RF, "Min Reflection")
                    )
                )

                Pair(vMap, sMap)
            }
        }

        val preds = mutableMapOf<Modality, RichModalityPrediction>()
        vectors.forEach { (m, v) ->
            preds[m] = runModalityInferenceUseCase.execute(v)
        }

        val decision = runFusionUseCase.execute(
            sessionId = sessionId,
            subjectId = subjectId,
            vectors = vectors,
            sqiScores = sqiScores,
            isDemoSimulation = true
        )

        Triple(vectors, preds, decision)
    }

    val (currentVectors, currentPredictions, currentDecision) = demoData

    val steps = listOf(
        "1. START SESSION" to "Deterministic Session Initialization (Seed 26004)",
        "2. DEVICE CHECK" to "Universal Hardware Bus Ping & Voltage Telemetry",
        "3. ZERO CALIBRATION" to "Sensor Zero-Offset Baseline Tare in Neutral Extension",
        "4. RF SENSING" to "S11 Return Loss & Dielectric Resonance Acquisition",
        "5. VAG ACOUSTIC" to "Articular Crepitus Dynamic Piezoelectric Recording",
        "6. IMU & sEMG" to "Kinematic Flexion Tracking & Muscle Activation Ratio",
        "7. RESEARCH CONTEXT" to "Anonymized Participant Stratification Parameters",
        "8. QUALITY GATE" to "Module 3 SQI Thresholding & Artifact Rejection",
        "9. FEATURE STORE" to "Module 4 Schema v1.0 Normalization & Provenance Ledger",
        "10. MODALITY AI" to "Module 5 Parallel Dedicated Neural Feature Evaluators",
        "11. MULTIMODAL FUSION" to "Late Decision Fusion with Dynamic Renormalization",
        "12. UNCERTAINTY" to "Decoupled Epistemic & Aleatoric Calibration",
        "13. SCREENING TRIAGE" to "Standardized 4-Tier Non-Diagnostic Recommendation",
        "14. SUMMARY REPORT" to "Comprehensive Audit Summary & Retest Remediation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("showcase_live_demo_section"),
        color = Color(0xFF0B1015),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF1E2835))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = ArthroscanBlueBright.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, ArthroscanBlueBright)
                    ) {
                        Text(
                            text = "08 — LIVE SYSTEM DEMONSTRATOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ArthroscanBlueBright
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Deterministic Screening Orchestrator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF071420),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ArthroscanBlueBright)
                ) {
                    Text(
                        text = ShowcaseData.SEED_DISPLAY,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ArthroscanBlueBright
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Scenario Selectors
            Text(
                text = "SELECT REPRODUCIBLE RESEARCH SCENARIO:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextTechnicalLabel
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShowcaseScenario.values().forEach { scenario ->
                    val isSel = selectedScenario == scenario
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedScenario = scenario
                                currentStepIndex = 0
                            }
                            .testTag("scenario_${scenario.name.lowercase()}"),
                        color = if (isSel) ArthroscanBlueBright.copy(alpha = 0.15f) else Color(0xFF070B0E),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isSel) ArthroscanBlueBright else Color(0xFF1E2835))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = scenario.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) ArthroscanBlueBright else TextCoolGray
                                )
                            )
                            Text(
                                text = scenario.label.substringAfter(": "),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) TextNearWhite else TextCoolGray
                                )
                            )
                        }
                    }
                }
            }

            // Stepper Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF070B0E),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STEP ${currentStepIndex + 1} OF ${steps.size}: ${steps[currentStepIndex].first}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MedicalAccentTeal
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { if (currentStepIndex > 0) currentStepIndex-- },
                                enabled = currentStepIndex > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16212D)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("demo_step_back_button")
                            ) {
                                Text("PREV", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace))
                            }
                            Button(
                                onClick = { if (currentStepIndex < steps.size - 1) currentStepIndex++ },
                                enabled = currentStepIndex < steps.size - 1,
                                colors = ButtonDefaults.buttonColors(containerColor = ArthroscanBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("demo_step_next_button")
                            ) {
                                Text("NEXT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.White))
                            }
                            Button(
                                onClick = { currentStepIndex = 13 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2A1E)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("demo_jump_summary_button")
                            ) {
                                Text("JUMP TO REPORT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StatusPassGreen))
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (currentStepIndex + 1).toFloat() / steps.size },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = ArthroscanBlueBright,
                        trackColor = Color(0xFF16212D)
                    )

                    Text(
                        text = steps[currentStepIndex].second,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = TextCoolGray
                        )
                    )
                }
            }

            // Step Content Display or Comprehensive Summary Card
            if (currentStepIndex == 13) {
                // RENDER EXISTING COMPREHENSIVE SCREENING SUMMARY CARD!
                ComprehensiveScreeningSummaryCard(
                    decision = currentDecision,
                    totalPackets = 4280L,
                    droppedPackets = if (selectedScenario == ShowcaseScenario.SCENARIO_A) 0L else 32L,
                    sqiScores = mapOf(
                        Modality.IMU to 0.94,
                        Modality.VAG to (if (selectedScenario == ShowcaseScenario.SCENARIO_C) 0.52 else 0.92),
                        Modality.SEMG to (if (selectedScenario == ShowcaseScenario.SCENARIO_C) 0.12 else 0.90),
                        Modality.RF to (if (selectedScenario == ShowcaseScenario.SCENARIO_B) 0.09 else if (selectedScenario == ShowcaseScenario.SCENARIO_C) 0.15 else 0.94)
                    ),
                    featureStoreCount = currentVectors.size,
                    predictions = currentPredictions,
                    profile = ProfileType.DEMO,
                    isStreaming = false,
                    onRunInference = { /* Deterministic in showcase */ },
                    onClearDecision = { currentStepIndex = 0 }
                )
            } else {
                // Step intermediate details
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ACTIVE PROTOCOL STAGE EXECUTION",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = TextTechnicalLabel)
                        )
                        Text(
                            text = steps[currentStepIndex].first,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            )
                        )
                        Text(
                            text = steps[currentStepIndex].second,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
                        )

                        // Technical State snapshot
                        Surface(
                            color = Color(0xFF040608),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF121B24))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("EXECUTION LOG (SEED 26004):", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                                Text("• Target Session: SHOWCASE_SESS_26004", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                                Text("• Active Modalities: ${currentVectors.keys.joinToString { it.name }}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MedicalAccentTeal))
                                Text("• Quality Filter: Gate Passed for ${currentDecision.availableModalities.size}/4 modalities", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StatusPassGreen))
                                if (currentDecision.excludedModalities.isNotEmpty()) {
                                    Text("• Quality Gate Rejection: ${currentDecision.excludedModalities.joinToString { it.name }} (Zero-substitution prevented)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StatusWarningAmber))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun ShowcaseLiveDemoSectionPreview() {
    MyApplicationTheme {
        ShowcaseLiveDemoSection()
    }
}


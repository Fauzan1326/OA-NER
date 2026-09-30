package com.example.ui.showcase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.contract.Modality
import com.example.hardware.streaming.StreamHealthTelemetry
import com.example.ui.theme.*

/**
 * SECTION 8 — SHOWCASE ARCHITECTURE VISUALIZATIONS & ENGINEERING DIAGRAMS
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 *
 * Strict Compliance:
 * - Technical dark surfaces (#05070A, #0B1015, #171E26)
 * - Monospace telemetry and data flow
 * - Quality-First AI, Decoupled Uncertainty, 4 Screening Tiers
 * - Complementary to X-ray (Does NOT replace X-ray)
 */

@Composable
fun HeroDataFlowVisual() {
    val steps = listOf(
        "PERSON" to "Point-of-Care Knee Screening",
        "WEARABLE SLEEVE" to "Conformal Transducer Array",
        "MULTIMODAL SENSING" to "RF + VAG + IMU + sEMG",
        "SIGNAL QUALITY" to "Module 3 SQI & Artifact Gate",
        "FEATURE STORE" to "Module 4 Schema v1.0 (<300s)",
        "MULTIMODAL AI" to "Module 5 Late-Fusion Ensembles",
        "UNCERTAINTY" to "Decoupled Epistemic Calibration",
        "SCREENING TIER" to "Standardized Risk Triage",
        "RETEST / REFERRAL" to "Action Decision Support"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_data_flow_visual"),
        color = Color(0xFF060A0E),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF1B2633))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "END-TO-END DATA-FLOW PIPELINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ArthroscanBlueBright
                    )
                )
                Text(
                    text = "SCHEMA v1.0 | DETERMINISTIC SEED: 26004",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextTechnicalLabel
                    )
                )
            }

            // Flow Nodes Layout
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                steps.forEachIndexed { index, (node, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B1117), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF16212D), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                color = if (index == 3) StatusWarningAmber.copy(alpha = 0.2f)
                                else if (index == 6) StatusExperimentalPurple.copy(alpha = 0.2f)
                                else ArthroscanBlueBright.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(3.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (index == 3) StatusWarningAmber else if (index == 6) StatusExperimentalPurple else ArthroscanBlueBright
                                )
                            ) {
                                Text(
                                    text = "0${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (index == 3) StatusWarningAmber else if (index == 6) StatusExperimentalPurple else ArthroscanBlueBright
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = node,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextNearWhite
                                )
                            )
                        }

                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextCoolGray
                            )
                        )
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "↓",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = ArthroscanBlueBright.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveWearableSleeveCard() {
    var selectedComponent by remember { mutableStateOf("RF ANTENNA") }

    val components = listOf(
        "FLEXIBLE SLEEVE" to Pair("Mechanical Carrier", "Medical-grade compressive neoprene with breathable micro-perforations, patellar stabilization ring, and dual hook-and-loop tensioning straps to ensure reproducible skin contact pressure."),
        "RF ANTENNA" to Pair("Experimental RF", "EXPERIMENTAL RF — NON-DIAGNOSTIC RESEARCH. Flexible planar copper resonator tuned to 2.45 GHz ISM band; investigates electromagnetic near-field interaction with joint dielectric tissue boundaries."),
        "VAG SENSOR" to Pair("Acoustic Vibroarthrography", "VIBRATION / ACOUSTIC SIGNAL SENSING. High-bandwidth piezoelectric contact accelerometer positioned over lateral joint line; captures micro-acoustic emissions from articular cartilage friction."),
        "IMU" to Pair("Kinematics / Motion", "MOTION / GAIT / JOINT KINEMATICS. 6-axis MEMS accelerometer and rate gyroscope measuring joint flexion-extension angle, cadence symmetry, and dynamic angular velocity during 60-second protocol."),
        "sEMG ELECTRODES" to Pair("Muscle Activation", "MUSCLE ACTIVATION CONTEXT. Dual-channel dry stainless-steel differential electrodes recording vastus medialis and rectus femoris co-contraction index to gate acoustic bursts."),
        "ELECTRONICS" to Pair("Signal Processing Pod", "Low-noise analog front-end (AFE), 24-bit delta-sigma ADCs, integrated hardware filters, and ARM Cortex-M4 dual-core microcontroller running local SQI calculation."),
        "POWER / COMMUNICATION" to Pair("Universal Bus Interface", "Rechargeable 3.7V 1200mAh LiPo battery with safety PCM; low-latency Bluetooth Low Energy (BLE 5.2) streaming telemetry to field terminal tablet/phone.")
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wearable_sleeve_card"),
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
                        color = ArthroscanBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, ArthroscanBlue)
                    ) {
                        Text(
                            text = "03 — WEARABLE SYSTEM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ArthroscanBlueBright
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Conformal Smart Knee Sleeve",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF09141F),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ArthroscanBlue)
                ) {
                    Text(
                        text = "PROTOTYPE CANDIDATE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ArthroscanBlueBright
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Interactive breakdown of smart sleeve hardware nodes. Select any component below to view engineering specifications and research status. Non-diagnostic research prototype.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // Component Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                components.take(4).forEach { (name, _) ->
                    FilterChip(
                        selected = selectedComponent == name,
                        onClick = { selectedComponent = name },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace)) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                components.drop(4).forEach { (name, _) ->
                    FilterChip(
                        selected = selectedComponent == name,
                        onClick = { selectedComponent = name },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace)) }
                    )
                }
            }

            // Component Detail Box
            val compDetail = components.firstOrNull { it.first == selectedComponent }?.second
            Surface(
                color = Color(0xFF070B0E),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedComponent,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            )
                        )
                        Surface(
                            color = Color(0xFF131D27),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF26374A))
                        ) {
                            Text(
                                text = compDetail?.first ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MedicalAccentTeal
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = compDetail?.second ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextCoolGray,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AiPipelineAndQualityFirstCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_pipeline_card"),
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
                        color = Color(0xFF1C132B),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, StatusExperimentalPurple)
                    ) {
                        Text(
                            text = "07 — AI SCREENING PIPELINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = StatusExperimentalPurple
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Quality-First Architecture",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF140E20),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusExperimentalPurple)
                ) {
                    Text(
                        text = "QUALITY BEFORE AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusExperimentalPurple
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "The system enforces a fundamental engineering principle: Quality Before AI. Instead of blindly propagating noisy signals or silently substituting missing values with zero (which skews neural weights), defective modalities are rejected at the gate and omitted from downstream fusion.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // Flow Diagram
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF070B0E),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PIPELINE GATING SEQUENCE:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextTechnicalLabel
                            )
                        )
                        Text(
                            text = "ZERO-SUBSTITUTION: DISABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = StatusPassGreen
                            )
                        )
                    }

                    val gateSteps = listOf(
                        "1. Hardware Stream" to "Packets via Universal Bus; drops tracked",
                        "2. SQI Gate" to "Modality SQI evaluated against locked thresholds",
                        "3. Artifact Filter" to "Motion / clothing / RF detuning filtered",
                        "4. Feature Store" to "Only PASS vectors committed (freshness <300s)",
                        "5. Late Fusion" to "Dynamic weight renormalization without zero-fill",
                        "6. Uncertainty" to "Calibrated epistemic & aleatoric confidence"
                    )

                    gateSteps.forEach { (st, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0A0F15), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = st,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextNearWhite
                                )
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextCoolGray
                                )
                            )
                        }
                    }
                }
            }

            // Rejection Principle Box
            Surface(
                color = Color(0xFF16101D),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, StatusExperimentalPurple.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = StatusExperimentalPurple, modifier = Modifier.size(16.dp))
                    Text(
                        text = "FAILURE HANDLING: BAD SIGNAL → QUALITY REJECTION → NO FEATURE VECTOR → NO STALE VECTOR → NO INVALID FUSION → GUIDED RETEST REQUIRED.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StatusExperimentalPurple
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun UncertaintyAndTiersCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("uncertainty_tiers_card"),
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
                        color = Color(0xFF1B170C),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, StatusWarningAmber)
                    ) {
                        Text(
                            text = "DECOUPLED UNCERTAINTY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = StatusWarningAmber
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Risk vs Uncertainty & Tiers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF121418),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF263342))
                ) {
                    Text(
                        text = "NON-DIAGNOSTIC TIERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextCoolGray
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Screening risk score and epistemic uncertainty are distinct mathematical entities. High uncertainty does NOT artificially escalate the screening risk; instead, it triggers a mandatory RETEST to prevent premature or misleading referral decisions.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // The 4 Standard Screening Tiers
            Text(
                text = "FOUR STANDARDIZED NON-DIAGNOSTIC SCREENING TIERS:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextTechnicalLabel
                )
            )

            val tiers = listOf(
                Triple("LOWER SCREENING RISK", StatusPassGreen, "Biomechanical & acoustic markers within expected physiological range. Routine annual field follow-up."),
                Triple("MODERATE SCREENING RISK", StatusWarningAmber, "Mild elevation in acoustic crepitus or kinematic asymmetry. Lifestyle guidance & 3-month field re-evaluation."),
                Triple("HIGHER SCREENING RISK", StatusFailRed, "Significant multimodal concordance of risk markers. Structured referral to district medical facility."),
                Triple("HIGH UNCERTAINTY — RETEST", StatusExperimentalPurple, "Sensor disagreement or marginal SQI quality. Guided hardware re-calibration and immediate retest required.")
            )

            tiers.forEach { (name, color, desc) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, color)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = color
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = TextCoolGray
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                text = "STRICT LANGUAGE POLICY: The system NEVER outputs 'OA Positive', 'OA Negative', 'Diagnosed', or 'Healthy'.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = StatusWarningAmber
                )
            )
        }
    }
}

@Composable
fun XrayComparisonCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("xray_comparison_card"),
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
                        color = Color(0xFF1B232D),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF324152))
                    ) {
                        Text(
                            text = "09 — COMPLEMENTARY TO CLINICAL ASSESSMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF160D0D),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusFailRed)
                ) {
                    Text(
                        text = "DOES NOT REPLACE X-RAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StatusFailRed
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Dual Column Comparison Table
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = Color(0xFF16212D),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "CONVENTIONAL RADIOGRAPHY (X-RAY)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ArthroscanBlueBright
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text("• Modality: Ionizing radiation transmission projection", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                        Text("• Clinical Target: Macroscopic bony changes, joint space width, osteophytes (Kellgren-Lawrence grading)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                        Text("• Infrastructure: Shielded radiology suite, fixed high-voltage source, trained radiographer", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                        Text("• Role: Established gold standard for structural imaging when clinically indicated", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = Color(0xFF161F1A),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "ARTHROSCAN-NER WEARABLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MedicalAccentTeal
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text("• Modality: Non-ionizing acoustic, kinematic, sEMG & experimental RF", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                        Text("• Clinical Target: Subclinical dynamic friction, kinematic asymmetry, tissue dielectric response", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                        Text("• Infrastructure: Battery-operated, portable sleeve for remote community health workers", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                        Text("• Role: Point-of-care screening decision support to trigger structured referral", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextCoolGray))
                    }
                }
            }

            // Solemn Statement
            Surface(
                color = Color(0xFF140D0D),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, StatusFailRed.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "CRITICAL MANDATE: THE WEARABLE DOES NOT REPLACE X-RAY. It does not replace clinical examination, orthopedic evaluation, or professional medical diagnosis.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = StatusFailRed
                    ),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun HardwareStackAndTelemetryCard(
    telemetry: Map<Modality, StreamHealthTelemetry> = emptyMap(),
    totalPackets: Long = 0L,
    droppedPackets: Long = 0L
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hardware_stack_card"),
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
                        color = Color(0xFF1B232D),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF324152))
                    ) {
                        Text(
                            text = "10 — HARDWARE STACK & TELEMETRY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Universal Bus Architecture",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF081420),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ArthroscanBlue)
                ) {
                    Text(
                        text = "LIVE BUS TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ArthroscanBlueBright
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Telemetry Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL PACKETS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                        Text(
                            text = if (totalPackets > 0) totalPackets.toString() else "NOT CONNECTED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (totalPackets > 0) StatusPassGreen else TextCoolGray
                            )
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DROPPED PACKETS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                        Text(
                            text = if (totalPackets > 0) droppedPackets.toString() else "0",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (droppedPackets > 0) StatusWarningAmber else StatusPassGreen
                            )
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCHEMA VERSION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                        Text(
                            text = "v1.0 (LOCKED)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MedicalAccentTeal
                            )
                        )
                    }
                }
            }

            // Hardware Stack Components List
            val stack = listOf(
                "FLEXIBLE RF RESONATOR" to "Polyimide substrate planar resonator (Prototype Candidate)",
                "CONTACT PIEZO VAG SENSOR" to "Bandwidth 10 Hz – 4 kHz contact acoustic pickup",
                "6-AXIS MEMS IMU" to "LSM6DSO 200 Hz joint kinematic tracking",
                "DUAL-CHANNEL sEMG AFE" to "ADS1292 differential biopotential instrumentation amplifier",
                "DUAL-CORE MCU" to "ARM Cortex-M4 @ 64MHz running deterministic feature extraction",
                "POWER SUBSYSTEM" to "3.7V 1200mAh LiPo with hardware power supervisor & overcurrent lock"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                stack.forEach { (comp, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070B0E), RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(comp, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextNearWhite))
                        Text(desc, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                    }
                }
            }
        }
    }
}

@Composable
fun TenGateVerificationSuiteCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ten_gate_card"),
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
                        color = StatusPassGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, StatusPassGreen)
                    ) {
                        Text(
                            text = "10-GATE VERIFICATION SUITE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = StatusPassGreen
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Deterministic Automated Checks",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF0A1810),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusPassGreen)
                ) {
                    Text(
                        text = "10/10 GATES PASSING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = StatusPassGreen
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Public visual summary of the automated 10-gate quality and governance verification suite. Enforced across every single build, test execution, and screening session.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // Grid of 10 Gates
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShowcaseData.TEN_VERIFICATION_GATES.forEach { (gate, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070B0E), RoundedCornerShape(3.dp))
                            .border(1.dp, Color(0xFF16212D), RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusPassGreen, modifier = Modifier.size(14.dp))
                            Text(
                                text = gate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextNearWhite
                                )
                            )
                        }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextCoolGray
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScientificGovernanceCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scientific_governance_card"),
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
                        color = Color(0xFF1B232D),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF324152))
                    ) {
                        Text(
                            text = "SCIENTIFIC GOVERNANCE LEDGER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Current Frozen Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF16140D),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusWarningAmber)
                ) {
                    Text(
                        text = "READ-ONLY PUBLIC LEDGER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusWarningAmber
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Governance Parameters Grid
            val params = listOf(
                "CONFIG VERSION" to "v1.0-SIH26004-FROZEN",
                "DETERMINISTIC SEED" to "26004L (FROZEN)",
                "FEATURE SCHEMA" to "v1.0 (IMMUTABLE)",
                "FEATURE FRESHNESS" to "< 300 seconds",
                "QUALITY REJECTION" to "ENABLED (STRICT)",
                "ZERO-SUBSTITUTION" to "DISABLED (FORBIDDEN)",
                "INFERENCE TRACE" to "SHA-256 BLOCK HASH",
                "FUSION WEIGHTS" to "RF: 0.35 | VAG: 0.30 | IMU: 0.20 | sEMG: 0.15"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                params.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (key, value) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF070B0E),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E2835))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(key, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextNearWhite
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "REGULATORY REFERENCE FRAMEWORKS: ICMR National Ethical Guidelines (2017) & Digital Personal Data Protection (DPDP) Act (2023) — Used solely as design reference frameworks; not certified medical device claims.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextCoolGray
                )
            )
        }
    }
}

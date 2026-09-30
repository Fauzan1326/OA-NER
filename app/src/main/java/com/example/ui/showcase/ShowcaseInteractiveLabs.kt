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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

/**
 * SECTION 8 — INTERACTIVE RESEARCH LABS & CONCEPTUAL ANATOMICAL EXPLORERS
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 *
 * Strict Disclaimers Applied:
 * - ILLUSTRATIVE SIMULATION — NOT MEASURED CLINICAL DATA
 * - EXPERIMENTAL RF — NON-DIAGNOSTIC RESEARCH
 * - CONCEPTUAL ANATOMICAL VISUALIZATION
 * - CONCEPTUAL OA PROGRESSION — NOT CLINICALLY VALIDATED STAGING
 */

@Composable
fun InteractiveRfLabCard() {
    var perturbationPercent by remember { mutableStateOf(15f) }
    var tissueThicknessMm by remember { mutableStateOf(3.5f) }
    var antennaOffsetMm by remember { mutableStateOf(1.0f) }
    var selectedFreqGhz by remember { mutableStateOf(2.45f) }

    // Derived illustrative parameters based on perturbation
    val simulatedDeltaF = remember(perturbationPercent, antennaOffsetMm) {
        val baseShift = (perturbationPercent * 0.18f)
        val offsetAttenuation = 1.0f - (antennaOffsetMm * 0.05f).coerceIn(0f, 0.5f)
        baseShift * offsetAttenuation
    }
    val resonantFreqMhz = 2450.0f + simulatedDeltaF
    val returnLossDb = remember(perturbationPercent, antennaOffsetMm) {
        -24.5f + (perturbationPercent * 0.28f) + (antennaOffsetMm * 0.9f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rf_lab_card"),
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
                        color = StatusExperimentalPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, StatusExperimentalPurple)
                    ) {
                        Text(
                            text = "04 — EXPERIMENTAL RF LAB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = StatusExperimentalPurple
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Electromagnetic Response Explorer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF1A1224),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusExperimentalPurple)
                ) {
                    Text(
                        text = "NON-DIAGNOSTIC RESEARCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusExperimentalPurple
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Prominent Lab Disclaimer
            Surface(
                color = Color(0xFF16140D),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, StatusWarningAmber.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = StatusWarningAmber, modifier = Modifier.size(16.dp))
                    Text(
                        text = "CRITICAL: ILLUSTRATIVE SIMULATION — NOT MEASURED CLINICAL DATA. Investigates whether tissue-equivalent dielectric perturbation produces detectable S11 resonance shifts.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusWarningAmber
                        )
                    )
                }
            }

            // Interactive Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B0E), RoundedCornerShape(6.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Cartilage Permittivity Perturbation: ${perturbationPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = TextCoolGray)
                    )
                    Text(
                        text = "Simulated Δf: +${String.format("%.2f", simulatedDeltaF)} MHz",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ArthroscanBlueBright
                        )
                    )
                }
                Slider(
                    value = perturbationPercent,
                    onValueChange = { perturbationPercent = it },
                    valueRange = 0f..40f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth().testTag("rf_perturbation_slider")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tissue Thickness: ${String.format("%.1f", tissueThicknessMm)} mm",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray)
                        )
                        Slider(
                            value = tissueThicknessMm,
                            onValueChange = { tissueThicknessMm = it },
                            valueRange = 2.0f..6.0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Antenna Offset: ${String.format("%.1f", antennaOffsetMm)} mm",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray)
                        )
                        Slider(
                            value = antennaOffsetMm,
                            onValueChange = { antennaOffsetMm = it },
                            valueRange = 0.0f..10.0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // S11 Return Loss Curve Visualization
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                color = Color(0xFF05080B),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "S11 RETURN LOSS (dB) vs FREQUENCY (GHz)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextTechnicalLabel
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(StatusPassGreen, CircleShape))
                                Text("Baseline (-24.5 dB @ 2.45 GHz)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextCoolGray))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(StatusExperimentalPurple, CircleShape))
                                Text("Perturbed (${String.format("%.1f", returnLossDb)} dB)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = StatusExperimentalPurple))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val midY = h * 0.2f

                        // Grid lines
                        drawLine(Color(0xFF1B232D), Offset(0f, midY), Offset(w, midY), 1f)
                        drawLine(Color(0xFF1B232D), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1f)
                        drawLine(Color(0xFF1B232D), Offset(0f, h * 0.85f), Offset(w, h * 0.85f), 1f)
                        drawLine(Color(0xFF1B232D), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), 1f)

                        // Baseline Dip Curve (Green)
                        val baselinePath = Path().apply {
                            moveTo(0f, midY)
                            cubicTo(
                                w * 0.35f, midY,
                                w * 0.45f, h * 0.90f,
                                w * 0.50f, h * 0.90f
                            )
                            cubicTo(
                                w * 0.55f, h * 0.90f,
                                w * 0.65f, midY,
                                w, midY
                            )
                        }
                        drawPath(baselinePath, StatusPassGreen.copy(alpha = 0.7f), style = Stroke(width = 2.dp.toPx()))

                        // Perturbed Dip Curve (Purple)
                        val shiftRatio = (simulatedDeltaF / 20f).coerceIn(-0.2f, 0.3f)
                        val dipX = w * (0.50f + shiftRatio * 0.4f)
                        val dipY = (h * 0.90f) * (1.0f - (returnLossDb + 24.5f) / 30f).coerceIn(0.4f, 0.95f)

                        val perturbedPath = Path().apply {
                            moveTo(0f, midY)
                            cubicTo(
                                dipX - w * 0.15f, midY,
                                dipX - w * 0.05f, dipY,
                                dipX, dipY
                            )
                            cubicTo(
                                dipX + w * 0.05f, dipY,
                                dipX + w * 0.15f, midY,
                                w, midY
                            )
                        }
                        drawPath(perturbedPath, StatusExperimentalPurple, style = Stroke(width = 2.5.dp.toPx()))
                    }
                }
            }

            // Research Pathway Stepper
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "EXPERIMENTAL RF RESEARCH TRANSLATION PATHWAY:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextTechnicalLabel
                    )
                )
                Text(
                    text = "HFSS Finite-Element Model → 5-Layer Tissue Model → Dielectric Perturbation Sweep → Flexible Antenna Fabrication → Multilayer Phantom → Vector Network Analyzer (VNA) Bench Verification → Placement Robustness Study → Future Human Feasibility Study",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextCoolGray,
                        lineHeight = 14.sp
                    )
                )
                Text(
                    text = "STATUS: TO BE EXPERIMENTALLY VALIDATED IN HUMAN POPULATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = StatusExperimentalPurple
                    )
                )
            }
        }
    }
}

@Composable
fun InteractiveVagLabCard() {
    var motionLevel by remember { mutableStateOf("Medium") }
    var recordingWindowSec by remember { mutableStateOf(2) }
    var isSimulatingArtifact by remember { mutableStateOf(false) }

    val sqiScore = remember(motionLevel, isSimulatingArtifact) {
        if (isSimulatingArtifact) 0.38 else 0.93
    }
    val peakAmp = remember(motionLevel, isSimulatingArtifact) {
        if (isSimulatingArtifact) 0.62 else 0.18
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vag_lab_card"),
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
                        color = MedicalAccentTeal.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MedicalAccentTeal)
                    ) {
                        Text(
                            text = "05 — VAG ACOUSTIC SIGNAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MedicalAccentTeal
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Vibroarthrography Crepitus Lab",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF0A1820),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MedicalAccentTeal)
                ) {
                    Text(
                        text = "ILLUSTRATIVE SIGNAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MedicalAccentTeal
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Vibroarthrography (VAG) refers to vibration and acoustic sensing around the joint during flexion-extension. The research objective is to investigate whether acoustic burst characteristics provide useful features for screening decision support. Not a standalone diagnosis (crepitus does not prove OA).",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { motionLevel = if (motionLevel == "Low") "Medium" else if (motionLevel == "Medium") "High" else "Low" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Motion: $motionLevel", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace))
                }
                OutlinedButton(
                    onClick = { recordingWindowSec = if (recordingWindowSec == 1) 2 else if (recordingWindowSec == 2) 4 else 1 },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Window: ${recordingWindowSec}s", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace))
                }
                FilterChip(
                    selected = isSimulatingArtifact,
                    onClick = { isSimulatingArtifact = !isSimulatingArtifact },
                    label = { Text("Artifact", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Waveform Canvas
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                color = Color(0xFF05080B),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    drawLine(Color(0xFF1B232D), Offset(0f, centerY), Offset(w, centerY), 1f)

                    val path = Path().apply {
                        moveTo(0f, centerY)
                        val points = 120
                        for (i in 1..points) {
                            val x = (i.toFloat() / points) * w
                            val freqMult = if (isSimulatingArtifact) 0.08f else 0.25f
                            val burst = if (i in 40..65 || i in 85..105) 2.2f else 0.4f
                            val noise = sin(i * freqMult * 10f) * cos(i * 0.4f) * burst
                            val artifactNoise = if (isSimulatingArtifact && i in 50..80) sin(i * 0.15f) * 45f else 0f
                            val y = centerY + (noise * 22f) + artifactNoise
                            lineTo(x, y.coerceIn(5f, h - 5f))
                        }
                    }
                    val lineColor = if (isSimulatingArtifact) StatusWarningAmber else MedicalAccentTeal
                    drawPath(path, lineColor, style = Stroke(width = 1.8.dp.toPx()))
                }
            }

            // Real-time Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B0E), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SQI SCORE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                    Text(
                        text = String.format("%.2f", sqiScore),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (sqiScore >= 0.70) StatusPassGreen else StatusWarningAmber
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PEAK AMPLITUDE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                    Text(
                        text = "${String.format("%.3f", peakAmp)} a.u.",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, color = TextNearWhite)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ARTIFACT STATUS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = TextTechnicalLabel))
                    Text(
                        text = if (isSimulatingArtifact) "FRICTION ARTIFACT" else "PASS NOMINAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSimulatingArtifact) StatusWarningAmber else StatusPassGreen
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveImuSemgLabCard() {
    var flexionAngle by remember { mutableStateOf(45f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("imu_semg_lab_card"),
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
                            text = "IMU & sEMG CONTEXT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ArthroscanBlueBright
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Kinematics & Muscle Activation",
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
                        text = "RESEARCH SIGNALS",
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
                text = "IMU and sEMG channels provide functional biomechanical context (flexion-extension angle, cadence, quadriceps co-contraction) to gate acoustic bursts and ensure standardized screening protocols.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // Angle Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Simulated Joint Flexion: ${flexionAngle.toInt()}°",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = TextCoolGray)
                )
                Text(
                    text = if (flexionAngle < 20f) "FULL EXTENSION" else if (flexionAngle > 70f) "DEEP FLEXION" else "MID-RANGE",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = ArthroscanBlueBright)
                )
            }
            Slider(
                value = flexionAngle,
                onValueChange = { flexionAngle = it },
                valueRange = 0f..90f,
                modifier = Modifier.fillMaxWidth()
            )

            // Dual Grid: IMU vs sEMG Metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("IMU KINEMATICS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArthroscanBlueBright))
                        Text("• Joint Angle: ${flexionAngle.toInt()}° (Target: 0–90°)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                        Text("• Angular Velocity: ${String.format("%.1f", 1.8f + (flexionAngle * 0.04f))} rad/s", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                        Text("• Acceleration: 9.81 m/s² (1G Ref)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF070B0E),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2835))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("sEMG MUSCLE CONTEXT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedicalAccentTeal))
                        Text("• Ch1 Vastus Medialis: ${String.format("%.2f", 0.08f + (flexionAngle * 0.002f))} mV", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                        Text("• Ch2 Rectus Femoris: ${String.format("%.2f", 0.06f + (flexionAngle * 0.0015f))} mV", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                        Text("• Co-Contraction Ratio: 0.74 (Nominal)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextCoolGray))
                    }
                }
            }
        }
    }
}

@Composable
fun ConceptualKneeCutawayCard() {
    var selectedAnatomicalLayer by remember { mutableStateOf("ARTICULAR CARTILAGE") }
    var zoomLevel by remember { mutableStateOf(1.0f) }

    val layers = listOf(
        "FEMUR" to "Proximal bone providing upper articular condyles for weight transfer.",
        "PATELLA" to "Sesamoid bone distributing quadriceps tendon tension across anterior knee.",
        "ARTICULAR CARTILAGE" to "Avascular hyaline cartilage (2–4mm) providing low-friction joint articulation.",
        "MENISCUS" to "Fibrocartilaginous shock-absorbing wedges (medial & lateral).",
        "JOINT SPACE" to "Synovial fluid compartment; radiographic narrowing marks advanced change.",
        "TIBIA" to "Weight-bearing tibial plateau accommodating medial & lateral condyles.",
        "FIBULA" to "Lateral stabilization strut supporting ligamentous attachments."
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("knee_cutaway_card"),
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
                            text = "ANATOMICAL MODEL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Conceptual Knee Cutaway Visualizer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF141920),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF263342))
                ) {
                    Text(
                        text = "CONCEPTUAL ANATOMICAL VISUALIZATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextCoolGray
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Illustrative structural representation of knee joint components investigated by wearable acoustic and electromagnetic sensing. Does not imply radiographic or magnetic resonance imaging of a real participant.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextCoolGray, lineHeight = 16.sp)
            )

            // Layer Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                layers.take(4).forEach { (name, _) ->
                    FilterChip(
                        selected = selectedAnatomicalLayer == name,
                        onClick = { selectedAnatomicalLayer = name },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace)) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                layers.drop(4).forEach { (name, _) ->
                    FilterChip(
                        selected = selectedAnatomicalLayer == name,
                        onClick = { selectedAnatomicalLayer = name },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace)) }
                    )
                }
            }

            // Layer Visual Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = Color(0xFF06090D),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val w = size.width
                    val h = size.height

                    // Femur outline
                    drawRoundRect(
                        color = Color(0xFF2E3A4A),
                        topLeft = Offset(w * 0.35f, 0f),
                        size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.35f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    // Articular Cartilage layer (highlighted if selected)
                    val cartilageColor = if (selectedAnatomicalLayer == "ARTICULAR CARTILAGE") StatusExperimentalPurple else Color(0xFF4A6572)
                    drawRoundRect(
                        color = cartilageColor,
                        topLeft = Offset(w * 0.32f, h * 0.35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.10f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    // Joint Space
                    drawLine(Color(0xFF00ADB5), Offset(w * 0.28f, h * 0.48f), Offset(w * 0.72f, h * 0.48f), 2f)

                    // Meniscus pads
                    val menColor = if (selectedAnatomicalLayer == "MENISCUS") StatusWarningAmber else Color(0xFF34495E)
                    drawCircle(menColor, 12f, Offset(w * 0.30f, h * 0.50f))
                    drawCircle(menColor, 12f, Offset(w * 0.70f, h * 0.50f))

                    // Tibial Plateau
                    drawRoundRect(
                        color = Color(0xFF222C38),
                        topLeft = Offset(w * 0.30f, h * 0.54f),
                        size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.46f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }
            }

            // Layer Description
            Surface(
                color = Color(0xFF070B0E),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SELECTED STRUCTURE: $selectedAnatomicalLayer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MedicalAccentTeal
                        )
                    )
                    Text(
                        text = layers.firstOrNull { it.first == selectedAnatomicalLayer }?.second ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextCoolGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ConceptualOaProgressionCard() {
    var selectedStage by remember { mutableStateOf(1) }

    val stages = listOf(
        Triple(0, "REFERENCE", "Nominal physiological baseline; smooth cartilage surface, preserved joint space, absence of pathological crepitus."),
        Triple(1, "EARLY CHANGE", "Micro-fibrillation and superficial proteoglycan depletion. Subclinical high-frequency acoustic burst initiation."),
        Triple(2, "PROGRESSIVE CHANGE", "Fissuring of hyaline cartilage, increased acoustic crepitus, subtle joint space reduction."),
        Triple(3, "ADVANCED CHANGE", "Marked cartilage erosion, subchondral bone sclerosis, persistent friction crepitus, restricted RoM."),
        Triple(4, "SEVERE STRUCTURAL CHANGE", "Full-thickness cartilage denudation, bone-on-bone contact, osteophyte proliferation.")
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("oa_progression_card"),
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
                            text = "06 — CONCEPTUAL OA PROGRESSION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextNearWhite
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Disease Progression Model",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextNearWhite
                        )
                    )
                }

                Surface(
                    color = Color(0xFF1B140D),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusWarningAmber)
                ) {
                    Text(
                        text = "CONCEPTUAL ONLY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusWarningAmber
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Disclaimer Banner
            Surface(
                color = Color(0xFF140D0D),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, StatusFailRed.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusFailRed, modifier = Modifier.size(16.dp))
                    Text(
                        text = "CRITICAL: CONCEPTUAL DISEASE-PROGRESSION VISUALIZATION — NOT CLINICALLY VALIDATED STAGING. The wearable does NOT determine clinical stages or perform radiographic staging.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusFailRed
                        )
                    )
                }
            }

            // Stage Step Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stages.forEach { (stage, label, _) ->
                    val isSel = selectedStage == stage
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedStage = stage },
                        color = if (isSel) ArthroscanBlueBright.copy(alpha = 0.2f) else Color(0xFF070B0E),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (isSel) ArthroscanBlueBright else Color(0xFF1E2835))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "STAGE $stage",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) ArthroscanBlueBright else TextCoolGray
                                )
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) TextNearWhite else TextTechnicalLabel
                                )
                            )
                        }
                    }
                }
            }

            // Detail of selected stage
            val currentStage = stages.first { it.first == selectedStage }
            Surface(
                color = Color(0xFF070B0E),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2835))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "STAGE ${currentStage.first} — ${currentStage.second}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MedicalAccentTeal
                        )
                    )
                    Text(
                        text = currentStage.third,
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

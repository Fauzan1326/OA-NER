package com.example.ui.portal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.contract.Modality
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaStep
import com.example.ui.DashboardUiState
import com.example.ui.components.ScientificMetricItem
import com.example.ui.theme.*

@Composable
fun RetestWorkflowDialog(
    session: AshaScreeningSession,
    onRestartFromCalibration: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceLevel1_Primary,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, StatusFailRed),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusFailRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RETEST REQUIRED — REMEDIATION",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        color = StatusFailRed
                    )
                }

                Text(
                    text = "RETEST REASON: ${session.retestReason ?: "Elevated uncertainty or signal quality gate failure."}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = TextNearWhite
                )

                Divider(color = SurfaceSubtleBorder)

                Text(
                    text = "GUIDED REMEDIATION PROTOCOL:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                    color = TextTechnicalLabel
                )

                val guidanceSteps = if (session.retestGuidance.isNotEmpty()) session.retestGuidance else listOf(
                    "1. Reposition sleeve and inspect sensor alignment markers.",
                    "2. Confirm skin contact across acoustic and RF sensing elements.",
                    "3. Re-run zero-offset hardware tare calibration.",
                    "4. Repeat measurement window while subject remains resting.",
                    "5. Re-run Module 3 quality gate prior to AI inference."
                )

                guidanceSteps.forEach { stepText ->
                    Text(stepText, style = MaterialTheme.typography.bodySmall, color = TextCoolGray)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CLOSE", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = onRestartFromCalibration,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusFailRed),
                        modifier = Modifier.weight(1.5f).testTag("retest_restart_button")
                    ) {
                        Text("RETEST FROM STEP 03", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedRouteDialog(
    targetRouteName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceLevel1_Primary,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, StatusFailRed),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = StatusFailRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACCESS DENIED (SECURITY POLICY)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        color = StatusFailRed
                    )
                }

                Text(
                    text = "Role ASHA_WORKER is strictly unauthorized to access route: $targetRouteName.\n\nASHA workers cannot modify model definitions, alter scientific thresholds, configure RF hardware registers, change user roles, or edit immutable audit records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextNearWhite
                )

                Surface(
                    color = SurfaceLevel2_Elevated,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SurfaceSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SECURITY AUDIT: Authorization Guard blocked route traversal and logged attempt.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = FontFamily.Monospace),
                        color = TextCoolGray,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_blocked_dialog_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArthroscanBlueBright)
                ) {
                    Text("RETURN TO ASHA PORTAL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                }
            }
        }
    }
}

@Composable
fun AshaTechnicalDrawerDialog(
    uiState: DashboardUiState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceLevel1_Primary,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SurfaceControlBorder),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TECHNICAL DETAILS DRAWER (READ-ONLY)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        color = ArthroscanBlueBright
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextCoolGray)
                    }
                }

                Divider(color = SurfaceSubtleBorder)

                ThemeSelectorRow()

                Divider(color = SurfaceSubtleBorder)

                TechDrawerItem("Canonical Schema", "v${uiState.schemaVersion} (Universal Data Contract)")
                TechDrawerItem("Feature Store Freshness", "<300s (Deterministic Purge Active)")
                TechDrawerItem("Packet Loss / Gaps", "${uiState.droppedPacketsCount} pkts dropped / 0 gaps")
                TechDrawerItem("Normalization Method", "Z-Score & Min-Max (Locked Parameters)")
                TechDrawerItem("IMU Sampling Rate", "100 Hz / 200 Hz Dual-Array Kinematics")
                TechDrawerItem("VAG Sampling Rate", "2000 Hz Acoustic Micro-Crepitus")
                TechDrawerItem("sEMG Sampling Rate", "1000 Hz 2-Channel Muscle Activation")
                TechDrawerItem("RF Operating Band", "0.5–3.0 GHz (2.45 GHz ISM Reference)")
                TechDrawerItem("RF Classification", "EXPERIMENTAL RF NON-DIAGNOSTIC RESEARCH")
                TechDrawerItem("AI Inference Engines", "4 Modality Models + Multimodal Fusion")
                TechDrawerItem("Audit Integrity Hash", "SHA-256 Provenance Locked")

                Surface(
                    color = SurfaceLevel2_Card,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SurfaceSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ASHA Worker role does not possess editing privileges for technical configuration parameters.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = TextCoolGray,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ArthroscanBlueBright)
                ) {
                    Text("CLOSE TECHNICAL DRAWER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                }
            }
        }
    }
}

@Composable
fun AshaHelpCenterDialog(onDismiss: () -> Unit) {
    AshaFieldScreeningGuideDialog(onDismiss = onDismiss)
}

@Composable
private fun TechDrawerItem(title: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = TextTechnicalLabel)
        Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun TroubleCard(title: String, points: List<String>) {
    Surface(
        color = SurfaceLevel2_Card,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, SurfaceSubtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = StatusWarningAmber)
            points.forEach { pt ->
                Text(pt, style = MaterialTheme.typography.bodySmall, color = TextNearWhite)
            }
        }
    }
}

@Composable
fun ThemeSelectorRow() {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "THEME SELECTION",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily),
            color = TextTechnicalLabel
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val currentMode = ThemeManager.themeMode.value
            ThemeMode.values().forEach { mode ->
                val isSel = currentMode == mode
                OutlinedButton(
                    onClick = { ThemeManager.setThemeMode(mode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSel) ArthroscanBlue.copy(alpha = 0.15f) else SurfaceLevel2_Card
                    ),
                    border = BorderStroke(1.dp, if (isSel) ArthroscanBlue else SurfaceControlBorder)
                ) {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFontFamily,
                            color = if (isSel) ArthroscanBlue else TextCoolGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AshaAboutPlatformDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceLevel1_Primary,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SurfaceControlBorder),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ArthroscanBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ABOUT ARTHROSCAN-NER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily),
                            color = TextNearWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextCoolGray)
                    }
                }

                Divider(color = SurfaceSubtleBorder)

                TroubleCard("PLATFORM IDENTITY", listOf(
                    "AI-Assisted Research & Signal Analysis Platform",
                    "Problem Statement: SIH26004",
                    "Team: GOD'S PLAN",
                    "Deterministic Seed: 26004"
                ))

                TroubleCard("RESEARCH BASIS & SENSING MODALITIES", listOf(
                    "• Kinematic Flexion Array (IMU): 200 Hz joint angle and movement symmetry.",
                    "• Vibroarthrography (VAG): 2000 Hz acoustic micro-crepitus recording.",
                    "• Surface Electromyography (sEMG): 1000 Hz Vastus Medialis muscle activation.",
                    "• Experimental RF Dielectric Resonance: S11 reflection analysis (2.45 GHz reference)."
                ))

                TroubleCard("AI ARCHITECTURE & FUSION", listOf(
                    "• Parallel Modality Evaluators: Dedicated feature evaluators per channel.",
                    "• Late Decision Fusion: Dynamic renormalization based on SQI gate outputs.",
                    "• Decoupled Uncertainty Calibration: Separates epistemic (model) and aleatoric (data) uncertainty."
                ))

                TroubleCard("VALIDATION ROADMAP & HONESTY", listOf(
                    "• Phase 1-5: Verification gates and deterministic simulation complete.",
                    "• Research Honesty: Experimental RF is non-diagnostic. Platform is an AI research prototype intended for decision support studies."
                ))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ArthroscanBlue)
                ) {
                    Text("CLOSE ABOUT PLATFORM", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily))
                }
            }
        }
    }
}

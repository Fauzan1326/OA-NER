package com.example.ui.portal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.portal.AshaStep
import com.example.ui.theme.*

/**
 * PRODUCTION FIELD SCREENING GUIDE
 * ARTHROSCAN-NER | 11-step field screening protocol & troubleshooting
 */
@Composable
fun AshaFieldScreeningGuideDialog(
    onDismiss: () -> Unit
) {
    // Clinical Design Tokens
    val bgCanvas = Color(0xFFF8FAFC)
    val surfaceCard = Color.White
    val surfaceLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val accentCyan = Color(0xFF0891B2)
    val warningAmber = Color(0xFFD97706)
    val successGreen = Color(0xFF059669)

    var expandedStepIndex by remember { mutableStateOf<Int?>(0) }
    var expandedTroubleIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("guide_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "FIELD SCREENING GUIDE",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = "ASHA Worker Field Protocol",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }
            },
            containerColor = bgCanvas
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // Intro Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(primaryBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = primaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "FIELD SCREENING GUIDE",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = textPrimary
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = primaryBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "ASHA FIELD WORKER",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = primaryBlue
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "11-step ARTHROSCAN-NER field screening protocol",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = primaryBlue
                            )
                        )

                        Text(
                            text = "Use this guide to prepare the participant, operate the sensor system, validate measurements and complete the screening workflow safely.",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = textSecondary
                            )
                        )
                    }
                }

                // 11 Canonical Steps Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "11-STEP PROTOCOL SEQUENCE",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp,
                            color = textSecondary
                        )
                    )
                    Text(
                        text = "11 STEPS COMPLETE",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = successGreen
                        )
                    )
                }

                // Expandable Step Cards
                val stepDetails = remember {
                    listOf(
                        StepGuideItem(
                            step = AshaStep.DEVICE,
                            instructions = "Power on sensor hub, pair via Bluetooth or universal hardware bus, and verify battery voltage > 3.6V.",
                            warning = "Do not proceed if hardware communication bus reports packet drop > 2%.",
                            moduleInfo = "Hardware Link • Universal Bus • Power Management"
                        ),
                        StepGuideItem(
                            step = AshaStep.SLEEVE,
                            instructions = "Position calibrated orthopedic sleeve snug over patella. Ensure skin contact window matches alignment lines.",
                            warning = "Ensure sleeve is oriented along the sagittal knee axis without twisting.",
                            moduleInfo = "Bilateral Sleeve Fit • Acoustic Gel Interface"
                        ),
                        StepGuideItem(
                            step = AshaStep.CALIBRATION,
                            instructions = "Keep limb stationary at full rest. Initiate automated zero-offset tare calculation.",
                            warning = "Participant must remain completely relaxed during the 3-second tare cycle.",
                            moduleInfo = "Module 2 Zero-Offset Tare • SNR Calibrated at 0.02 µV"
                        ),
                        StepGuideItem(
                            step = AshaStep.RF,
                            instructions = "Capture 0.5–3.0 GHz dielectric resonance frequency sweeps across patellar cartilage zone.",
                            warning = "Ensure RF probe remains flush against cartilage boundary.",
                            moduleInfo = "S11 Resonance Sweep • Dielectric Permittivity"
                        ),
                        StepGuideItem(
                            step = AshaStep.VAG,
                            instructions = "Record acoustic micro-crepitus emissions at 2000 Hz while guiding smooth active flexion.",
                            warning = "Instruct participant to avoid sudden tremors or jerky leg movements.",
                            moduleInfo = "Acoustic Transducer • 2000 Hz High-Res Crepitus"
                        ),
                        StepGuideItem(
                            step = AshaStep.IMU_SEMG,
                            instructions = "Track 200 Hz joint angle trajectory paired with 1000 Hz Vastus Medialis sEMG muscular firing.",
                            warning = "Verify sEMG electrodes make uninterrupted dermal contact.",
                            moduleInfo = "Kinematic 6-Axis IMU (200 Hz) • 2-Ch sEMG (1000 Hz)"
                        ),
                        StepGuideItem(
                            step = AshaStep.QUESTIONNAIRE,
                            instructions = "Record observational symptom context (pain frequency, squatting difficulty, field labor exposure).",
                            warning = "Enter factual participant responses without extrapolating medical diagnosis.",
                            moduleInfo = "Clinical Observational Priors • Functional Scoring"
                        ),
                        StepGuideItem(
                            step = AshaStep.QUALITY,
                            instructions = "Evaluate Module 3 Signal Quality Index (SQI) gates across all active modalities.",
                            warning = "If any sensor SQI is below 0.65, inspect electrode seating and re-tare.",
                            moduleInfo = "Module 3 Preprocessing • Multi-Modality Quality Gates"
                        ),
                        StepGuideItem(
                            step = AshaStep.AI,
                            instructions = "Run federated late-decision AI multimodal inference pipeline on verified sensor features.",
                            warning = "AI processing executes on-device with zero unencrypted cloud transmission.",
                            moduleInfo = "Module 5 Late Decision Fusion • Uncertainty Calibration"
                        ),
                        StepGuideItem(
                            step = AshaStep.RESULT,
                            instructions = "Inspect triage risk stratification tier (Low, Moderate, High, or Retest Required).",
                            warning = "High uncertainty indicates sensor noise or modality discordance, NOT high arthritis risk.",
                            moduleInfo = "Triage Classification • Actionable Referral Pathway"
                        ),
                        StepGuideItem(
                            step = AshaStep.SUMMARY,
                            instructions = "Review comprehensive screening session summary audit ledger and export cryptographic report.",
                            warning = "Session completion permanently timestamps data with SHA-256 tamper-evident seal.",
                            moduleInfo = "SHA-256 Audit Seal • Offline PDF / JSON Generation"
                        )
                    )
                }

                stepDetails.forEachIndexed { index, item ->
                    val isExpanded = expandedStepIndex == index
                    val stepNum = item.step.stepNumber.toString().padStart(2, '0')
                    val cleanTitle = if (item.step.code.matches(Regex("""^\d+\s+.*"""))) {
                        item.step.code.substringAfter(' ')
                    } else {
                        item.step.code
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceCard,
                        border = BorderStroke(
                            1.dp,
                            if (isExpanded) primaryBlue.copy(alpha = 0.6f) else borderStrokeColor
                        ),
                        shadowElevation = if (isExpanded) 2.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedStepIndex = if (isExpanded) null else index
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isExpanded) primaryBlue else surfaceLow
                                    ) {
                                        Text(
                                            text = stepNum,
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isExpanded) Color.White else textSecondary
                                            ),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = cleanTitle,
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = textPrimary
                                            )
                                        )
                                        Text(
                                            text = item.step.title,
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.sp,
                                                color = textSecondary
                                            )
                                        )
                                    }
                                }

                                val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(rotationState)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = borderStrokeColor, thickness = 0.8.dp)

                                    // Field Instructions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircleOutline,
                                            contentDescription = null,
                                            tint = successGreen,
                                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                        )
                                        Text(
                                            text = item.instructions,
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                color = textPrimary
                                            )
                                        )
                                    }

                                    // Warning
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(warningAmber.copy(alpha = 0.08f))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = warningAmber,
                                            modifier = Modifier.size(15.dp).padding(top = 1.dp)
                                        )
                                        Text(
                                            text = item.warning,
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = Color(0xFF92400E)
                                            )
                                        )
                                    }

                                    // Module Tag
                                    Text(
                                        text = "Subsystem: ${item.moduleInfo}",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = textSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quick Troubleshooting Section
                Text(
                    text = "QUICK TROUBLESHOOTING",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.8.sp,
                        color = textSecondary
                    )
                )

                val troubleList = remember {
                    listOf(
                        TroubleItem(
                            title = "DEVICE NOT CONNECTING",
                            points = listOf(
                                "• Check sensor hub battery switch and power status.",
                                "• Verify Bluetooth / USB connection is enabled.",
                                "• Check universal hardware bus status.",
                                "• Retry connection from Device Check step."
                            )
                        ),
                        TroubleItem(
                            title = "RF QUALITY FAILURE",
                            points = listOf(
                                "• Verify patellar alignment marker on knee sleeve.",
                                "• Ensure uniform dielectric probe skin contact.",
                                "• Re-run Step 03 hardware zero-offset calibration."
                            )
                        ),
                        TroubleItem(
                            title = "VAG ARTIFACT / NOISE",
                            points = listOf(
                                "• Check acoustic transducer coupling on medial tibial plateau.",
                                "• Instruct participant to perform smooth, continuous flexion.",
                                "• Minimize garment friction against acoustic probe."
                            )
                        ),
                        TroubleItem(
                            title = "sEMG CONTACT FAILURE",
                            points = listOf(
                                "• Inspect electrode seating over Vastus Medialis muscle.",
                                "• Clean skin surface to eliminate sweat/oil resistance.",
                                "• Confirm lead cables are securely plugged into sensor hub."
                            )
                        ),
                        TroubleItem(
                            title = "IMU SIGNAL FAILURE",
                            points = listOf(
                                "• Confirm sleeve has not twisted out of limb sagittal plane.",
                                "• Inspect 6-axis IMU packet transmission status.",
                                "• Re-tare limb angle at 0° fully extended position."
                            )
                        ),
                        TroubleItem(
                            title = "CALIBRATION FAILURE",
                            points = listOf(
                                "• Ensure participant limb is resting without tremors.",
                                "• Check sensor hub ground connection.",
                                "• Re-run zero-offset tare baseline sequence."
                            )
                        ),
                        TroubleItem(
                            title = "LOW SIGNAL QUALITY",
                            points = listOf(
                                "• Review Module 3 Signal Quality Index (SQI) readout.",
                                "• Re-seat sleeve tensioning straps.",
                                "• If discordance persists, execute guided retest sequence."
                            )
                        )
                    )
                }

                troubleList.forEachIndexed { index, trouble ->
                    val isExpanded = expandedTroubleIndex == index

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = surfaceCard,
                        border = BorderStroke(1.dp, borderStrokeColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedTroubleIndex = if (isExpanded) null else index
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = warningAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = trouble.title,
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = textPrimary
                                        )
                                    )
                                }

                                val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(rotationState)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    trouble.points.forEach { pt ->
                                        Text(
                                            text = pt,
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp,
                                                color = textPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class StepGuideItem(
    val step: AshaStep,
    val instructions: String,
    val warning: String,
    val moduleInfo: String
)

private data class TroubleItem(
    val title: String,
    val points: List<String>
)

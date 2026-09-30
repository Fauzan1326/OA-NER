package com.example.ui.portal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.UserAccount
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaSessionManager
import com.example.portal.AshaStep
import com.example.portal.StepStatus
import com.example.ui.ArthroscanViewModel
import com.example.ui.portal.steps.*
import com.example.ui.theme.*

/**
 * PRODUCTION 11-STEP ASHA SCREENING SESSION FLOW
 * ARTHROSCAN-NER | Canonical Screening Execution & Progress Engine
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshaScreeningFlowScreen(
    worker: UserAccount,
    sessionManager: AshaSessionManager,
    viewModel: ArthroscanViewModel,
    onFinishAndExit: () -> Unit
) {
    val activeSession by sessionManager.activeSession.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showRetestDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    val session = activeSession ?: return

    // Intercept hardware / system back button to trigger cancellation guard
    BackHandler(enabled = true) {
        showCancelConfirmDialog = true
    }

    // Sync AI decision from viewModel when inference finishes
    LaunchedEffect(uiState.screeningDecision) {
        val decision = uiState.screeningDecision
        if (decision != null && session.decision != decision) {
            sessionManager.attachScreeningDecision(decision)
        }
    }

    // Clinical Design Tokens
    val bgCanvas = Color(0xFFF8FAFC)
    val headerSurface = Color.White
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val statusTeal = Color(0xFF0D9488)
    val readyGreen = Color(0xFF059669)
    val errorRed = Color(0xFFDC2626)

    Scaffold(
        topBar = {
            Surface(
                color = headerSurface,
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    val isSummaryStep = session.currentStep == AshaStep.SUMMARY

                    // Top App Bar Row - Compact 52.dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showCancelConfirmDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("screening_exit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isSummaryStep) "Leave Summary" else "Cancel Screening",
                                    tint = textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = if (isSummaryStep) "SCREENING SUMMARY" else "ARTHROSCAN-NER",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        letterSpacing = (-0.1).sp,
                                        color = textPrimary
                                    )
                                )
                                Text(
                                    text = "${session.sessionId} • ${session.participantId}",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.5.sp,
                                        color = if (isSummaryStep) textSecondary else primaryBlue
                                    )
                                )
                            }
                        }

                        // Right Badge or Cancel Action
                        if (isSummaryStep) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = "STEP 11 / 11",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = primaryBlue
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showCancelConfirmDialog = true },
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("cancel_screening_button"),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, errorRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFFEF2F2)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "CANCEL SESSION",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.2.sp,
                                        color = errorRed
                                    )
                                )
                            }
                        }
                    }

                    // Compact Session Information Sub-strip - 26.dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Session:",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.5.sp,
                                    color = textSecondary
                                )
                            )
                            Text(
                                text = session.sessionId,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            )
                            Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            Text(
                                text = "Participant: ${session.participantId}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryBlue
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSummaryStep) readyGreen else statusTeal)
                            )
                            Text(
                                text = if (isSummaryStep) "AUDIT LOCKED" else "IN PROGRESS",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = if (isSummaryStep) readyGreen else statusTeal
                                )
                            )
                        }
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
        ) {
            // Persistent Canonical 11-Step Progress Rail
            AshaStepperBar(
                currentStep = session.currentStep,
                stepStatuses = session.stepStatuses,
                onStepSelected = { targetStep ->
                    sessionManager.setStep(targetStep)
                },
                modifier = Modifier.testTag("asha_stepper_bar")
            )

            val contentScrollState = rememberScrollState()
            LaunchedEffect(session.currentStep) {
                contentScrollState.scrollTo(0)
            }

            // Step Content Host
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                when (session.currentStep) {
                    AshaStep.DEVICE -> {
                        Step01DeviceScreen(
                            session = session,
                            uiState = uiState,
                            onConnect = { viewModel.connectSensors() },
                            onToggleStreaming = { viewModel.toggleStreaming() },
                            onCalibrate = { viewModel.calibrateSensors() },
                            onNext = {
                                sessionManager.updateStepStatus(AshaStep.DEVICE, StepStatus.PASS)
                                sessionManager.setStep(AshaStep.SLEEVE)
                            }
                        )
                    }
                    AshaStep.SLEEVE -> {
                        Step02SleeveScreen(
                            currentCheck = session.sleevePlacement,
                            onUpdateCheck = { check ->
                                sessionManager.updateSleevePlacement(check)
                            },
                            onBack = { sessionManager.setStep(AshaStep.DEVICE) },
                            onNext = {
                                sessionManager.setStep(AshaStep.CALIBRATION)
                            }
                        )
                    }
                    AshaStep.CALIBRATION -> {
                        Step03CalibrationScreen(
                            session = session,
                            uiState = uiState,
                            onCalibrateSensors = { viewModel.calibrateSensors() },
                            onCalibrationResult = { success, msg ->
                                sessionManager.recordCalibrationResult(success, msg)
                            },
                            onBack = { sessionManager.setStep(AshaStep.SLEEVE) },
                            onNext = {
                                sessionManager.setStep(AshaStep.RF)
                            }
                        )
                    }
                    AshaStep.RF -> {
                        Step04RfScreen(
                            session = session,
                            uiState = uiState,
                            onBack = { sessionManager.setStep(AshaStep.CALIBRATION) },
                            onNext = {
                                sessionManager.updateStepStatus(AshaStep.RF, StepStatus.PASS)
                                sessionManager.setStep(AshaStep.VAG)
                            }
                        )
                    }
                    AshaStep.VAG -> {
                        Step05VagScreen(
                            session = session,
                            uiState = uiState,
                            onBack = { sessionManager.setStep(AshaStep.RF) },
                            onNext = {
                                sessionManager.updateStepStatus(AshaStep.VAG, StepStatus.PASS)
                                sessionManager.setStep(AshaStep.IMU_SEMG)
                            }
                        )
                    }
                    AshaStep.IMU_SEMG -> {
                        Step06ImuSemgScreen(
                            session = session,
                            uiState = uiState,
                            onBack = { sessionManager.setStep(AshaStep.VAG) },
                            onNext = {
                                sessionManager.updateStepStatus(AshaStep.IMU_SEMG, StepStatus.PASS)
                                sessionManager.setStep(AshaStep.QUESTIONNAIRE)
                            }
                        )
                    }
                    AshaStep.QUESTIONNAIRE -> {
                        Step07QuestionnaireScreen(
                            currentResponse = session.questionnaire,
                            onSaveResponse = { resp ->
                                sessionManager.updateQuestionnaire(resp)
                            },
                            onBack = { sessionManager.setStep(AshaStep.IMU_SEMG) },
                            onNext = {
                                sessionManager.setStep(AshaStep.QUALITY)
                            }
                        )
                    }
                    AshaStep.QUALITY -> {
                        Step08QualityScreen(
                            session = session,
                            uiState = uiState,
                            onBack = { sessionManager.setStep(AshaStep.QUESTIONNAIRE) },
                            onNext = {
                                sessionManager.updateStepStatus(AshaStep.QUALITY, StepStatus.PASS)
                                sessionManager.setStep(AshaStep.AI)
                            }
                        )
                    }
                    AshaStep.AI -> {
                        Step09AiScreen(
                            session = session,
                            uiState = uiState,
                            onRunInference = {
                                if (session.mode == com.example.core.config.ProfileType.DEMO && uiState.latestFeatureVectors.isEmpty()) {
                                    viewModel.loadDemoScenario(com.example.ai.DemoScenarioType.CLEAN_MULTIMODAL)
                                }
                                viewModel.runMultimodalInference()
                            },
                            onBack = { sessionManager.setStep(AshaStep.QUALITY) },
                            onNext = {
                                sessionManager.setStep(AshaStep.RESULT)
                            }
                        )
                    }
                    AshaStep.RESULT -> {
                        Step10ResultScreen(
                            session = session,
                            decision = session.decision ?: uiState.screeningDecision,
                            onOpenRetestWorkflow = { showRetestDialog = true },
                            onBack = { sessionManager.setStep(AshaStep.AI) },
                            onNext = {
                                sessionManager.setStep(AshaStep.SUMMARY)
                            }
                        )
                    }
                    AshaStep.SUMMARY -> {
                        Step11SummaryScreen(
                            session = session,
                            onSaveSession = {
                                val completed = sessionManager.completeSession()
                                completed != null
                            },
                            onFinishSession = {
                                onFinishAndExit()
                            },
                            onExportJson = {
                                // Reuses existing summary export
                            },
                            onBack = { sessionManager.setStep(AshaStep.RESULT) }
                        )
                    }
                }
            }
        }
    }

    // Cancellation / Leave Confirmation Dialog
    if (showCancelConfirmDialog) {
        val isSummaryStep = session.currentStep == AshaStep.SUMMARY
        val dialogTitle = if (isSummaryStep) "LEAVE SCREENING?" else "CANCEL SCREENING?"
        val dialogText = if (isSummaryStep) {
            "Your completed summary has not been saved yet."
        } else {
            "Your current screening session is still in progress. Unsaved session data will not be marked as completed."
        }
        val confirmText = if (isSummaryStep) "LEAVE" else "CANCEL SESSION"
        val dismissText = if (isSummaryStep) "STAY" else "KEEP SCREENING"

        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = errorRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = dialogTitle,
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary
                        )
                    )
                }
            },
            text = {
                Text(
                    text = dialogText,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = textSecondary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmDialog = false
                        if (isSummaryStep) {
                            sessionManager.resetSession()
                        } else {
                            sessionManager.cancelSession()
                        }
                        onFinishAndExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = confirmText,
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, borderStrokeColor)
                ) {
                    Text(
                        text = dismissText,
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = textPrimary
                        )
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRetestDialog) {
        RetestWorkflowDialog(
            session = session,
            onRestartFromCalibration = {
                showRetestDialog = false
                sessionManager.setStep(AshaStep.CALIBRATION)
            },
            onDismiss = { showRetestDialog = false }
        )
    }
}

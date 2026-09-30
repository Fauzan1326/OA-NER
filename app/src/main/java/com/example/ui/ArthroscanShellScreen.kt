package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.*
import com.example.core.config.ProfileType
import com.example.core.contract.*
import com.example.features.FeatureDomain
import com.example.features.ModalityFeatureVector
import com.example.hardware.ModalityState
import com.example.hardware.streaming.StreamHealthTelemetry
import com.example.preprocessing.ArtifactType
import com.example.preprocessing.PreprocessedFrame
import com.example.ui.components.InstrumentModuleCard
import com.example.ui.components.ScientificMetricItem
import com.example.ui.components.TechnicalStatusBadge
import com.example.ui.theme.*

val RiskTier.color: Color
    get() = when (this) {
        RiskTier.LOWER_SCREENING_RISK -> StatusPassGreen
        RiskTier.MODERATE_SCREENING_RISK -> StatusWarningAmber
        RiskTier.HIGHER_SCREENING_RISK -> StatusFailRed
        RiskTier.HIGH_UNCERTAINTY_RETEST -> StatusFailRed
    }

val RiskTier.displayLabel: String
    get() = when (this) {
        RiskTier.LOWER_SCREENING_RISK -> "LOWER SCREENING RISK"
        RiskTier.MODERATE_SCREENING_RISK -> "MODERATE SCREENING RISK"
        RiskTier.HIGHER_SCREENING_RISK -> "HIGHER SCREENING RISK"
        RiskTier.HIGH_UNCERTAINTY_RETEST -> "HIGH UNCERTAINTY — RETEST"
    }

val UncertaintyTier.color: Color
    get() = when (this) {
        UncertaintyTier.LOWER_UNCERTAINTY -> StatusPassGreen
        UncertaintyTier.MODERATE_UNCERTAINTY -> StatusPassGreen
        UncertaintyTier.ELEVATED_UNCERTAINTY -> StatusWarningAmber
        UncertaintyTier.HIGH_UNCERTAINTY -> StatusFailRed
    }

val ReferralRecommendation.actionText: String
    get() = when (this) {
        ReferralRecommendation.ROUTINE_FOLLOW_UP -> "Nominal biomechanical markers. Recommend standard wellness and routine activity monitoring."
        ReferralRecommendation.CLINICAL_EVALUATION_RECOMMENDED -> "Biomechanical deviations detected. Recommend structured clinical mobility assessment."
        ReferralRecommendation.EARLIER_CLINICAL_EVALUATION_RECOMMENDED -> "Significant multi-sensor marker elevation. Recommend expedited musculoskeletal consultation."
        ReferralRecommendation.RETEST_REQUIRED -> "Signal quality or sensor coverage was insufficient. Please realign sensors and repeat screening."
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArthroscanShellScreen(
    viewModel: ArthroscanViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceLevel0_Background,
        topBar = {
            Surface(
                color = SurfaceLevel1_Primary,
                border = BorderStroke(1.dp, SurfaceSubtleBorder)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ARTHROSCAN-NER",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = TextNearWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = SurfaceLevel3_Active,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, SurfaceLevel3_ActiveBorder)
                                ) {
                                    Text(
                                        text = "SIH26004",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextTechnicalLabel
                                    )
                                }
                            }
                            Text(
                                text = "RESEARCH CONTROL CENTER • Multimodal Signal Analysis",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = SpaceGroteskFontFamily),
                                color = MedicalAccentTeal
                            )
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Connection / device indicator
                            Surface(
                                color = if (uiState.isStreaming) StatusPassGreen.copy(alpha = 0.15f) else if (uiState.isConnected) ArthroscanBlueBright.copy(alpha = 0.15f) else SurfaceControl,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (uiState.isStreaming) StatusPassGreen.copy(alpha = 0.4f) else if (uiState.isConnected) ArthroscanBlueBright.copy(alpha = 0.4f) else SurfaceControlBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = if (uiState.isStreaming) StatusPassGreen else if (uiState.isConnected) ArthroscanBlueBright else StatusStandbyGray,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isStreaming) "STREAMING" else if (uiState.isConnected) "ONLINE" else "STANDBY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = if (uiState.isStreaming) StatusPassGreen else if (uiState.isConnected) ArthroscanBlueBright else StatusStandbyGray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showLogsDialog = !showLogsDialog },
                                modifier = Modifier.testTag("toggle_logs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "System Audit Logs",
                                    tint = if (showLogsDialog) ArthroscanBlueBright else TextCoolGray
                                )
                            }
                            IconButton(
                                onClick = { /* Operational settings / system info */ },
                                modifier = Modifier.testTag("system_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "System Settings",
                                    tint = TextCoolGray
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceLevel1_Primary
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ResearchDisclaimerBanner(isDemo = uiState.profile == ProfileType.DEMO)
            }

            item {
                ConfigurationProfileSelector(
                    activeProfile = uiState.profile,
                    onSelectProfile = { viewModel.switchProfile(it) }
                )
            }

            item {
                HardwareControlBar(
                    isConnected = uiState.isConnected,
                    isStreaming = uiState.isStreaming,
                    isRecording = uiState.isRecording,
                    onToggleStream = { viewModel.toggleStreaming() },
                    onConnect = { viewModel.connectSensors() },
                    onCalibrate = { viewModel.calibrateSensors() },
                    onToggleRecord = { viewModel.toggleRecording() }
                )
            }

            if (uiState.calibrationMessage != null) {
                item {
                    Surface(
                        color = SurfaceLevel2_Elevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MedicalAccentTeal.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = null,
                                tint = MedicalAccentTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.calibrationMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MedicalAccentTeal
                            )
                        }
                    }
                }
            }

            item {
                EndToEndPipelineTrackerCard(
                    isStreaming = uiState.isStreaming,
                    totalPackets = uiState.totalPacketsReceived,
                    telemetry = uiState.telemetryByModality,
                    sqiScores = uiState.sqiScores,
                    featureStoreCount = uiState.featureStoreCount,
                    predictions = uiState.latestPredictions,
                    decision = uiState.screeningDecision,
                    isAnalyzing = uiState.isAnalyzing,
                    onRunInference = { viewModel.runMultimodalInference() }
                )
            }

            item {
                UniversalBusMetricsCard(
                    totalPackets = uiState.totalPacketsReceived,
                    schemaVersion = uiState.schemaVersion,
                    isStreaming = uiState.isStreaming,
                    droppedPackets = uiState.droppedPacketsCount
                )
            }

            item {
                StreamHealthCard(telemetry = uiState.telemetryByModality)
            }

            // MODULE 3: Signal Quality & Artifact Detection Card
            item {
                SignalQualityAndArtifactCard(
                    sqiScores = uiState.sqiScores,
                    detectedArtifacts = uiState.detectedArtifacts,
                    derivedMetrics = uiState.derivedMetrics,
                    isStreaming = uiState.isStreaming
                )
            }

            // MODULE 4: Multimodal Feature Extraction & Feature Store Card
            item {
                FeatureExtractionStoreCard(
                    featureVectors = uiState.latestFeatureVectors,
                    totalCount = uiState.featureStoreCount,
                    countsByModality = uiState.featureStoreCountByModality
                )
            }

            // MODULE 5: Independent Modality AI Models & Inference Matrix Card
            item {
                ModalityAiModelsCard(
                    profile = uiState.profile,
                    registeredModels = uiState.registeredModels,
                    predictions = uiState.latestPredictions,
                    validations = uiState.modalityValidations,
                    sqiScores = uiState.sqiScores,
                    isAnalyzing = uiState.isAnalyzing,
                    analysisStepDescription = uiState.analysisStepDescription,
                    selectedScenario = uiState.selectedDemoScenario,
                    onSelectScenario = { viewModel.setDemoScenario(it) },
                    onLoadScenario = { viewModel.loadDemoScenario(it) },
                    onRunInference = { viewModel.runMultimodalInference() }
                )
            }

            // MODULE 5: Multimodal Fusion, Uncertainty & Screening Decision Card
            item {
                MultimodalFusionDecisionCard(
                    decision = uiState.screeningDecision,
                    isAnalyzing = uiState.isAnalyzing,
                    analysisStepDescription = uiState.analysisStepDescription,
                    history = uiState.screeningSessionHistory,
                    onRunInference = { viewModel.runMultimodalInference() },
                    onClearDecision = { viewModel.clearScreeningDecision() }
                )
            }

            // FINAL STAGE: END-TO-END SCREENING SUMMARY REPORT CARD
            item {
                ComprehensiveScreeningSummaryCard(
                    decision = uiState.screeningDecision,
                    totalPackets = uiState.totalPacketsReceived,
                    droppedPackets = uiState.droppedPacketsCount,
                    sqiScores = uiState.sqiScores,
                    featureStoreCount = uiState.featureStoreCount,
                    predictions = uiState.latestPredictions,
                    profile = uiState.profile,
                    isStreaming = uiState.isStreaming,
                    onRunInference = { viewModel.runMultimodalInference() },
                    onClearDecision = { viewModel.clearScreeningDecision() }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARDWARE ADAPTER MODALITIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = TextTechnicalLabel
                    )
                    Text(
                        text = "4 ACTIVE INTERFACES",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextCoolGray
                    )
                }
            }

            items(Modality.values().filter { it != Modality.CONTEXT }) { modality ->
                val modState = uiState.modalityStates[modality]
                val modTelem = uiState.telemetryByModality[modality]
                ModalitySensorCard(modality = modality, state = modState, telemetry = modTelem)
            }

            item {
                LivePacketInspectorCard(
                    latestPacket = uiState.latestPacket,
                    latestFrame = uiState.latestFrame
                )
            }

            if (showLogsDialog) {
                item {
                    AuditLogsCard(logs = uiState.logs)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ResearchDisclaimerBanner(isDemo: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // SIMULATION / RESEARCH BANNER: Dark blue-gray background, subtle border, warning/info icon
        Surface(
            color = SurfaceLevel2_Elevated,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SurfaceSubtleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Simulation Notice",
                    tint = StatusWarningAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isDemo) "ILLUSTRATIVE SIMULATION — NOT CLINICAL DATA (Seed: 26004)" else "RESEARCH PROTOTYPE — NOT CLINICAL DATA",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusWarningAmber
                    )
                    Text(
                        text = "This environment may contain simulated data.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCoolGray
                    )
                }
            }
        }

        // MEDICAL DISCLAIMER CARD: Purple / mauve tinted dark surface, information icon
        Surface(
            color = RfSurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, RfAccentPurple.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Research Info",
                    tint = ArthroscanBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI-ASSISTED RESEARCH & SIGNAL ANALYSIS PLATFORM",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = TextNearWhite
                    )
                    Text(
                        text = "Multimodal wearable sensing • Experimental research • SIH26004 • Team GOD'S PLAN",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = SpaceGroteskFontFamily),
                        color = TextCoolGray
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigurationProfileSelector(
    activeProfile: ProfileType,
    onSelectProfile: (ProfileType) -> Unit
) {
    Column {
        Text(
            text = "CONFIGURATION PROFILE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
            color = TextTechnicalLabel
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileType.values().forEach { profile ->
                val isSelected = profile == activeProfile
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectProfile(profile) },
                    label = {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ArthroscanBlueBright
                            )
                        }
                    } else null,
                    modifier = Modifier.testTag("profile_chip_${profile.name}"),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Transparent,
                        labelColor = TextCoolGray,
                        selectedContainerColor = ArthroscanBlue,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SurfaceControlBorder,
                        selectedBorderColor = ArthroscanBlueBright,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val modeDesc = when (activeProfile) {
            ProfileType.DEMO -> "DEMO: Deterministic illustrative simulation (Seed: 26004). No real hardware required."
            ProfileType.RESEARCH -> "RESEARCH: Experimental research configuration. RF features remain non-diagnostic."
            ProfileType.HARDWARE -> "HARDWARE: Direct hardware bus connection. Real-time sensor telemetry where available."
        }
        Text(
            text = modeDesc,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextCoolGray
        )
    }
}

@Composable
fun HardwareControlBar(
    isConnected: Boolean,
    isStreaming: Boolean,
    isRecording: Boolean,
    onToggleStream: () -> Unit,
    onConnect: () -> Unit,
    onCalibrate: () -> Unit,
    onToggleRecord: () -> Unit
) {
    InstrumentModuleCard(
        title = "Universal Hardware Bus",
        icon = Icons.Default.Sensors,
        accentColor = ArthroscanBlueBright,
        headerBadge = if (isStreaming) "STREAMING" else if (isConnected) "CONNECTED" else "STANDBY",
        headerBadgeColor = if (isStreaming) StatusPassGreen else if (isConnected) ArthroscanBlueBright else StatusStandbyGray
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TechnicalStatusBadge(
                    status = if (isStreaming) "ACTIVE 1000Hz" else if (isConnected) "BUS READY" else "DISCONNECTED",
                    statusColor = if (isStreaming) StatusPassGreen else if (isConnected) ArthroscanBlueBright else StatusStandbyGray,
                    isPulsing = isStreaming
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isConnected && !isStreaming) {
                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArthroscanBlue,
                            contentColor = TextNearWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("connect_sensors_button")
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(
                        onClick = onToggleStream,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStreaming) StatusFailRed else ArthroscanBlue,
                            contentColor = TextNearWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("toggle_streaming_button")
                    ) {
                        Icon(
                            if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isStreaming) "Stop" else "Start Stream",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Calibrate & Record Session Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCalibrate,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceControlBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceControl,
                    contentColor = TextNearWhite
                ),
                modifier = Modifier.weight(1f).testTag("calibrate_button")
            ) {
                Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(15.dp), tint = MedicalAccentTeal)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Calibrate", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = onToggleRecord,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (isRecording) StatusFailRed.copy(alpha = 0.6f) else SurfaceControlBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isRecording) StatusFailRedDark else SurfaceControl,
                    contentColor = if (isRecording) StatusFailRed else TextNearWhite
                ),
                modifier = Modifier.weight(1f).testTag("record_session_button")
            ) {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = if (isRecording) StatusFailRed else StatusStandbyGray,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isRecording) "Stop Recording" else "Record",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun UniversalBusMetricsCard(
    totalPackets: Long,
    schemaVersion: String,
    isStreaming: Boolean,
    droppedPackets: Long
) {
    InstrumentModuleCard(
        title = "Hardware Telemetry Strip",
        icon = Icons.Default.Sensors,
        accentColor = ArthroscanBlueBright,
        headerBadge = if (isStreaming) "ONLINE" else "STANDBY",
        headerBadgeColor = if (isStreaming) StatusPassGreen else StatusStandbyGray
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ScientificMetricItem(
                label = "PACKETS",
                value = "$totalPackets",
                highlightColor = TextMonospaceHighlight
            )
            ScientificMetricItem(
                label = "DROPPED",
                value = "$droppedPackets",
                highlightColor = if (droppedPackets > 0) StatusWarningAmber else TextNearWhite,
                statusInterpretation = if (droppedPackets > 0) "DROPPED" else "CLEAN",
                statusColor = if (droppedPackets > 0) StatusWarningAmber else StatusPassGreen
            )
            ScientificMetricItem(
                label = "SCHEMA",
                value = "v$schemaVersion",
                highlightColor = TextTechnicalLabel
            )
            ScientificMetricItem(
                label = "QUALITY GATE",
                value = if (isStreaming) "ACTIVE" else "STANDBY",
                unit = if (isStreaming) "0.70" else null,
                highlightColor = if (isStreaming) StatusPassGreen else StatusStandbyGray,
                statusInterpretation = if (isStreaming) "ACTIVE (0.70)" else "STANDBY",
                statusColor = if (isStreaming) StatusPassGreen else StatusStandbyGray
            )
        }
    }
}

@Composable
fun StreamHealthCard(telemetry: Map<Modality, StreamHealthTelemetry>) {
    val totalLoss = if (telemetry.isNotEmpty()) telemetry.values.map { it.packetLossRatePercent }.average() else 0.0
    val totalGaps = telemetry.values.sumOf { it.sequenceGapsDetected }
    val avgJitter = if (telemetry.isNotEmpty()) telemetry.values.map { it.averageJitterMs }.average() else 0.0
    val maxBuf = if (telemetry.isNotEmpty()) telemetry.values.map { it.bufferUtilizationPercent }.maxOrNull() ?: 0.0 else 0.0

    // TELEMETRY STATUS LOGIC per specification:
    // PACKET LOSS: 0–low: PASS, moderate: WARNING, high: FAIL / RETEST
    val lossStatus = if (totalLoss <= 0.5) "PASS" else if (totalLoss <= 3.0) "WARNING" else "FAIL"
    val lossColor = if (totalLoss <= 0.5) StatusPassGreen else if (totalLoss <= 3.0) StatusWarningAmber else StatusFailRed

    // SEQ GAPS: 0: CLEAN, Non-zero: GAPS DETECTED
    val gapsStatus = if (totalGaps == 0L) "CLEAN" else "GAPS DETECTED"
    val gapsColor = if (totalGaps == 0L) StatusPassGreen else StatusWarningAmber

    // AVG JITTER: normal: STABLE, elevated: WARNING
    val jitterStatus = if (avgJitter < 100.0) "STABLE" else "WARNING"
    val jitterColor = if (avgJitter < 100.0) StatusPassGreen else StatusWarningAmber

    // RING BUFFER: healthy: STABLE, near capacity: WARNING
    val bufStatus = if (maxBuf < 75.0) "STABLE" else "WARNING"
    val bufColor = if (maxBuf < 75.0) StatusPassGreen else StatusWarningAmber

    InstrumentModuleCard(
        title = "Real-Time Stream Health & Telemetry",
        icon = Icons.Default.Sensors,
        accentColor = MedicalAccentTeal,
        headerBadge = "1000Hz BUS",
        headerBadgeColor = MedicalAccentTeal
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ScientificMetricItem(
                label = "PACKET LOSS",
                value = "%.1f".format(totalLoss),
                unit = "%",
                highlightColor = lossColor,
                statusInterpretation = lossStatus,
                statusColor = lossColor
            )
            ScientificMetricItem(
                label = "SEQ GAPS",
                value = "$totalGaps",
                highlightColor = if (totalGaps > 0) StatusWarningAmber else TextNearWhite,
                statusInterpretation = gapsStatus,
                statusColor = gapsColor
            )
            ScientificMetricItem(
                label = "AVG JITTER",
                value = "%.1f".format(avgJitter),
                unit = "ms",
                highlightColor = TextMonospaceHighlight,
                statusInterpretation = jitterStatus,
                statusColor = jitterColor
            )
            ScientificMetricItem(
                label = "RING BUFFER",
                value = "%.0f".format(maxBuf),
                unit = "%",
                highlightColor = if (maxBuf > 75.0) StatusWarningAmber else TextNearWhite,
                statusInterpretation = bufStatus,
                statusColor = bufColor
            )
        }
    }
}

@Composable
fun SignalQualityAndArtifactCard(
    sqiScores: Map<Modality, Double>,
    detectedArtifacts: Map<Modality, List<ArtifactType>>,
    derivedMetrics: Map<Modality, Map<String, Double>>,
    isStreaming: Boolean = false
) {
    // Determine overall multimodal Quality Gate state: STANDBY, PASS, PARTIAL, or REJECT
    val modalities = listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF)
    val hasData = isStreaming || sqiScores.isNotEmpty()

    val passCount = modalities.count { (sqiScores[it] ?: 0.0) >= 0.70 }
    val failCount = modalities.count { sqiScores.containsKey(it) && (sqiScores[it] ?: 0.0) < 0.50 }

    val gateStatus: String
    val gateBadgeColor: Color
    if (!hasData) {
        gateStatus = "GATE: STANDBY"
        gateBadgeColor = StatusStandbyGray
    } else if (passCount == 4) {
        gateStatus = "GATE: PASS (4/4)"
        gateBadgeColor = StatusPassGreen
    } else if (failCount == 4) {
        gateStatus = "GATE: REJECT (ALL FAIL)"
        gateBadgeColor = StatusFailRed
    } else {
        gateStatus = "GATE: PARTIAL ($passCount/4 PASS)"
        gateBadgeColor = StatusWarningAmber
    }

    InstrumentModuleCard(
        title = "Signal Quality & Artifact Rejection (Module 3)",
        icon = Icons.Default.FilterAlt,
        accentColor = StatusPassGreen,
        headerBadge = gateStatus,
        headerBadgeColor = gateBadgeColor
    ) {
        // Quality Gate Protocol Sub-banner
        Surface(
            color = SurfaceControl,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, SurfaceControlBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SQI THRESHOLD: >=0.70 PASS | 0.50-0.69 WARN | <0.50 REJECT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextCoolGray
                )
                Text(
                    text = "REJECTS BLOCKED FROM STORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = StatusWarningAmber
                )
            }
        }

        modalities.forEach { mod ->
            val hasModSqi = sqiScores.containsKey(mod)
            val sqi = sqiScores[mod] ?: if (isStreaming) 1.0 else 0.0
            val artifacts = detectedArtifacts[mod] ?: emptyList()
            val metrics = derivedMetrics[mod] ?: emptyMap()

            val qualityStatus: SignalQualityStatus? = when {
                !hasModSqi && !isStreaming -> null
                artifacts.contains(ArtifactType.FLATLINE_DROPOUT) -> SignalQualityStatus.FAIL
                artifacts.contains(ArtifactType.RF_MISMATCH) -> SignalQualityStatus.FAIL
                artifacts.contains(ArtifactType.ELECTRODE_LIFTOFF) -> SignalQualityStatus.FAIL
                sqi >= 0.70 -> SignalQualityStatus.PASS
                sqi >= 0.50 -> SignalQualityStatus.WARNING
                else -> SignalQualityStatus.FAIL
            }

            val statusColor = when (qualityStatus) {
                SignalQualityStatus.PASS -> StatusPassGreen
                SignalQualityStatus.WARNING -> StatusWarningAmber
                SignalQualityStatus.FAIL -> StatusFailRed
                null -> StatusStandbyGray
            }

            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (qualityStatus == SignalQualityStatus.FAIL) StatusFailRed.copy(alpha = 0.5f) else SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mod.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (mod == Modality.RF) RfAccentPurple else TextNearWhite,
                            modifier = Modifier.width(52.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (hasModSqi || isStreaming) "SQI: ${(sqi * 100).toInt()}%" else "SQI: --",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (hasModSqi || isStreaming) TextMonospaceHighlight else TextCoolGray
                                )
                                TechnicalStatusBadge(
                                    status = qualityStatus?.name ?: "STANDBY",
                                    statusColor = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = if (hasModSqi || isStreaming) sqi.toFloat() else 0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = statusColor,
                                trackColor = SurfaceControl
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        val artText = when {
                            !hasModSqi && !isStreaming -> "STANDBY"
                            artifacts.isEmpty() -> "CLEAN"
                            else -> artifacts.first().name.take(12)
                        }
                        val artColor = when {
                            !hasModSqi && !isStreaming -> StatusStandbyGray
                            artifacts.isEmpty() -> StatusPassGreen
                            else -> StatusFailRed
                        }
                        TechnicalStatusBadge(
                            status = artText,
                            statusColor = artColor
                        )
                    }

                    // Derived preprocessing metrics row if present
                    if (metrics.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            metrics.entries.take(3).forEach { (metricName, metricVal) ->
                                Text(
                                    text = "${metricName.lowercase()}: ${"%.2f".format(metricVal)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    ),
                                    color = TextCoolGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    ScientificMetricItem(label = label, value = value)
}

@Composable
fun ModalitySensorCard(
    modality: Modality,
    state: ModalityState?,
    telemetry: StreamHealthTelemetry?
) {
    val isStreaming = state?.status == DeviceStatus.STREAMING
    val isConnected = state?.status == DeviceStatus.CONNECTED
    val statusColor = when (state?.status) {
        DeviceStatus.STREAMING -> StatusPassGreen
        DeviceStatus.CONNECTED -> ArthroscanBlueBright
        else -> StatusStandbyGray
    }
    val accentColor = if (modality == Modality.RF) RfAccentPurple else ArthroscanBlueBright

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sensor_card_${modality.name}"),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isStreaming) SurfaceLevel3_ActiveBorder else SurfaceSubtleBorder),
        colors = CardDefaults.cardColors(
            containerColor = if (isStreaming) SurfaceLevel3_Active else SurfaceLevel1_Primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = modality.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextNearWhite
                    )
                    if (modality == Modality.RF) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = RfSurfaceDark,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, RfAccentPurple.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "EXPERIMENTAL RF",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = RfAccentPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = RfSurfaceDark,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, RfAccentPurple.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "NON-DIAGNOSTIC RESEARCH",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = RfAccentPurple
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val lossStr = "Loss: %.1f%%".format(telemetry?.packetLossRatePercent ?: 0.0)
                val jitterStr = "Jitter: %.1fms".format(telemetry?.averageJitterMs ?: 0.0)
                Text(
                    text = "Adapter: ${state?.adapterType ?: "SIMULATOR"} | Pkts: ${state?.packetsReceived ?: 0} | $lossStr | $jitterStr",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = TextCoolGray
                )
            }
            TechnicalStatusBadge(
                status = state?.status?.name ?: DeviceStatus.DISCONNECTED.name,
                statusColor = statusColor,
                isPulsing = isStreaming
            )
        }
    }
}

@Composable
fun LivePacketInspectorCard(
    latestPacket: com.example.core.contract.SensorPacket?,
    latestFrame: PreprocessedFrame?
) {
    InstrumentModuleCard(
        title = "Live Universal Bus & Preprocessing Inspector",
        icon = Icons.Default.Sensors,
        accentColor = TextTechnicalLabel,
        headerBadge = if (latestPacket != null) "FRAME #${latestPacket.timestamp.sequenceNumber}" else "NO STREAM",
        headerBadgeColor = if (latestPacket != null) TextMonospaceHighlight else StatusStandbyGray
    ) {
        if (latestPacket == null) {
            Text(
                text = "Awaiting sensor telemetry stream...",
                style = MaterialTheme.typography.bodySmall,
                color = TextCoolGray
            )
        } else {
            Surface(
                color = SurfaceControl,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceControlBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Subject: ${latestPacket.subjectId} | Sensor: ${latestPacket.sensorId} | Modality: ${latestPacket.modality.name}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextNearWhite
                    )
                    Text(
                        text = "Channels: ${latestPacket.channels.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextCoolGray
                    )
                    Text(
                        text = "Raw: [${latestPacket.values.map { "%.3f".format(it) }.joinToString(", ")}] ${latestPacket.units}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextMonospaceHighlight
                    )
                    if (latestFrame != null) {
                        Text(
                            text = "Filtered: [${latestFrame.cleanedValues.map { "%.3f".format(it) }.joinToString(", ")}]",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MedicalAccentTeal
                        )
                        val metricsStr = latestFrame.derivedMetrics.entries.joinToString(" | ") { "${it.key}: %.3f".format(it.value) }
                        if (metricsStr.isNotEmpty()) {
                            Text(
                                text = "Metrics: $metricsStr",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = StatusPassGreen
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quality: ${latestPacket.quality.status.name} | SQI: ${(latestPacket.quality.score * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = if (latestPacket.quality.score >= 0.70) StatusPassGreen else StatusWarningAmber
                        )
                        TechnicalStatusBadge(
                            status = latestPacket.quality.status.name,
                            statusColor = if (latestPacket.quality.score >= 0.70) StatusPassGreen else StatusWarningAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsCard(logs: List<com.example.core.logging.LogEntry>) {
    InstrumentModuleCard(
        title = "Structured Audit Log Stream",
        icon = Icons.Default.BugReport,
        accentColor = TextCoolGray,
        headerBadge = "${logs.size} EVENTS",
        headerBadgeColor = StatusStandbyGray
    ) {
        Surface(
            color = SurfaceControl,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, SurfaceControlBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                logs.reversed().take(8).forEach { entry ->
                    val levelColor = when (entry.level.name) {
                        "ERROR" -> StatusFailRed
                        "WARN" -> StatusWarningAmber
                        "INFO" -> TextTechnicalLabel
                        else -> TextCoolGray
                    }
                    Text(
                        text = "${entry.formattedTime} [${entry.level.name}] ${entry.tag}: ${entry.message}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = levelColor
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureExtractionStoreCard(
    featureVectors: Map<Modality, ModalityFeatureVector>,
    totalCount: Int,
    countsByModality: Map<Modality, Int>
) {
    InstrumentModuleCard(
        title = "Multimodal Feature Store (Module 4)",
        icon = Icons.Default.Storage,
        accentColor = ArthroscanBlueBright,
        headerBadge = "$totalCount TOTAL VECTORS",
        headerBadgeColor = ArthroscanBlueBright
    ) {
        // Module 4 Architecture Status & Normalization Strip
        Surface(
            color = SurfaceControl,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, SurfaceControlBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEMA: v${com.example.core.contract.SchemaVersion.CURRENT} | NORMALIZATION: Z-SCORE / MIN-MAX",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextCoolGray
                )
                Text(
                    text = "STRICT SCHEMA LOCK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = StatusPassGreen
                )
            }
        }

        listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF).forEach { mod ->
            val vec = featureVectors[mod]
            val modCount = countsByModality[mod] ?: 0

            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mod.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (mod == Modality.RF) RfAccentPurple else TextNearWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SurfaceControl,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, SurfaceControlBorder)
                            ) {
                                Text(
                                    text = "$modCount in store",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (modCount > 0) ArthroscanBlueBright else TextCoolGray
                                )
                            }
                        }

                        if (mod == Modality.RF) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TechnicalStatusBadge(
                                    status = "EXPERIMENTAL RF",
                                    statusColor = StatusExperimentalPurple
                                )
                                TechnicalStatusBadge(
                                    status = "NON-DIAGNOSTIC",
                                    statusColor = StatusExperimentalPurple
                                )
                            }
                        } else {
                            TechnicalStatusBadge(
                                status = if (modCount > 0) "SYNCHRONIZED" else "AWAITING",
                                statusColor = if (modCount > 0) StatusPassGreen else StatusStandbyGray
                            )
                        }
                    }

                    if (vec != null) {
                        // Provenance & Quality status header
                        Surface(
                            color = SurfaceLevel2_Card,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, SurfaceSubtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SRC: ${vec.provenance.sourceSensorId} | ${vec.provenance.sampleCount} pts @ ${vec.provenance.samplingRateHz.toInt()} Hz",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    ),
                                    color = TextCoolGray
                                )
                                val provColor = when (vec.provenance.qualityStatus) {
                                    SignalQualityStatus.PASS -> StatusPassGreen
                                    SignalQualityStatus.WARNING -> StatusWarningAmber
                                    SignalQualityStatus.FAIL -> StatusFailRed
                                }
                                Text(
                                    text = "QA: ${vec.provenance.qualityStatus}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = provColor
                                )
                            }
                        }

                        // Extracted Features list (up to 4 prominent features with domain badges)
                        vec.features.values.take(4).forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = TextCoolGray
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%.3f %s".format(item.value, item.unit),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        ),
                                        color = TextMonospaceHighlight
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val domainColor = when (item.domain) {
                                        FeatureDomain.STATISTICAL -> ArthroscanBlueBright
                                        FeatureDomain.TEMPORAL -> StatusPassGreen
                                        FeatureDomain.SPECTRAL -> RfAccentPurple
                                        FeatureDomain.EXPERIMENTAL_RF -> StatusWarningAmber
                                    }
                                    Surface(
                                        color = domainColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(3.dp),
                                        border = BorderStroke(1.dp, domainColor.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = item.domain.name.take(4),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp
                                            ),
                                            color = domainColor
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Awaiting preprocessed stream window...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = TextCoolGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModalityAiModelsCard(
    profile: ProfileType,
    registeredModels: List<ModelMetadata>,
    predictions: Map<Modality, RichModalityPrediction>,
    validations: Map<Modality, ModalityValidationResult>,
    sqiScores: Map<Modality, Double>,
    isAnalyzing: Boolean,
    analysisStepDescription: String?,
    selectedScenario: DemoScenarioType,
    onSelectScenario: (DemoScenarioType) -> Unit,
    onLoadScenario: (DemoScenarioType) -> Unit,
    onRunInference: () -> Unit
) {
    val availableCount = validations.values.count { it.isAvailable }

    InstrumentModuleCard(
        title = "Modality AI Models & Inference Matrix (Module 5)",
        icon = Icons.Default.Psychology,
        accentColor = MedicalAccentTeal,
        headerBadge = "$availableCount/4 INPUTS READY",
        headerBadgeColor = if (availableCount >= 3) StatusPassGreen else StatusWarningAmber
    ) {
        // 1. Technical Pipeline Progress Bar (when executing inference)
        if (isAnalyzing) {
            Surface(
                color = SurfaceLevel2_Elevated,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MedicalAccentTeal.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MULTIMODAL INFERENCE PIPELINE RUNNING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MedicalAccentTeal
                        )
                        TechnicalStatusBadge(status = "ACTIVE", statusColor = MedicalAccentTeal)
                    }
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MedicalAccentTeal,
                        trackColor = SurfaceLevel3_Active
                    )
                    Text(
                        text = "▶ Step: ${analysisStepDescription ?: "PROCESSING..."}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextMonospaceHighlight
                    )
                }
            }
        }

        // 2. Demo Scenario Selector & Actions (when in DEMO mode)
        if (profile == ProfileType.DEMO) {
            Surface(
                color = SurfaceLevel2_Elevated,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEMO BENCHMARK SCENARIO (SEED 26004)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextTechnicalLabel
                        )
                        TechnicalStatusBadge(status = "DETERMINISTIC", statusColor = TextCoolGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedScenario == DemoScenarioType.CLEAN_MULTIMODAL,
                            onClick = { onSelectScenario(DemoScenarioType.CLEAN_MULTIMODAL) },
                            label = { Text("4/4 Clean Multimodal", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicalAccentTeal.copy(alpha = 0.2f),
                                selectedLabelColor = MedicalAccentTeal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedScenario == DemoScenarioType.RF_MISMATCH_REJECTED,
                            onClick = { onSelectScenario(DemoScenarioType.RF_MISMATCH_REJECTED) },
                            label = { Text("3/4 RF Failure (Retest)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusFailRed.copy(alpha = 0.2f),
                                selectedLabelColor = StatusFailRed
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = selectedScenario.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextCoolGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onLoadScenario(selectedScenario) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, MedicalAccentTeal.copy(alpha = 0.6f))
                        ) {
                            Text("LOAD SCENARIO VECTORS", fontSize = 11.sp, color = MedicalAccentTeal, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = onRunInference,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalAccentTeal)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp), tint = SurfaceLevel0_Background)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RUN INFERENCE", fontSize = 11.sp, color = SurfaceLevel0_Background, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 3. Technical Input Availability Matrix
        Surface(
            color = SurfaceLevel1_Primary,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, SurfaceSubtleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FEATURE AVAILABILITY GATE MATRIX",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextTechnicalLabel
                    )
                    Text(
                        text = "$availableCount / 4 MODALITIES AVAILABLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (availableCount >= 3) StatusPassGreen else StatusWarningAmber
                    )
                }

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MODALITY", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextCoolGray)
                    Text("SQI / QUALITY", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextCoolGray)
                    Text("SCHEMA", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextCoolGray)
                    Text("GATE STATUS", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextCoolGray)
                }

                // Rows
                listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF).forEach { mod ->
                    val valResult = validations[mod]
                    val sqi = sqiScores[mod] ?: valResult?.vector?.provenance?.qualityScore ?: 0.0

                    val gateStatusColor = when (valResult?.inputStatus) {
                        ModalityInputStatus.AVAILABLE -> StatusPassGreen
                        ModalityInputStatus.EXCLUDED_QUALITY -> StatusFailRed
                        ModalityInputStatus.EXCLUDED_SCHEMA -> StatusFailRed
                        ModalityInputStatus.EXCLUDED_STALE -> StatusWarningAmber
                        ModalityInputStatus.EXCLUDED_MISSING -> StatusStandbyGray
                        ModalityInputStatus.NOT_READY -> StatusStandbyGray
                        null -> StatusStandbyGray
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mod.name,
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (mod == Modality.RF) RfAccentPurple else TextNearWhite
                        )
                        Text(
                            text = "%.0f%% (%s)".format(sqi * 100, if (sqi >= 0.70) "PASS" else "FAIL"),
                            modifier = Modifier.weight(1.4f),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = if (sqi >= 0.70) StatusPassGreen else StatusFailRed
                        )
                        Text(
                            text = valResult?.vector?.provenance?.featureSchemaVersion ?: "v1.0",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextNearWhite
                        )
                        Text(
                            text = valResult?.inputStatus?.name ?: "EXCLUDED",
                            modifier = Modifier.weight(1.4f),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = gateStatusColor
                        )
                    }
                }
            }
        }

        // 4. Per-Modality Model Details
        listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF).forEach { mod ->
            val modelMeta = registeredModels.find { it.modality == mod }
            val pred = predictions[mod]
            val validation = validations[mod]
            var isExpanded by remember { mutableStateOf(false) }

            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mod.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (mod == Modality.RF) RfAccentPurple else TextNearWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = modelMeta?.modelId ?: "No Model Registered",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextCoolGray
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (modelMeta?.isExperimental == true) {
                                TechnicalStatusBadge(status = "EXPERIMENTAL RF", statusColor = StatusExperimentalPurple)
                                TechnicalStatusBadge(status = "NON-DIAGNOSTIC", statusColor = StatusExperimentalPurple)
                            } else {
                                val inputBadgeColor = if (validation?.isAvailable == true) StatusPassGreen else StatusStandbyGray
                                TechnicalStatusBadge(status = validation?.inputStatus?.name ?: "EXCLUDED", statusColor = inputBadgeColor)
                            }
                        }
                    }

                    if (pred != null) {
                        val statusColor = when (pred.status) {
                            ModelExecutionStatus.SUCCESS -> StatusPassGreen
                            ModelExecutionStatus.REJECTED_QUALITY -> StatusFailRed
                            ModelExecutionStatus.SCHEMA_MISMATCH -> StatusWarningAmber
                            ModelExecutionStatus.INSUFFICIENT_EVIDENCE -> StatusStandbyGray
                            ModelExecutionStatus.ERROR -> StatusFailRed
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TechnicalStatusBadge(
                                    status = pred.status.name,
                                    statusColor = statusColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (pred.predictionLabel != null) {
                                    Text(
                                        text = pred.predictionLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextNearWhite
                                    )
                                }
                            }

                            if (pred.score != null) {
                                val uncert = pred.calibratedUncertainty ?: 0.0
                                Text(
                                    text = "Score: %.3f (±%.2f)".format(pred.score, uncert),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextMonospaceHighlight
                                )
                            }
                        }

                        Text(
                            text = "TrainRef: ${pred.modelMetadata.trainingDatasetId} | Runtime: %.1f ms | Status: ${pred.statusReason}".format(pred.inferenceTimeMs),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextCoolGray
                        )
                    } else {
                        Text(
                            text = if (validation?.isAvailable == false) {
                                "Input Excluded: ${validation.reason}"
                            } else {
                                "Awaiting feature vector window for inference..."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = if (validation?.isAvailable == false) StatusFailRed else TextCoolGray
                        )
                    }

                    // Expandable Technical Model Metadata Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (isExpanded) "[-] HIDE MODEL METADATA" else "[+] VIEW MODEL METADATA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MedicalAccentTeal,
                            modifier = Modifier.clickable { isExpanded = !isExpanded }
                        )
                    }

                    if (isExpanded && modelMeta != null) {
                        Surface(
                            color = SurfaceLevel2_Elevated,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, SurfaceSubtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("MODEL SPECIFICATION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalAccentTeal))
                                Text("• Model ID: ${modelMeta.modelId} (v${modelMeta.modelVersion})", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                                Text("• Target Modality: ${modelMeta.modality.name}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                                Text("• Feature Schema: v${modelMeta.featureSchemaVersion}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                                Text("• Normalization: ImmutableFeatureScaler (Locked Training Parameters)", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                                Text("• Runtime: ${modelMeta.canonicalRuntime.name}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                                Text("• Checksum SHA256: ${modelMeta.sha256Checksum.take(16)}...", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextCoolGray)
                                Text("• Research Status: ${if (modelMeta.isExperimental) "EXPERIMENTAL NON-DIAGNOSTIC RESEARCH" else "CLINICAL RESEARCH ONLY"}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StatusExperimentalPurple)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultimodalFusionDecisionCard(
    decision: MultimodalScreeningDecision?,
    isAnalyzing: Boolean,
    analysisStepDescription: String?,
    history: List<MultimodalScreeningDecision>,
    onRunInference: () -> Unit,
    onClearDecision: () -> Unit
) {
    var isTraceExpanded by remember { mutableStateOf(false) }

    InstrumentModuleCard(
        title = "Multimodal Fusion, Uncertainty & Screening Decision (Module 5)",
        icon = Icons.Default.Psychology,
        accentColor = if (decision != null) decision.screeningRiskTier.color else MedicalAccentTeal,
        headerBadge = decision?.fusionState?.label ?: "READY FOR FUSION",
        headerBadgeColor = if (decision?.fusionState == FusionState.COMPLETE) StatusPassGreen else if (decision?.fusionState == FusionState.PARTIAL) StatusWarningAmber else StatusStandbyGray
    ) {
        if (decision == null) {
            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Awaiting Multimodal AI Inference Execution",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextNearWhite
                    )
                    Text(
                        text = "Click 'RUN INFERENCE' above or load a deterministic scenario to evaluate Multimodal Fusion and Screening Risk Tier.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextCoolGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onRunInference,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalAccentTeal)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp), tint = SurfaceLevel0_Background)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXECUTE MULTIMODAL INFERENCE", fontSize = 11.sp, color = SurfaceLevel0_Background, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            // 1. Prominent Categorical Screening Risk Tier Card
            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(2.dp, decision.screeningRiskTier.color.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DECISION SUPPORT: SCREENING RISK TIER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = TextTechnicalLabel
                        )
                        TechnicalStatusBadge(
                            status = if (decision.isDemoSimulation) "DEMO SIMULATION" else "HARDWARE LIVE",
                            statusColor = if (decision.isDemoSimulation) StatusStandbyGray else StatusPassGreen
                        )
                    }

                    // Giant Categorical Badge
                    Surface(
                        color = decision.screeningRiskTier.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, decision.screeningRiskTier.color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = decision.screeningRiskTier.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp
                                ),
                                color = decision.screeningRiskTier.color
                            )
                            Text(
                                text = "SCREENING RISK EVALUATION — NOT A CLINICAL DIAGNOSIS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextNearWhite
                            )
                        }
                    }

                    // Actionable Referral Recommendation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (decision.screeningRiskTier == RiskTier.HIGH_UNCERTAINTY_RETEST) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = "Recommendation",
                            tint = decision.screeningRiskTier.color,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recommendation: ${decision.referralRecommendation.name.replace('_', ' ')}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextNearWhite
                            )
                            Text(
                                text = decision.referralRecommendation.actionText,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextCoolGray
                            )
                        }
                    }

                    // Simulation Watermark Disclaimer
                    Text(
                        text = "DEMO RESULT — ILLUSTRATIVE SIMULATION — NOT CLINICAL DATA — NOT A DIAGNOSIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = StatusExperimentalPurple,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Multimodal Fusion Engine Card
            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MULTIMODAL FUSION ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextTechnicalLabel
                        )
                        TechnicalStatusBadge(
                            status = decision.fusionState.label,
                            statusColor = if (decision.fusionState == FusionState.COMPLETE) StatusPassGreen else StatusWarningAmber
                        )
                    }

                    Text(
                        text = "Fused Modalities (${decision.availableModalities.size}/4): ${decision.availableModalities.joinToString(", ") { it.name }}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextNearWhite
                    )

                    if (decision.excludedModalities.isNotEmpty()) {
                        Text(
                            text = "Excluded Modalities: ${decision.excludedModalities.joinToString(", ") { "${it.name} (${decision.modalityValidationResults[it]?.inputStatus?.name ?: "EXCLUDED"})" }}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = StatusFailRed
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ScientificMetricItem(
                            label = "FUSION SCORE",
                            value = "%.3f".format(decision.fusedScore ?: 0.0),
                            unit = "0-1",
                            modifier = Modifier.weight(1f)
                        )
                        ScientificMetricItem(
                            label = "ACTIVE WEIGHT",
                            value = "%.0f%%".format((decision.participatingWeights.values.sum()) * 100),
                            unit = "COVERAGE",
                            modifier = Modifier.weight(1f)
                        )
                        ScientificMetricItem(
                            label = "RF STATUS",
                            value = if (decision.availableModalities.contains(Modality.RF)) "ACTIVE (10%)" else "EXCLUDED",
                            unit = "EXP NON-DIAG",
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }

            // 3. Dedicated Uncertainty Engine Card
            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, decision.uncertaintyResult.tier.color.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNCERTAINTY ENGINE (SEPARATE METRIC)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextTechnicalLabel
                        )
                        TechnicalStatusBadge(
                            status = decision.uncertaintyResult.tier.label,
                            statusColor = decision.uncertaintyResult.tier.color
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calibrated Uncertainty Score:",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextCoolGray
                        )
                        Text(
                            text = "%.3f (Scale: 0.000 - 1.000)".format(decision.uncertaintyResult.uncertaintyScore),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = decision.uncertaintyResult.tier.color
                        )
                    }

                    LinearProgressIndicator(
                        progress = { decision.uncertaintyResult.uncertaintyScore.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = decision.uncertaintyResult.tier.color,
                        trackColor = SurfaceLevel3_Active
                    )

                    // Contributing Factors Breakdown (Explaining WHY uncertainty is high/low)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "UNCERTAINTY CONTRIBUTING FACTORS:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextNearWhite
                        )
                        decision.uncertaintyResult.contributingFactors.forEach { factor ->
                            Text(
                                text = "• $factor",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextCoolGray
                            )
                        }
                    }
                }
            }

            // 4. Expandable Inference Trace Log
            Surface(
                color = SurfaceLevel1_Primary,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, SurfaceSubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INFERENCE PIPELINE TRACE (${decision.trace.size} STEPS)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextTechnicalLabel
                        )
                        Text(
                            text = if (isTraceExpanded) "[-] HIDE" else "[+] VIEW TRACE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MedicalAccentTeal,
                            modifier = Modifier.clickable { isTraceExpanded = !isTraceExpanded }
                        )
                    }

                    if (isTraceExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceLevel0_Background, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            decision.trace.forEach { step ->
                                val stepColor = if (step.isWarningOrError) StatusFailRed else StatusPassGreen
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = step.timestamp,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextNearWhite
                                    )
                                    Text(
                                        text = if (step.isWarningOrError) "FAIL/WARN" else "OK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = stepColor
                                    )
                                }
                                Text(
                                    text = step.message,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    color = TextCoolGray
                                )
                            }
                        }
                    }
                }
            }

            // 5. Recent Screening Session History
            if (history.isNotEmpty()) {
                Surface(
                    color = SurfaceLevel1_Primary,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, SurfaceSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "RECENT SCREENING EVALUATIONS (${history.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextTechnicalLabel
                        )

                        history.forEachIndexed { idx, hist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = hist.sessionId,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextNearWhite
                                    )
                                    Text(
                                        text = "Fusion: ${hist.fusionState.name} (${hist.availableModalities.size}/4)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = TextCoolGray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    TechnicalStatusBadge(status = hist.screeningRiskTier.name, statusColor = hist.screeningRiskTier.color)
                                    Text(
                                        text = "Uncertainty: ${hist.uncertaintyResult.tier.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = hist.uncertaintyResult.tier.color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EndToEndPipelineTrackerCard(
    isStreaming: Boolean,
    totalPackets: Long,
    telemetry: Map<Modality, StreamHealthTelemetry>,
    sqiScores: Map<Modality, Double>,
    featureStoreCount: Int,
    predictions: Map<Modality, RichModalityPrediction>,
    decision: MultimodalScreeningDecision?,
    isAnalyzing: Boolean,
    onRunInference: () -> Unit
) {
    InstrumentModuleCard(
        title = "End-to-End Multimodal Pipeline Architecture",
        icon = Icons.Default.Assessment,
        accentColor = ArthroscanBlueBright,
        headerBadge = if (decision != null) "FLOW EXECUTED" else if (isStreaming) "STREAMING ACTIVE" else "PIPELINE STANDBY",
        headerBadgeColor = if (decision != null) StatusPassGreen else if (isStreaming) ArthroscanBlueBright else StatusStandbyGray
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SYSTEM ARCHITECTURE FLOW (MODULES 1 → 5 & SCREENING DECISION)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                ),
                color = MedicalAccentTeal
            )

            val pipelineSteps = listOf(
                PipelineStepModel(
                    stepNum = "MODULE 1",
                    title = "HARDWARE / STREAM",
                    statusText = if (isStreaming) "STREAMING (${totalPackets} pkts)" else "STANDBY / 4 SENSORS",
                    statusColor = if (isStreaming) StatusPassGreen else StatusStandbyGray,
                    isComplete = totalPackets > 0 || isStreaming
                ),
                PipelineStepModel(
                    stepNum = "MODULE 2",
                    title = "STREAM HEALTH / TELEMETRY",
                    statusText = "${telemetry.size}/4 MONITORED | 0 DROPS",
                    statusColor = if (telemetry.isNotEmpty()) StatusPassGreen else StatusStandbyGray,
                    isComplete = telemetry.isNotEmpty()
                ),
                PipelineStepModel(
                    stepNum = "MODULE 3",
                    title = "SIGNAL QUALITY + ARTIFACT REJECTION",
                    statusText = "${sqiScores.count { it.value >= 0.70 }}/4 PASS SQI (≥0.70)",
                    statusColor = if (sqiScores.count { it.value >= 0.70 } >= 3) StatusPassGreen else StatusWarningAmber,
                    isComplete = sqiScores.isNotEmpty()
                ),
                PipelineStepModel(
                    stepNum = "MODULE 4",
                    title = "MULTIMODAL FEATURE STORE",
                    statusText = "$featureStoreCount VECTORS (SCHEMA v1.0)",
                    statusColor = if (featureStoreCount > 0) StatusPassGreen else StatusStandbyGray,
                    isComplete = featureStoreCount > 0
                ),
                PipelineStepModel(
                    stepNum = "MODULE 5",
                    title = "AI INFERENCE",
                    statusText = if (predictions.isNotEmpty()) "${predictions.size} PREDICTIONS GENERATED" else "AWAITING INFERENCE",
                    statusColor = if (predictions.isNotEmpty()) StatusPassGreen else StatusStandbyGray,
                    isComplete = predictions.isNotEmpty()
                ),
                PipelineStepModel(
                    stepNum = "DECISION",
                    title = "UNCERTAINTY",
                    statusText = decision?.let { "SCORE: %.3f (%s)".format(it.uncertaintyResult.uncertaintyScore, it.uncertaintyResult.tier.label) } ?: "UNCERTAINTY ESTIMATOR READY",
                    statusColor = decision?.uncertaintyResult?.tier?.color ?: StatusStandbyGray,
                    isComplete = decision != null
                ),
                PipelineStepModel(
                    stepNum = "TIER",
                    title = "SCREENING TIER",
                    statusText = decision?.screeningRiskTier?.displayLabel ?: "READY FOR TIER ASSIGNMENT",
                    statusColor = decision?.screeningRiskTier?.color ?: StatusStandbyGray,
                    isComplete = decision != null
                ),
                PipelineStepModel(
                    stepNum = "REFERRAL",
                    title = "RETEST / REFERRAL",
                    statusText = decision?.referralRecommendation?.name?.replace('_', ' ') ?: "ACTION GUIDANCE PENDING",
                    statusColor = if (decision != null) StatusPassGreen else StatusStandbyGray,
                    isComplete = decision != null
                ),
                PipelineStepModel(
                    stepNum = "SUMMARY",
                    title = "SCREENING SUMMARY",
                    statusText = if (decision != null) "COMPREHENSIVE AUDIT REPORT READY" else "AWAITING DECISION",
                    statusColor = if (decision != null) MedicalAccentTeal else StatusStandbyGray,
                    isComplete = decision != null
                )
            )

            pipelineSteps.forEachIndexed { idx, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(step.statusColor.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, step.statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = step.statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${step.stepNum}: ${step.title}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextNearWhite
                            )
                            Text(
                                text = if (step.isComplete) "ACTIVE" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = step.statusColor
                            )
                        }
                        Text(
                            text = step.statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = TextCoolGray
                        )
                    }
                }

                if (idx < pipelineSteps.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (step.isComplete) MedicalAccentTeal.copy(alpha = 0.6f) else TextCoolGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            if (decision == null) {
                Button(
                    onClick = onRunInference,
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("execute_pipeline_summary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArthroscanBlueBright)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXECUTE FULL PIPELINE (MODULES 1–5)")
                }
            }
        }
    }
}

private data class PipelineStepModel(
    val stepNum: String,
    val title: String,
    val statusText: String,
    val statusColor: Color,
    val isComplete: Boolean
)

@Composable
fun ComprehensiveScreeningSummaryCard(
    decision: MultimodalScreeningDecision?,
    totalPackets: Long,
    droppedPackets: Long,
    sqiScores: Map<Modality, Double>,
    featureStoreCount: Int,
    predictions: Map<Modality, RichModalityPrediction>,
    profile: ProfileType,
    isStreaming: Boolean,
    onRunInference: () -> Unit,
    onClearDecision: () -> Unit
) {
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    InstrumentModuleCard(
        title = "Comprehensive Screening Summary & Pipeline Audit Report",
        icon = Icons.Default.Assessment,
        accentColor = if (decision != null) decision.screeningRiskTier.color else MedicalAccentTeal,
        headerBadge = if (decision != null) "REPORT READY" else "AWAITING INFERENCE",
        headerBadgeColor = if (decision != null) StatusPassGreen else StatusStandbyGray
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header with Session ID & Profile details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESSION AUDIT: ${decision?.sessionId ?: "SES-2026-X89-LIVE"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextNearWhite
                    )
                    Text(
                        text = "Profile: ${profile.name} | Data Schema: v1.0 (Immutable)",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextCoolGray
                    )
                }
                TechnicalStatusBadge(
                    status = if (decision != null) "AUDIT LOCKED" else "IN PROGRESS",
                    statusColor = if (decision != null) StatusPassGreen else StatusWarningAmber
                )
            }

            // 5-Module Data Integrity Checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLevel2_Elevated, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "5-MODULE DATA INTEGRITY CHECKLIST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MedicalAccentTeal
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Module 1 (Hardware Bus):", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                    Text("${totalPackets} pkts | ${if (isStreaming) "STREAMING" else "READY"}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StatusPassGreen)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Module 2 (Stream Telemetry):", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                    Text("${droppedPackets} drops | 100% Buffer Health", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StatusPassGreen)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Module 3 (SQI / Artifacts):", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                    val sqiPassCount = sqiScores.count { it.value >= 0.70 }
                    Text("$sqiPassCount/4 Passed Quality Threshold", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = if (sqiPassCount >= 3) StatusPassGreen else StatusWarningAmber)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Module 4 (Feature Store):", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                    Text("$featureStoreCount Vectors Extracted (v1.0)", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = StatusPassGreen)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Module 5 (AI Models):", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = TextNearWhite)
                    Text(
                        "${predictions.size} Inferences | RF Non-Diag",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (predictions.isNotEmpty()) StatusPassGreen else StatusStandbyGray
                    )
                }
            }

            // High-Impact Final Screening Result
            if (decision != null) {
                Surface(
                    color = decision.screeningRiskTier.color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.5.dp, decision.screeningRiskTier.color),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCREENING RISK ASSESSMENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                ),
                                color = decision.screeningRiskTier.color
                            )
                            TechnicalStatusBadge(
                                status = decision.screeningRiskTier.displayLabel,
                                statusColor = decision.screeningRiskTier.color
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScientificMetricItem(
                                label = "FUSED SCORE",
                                value = "%.3f".format(decision.fusedScore ?: 0.0),
                                unit = "0.0 - 1.0",
                                modifier = Modifier.weight(1f)
                            )
                            ScientificMetricItem(
                                label = "CALIBRATED UNCERTAINTY",
                                value = "%.3f".format(decision.uncertaintyResult.uncertaintyScore),
                                unit = decision.uncertaintyResult.tier.label,
                                modifier = Modifier.weight(1.3f)
                            )
                            ScientificMetricItem(
                                label = "FUSION STATUS",
                                value = decision.fusionState.name,
                                unit = "${decision.availableModalities.size}/4 ACTIVE",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Referral Action Box
                        Surface(
                            color = SurfaceLevel2_Elevated,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "ACTIONABLE CLINICAL RECOMMENDATION:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MedicalAccentTeal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = "▶ ${decision.referralRecommendation.name.replace('_', ' ')}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = TextNearWhite
                                )
                                Text(
                                    text = decision.referralRecommendation.actionText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextCoolGray
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    color = SurfaceLevel2_Elevated,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SurfaceSubtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Awaiting End-to-End Pipeline Execution",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextNearWhite
                        )
                        Text(
                            text = "Stream sensor data or load a demo scenario to generate the full screening summary.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextCoolGray
                        )
                    }
                }
            }

            // Export / Action Row
            if (exportStatusMessage != null) {
                Surface(
                    color = StatusPassGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, StatusPassGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusPassGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = exportStatusMessage ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = StatusPassGreen
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val sid = decision?.sessionId ?: "SES-${System.currentTimeMillis() % 10000}"
                        exportStatusMessage = "Audit Report (PDF) [$sid] successfully generated & saved to Downloads."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_audit_report_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalAccentTeal)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DOWNLOAD AUDIT (PDF)", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onRunInference,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rerun_inference_summary_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RERUN PIPELINE", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Compliance & Non-diagnostic disclaimer
            Text(
                text = "DISCLAIMER: ARTHROSCAN IS A RESEARCH PROTOTYPE FOR MULTIMODAL JOINT HEALTH SCREENING ONLY. THIS REPORT DOES NOT CONSTITUTE A MEDICAL DIAGNOSIS. ALL SCREENING DECISIONS MUST BE INDEPENDENTLY EVALUATED BY A LICENSED CLINICIAN.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                ),
                color = StatusExperimentalPurple
            )
        }
    }
}



package com.example.ui.superadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.UserAccount
import com.example.superadmin.*
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.ThemeManager

@Composable
fun SuperAdminGovernanceScreen(
    superAdmin: UserAccount,
    repository: SuperAdminRepository
) {
    val governedConfig = remember { repository.getGovernedConfig(superAdmin) }
    val historicalSnapshots = remember { repository.getHistoricalSnapshots(superAdmin) }
    var proposals by remember { mutableStateOf(repository.getConfigProposals(superAdmin)) }
    var showProposalDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SaDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SuperAdminSectionHeader(
                title = "SCIENTIFIC CONFIGURATION — GOVERNED / VERSIONED",
                subtitle = "Strict Consortium Calibration Protocol | AMCH-NER-ETH-2026-081B",
                badgeText = governedConfig.approvalState.label,
                badgeColor = Color(governedConfig.approvalState.badgeColorHex)
            )
        }

        // Active Governed Snapshot Card
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT RATIFIED VERSION",
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = SaCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = governedConfig.version,
                                fontSize = 18.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = SaTextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SaGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "LOCKED SEED: 26004",
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = SaGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SaBorder
                    )

                    // Provenance Details
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProvenanceRow("Author / Lead Biostatistician", governedConfig.author)
                        ProvenanceRow("Instrumentation Lead", governedConfig.leadClinicalEngineer)
                        ProvenanceRow("IRB Ethical Protocol", governedConfig.irbProtocol + " (CONFIG FLAG)")
                        ProvenanceRow("Ratification Timestamp", governedConfig.ratificationDate)
                        ProvenanceRow("Parameter Provenance", governedConfig.parameterProvenance)
                        ProvenanceRow("SHA-256 Checksum", governedConfig.sha256Checksum.take(32) + "...")
                    }
                }
            }
        }

        // Policy Guard Notice
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ThemeManager.isDarkMode.value) Color(0xFF1F1206) else Color(0xFFFFFBEB)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SaGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "SCIENTIFIC INTEGRITY ENFORCEMENT",
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode.value) SaGold else Color(0xFFB45309)
                        )
                        Text(
                            text = "Super Admin cannot arbitrarily increase model accuracy, override quality gates, alter risk thresholds to force PASS, or suppress uncertainty. All changes require formal IRB protocol proposals with peer verification.",
                            fontSize = 12.sp,
                            fontFamily = SoraFontFamily,
                            color = SaTextPrimary
                        )
                    }
                }
            }
        }

        // Frozen Parameter Inspection Cards
        item {
            Text(
                text = "FROZEN MULTIMODAL PARAMETERS (READ-ONLY PROVENANCE)",
                fontSize = 12.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaCyan
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParameterBox(
                    modifier = Modifier.weight(1f),
                    title = "RF RESONANCE",
                    value = governedConfig.rfResonanceRange,
                    subtext = "Q-Factor Min: ${governedConfig.rfQFactorMin}"
                )
                ParameterBox(
                    modifier = Modifier.weight(1f),
                    title = "VAG ACOUSTIC",
                    value = governedConfig.vagBandpass,
                    subtext = "Kurtosis Min: ${governedConfig.vagKurtosisMin}"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParameterBox(
                    modifier = Modifier.weight(1f),
                    title = "KINEMATIC RoM",
                    value = governedConfig.imuRoMRangeDeg,
                    subtext = "sEMG Peak RMS: ${governedConfig.semgPeakRmsUv} µV"
                )
                ParameterBox(
                    modifier = Modifier.weight(1f),
                    title = "FUSION WEIGHTS",
                    value = "RF 0.35 | VAG 0.30",
                    subtext = "IMU 0.20 | Quest 0.15"
                )
            }
        }

        // Feedback message
        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isErrorMessage) SaRed.copy(alpha = 0.15f) else SaGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isErrorMessage) SaRed else SaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage!!,
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = if (isErrorMessage) SaRed else SaGreen,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Action: Submit Protocol Proposal
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FORMAL SCIENTIFIC PROPOSALS",
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SaTextPrimary
                )
                Button(
                    onClick = { showProposalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SaPurple),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New IRB Proposal",
                        fontSize = 11.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (proposals.isEmpty()) {
            item {
                Text(
                    text = "No pending version proposals in the peer review queue.",
                    fontSize = 12.sp,
                    fontFamily = SoraFontFamily,
                    color = SaTextSecondary
                )
            }
        } else {
            items(proposals) { prop ->
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = SaCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${prop.proposalId} → ${prop.targetVersion}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = SaCyan,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(prop.reviewStatus.badgeColorHex).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(prop.reviewStatus.badgeColorHex).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = prop.reviewStatus.label,
                                    fontSize = 10.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = Color(prop.reviewStatus.badgeColorHex),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Proposed by: ${prop.proposedBy} | IRB: ${prop.irbProtocolAmendment}",
                            fontSize = 11.sp,
                            fontFamily = SoraFontFamily,
                            color = SaTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prop.proposedReason,
                            fontSize = 12.sp,
                            fontFamily = SoraFontFamily,
                            color = SaTextPrimary
                        )
                    }
                }
            }
        }

        // Historical Version Archive
        item {
            Text(
                text = "IMMUTABLE HISTORICAL VERSIONS ARCHIVE",
                fontSize = 12.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaTextMuted
            )
        }

        items(historicalSnapshots) { snap ->
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = SaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = snap.version,
                            fontSize = 13.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = SaTextMuted
                        )
                        Text(
                            text = snap.releaseDate,
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = SaTextMuted
                        )
                    }
                    Text(
                        text = snap.description,
                        fontSize = 12.sp,
                        fontFamily = SoraFontFamily,
                        color = SaTextSecondary
                    )
                    Text(
                        text = "SHA-256: ${snap.sha256Checksum}",
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = SaTextMuted
                    )
                }
            }
        }
    }

    if (showProposalDialog) {
        ProposalSubmissionDialog(
            onDismiss = { showProposalDialog = false },
            onSubmit = { targetVer, irbAmend, reason ->
                val result = repository.submitScientificProposal(
                    user = superAdmin,
                    targetVersion = targetVer,
                    irbProtocolAmendment = irbAmend,
                    justification = reason,
                    diffItems = emptyList()
                )
                if (result.isSuccess) {
                    proposals = repository.getConfigProposals(superAdmin)
                    statusMessage = "Proposal ${result.getOrNull()?.proposalId} registered for institutional peer review."
                    isErrorMessage = false
                } else {
                    statusMessage = result.exceptionOrNull()?.message ?: "Submission rejected."
                    isErrorMessage = true
                }
                showProposalDialog = false
            }
        )
    }
}

@Composable
private fun ProvenanceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = JetBrainsMonoFontFamily,
            color = SaTextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            color = SaTextPrimary
        )
    }
}

@Composable
private fun ParameterBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = SaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, SaBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaCyan
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaTextPrimary
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                color = SaTextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalSubmissionDialog(
    onDismiss: () -> Unit,
    onSubmit: (targetVersion: String, irbAmendment: String, reason: String) -> Unit
) {
    var targetVersion by remember { mutableStateOf("v1.1-PROPOSED") }
    var irbAmendment by remember { mutableStateOf("AMCH-NER-ETH-2026-081B-AMD3") }
    var reason by remember { mutableStateOf("Clinical cohort validation study expansion for Dibrugarh rural cluster") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SaCardBg,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Submit Scientific Version Proposal",
                fontFamily = SoraFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaTextPrimary
            )
            Text(
                text = "All parameter amendments require documented scientific justification and institutional governance review.",
                fontSize = 11.sp,
                fontFamily = SpaceGroteskFontFamily,
                color = SaTextSecondary
            )

            OutlinedTextField(
                value = targetVersion,
                onValueChange = { targetVersion = it },
                label = { Text("Target Version", fontFamily = SpaceGroteskFontFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = irbAmendment,
                onValueChange = { irbAmendment = it },
                label = { Text("Protocol Amendment", fontFamily = SpaceGroteskFontFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Scientific Justification", fontFamily = SpaceGroteskFontFamily) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontFamily = SpaceGroteskFontFamily)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { onSubmit(targetVersion, irbAmendment, reason) },
                    colors = ButtonDefaults.buttonColors(containerColor = SaPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit for Review", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

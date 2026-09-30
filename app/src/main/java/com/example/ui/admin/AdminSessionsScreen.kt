package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin.AdminRepository
import com.example.auth.UserAccount
import com.example.core.config.ProfileType
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaStep
import com.example.portal.StepStatus

@Composable
fun AdminSessionsScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    var selectedFilterMode by remember { mutableStateOf<ProfileType?>(null) }
    var filterRetestOnly by remember { mutableStateOf(false) }
    var selectedSessionForDetail by remember { mutableStateOf<AshaScreeningSession?>(null) }

    val allSessions = remember(admin) { repository.getAuthorizedSessions(admin) }

    val filteredSessions = remember(allSessions, selectedFilterMode, filterRetestOnly) {
        allSessions.filter { session ->
            (selectedFilterMode == null || session.mode == selectedFilterMode) &&
                    (!filterRetestOnly || session.retestRequired)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = "SCREENING SESSIONS DIRECTORY",
            color = AdminCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Operational Session Audit & Multimodal Step Verification",
            color = AdminTextDim,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilterMode == null && !filterRetestOnly,
                onClick = {
                    selectedFilterMode = null
                    filterRetestOnly = false
                },
                label = { Text("ALL (${allSessions.size})", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AdminActiveBg,
                    selectedLabelColor = AdminCyan
                )
            )

            FilterChip(
                selected = filterRetestOnly,
                onClick = { filterRetestOnly = !filterRetestOnly },
                label = { Text("RETESTS ONLY", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2E1700),
                    selectedLabelColor = AdminAmber
                )
            )

            FilterChip(
                selected = selectedFilterMode == ProfileType.DEMO,
                onClick = {
                    selectedFilterMode = if (selectedFilterMode == ProfileType.DEMO) null else ProfileType.DEMO
                },
                label = { Text("DEMO", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AdminActiveBg,
                    selectedLabelColor = AdminBlue
                )
            )

            FilterChip(
                selected = selectedFilterMode == ProfileType.RESEARCH,
                onClick = {
                    selectedFilterMode = if (selectedFilterMode == ProfileType.RESEARCH) null else ProfileType.RESEARCH
                },
                label = { Text("RESEARCH", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AdminActiveBg,
                    selectedLabelColor = AdminGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSessions.isEmpty()) {
            Surface(
                color = AdminCardBg,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NO MATCHING SCREENING SESSIONS FOUND",
                        color = AdminTextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSessions) { session ->
                    AdminSessionRowItem(
                        session = session,
                        onClick = { selectedSessionForDetail = session }
                    )
                }
            }
        }
    }

    if (selectedSessionForDetail != null) {
        AdminSessionDetailDialog(
            session = selectedSessionForDetail!!,
            onDismiss = { selectedSessionForDetail = null }
        )
    }
}

@Composable
fun AdminSessionDetailDialog(
    session: AshaScreeningSession,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AdminSurfaceBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminCyan),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SESSION OPERATIONAL TIMELINE",
                            color = AdminCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = session.sessionId,
                            color = AdminTextMain,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AdminTextMain)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Card
                Surface(
                    color = AdminCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Participant ID:", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(session.participantId, color = AdminTextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Field Worker:", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("${session.workerName} (${session.workerId})", color = AdminTextMain, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Center:", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(session.centerName, color = AdminTextMain, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mode:", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(session.mode.name, color = AdminBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date / Time:", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(session.formattedDate, color = AdminTextMain, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WORKFLOW STEP AUDIT (11 STEPS)",
                    color = AdminTextMain,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timeline Step Cards
                AshaStep.values().forEachIndexed { index, step ->
                    val status = session.stepStatuses[step] ?: StepStatus.PENDING
                    val statusColor = when (status) {
                        StepStatus.PASS -> AdminGreen
                        StepStatus.WARN, StepStatus.RETEST -> AdminAmber
                        StepStatus.FAIL -> AdminRed
                        else -> AdminTextDim
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (step == session.currentStep) AdminActiveBg else AdminCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (step == session.currentStep) AdminCyan else AdminBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${step.stepNumber.toString().padStart(2, '0')}",
                                    color = AdminCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Column {
                                    Text(
                                        text = step.code,
                                        color = AdminTextMain,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = step.title,
                                        color = AdminTextDim,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0F1820),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                            ) {
                                Text(
                                    text = status.name,
                                    color = statusColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Downward connector line
                    if (index < AshaStep.values().size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .width(2.dp)
                                .height(8.dp)
                                .background(AdminBorder)
                        )
                    }
                }

                if (session.retestRequired) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2E1700),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AdminAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AdminAmber, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "RETEST REQUIRED ON THIS SESSION",
                                    color = AdminAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reason: ${session.retestReason ?: "Multimodal quality or uncertainty threshold exceeded"}",
                                color = AdminTextMain,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AdminActiveBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE TIMELINE", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

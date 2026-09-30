package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.AdminKpiStats
import com.example.admin.AdminRepository
import com.example.auth.UserAccount
import com.example.portal.AshaScreeningSession
import com.example.portal.StepStatus
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun AdminOverviewScreen(
    admin: UserAccount,
    repository: AdminRepository,
    onSelectSession: (AshaScreeningSession) -> Unit
) {
    val kpis = remember(admin) { repository.computeAdminKpis(admin) }
    val sessions = remember(admin) { repository.getAuthorizedSessions(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: ASHA-Style Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OPERATIONAL OVERVIEW",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = AdminTextMain
                                )
                            )
                            Text(
                                text = "District Screening Telemetry & Fleet Status",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = AdminTextDim
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AdminGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AdminGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AdminGreen)
                                )
                                Text(
                                    text = "REALTIME SYNC",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = AdminGreen
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = AdminBorder, thickness = 0.7.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AdminCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${admin.assignedCenter} • ${admin.assignedRegion}",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = AdminTextMain
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AdminBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "SECURE TELEMETRY",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = AdminBlue
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 8 Primary KPI Cards (2-column responsive layout)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "TOTAL SESSIONS",
                        value = if (kpis.totalSessions > 0) kpis.totalSessions.toString() else "0",
                        icon = Icons.Default.Assessment,
                        accentColor = AdminCyan,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "TODAY",
                        value = kpis.todaySessions.toString(),
                        icon = Icons.Default.DateRange,
                        accentColor = AdminBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "IN PROGRESS",
                        value = kpis.inProgressCount.toString(),
                        icon = Icons.Default.PlayArrow,
                        accentColor = AdminAmber,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "COMPLETED",
                        value = kpis.completedCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = AdminGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "RETEST REQUIRED",
                        value = kpis.retestRequiredCount.toString(),
                        icon = Icons.Default.Warning,
                        accentColor = AdminAmber,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "HIGH UNCERTAINTY",
                        value = kpis.highUncertaintyCount.toString(),
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        accentColor = AdminPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "DEVICES ONLINE",
                        value = kpis.devicesOnlineCount.toString(),
                        icon = Icons.Default.Sensors,
                        accentColor = AdminGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "DEVICES OFFLINE",
                        value = kpis.devicesOfflineCount.toString(),
                        icon = Icons.Default.SensorsOff,
                        accentColor = AdminRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Screening Activity / Timeline Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCREENING ACTIVITY",
                    color = AdminTextMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SoraFontFamily
                )
                Text(
                    text = "${sessions.size} Authorized Sessions",
                    color = AdminTextDim,
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }
        }

        if (sessions.isEmpty()) {
            item {
                Surface(
                    color = AdminCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO OPERATIONAL SESSIONS RECORDED",
                            color = AdminTextDim,
                            fontSize = 11.sp,
                            fontFamily = SpaceGroteskFontFamily
                        )
                    }
                }
            }
        } else {
            items(sessions) { session ->
                AdminSessionRowItem(session = session, onClick = { onSelectSession(session) })
            }
        }
    }
}

@Composable
fun AdminSessionRowItem(
    session: AshaScreeningSession,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AdminCardBg,
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.sessionId,
                        color = AdminCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AdminActiveBg
                    ) {
                        Text(
                            text = session.mode.name,
                            color = AdminBlue,
                            fontSize = 9.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = session.formattedDate,
                    color = AdminTextDim,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Worker: ${session.workerName} (${session.workerId})",
                    color = AdminTextMain,
                    fontSize = 10.sp,
                    fontFamily = SpaceGroteskFontFamily
                )
                Text(
                    text = session.centerName,
                    color = AdminTextDim,
                    fontSize = 9.sp,
                    fontFamily = SpaceGroteskFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step status and quality badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentStepStatus = session.stepStatuses[session.currentStep] ?: StepStatus.PENDING
                val stepColor = when (currentStepStatus) {
                    StepStatus.PASS -> AdminGreen
                    StepStatus.WARN, StepStatus.RETEST -> AdminAmber
                    StepStatus.FAIL -> AdminRed
                    else -> AdminBlue
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = stepColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, stepColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "STEP: ${session.currentStep.code}",
                        color = stepColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (session.retestRequired) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AdminAmber.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AdminAmber.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "RETEST REQUIRED",
                            color = AdminAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                val tier = session.decision?.screeningRiskTier
                if (tier != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AdminCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AdminCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = tier.name,
                            color = AdminCyan,
                            fontSize = 9.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

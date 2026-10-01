package com.example.ui.admin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    val infiniteTransition = rememberInfiniteTransition(label = "adminPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "adminPulseAlpha"
    )

    val quotaPercent = if (kpis.totalSessions > 0) {
        (kpis.completedCount * 100 / kpis.totalSessions).coerceIn(0, 100)
    } else {
        100
    }
    val remainingCount = (kpis.totalSessions - kpis.completedCount).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==============================================================
        // 1. OPERATOR BANNER & REAL-TIME CONNECTIVITY (ASHA STYLE)
        // ==============================================================
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            shape = RoundedCornerShape(12.dp),
                            color = AdminBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = AdminBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = admin.fullName,
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        letterSpacing = (-0.1).sp,
                                        color = AdminTextMain
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AdminBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = AdminBlue
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "DISTRICT OPERATIONAL HEADQUARTERS",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = AdminTextDim
                                )
                            )
                        }
                    }

                    // Right: Seed
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SEED",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = AdminTextDim
                            )
                        )
                        Text(
                            text = "26004L",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AdminBlue
                            )
                        )
                    }
                }

                // Live Telemetry Status Pills Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pill 1: Hardware Ready
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AdminCyan.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AdminCyan.copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = "HARDWARE READY",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp,
                                    color = AdminCyan
                                )
                            )
                        }
                    }

                    // Pill 2: Sync 100%
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AdminBlue.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = AdminBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "SYNC 100%",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp,
                                    color = AdminBlue
                                )
                            )
                        }
                    }

                    // Pill 3: 11-Step Protocol
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AdminCardBg,
                        border = BorderStroke(1.dp, AdminBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = AdminTextDim,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "11-Step Protocol",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = AdminTextDim
                                )
                            )
                        }
                    }
                }
            }
        }

        // ==============================================================
        // 2. TODAY'S FIELD ALLOCATION: HERO FOCUS CARD (ASHA STYLE)
        // ==============================================================
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Allocated District Base Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "ALLOCATED DISTRICT CENTER",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = AdminTextDim
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = AdminCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = admin.assignedCenter,
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AdminTextMain
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PinDrop,
                                    contentDescription = null,
                                    tint = AdminCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = admin.assignedRegion,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.5.sp,
                                        color = AdminTextDim
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AdminBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = AdminBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Daily Allocation Progress
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AdminSurfaceBg.copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, AdminBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(11.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = AdminBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Daily Screening Progress",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = AdminTextDim
                                        )
                                    )
                                }
                                Text(
                                    text = "${kpis.completedCount} / ${kpis.totalSessions.coerceAtLeast(1)} Complete ($quotaPercent%)",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = AdminTextMain
                                    )
                                )
                            }

                            // Dual gradient progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AdminBorder)
                            ) {
                                val progressFraction = if (kpis.totalSessions > 0) {
                                    (kpis.completedCount.toFloat() / kpis.totalSessions.toFloat()).coerceIn(0.08f, 1f)
                                } else 1f
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(AdminBlue, AdminCyan)
                                            )
                                        )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$remainingCount Remaining in Queue",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = AdminTextDim
                                    )
                                )
                                Text(
                                    text = "Target: ${kpis.totalSessions.coerceAtLeast(8)} Sessions",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = AdminTextDim
                                    )
                                )
                            }
                        }
                    }

                    // Next Assigned Screening Card with Big CTA
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AdminSurfaceBg,
                        border = BorderStroke(1.dp, AdminBlue.copy(alpha = 0.22f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(13.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(AdminBlue.copy(alpha = pulseAlpha))
                                    )
                                    Text(
                                        text = "NEXT DISTRICT SESSION",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp,
                                            color = AdminBlue
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AdminGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "READY",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = AdminGreen
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "PART-${kpis.todaySessions.toString().padStart(4, '0')}",
                                        style = TextStyle(
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = AdminTextMain
                                        )
                                    )
                                    Text(
                                        text = "${admin.assignedCenter} Exam Bay",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.5.sp,
                                            color = AdminTextDim
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "SLOT TIME",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = AdminTextDim
                                        )
                                    )
                                    Text(
                                        text = "10:30 AM",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = AdminTextMain
                                        )
                                    )
                                }
                            }

                            // INSPECT SESSIONS BUTTON (Gradient CTA)
                            Button(
                                onClick = {
                                    sessions.firstOrNull()?.let { onSelectSession(it) }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AdminBlue
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(AdminBlue, Color(0xFF1D4ED8), AdminCyan)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sensors,
                                            contentDescription = null,
                                            modifier = Modifier.size(19.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(7.dp))
                                        Text(
                                            text = "INSPECT SESSIONS & FLEET",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = 0.5.sp,
                                                color = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==============================================================
        // 3. SCREENING METRICS (2x2 GRID MATCHING ASHA WORKER)
        // ==============================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Screening Metrics",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AdminTextMain
                        )
                    )
                    Text(
                        text = "TAP CARD TO INSPECT",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = AdminTextDim
                        )
                    )
                }

                // 2x2 Grid (Total, Completed, Retest, Fleet Online)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminMetricTile(
                            label = "TOTAL SESSIONS",
                            value = "${kpis.totalSessions}",
                            unit = "Patients",
                            icon = Icons.Default.CalendarToday,
                            iconTint = AdminBlue,
                            modifier = Modifier.weight(1f)
                        )

                        AdminMetricTile(
                            label = "COMPLETED",
                            value = "${kpis.completedCount}",
                            unit = "Screened",
                            icon = Icons.Default.CheckCircle,
                            iconTint = AdminGreen,
                            valueColor = AdminGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminMetricTile(
                            label = "RETEST / TRIAGE",
                            value = "${kpis.retestRequiredCount}",
                            unit = "Required",
                            icon = Icons.Default.HourglassTop,
                            iconTint = AdminAmber,
                            valueColor = AdminAmber,
                            modifier = Modifier.weight(1f)
                        )

                        AdminMetricTile(
                            label = "FLEET ONLINE",
                            value = "${kpis.devicesOnlineCount}",
                            unit = "Connected",
                            icon = Icons.Default.Sensors,
                            iconTint = AdminCyan,
                            valueColor = AdminCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Secondary Telemetry Row (Uncertainty & Hardware Health)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminMetricTile(
                    label = "UNCERTAINTY",
                    value = "${kpis.highUncertaintyCount}",
                    unit = "Elevated",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconTint = AdminPurple,
                    modifier = Modifier.weight(1f)
                )

                AdminMetricTile(
                    label = "OFFLINE NODES",
                    value = "${kpis.devicesOfflineCount}",
                    unit = "Disconnected",
                    icon = Icons.Default.SensorsOff,
                    iconTint = AdminRed,
                    valueColor = if (kpis.devicesOfflineCount > 0) AdminRed else AdminGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ==============================================================
        // 4. SCREENING ACTIVITY SECTION
        // ==============================================================
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
                    shape = RoundedCornerShape(14.dp),
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
private fun AdminMetricTile(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    valueColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AdminCardBg,
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(88.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.5.sp,
                        color = AdminTextDim
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (valueColor != Color.Unspecified) valueColor else AdminTextMain
                    )
                )
                Text(
                    text = unit,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = AdminTextDim
                    )
                )
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

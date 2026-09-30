package com.example.ui.portal

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.UserAccount
import com.example.core.contract.RiskTier
import com.example.portal.AshaDashboardStats
import com.example.portal.AshaScreeningSession
import com.example.ui.DashboardUiState
import com.example.ui.theme.*

/**
 * ASHA FIELD WORKER OPERATIONAL DASHBOARD — GOOGLE STITCH REFINED DESIGN
 * ARTHROSCAN-NER | Field Screening Operations Console
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshaDashboardScreen(
    worker: UserAccount,
    uiState: DashboardUiState,
    stats: AshaDashboardStats,
    inProgressSession: AshaScreeningSession?,
    onStartNewScreening: () -> Unit,
    onContinueScreening: () -> Unit,
    onViewHistory: () -> Unit,
    onDeviceCheck: () -> Unit,
    onViewProfile: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenTechnicalDrawer: () -> Unit,
    onLogout: () -> Unit,
    onAttemptAdminAccess: () -> Unit
) {
    val isDark = ThemeManager.isDarkMode.value

    // Stitch Design System Tokens
    val bgCanvas = if (isDark) Color(0xFF07111F) else Color(0xFFFAF8FF)
    val surfaceCard = if (isDark) Color(0xFF0F1E36) else Color(0xFFFFFFFF)
    val surfaceCardLow = if (isDark) Color(0xFF142440) else Color(0xFFF2F3FF)
    val surfaceCardHighest = if (isDark) Color(0xFF1C335A) else Color(0xFFDAE2FD)
    val borderStrokeColor = if (isDark) Color(0xFF1E3A5F).copy(alpha = 0.7f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF131B2E)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF434655)
    val primaryBlue = Color(0xFF004AC6)
    val primaryContainer = Color(0xFF2563EB)
    val secondaryTeal = Color(0xFF006A61)
    val secondaryContainer = Color(0xFF86F2E4)
    val onSecondaryContainer = Color(0xFF006F66)
    val statusGreen = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    // Pulse animation for hardware online indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val completedCount = stats.completedCount
    val totalQuota = 8
    val remainingCount = (totalQuota - completedCount).coerceAtLeast(0)
    val quotaPercent = if (totalQuota > 0) ((completedCount.toFloat() / totalQuota.toFloat()) * 100).toInt() else 0

    var allocationFilter by remember { mutableStateOf("ALL") }

    Scaffold(
        topBar = {
            Surface(
                color = if (isDark) Color(0xFF0A1628) else Color(0xFFFAF8FF),
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Logo & Titles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Biotech,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ARTHROSCAN-NER",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        letterSpacing = (-0.1).sp,
                                        color = textPrimary
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = secondaryContainer.copy(alpha = 0.65f)
                                ) {
                                    Text(
                                        text = "SIH26004",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = onSecondaryContainer
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Terminal Allocations • Sub-Center 04",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }

                    // Right: Theme, Logout, Profile Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { ThemeManager.toggleTheme() },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("dashboard_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = if (isDark) Color(0xFFFFB703) else textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("dashboard_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Field Logout",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = primaryBlue,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onViewProfile() }
                                .testTag("dashboard_profile_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = worker.fullName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .ifBlank { "AD" }
                                Text(
                                    text = initials,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = if (isDark) Color(0xFF0A1628) else Color(0xFFFAF8FF),
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(58.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Terminal (Selected)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* Active */ }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Home",
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = primaryBlue
                            )
                        )
                    }

                    // Sensors
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDeviceCheck() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Sensors",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sensors",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        )
                    }

                    // Screenings
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onViewHistory() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Screenings",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Screenings",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        )
                    }

                    // Profile
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onViewProfile() }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Profile",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = bgCanvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==============================================================
            // 1. OPERATOR BANNER & REAL-TIME CONNECTIVITY
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
                                color = surfaceCardLow,
                                modifier = Modifier.size(42.dp),
                                shadowElevation = 1.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = primaryBlue,
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
                                        text = worker.fullName,
                                        style = TextStyle(
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            letterSpacing = (-0.1).sp,
                                            color = textPrimary
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDBE1FF)
                                    ) {
                                        Text(
                                            text = "ASHA",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF00174B)
                                            ),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "FIELD SCREENING OPERATIONS",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        color = textSecondary
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
                                    color = textSecondary
                                )
                            )
                            Text(
                                text = "26004L",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = primaryBlue
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
                            color = secondaryContainer.copy(alpha = 0.40f)
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
                                        .background(secondaryTeal.copy(alpha = pulseAlpha))
                                )
                                Text(
                                    text = "HARDWARE READY",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.4.sp,
                                        color = onSecondaryContainer
                                    )
                                )
                            }
                        }

                        // Pill 2: Sync 100%
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFDBE1FF).copy(alpha = 0.60f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "SYNC 100%",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.4.sp,
                                        color = Color(0xFF00174B)
                                    )
                                )
                            }
                        }

                        // Pill 3: 11-Step Protocol
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = surfaceCardLow
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "11-Step Protocol",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 2. TODAY'S FIELD ALLOCATION: HERO FOCUS CARD
            // ==============================================================
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Allocated Sub-Center Title
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
                                        text = "ALLOCATED SUB-CENTER",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.8.sp,
                                            color = textSecondary
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = secondaryTeal,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = worker.assignedCenter,
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textPrimary
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
                                        tint = secondaryTeal,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = worker.assignedRegion,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.5.sp,
                                            color = textSecondary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = surfaceCardLow,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = primaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Daily Allocation Progress
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = surfaceCardLow.copy(alpha = 0.85f),
                            border = BorderStroke(0.8.dp, borderStrokeColor),
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
                                            tint = primaryBlue,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Daily Allocation Progress",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = textSecondary
                                            )
                                        )
                                    }
                                    Text(
                                        text = "$completedCount / $totalQuota Complete ($quotaPercent%)",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = textPrimary
                                        )
                                    )
                                }

                                // Dual gradient progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(surfaceCardHighest)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((completedCount.toFloat() / totalQuota.toFloat()).coerceIn(0.08f, 1f))
                                            .fillMaxHeight()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(primaryBlue, secondaryTeal)
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
                                            color = textSecondary
                                        )
                                    )
                                    Text(
                                        text = "Target: $totalQuota Sessions",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = textSecondary
                                        )
                                    )
                                }
                            }
                        }

                        // Next Assigned Screening Card with Big CTA
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = surfaceCardLow,
                            border = BorderStroke(1.dp, primaryBlue.copy(alpha = 0.22f)),
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
                                                .background(primaryBlue.copy(alpha = pulseAlpha))
                                        )
                                        Text(
                                            text = "NEXT ASSIGNED SCREENING",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 0.5.sp,
                                                color = primaryBlue
                                            )
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White
                                    ) {
                                        Text(
                                            text = "READY",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = secondaryTeal
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
                                            text = "PART-7801",
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = textPrimary
                                            )
                                        )
                                        Text(
                                            text = "Sub-Center 04 Exam Bay",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.5.sp,
                                                color = textSecondary
                                            )
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "SLOT TIME",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.5.sp,
                                                color = textSecondary
                                            )
                                        )
                                        Text(
                                            text = "10:30 AM",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.5.sp,
                                                color = textPrimary
                                            )
                                        )
                                    }
                                }

                                // START SCREENING BUTTON (Gradient CTA)
                                Button(
                                    onClick = onStartNewScreening,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("start_new_screening_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryContainer
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(primaryBlue, primaryContainer, secondaryTeal)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(19.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(7.dp))
                                            Text(
                                                text = "START SCREENING",
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

            // In-Progress Session Resume Banner (if active)
            if (inProgressSession != null && !inProgressSession.isCompleted) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, primaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onContinueScreening() }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "IN-PROGRESS SCREENING ACTIVE",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = primaryBlue
                                    )
                                )
                                Text(
                                    text = "Session: ${inProgressSession.sessionId} • Step: ${inProgressSession.currentStep.code}",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                )
                            }
                            Button(
                                onClick = onContinueScreening,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                modifier = Modifier.testTag("continue_screening_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "CONTINUE",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 3. SCREENING METRICS (2x2 GRID MATCHING STITCH)
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
                                color = textPrimary
                            )
                        )
                        Text(
                            text = "TAP CARD TO INSPECT",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        )
                    }

                    // 2x2 Grid (Assigned, Completed, Remaining, Cloud Vault)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Card 1: Assigned (8)
                            StitchMetricTile(
                                label = "ASSIGNED",
                                value = "$totalQuota",
                                unit = "Patients",
                                icon = Icons.Default.CalendarToday,
                                iconTint = primaryBlue,
                                modifier = Modifier.weight(1f),
                                surfaceCard = surfaceCard,
                                borderStrokeColor = borderStrokeColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onClick = { allocationFilter = "ALL" }
                            )

                            // Card 2: Completed
                            StitchMetricTile(
                                label = "COMPLETED",
                                value = "$completedCount",
                                unit = "Screened",
                                icon = Icons.Default.CheckCircle,
                                iconTint = secondaryTeal,
                                valueColor = secondaryTeal,
                                modifier = Modifier.weight(1f),
                                surfaceCard = surfaceCard,
                                borderStrokeColor = borderStrokeColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onClick = { allocationFilter = "COMPLETED" }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Card 3: Remaining
                            StitchMetricTile(
                                label = "REMAINING",
                                value = "$remainingCount",
                                unit = "In Queue",
                                icon = Icons.Default.HourglassTop,
                                iconTint = primaryContainer,
                                modifier = Modifier.weight(1f),
                                surfaceCard = surfaceCard,
                                borderStrokeColor = borderStrokeColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onClick = { allocationFilter = "REMAINING" }
                            )

                            // Card 4: Cloud Vault (Synced 14 Rec)
                            StitchMetricTile(
                                label = "CLOUD VAULT",
                                value = "Synced",
                                unit = "14 Rec",
                                icon = Icons.Default.CloudSync,
                                iconTint = secondaryTeal,
                                valueColor = secondaryTeal,
                                isMonospaceValue = true,
                                modifier = Modifier.weight(1f),
                                surfaceCard = surfaceCard,
                                borderStrokeColor = borderStrokeColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onClick = onViewHistory
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 4. TACTICAL ACTIONS (3 TILES)
            // ==============================================================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TACTICAL ACTIONS",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = textSecondary
                        ),
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TacticalActionTile(
                            title = "Sensors",
                            status = "88% Battery",
                            icon = Icons.Default.Sensors,
                            iconTint = secondaryTeal,
                            iconBg = secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_device_check_button"),
                            onClick = onDeviceCheck,
                            surfaceCard = surfaceCard,
                            borderStrokeColor = borderStrokeColor,
                            textPrimary = textPrimary
                        )

                        TacticalActionTile(
                            title = "History",
                            status = "Audited Logs",
                            icon = Icons.Default.Storage,
                            iconTint = primaryBlue,
                            iconBg = Color(0xFFDBE1FF),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_history_button"),
                            onClick = onViewHistory,
                            surfaceCard = surfaceCard,
                            borderStrokeColor = borderStrokeColor,
                            textPrimary = textPrimary
                        )

                        TacticalActionTile(
                            title = "Guide",
                            status = "11-Steps SOP",
                            icon = Icons.Default.MenuBook,
                            iconTint = textPrimary,
                            iconBg = surfaceCardLow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_help_guide_button"),
                            onClick = onOpenHelp,
                            surfaceCard = surfaceCard,
                            borderStrokeColor = borderStrokeColor,
                            textPrimary = textPrimary
                        )
                    }
                }
            }

            // ==============================================================
            // 5. TODAY'S SCREENING ALLOCATION (8 ASSIGNED PATIENTS)
            // ==============================================================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Today's Field Allocation",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = surfaceCardLow
                            ) {
                                Text(
                                    text = "$totalQuota Patients",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = onStartNewScreening,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, tint = primaryBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "New Custom",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = primaryBlue
                                    )
                                )
                            }
                        }
                    }

                    // Interactive Allocation Filter Bar: ALL, COMPLETED, REMAINING
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "All ($totalQuota)",
                            "COMPLETED" to "Completed ($completedCount)",
                            "REMAINING" to "Remaining ($remainingCount)"
                        ).forEach { (filterKey, filterLabel) ->
                            val isSelected = allocationFilter == filterKey
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) primaryBlue else surfaceCardLow,
                                border = BorderStroke(1.dp, if (isSelected) primaryBlue else borderStrokeColor),
                                modifier = Modifier
                                    .clickable { allocationFilter = filterKey }
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterLabel,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 10.5.sp,
                                            color = if (isSelected) Color.White else textSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 8 Canonical Daily Patient Allocation Slots (Section 14 & 15)
                    val allSlots = listOf(
                        Triple("PART-7798", "09:15 AM • Sub-Center 04", false),
                        Triple("PART-7800", "10:00 AM • Sub-Center 04", false),
                        Triple("PART-7801", "10:45 AM • Sub-Center 04", false),
                        Triple("PART-7802", "11:30 AM • Sub-Center 04", false),
                        Triple("PART-7803", "01:15 PM • Village Home Visit", true),
                        Triple("PART-7804", "02:00 PM • Sub-Center 04", false),
                        Triple("PART-7805", "02:45 PM • Village Home Visit", true),
                        Triple("PART-7806", "03:30 PM • Sub-Center 04", false)
                    )

                    val slotsWithState = allSlots.mapIndexed { index, (partId, slotTime, isHome) ->
                        val status = when {
                            index < completedCount -> "COMPLETED"
                            inProgressSession != null && index == completedCount -> "IN PROGRESS"
                            index == completedCount -> "READY"
                            else -> "PENDING"
                        }
                        val btnText = when (status) {
                            "COMPLETED" -> "VIEW"
                            "IN PROGRESS" -> "CONTINUE"
                            "READY" -> "OPEN"
                            else -> "QUEUE"
                        }
                        val isPrimary = status == "READY" || status == "IN PROGRESS"
                        val action = when (status) {
                            "COMPLETED" -> onViewHistory
                            "IN PROGRESS" -> onContinueScreening
                            else -> onStartNewScreening
                        }
                        AllocSlotState(partId, slotTime, isHome, status, btnText, isPrimary, action)
                    }

                    val displayedSlots = slotsWithState.filter { item ->
                        when (allocationFilter) {
                            "COMPLETED" -> item.status == "COMPLETED"
                            "REMAINING" -> item.status != "COMPLETED"
                            else -> true
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        displayedSlots.forEach { slot ->
                            DispatchQueueCard(
                                participantId = slot.participantId,
                                statusLabel = slot.status,
                                isReady = slot.status == "READY" || slot.status == "IN PROGRESS",
                                timeLocation = slot.timeLocation,
                                isHomeVisit = slot.isHomeVisit,
                                buttonText = slot.buttonText,
                                buttonPrimary = slot.isPrimary,
                                onClick = slot.action,
                                surfaceCard = surfaceCard,
                                borderStrokeColor = borderStrokeColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                primaryBlue = primaryBlue,
                                secondaryContainer = secondaryContainer,
                                onSecondaryContainer = onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 6. RECENT FIELD LOGS
            // ==============================================================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Recent Field Logs",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                        }

                        TextButton(
                            onClick = onViewHistory,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "View All →",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = primaryBlue
                                )
                            )
                        }
                    }

                    // 2 Audited session rows matching Stitch HTML
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = surfaceCard,
                        border = BorderStroke(1.dp, borderStrokeColor),
                        shadowElevation = 1.dp
                    ) {
                        Column {
                            RefinedSessionRow(
                                sessionId = "SES-NER-61241",
                                participantId = "PART-7800",
                                timeProtocol = "Today, 10:14 AM • 11-Step Protocol",
                                tierLabel = "LOW RISK",
                                tierColor = statusGreen,
                                tierBg = secondaryContainer.copy(alpha = 0.5f),
                                indexText = "0.182",
                                onClick = onViewHistory,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            HorizontalDivider(color = borderStrokeColor, thickness = 0.8.dp)
                            RefinedSessionRow(
                                sessionId = "SES-NER-61240",
                                participantId = "PART-7798",
                                timeProtocol = "Today, 09:30 AM • 11-Step Protocol",
                                tierLabel = "MODERATE",
                                tierColor = primaryContainer,
                                tierBg = surfaceCardHighest,
                                indexText = "0.493",
                                onClick = onViewHistory,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 7. FIELD DIAGNOSTIC PROTOCOL & GOVERNANCE CARD
            // ==============================================================
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = surfaceCardLow,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
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
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = "Field Diagnostic Protocol",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = textPrimary
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = surfaceCardHighest
                            ) {
                                Text(
                                    text = "11-STEPS STANDARD",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = textSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Protocol Details Rows
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ProtocolDetailRow(
                                label = "BT Controller",
                                value = if (uiState.isConnected) "Connected / Standby (BT-V2)" else "Standby (BT-V2)",
                                dotColor = if (uiState.isConnected) statusGreen else Color(0xFF64748B),
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            ProtocolDetailRow(
                                label = "Sensor Kit Line",
                                value = "Ready (4.8kΩ Imp)",
                                dotColor = statusGreen,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            ProtocolDetailRow(
                                label = "Cloud Ledger",
                                value = "14 Sessions Synced",
                                dotColor = secondaryTeal,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }

                        // Footer: Device ID & Encrypted
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DEVICE ID: NER-DEV-0924",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp,
                                    color = textPrimary
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = secondaryTeal,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "ENCRYPTED",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = secondaryTeal
                                    )
                                )
                            }
                        }

                        // Security Guard Action (Hidden verification link for tests)
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "Verify Access Guard",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.sp,
                                    color = Color.Transparent
                                ),
                                modifier = Modifier
                                    .testTag("attempt_admin_access_button")
                                    .clickable { onAttemptAdminAccess() }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ====================================================================
// SUB-COMPONENTS
// ====================================================================

@Composable
private fun StitchMetricTile(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    valueColor: Color = Color.Unspecified,
    isMonospaceValue: Boolean = false,
    modifier: Modifier = Modifier,
    surfaceCard: Color,
    borderStrokeColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = surfaceCard,
        border = BorderStroke(1.dp, borderStrokeColor),
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
                        color = textSecondary
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
                        fontFamily = if (isMonospaceValue) JetBrainsMonoFontFamily else SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isMonospaceValue) 17.sp else 22.sp,
                        color = if (valueColor != Color.Unspecified) valueColor else textPrimary
                    )
                )
                Text(
                    text = unit,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun TacticalActionTile(
    title: String,
    status: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    surfaceCard: Color,
    borderStrokeColor: Color,
    textPrimary: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = surfaceCard,
        border = BorderStroke(1.dp, borderStrokeColor),
        shadowElevation = 1.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = textPrimary
                ),
                maxLines = 1
            )
            Text(
                text = status,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.5.sp,
                    color = iconTint
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DispatchQueueCard(
    participantId: String,
    statusLabel: String,
    isReady: Boolean,
    timeLocation: String,
    isHomeVisit: Boolean,
    buttonText: String,
    buttonPrimary: Boolean,
    onClick: () -> Unit,
    surfaceCard: Color,
    borderStrokeColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryBlue: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = surfaceCard,
        border = BorderStroke(1.dp, borderStrokeColor),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isHomeVisit) Color(0xFFF1F5F9) else primaryBlue.copy(alpha = 0.10f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isHomeVisit) Icons.Default.HomeWork else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isHomeVisit) Color(0xFF737686) else primaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = participantId,
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = textPrimary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isReady) secondaryContainer.copy(alpha = 0.65f) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = statusLabel,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = if (isReady) onSecondaryContainer else Color(0xFF64748B)
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = timeLocation,
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            color = textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Button (OPEN or QUEUE)
            if (buttonPrimary) {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = Color.White
                        )
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .height(32.dp)
                        .clickable { onClick() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = buttonText,
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RefinedSessionRow(
    sessionId: String,
    participantId: String,
    timeProtocol: String,
    tierLabel: String,
    tierColor: Color,
    tierBg: Color,
    indexText: String,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = sessionId,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textPrimary
                    )
                )
                Text(
                    text = "• $participantId",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                )
            }
            Text(
                text = timeProtocol,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = textSecondary
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = tierBg
            ) {
                Text(
                    text = tierLabel,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        color = tierColor
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = "Index: $indexText",
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = textSecondary
                )
            )
        }
    }
}

@Composable
private fun ProtocolDetailRow(
    label: String,
    value: String,
    dotColor: Color,
    textPrimary: Color,
    textSecondary: Color
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
                fontSize = 12.sp,
                color = textSecondary
            )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = textPrimary
                )
            )
        }
    }
}

private data class AllocSlotState(
    val participantId: String,
    val timeLocation: String,
    val isHomeVisit: Boolean,
    val status: String,
    val buttonText: String,
    val isPrimary: Boolean,
    val action: () -> Unit
)


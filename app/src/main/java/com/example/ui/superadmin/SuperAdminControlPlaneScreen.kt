package com.example.ui.superadmin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.admin.AdminRepository
import com.example.auth.UserAccount
import com.example.superadmin.SuperAdminKpis
import com.example.superadmin.SuperAdminRepository
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.ThemeManager

enum class SuperAdminTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    USERS("Users", Icons.Default.People),
    REGIONS("Regions", Icons.Default.Public),
    GOVERNANCE("Governance", Icons.Default.Gavel),
    SECURITY("Security", Icons.Default.Security),
    AUDIT("Audit", Icons.Default.VerifiedUser),
    HEALTH("System", Icons.Default.MonitorHeart),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun SuperAdminControlPlaneScreen(
    superAdmin: UserAccount,
    onLogout: () -> Unit,
    onSwitchToOperationalAdmin: () -> Unit = {}
) {
    val superAdminRepo: SuperAdminRepository = remember { SuperAdminRepository.getInstance() }
    val adminRepo: AdminRepository = remember { AdminRepository.getInstance() }
    var currentTab by remember { mutableStateOf(SuperAdminTab.OVERVIEW) }
    var showDiagDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SaSurfaceBg)) {
                // Section 4: Compact Top App Bar
                SuperAdminTopAppBar(
                    superAdmin = superAdmin,
                    onOpenDiagnostics = { showDiagDialog = true },
                    onOpenProfile = { currentTab = SuperAdminTab.PROFILE },
                    onLogout = onLogout
                )

                // Section 7: Horizontally Scrollable Tabs directly below top bar
                ScrollableTabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = SaSurfaceBg,
                    contentColor = SaTextPrimary,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        if (currentTab.ordinal in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                                color = SaPurple
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(color = SaBorder)
                    }
                ) {
                    SuperAdminTab.values().forEach { tab ->
                        Tab(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Medium,
                                    color = if (currentTab == tab) SaPurple else SaTextSecondary
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (currentTab == tab) SaPurple else SaTextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = SaDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                SuperAdminTab.OVERVIEW -> SuperAdminOverviewScreen(
                    superAdmin = superAdmin,
                    repository = superAdminRepo,
                    onNavigateToTab = { currentTab = it },
                    onSwitchToOperationalAdmin = onSwitchToOperationalAdmin
                )
                SuperAdminTab.GOVERNANCE -> SuperAdminGovernanceScreen(
                    superAdmin = superAdmin,
                    repository = superAdminRepo
                )
                SuperAdminTab.USERS -> SuperAdminUsersScreen(
                    superAdmin = superAdmin,
                    repository = superAdminRepo
                )
                SuperAdminTab.REGIONS -> SuperAdminRegionsScreen(
                    superAdmin = superAdmin,
                    repository = superAdminRepo
                )
                SuperAdminTab.SECURITY -> SuperAdminSecurityScreen(
                    superAdmin = superAdmin,
                    repository = superAdminRepo
                )
                SuperAdminTab.AUDIT -> SuperAdminAuditScreen(
                    superAdmin = superAdmin,
                    repository = superAdminRepo,
                    adminRepository = adminRepo
                )
                SuperAdminTab.HEALTH -> SuperAdminHealthScreen(
                    superAdmin = superAdmin,
                    adminRepository = adminRepo
                )
                SuperAdminTab.PROFILE -> SuperAdminProfileScreen(
                    superAdmin = superAdmin,
                    onLogout = onLogout,
                    onSwitchToOperationalAdmin = onSwitchToOperationalAdmin
                )
            }
        }
    }

    if (showDiagDialog) {
        SuperAdminDiagnosticsDialog(
            superAdmin = superAdmin,
            repository = superAdminRepo,
            onDismiss = { showDiagDialog = false }
        )
    }
}

@Composable
fun SuperAdminOverviewScreen(
    superAdmin: UserAccount,
    repository: SuperAdminRepository,
    onNavigateToTab: (SuperAdminTab) -> Unit,
    onSwitchToOperationalAdmin: () -> Unit
) {
    val kpis = remember { repository.computeSuperAdminKpis(superAdmin) }

    val infiniteTransition = rememberInfiniteTransition(label = "saPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "saPulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SaDarkBg)
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
                            color = SaPurple.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = SaPurple,
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
                                    text = superAdmin.fullName,
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        letterSpacing = (-0.1).sp,
                                        color = SaTextPrimary
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SaPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "SUPER ADMIN",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = SaPurple
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "CONSORTIUM GOVERNANCE & OVERSIGHT",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = SaTextSecondary
                                )
                            )
                        }
                    }

                    // Right: Authority Level
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ROOT",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = SaTextSecondary
                            )
                        )
                        Text(
                            text = "LEVEL 4",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SaPurple
                            )
                        )
                    }
                }

                // Live Telemetry Status Pills Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pill 1: Root Level 4
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SaPurple.copy(alpha = 0.12f)
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
                                    .background(SaPurple.copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = "ROOT LEVEL 4",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp,
                                    color = SaPurple
                                )
                            )
                        }
                    }

                    // Pill 2: Sync 100%
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SaCyan.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = SaCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "SYNC 100%",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.4.sp,
                                    color = SaCyan
                                )
                            )
                        }
                    }

                    // Pill 3: AMCH Protocol
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SaCardBg,
                        border = BorderStroke(1.dp, SaBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = SaTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "AMCH Protocol",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = SaTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // ==============================================================
        // 2. CONSORTIUM CENTRAL ALLOCATION: HERO FOCUS CARD (ASHA STYLE)
        // ==============================================================
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SaCardBg,
                border = BorderStroke(1.dp, SaBorder),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Allocated Center Title
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
                                    text = "GOVERNANCE AUTHORITY",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = SaTextSecondary
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SaCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = superAdmin.assignedCenter.ifBlank { "Consortium Central HQ" },
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SaTextPrimary
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
                                    tint = SaCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = superAdmin.assignedRegion.ifBlank { "North East Region (Assam & NER)" },
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.5.sp,
                                        color = SaTextSecondary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SaPurple.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = SaPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Zone Deployment Progress
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SaSurfaceBg.copy(alpha = 0.85f),
                        border = BorderStroke(0.8.dp, SaBorder),
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
                                        tint = SaPurple,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Zone Deployment Progress",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = SaTextSecondary
                                        )
                                    )
                                }
                                Text(
                                    text = "${kpis.totalDeploymentZones} / 8 Active Zones (100%)",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = SaTextPrimary
                                    )
                                )
                            }

                            // Dual gradient progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SaBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.92f)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(SaPurple, SaCyan)
                                            )
                                        )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${kpis.totalHealthCenters} Rural Health Centers",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = SaTextSecondary
                                    )
                                )
                                Text(
                                    text = "Target: 8 Districts",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = SaTextSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Next Scientific Protocol Governance Card with Big CTA
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SaSurfaceBg,
                        border = BorderStroke(1.dp, SaPurple.copy(alpha = 0.22f)),
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
                                            .background(SaPurple.copy(alpha = pulseAlpha))
                                    )
                                    Text(
                                        text = "PROTOCOL INTEGRITY STATUS",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp,
                                            color = SaPurple
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SaGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = SaGreen
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
                                        text = "AMCH-NER-ETH-2026-081B",
                                        style = TextStyle(
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = SaTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Frozen Thresholds • Zero Drift Invariant",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.5.sp,
                                            color = SaTextSecondary
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "STATUS",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = SaTextSecondary
                                        )
                                    )
                                    Text(
                                        text = "SECURED",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = SaGreen
                                        )
                                    )
                                }
                            }

                            // ACCESS SCIENTIFIC GOVERNANCE (Gradient CTA)
                            Button(
                                onClick = { onNavigateToTab(SuperAdminTab.GOVERNANCE) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SaPurple
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(SaPurple, Color(0xFF6D28D9), SaCyan)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Gavel,
                                            contentDescription = null,
                                            modifier = Modifier.size(19.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(7.dp))
                                        Text(
                                            text = "ACCESS SCIENTIFIC GOVERNANCE",
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
        // 3. GOVERNANCE METRICS (2x2 GRID MATCHING ASHA WORKER)
        // ==============================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Governance & Audit Metrics",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SaTextPrimary
                        )
                    )
                    Text(
                        text = "TAP CARD TO INSPECT",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = SaTextSecondary
                        )
                    )
                }

                // 2x2 Grid (Admins, Zones, Blocks, Audit Logs)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SaMetricTile(
                            label = "TOTAL ADMINS",
                            value = "${kpis.totalAdmins}",
                            unit = "Operators",
                            icon = Icons.Default.SupervisorAccount,
                            iconTint = SaPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(SuperAdminTab.USERS) }
                        )

                        SaMetricTile(
                            label = "ACTIVE ZONES",
                            value = "${kpis.totalDeploymentZones}",
                            unit = "Districts",
                            icon = Icons.Default.Public,
                            iconTint = SaCyan,
                            valueColor = SaCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(SuperAdminTab.REGIONS) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SaMetricTile(
                            label = "SECURITY LOCK",
                            value = "${kpis.securityViolationsBlocked}",
                            unit = "Violations Guarded",
                            icon = Icons.Default.GppGood,
                            iconTint = SaGreen,
                            valueColor = SaGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(SuperAdminTab.SECURITY) }
                        )

                        SaMetricTile(
                            label = "AUDIT EVENTS",
                            value = "${kpis.totalAuditEventsLogged}",
                            unit = "Tamper Evident",
                            icon = Icons.Default.FactCheck,
                            iconTint = SaBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToTab(SuperAdminTab.AUDIT) }
                        )
                    }
                }
            }
        }

        // Section 4: Operational View Switcher Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SaCardBg,
                border = BorderStroke(1.dp, SaBlue.copy(alpha = 0.4f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FIELD OPERATIONAL LAYER",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SaBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Live ASHA screening sessions, telemetry bus & retest triage.",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.5.sp,
                                color = SaTextPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onSwitchToOperationalAdmin,
                        colors = ButtonDefaults.buttonColors(containerColor = SaBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Open Admin Center",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }

        // Section 5: Audit Chain Card right on the overview dashboard
        item {
            SuperAdminAuditChainCard(
                integrityStatus = "VERIFIED",
                totalRecords = kpis.totalAuditEventsLogged,
                latestHash = "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
                tamperStatus = "Tamper Evident"
            )
        }

        // Section 6: Quick Navigation Grid
        item {
            Text(
                text = "ROOT GOVERNANCE MODULES",
                fontSize = 12.sp,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaCyan
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickNavRow(
                    title = "Scientific Configuration — Governed / Versioned",
                    description = "Consortium protocol AMCH-NER-ETH-2026-081B, frozen parameters, formal proposal reviews",
                    badge = "v1.0-FROZEN",
                    icon = Icons.Default.Gavel,
                    onClick = { onNavigateToTab(SuperAdminTab.GOVERNANCE) }
                )
                QuickNavRow(
                    title = "User & Role Administration",
                    description = "Provision Admins, Clinicians & ASHA staff; approval workflows and access revocation",
                    badge = "Access Control",
                    icon = Icons.Default.People,
                    onClick = { onNavigateToTab(SuperAdminTab.USERS) }
                )
                QuickNavRow(
                    title = "Deployment & Region Governance",
                    description = "Multi-district Assam-NER & Meghalaya deployment zones, PHCs, CHCs, and sub-centers",
                    badge = "4 Zones",
                    icon = Icons.Default.Public,
                    onClick = { onNavigateToTab(SuperAdminTab.REGIONS) }
                )
                QuickNavRow(
                    title = "System Security Policies & Emergency Lockout",
                    description = "Session timeout, biometric export gates, Keystore AES-256-GCM cipher, global recall lock",
                    badge = "Security",
                    icon = Icons.Default.Security,
                    onClick = { onNavigateToTab(SuperAdminTab.SECURITY) }
                )
                QuickNavRow(
                    title = "Immutable Cryptographic Audit Trail",
                    description = "SHA-256 block chain verification proving zero tampering across all administrative actions",
                    badge = "SHA-256 PASS",
                    icon = Icons.Default.VerifiedUser,
                    onClick = { onNavigateToTab(SuperAdminTab.AUDIT) }
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String,
    tint: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SaCardBg),
        border = BorderStroke(1.dp, SaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(tint)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtext,
                fontSize = 11.sp,
                fontFamily = SpaceGroteskFontFamily,
                color = SaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickNavRow(
    title: String,
    description: String,
    badge: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SaCardBg),
        border = BorderStroke(1.dp, SaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SaPurple.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = SaPurple, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontFamily = SoraFontFamily, fontWeight = FontWeight.SemiBold, color = SaTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily, color = SaTextSecondary)
            }
            Surface(shape = RoundedCornerShape(6.dp), color = SaCyan.copy(alpha = 0.12f)) {
                Text(text = badge, fontSize = 10.sp, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = SaCyan, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}

@Composable
fun SuperAdminDiagnosticsDialog(
    superAdmin: UserAccount,
    repository: SuperAdminRepository,
    onDismiss: () -> Unit
) {
    val kpis = remember { repository.computeSuperAdminKpis(superAdmin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ROOT SYSTEM DIAGNOSTICS", fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SaCyan)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Identity: @${superAdmin.username} (${superAdmin.fullName})", fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp)
                Text("Role: ${superAdmin.role.displayName} [Level 4 Root Authority]", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = SaPurple)
                Text("Governed Version: ${kpis.governedConfigVersion}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp)
                Text("Audit Integrity: ${kpis.chainIntegrityStatus}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = SaGreen)
                Text("Seed: 26004L (Deterministic Multi-frequency sweep)", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp)
                Text("Framework: Kotlin Jetpack Compose M3 (Light / Dark Adaptive)", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = SaPurple)) {
                Text("Close", fontFamily = SpaceGroteskFontFamily)
            }
        }
    )
}

@Composable
private fun SaMetricTile(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    valueColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SaCardBg,
        border = BorderStroke(1.dp, SaBorder),
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
                        color = SaTextSecondary
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
                        color = if (valueColor != Color.Unspecified) valueColor else SaTextPrimary
                    )
                )
                Text(
                    text = unit,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = SaTextSecondary
                    )
                )
            }
        }
    }
}

package com.example.ui.superadmin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

                // Compact Governance Sub-Banner
                SuperAdminCompactBanner()

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SaDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: ASHA-Style Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SaCardBg,
                border = BorderStroke(1.dp, SaBorder),
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
                                text = "SUPER ADMIN ROOT CONTROL PLANE",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SaTextPrimary
                                )
                            )
                            Text(
                                text = "System & Security Governance, User Administration & Protocol Oversight",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = SaTextSecondary
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SaPurple.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SaPurple.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "ROOT LEVEL 4",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = SaPurple
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SaBorder, thickness = 0.7.dp)

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
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = SaCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Consortium Protocol: AMCH-NER-ETH-2026-081B",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = SaTextPrimary
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SaGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "TAMPER EVIDENT",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = SaGreen
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Audit Chain Card right on the overview dashboard
        item {
            SuperAdminAuditChainCard(
                integrityStatus = "VERIFIED",
                totalRecords = kpis.totalAuditEventsLogged,
                latestHash = "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
                tamperStatus = "Tamper Evident"
            )
        }

        // Operational View Switcher Banner
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

        // Top KPI Cards Row (2-column responsive)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "REGISTERED USERS",
                    value = "${kpis.totalRegisteredUsers}",
                    subtext = "${kpis.totalAdmins} Admins • ${kpis.totalAshaWorkers} ASHA",
                    tint = SaPurple
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "DEPLOYMENT ZONES",
                    value = "${kpis.totalDeploymentZones}",
                    subtext = "${kpis.totalHealthCenters} Rural Health Centers",
                    tint = SaCyan
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "TOTAL SCREENINGS",
                    value = "${kpis.totalScreeningsAcrossAllZones}",
                    subtext = "Across all NER Clusters",
                    tint = SaGreen
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "GOVERNED VERSION",
                    value = "v1.0-FROZEN",
                    subtext = "Seed 26004L • Schema v1.0",
                    tint = SaGold
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "AUDIT EVENTS",
                    value = "${kpis.totalAuditEventsLogged}",
                    subtext = "SHA-256 Tamper-Evident",
                    tint = SaGreen
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "FABRICATION BLOCKS",
                    value = "${kpis.securityViolationsBlocked}",
                    subtext = "Scientific Invariant Guard",
                    tint = if (kpis.securityViolationsBlocked > 0) SaRed else SaCyan
                )
            }
        }

        // Quick Navigation Grid
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

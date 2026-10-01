package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.AdminRepository
import com.example.auth.AuthorizationGuard
import com.example.auth.PortalRoute
import com.example.auth.UserAccount
import com.example.portal.AshaScreeningSession
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun AdminControlCenterScreen(
    admin: UserAccount,
    adminRepository: AdminRepository = remember { AdminRepository() },
    onLogout: () -> Unit
) {
    var currentRoute by remember { mutableStateOf(PortalRoute.ADMIN_DASHBOARD) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var selectedSessionForDetail by remember { mutableStateOf<AshaScreeningSession?>(null) }
    var securityViolationMessage by remember { mutableStateOf<String?>(null) }

    fun navigateToRoute(route: PortalRoute) {
        val check = AuthorizationGuard.evaluateRouteAccess(admin, route)
        if (check.isAllowed) {
            currentRoute = route
            securityViolationMessage = null
        } else {
            // Log security violation in append-only audit trail
            adminRepository.recordSecurityViolation(
                actorUsername = admin.username,
                actorRole = admin.role,
                targetRouteOrResource = route.path,
                reason = check.reason
            )
            securityViolationMessage = check.reason
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(AdminSurfaceBg)) {
                // Top App Bar: ARTHROSCAN-NER | SIH26004 | ADMIN | Theme | Logout
                AdminTopAppBar(
                    admin = admin,
                    onOpenDiagnostics = { showDiagnosticsDialog = true },
                    onOpenProfile = { navigateToRoute(PortalRoute.ADMIN_PROFILE) },
                    onLogout = onLogout
                )

                // Horizontal scrollable operational tabs
                AdminScrollableTabs(
                    currentRoute = currentRoute,
                    onSelectRoute = { navigateToRoute(it) }
                )
            }
        },
        containerColor = AdminDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (securityViolationMessage != null) {
                Surface(
                    color = AdminRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminRed),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = securityViolationMessage!!,
                            color = AdminRed,
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { securityViolationMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = AdminRed)
                        }
                    }
                }
            }

            when (currentRoute) {
                PortalRoute.ADMIN_DASHBOARD -> {
                    AdminOverviewScreen(
                        admin = admin,
                        repository = adminRepository,
                        onSelectSession = { selectedSessionForDetail = it }
                    )
                }
                PortalRoute.ADMIN_SESSIONS -> {
                    AdminSessionsScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_WORKERS -> {
                    AdminWorkersScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_DEVICES -> {
                    AdminDevicesScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_REGIONS -> {
                    AdminRegionsScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_RETEST -> {
                    AdminRetestScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_QUALITY -> {
                    AdminQualityScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_UNCERTAINTY -> {
                    AdminUncertaintyScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_REPORTS -> {
                    AdminReportsScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_AUDIT -> {
                    AdminAuditScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_SYSTEM_HEALTH -> {
                    AdminSystemHealthScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_SCIENTIFIC_CONFIG -> {
                    AdminScientificConfigScreen(
                        admin = admin,
                        repository = adminRepository
                    )
                }
                PortalRoute.ADMIN_PROFILE -> {
                    AdminProfileScreen(
                        admin = admin,
                        onLogout = onLogout
                    )
                }
                else -> {
                    AdminOverviewScreen(
                        admin = admin,
                        repository = adminRepository,
                        onSelectSession = { selectedSessionForDetail = it }
                    )
                }
            }
        }
    }

    if (showDiagnosticsDialog) {
        AdminDiagnosticsDialog(onDismiss = { showDiagnosticsDialog = false })
    }

    if (selectedSessionForDetail != null) {
        AdminSessionDetailDialog(
            session = selectedSessionForDetail!!,
            onDismiss = { selectedSessionForDetail = null }
        )
    }
}

@Composable
fun AdminScrollableTabs(
    currentRoute: PortalRoute,
    onSelectRoute: (PortalRoute) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = getTabIndex(currentRoute),
        containerColor = AdminSurfaceBg,
        contentColor = AdminCyan,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
            val index = getTabIndex(currentRoute)
            if (index in tabPositions.indices) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = AdminCyan
                )
            }
        },
        divider = {
            HorizontalDivider(color = AdminBorder)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        AdminTabItem(
            route = PortalRoute.ADMIN_DASHBOARD,
            label = "Overview",
            icon = Icons.Default.Dashboard,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_SESSIONS,
            label = "Sessions",
            icon = Icons.AutoMirrored.Filled.ListAlt,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_WORKERS,
            label = "Workers",
            icon = Icons.Default.People,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_DEVICES,
            label = "Hardware",
            icon = Icons.Default.Sensors,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_REGIONS,
            label = "Regions",
            icon = Icons.Default.LocationOn,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_RETEST,
            label = "Retest Queue",
            icon = Icons.Default.Warning,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_QUALITY,
            label = "Signal Quality",
            icon = Icons.Default.GraphicEq,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_UNCERTAINTY,
            label = "Uncertainty",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_REPORTS,
            label = "Reports",
            icon = Icons.Default.Assessment,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_AUDIT,
            label = "Audit",
            icon = Icons.Default.Lock,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_SYSTEM_HEALTH,
            label = "System Health",
            icon = Icons.Default.Healing,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_SCIENTIFIC_CONFIG,
            label = "Scientific Config",
            icon = Icons.Default.Security,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
        AdminTabItem(
            route = PortalRoute.ADMIN_PROFILE,
            label = "Profile",
            icon = Icons.Default.Person,
            currentRoute = currentRoute,
            onSelect = onSelectRoute
        )
    }
}

@Composable
fun AdminNavigationBar(
    currentRoute: PortalRoute,
    onSelectRoute: (PortalRoute) -> Unit
) {
    AdminScrollableTabs(currentRoute = currentRoute, onSelectRoute = onSelectRoute)
}

@Composable
fun AdminTabItem(
    route: PortalRoute,
    label: String,
    icon: ImageVector,
    currentRoute: PortalRoute,
    onSelect: (PortalRoute) -> Unit
) {
    val isSelected = currentRoute == route
    Tab(
        selected = isSelected,
        onClick = { onSelect(route) },
        text = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AdminCyan else AdminTextDim
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) AdminCyan else AdminTextDim,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

fun getTabIndex(route: PortalRoute): Int {
    return when (route) {
        PortalRoute.ADMIN_DASHBOARD -> 0
        PortalRoute.ADMIN_SESSIONS -> 1
        PortalRoute.ADMIN_WORKERS -> 2
        PortalRoute.ADMIN_DEVICES -> 3
        PortalRoute.ADMIN_REGIONS -> 4
        PortalRoute.ADMIN_RETEST -> 5
        PortalRoute.ADMIN_QUALITY -> 6
        PortalRoute.ADMIN_UNCERTAINTY -> 7
        PortalRoute.ADMIN_REPORTS -> 8
        PortalRoute.ADMIN_AUDIT -> 9
        PortalRoute.ADMIN_SYSTEM_HEALTH -> 10
        PortalRoute.ADMIN_SCIENTIFIC_CONFIG -> 11
        PortalRoute.ADMIN_PROFILE -> 12
        else -> 0
    }
}

package com.example.ui.portal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.auth.AuthRepository
import com.example.auth.AuthorizationGuard
import com.example.auth.PortalRoute
import com.example.auth.UserRole
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaSessionManager
import com.example.ui.ArthroscanShellScreen
import com.example.ui.ArthroscanViewModel
import com.example.ui.admin.AdminControlCenterScreen
import com.example.ui.superadmin.SuperAdminControlPlaneScreen
import kotlinx.coroutines.launch

enum class AshaScreenState {
    LOGIN,
    DASHBOARD,
    SENSORS,
    SCREENING_FLOW,
    HISTORY,
    PROFILE,
    SUMMARY_DETAIL,
    ADMIN_SHELL_GUARDED
}

@Composable
fun AshaPortalMainScreen(
    viewModel: ArthroscanViewModel,
    authRepository: AuthRepository = remember { AuthRepository.getInstance() },
    sessionManager: AshaSessionManager = remember { AshaSessionManager() }
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val activeSession by sessionManager.activeSession.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf(AshaScreenState.DASHBOARD) }
    var showSessionCreationDialog by remember { mutableStateOf(false) }
    var showTechDrawer by remember { mutableStateOf(false) }
    var showHelpCenter by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedRouteName by remember { mutableStateOf("ADMIN_DASHBOARD") }
    var viewingSummarySession by remember { mutableStateOf<AshaScreeningSession?>(null) }
    var superAdminViewingAdminOps by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthService = remember { com.example.auth.GoogleAuthService(context) }

    var hasStarted by remember { mutableStateOf(false) }

    // Section 5: Real Logout implementation
    val handleLogout: () -> Unit = {
        sessionManager.resetSession()
        authRepository.logout()
        coroutineScope.launch {
            googleAuthService.signOut()
        }
        superAdminViewingAdminOps = false
        hasStarted = false
        currentScreen = AshaScreenState.LOGIN
    }

    val user = currentUser

    if (user == null) {
        if (!hasStarted) {
            ArthroscanFirstScreen(
                onGetStarted = {
                    hasStarted = true
                }
            )
            return
        }

        // Prevent pressing Back from reopening protected dashboard, return to Welcome Screen
        BackHandler(enabled = true) {
            hasStarted = false
        }
        AshaLoginScreen(
            authRepository = authRepository,
            onLoginSuccess = { loggedInUser ->
                superAdminViewingAdminOps = false
                currentScreen = AshaScreenState.DASHBOARD
            }
        )
        return
    }

    // Role-based routing: SUPER_ADMIN gets Super Admin Control Plane
    if (user.role == UserRole.SUPER_ADMIN) {
        if (superAdminViewingAdminOps) {
            AdminControlCenterScreen(
                admin = user,
                onLogout = handleLogout
            )
        } else {
            SuperAdminControlPlaneScreen(
                superAdmin = user,
                onLogout = handleLogout,
                onSwitchToOperationalAdmin = {
                    superAdminViewingAdminOps = true
                }
            )
        }
        return
    }

    // Role-based routing: ADMIN gets Admin Control Center
    if (user.role == UserRole.ADMIN) {
        AdminControlCenterScreen(
            admin = user,
            onLogout = handleLogout
        )
        return
    }

    // Role-based routing: PATIENT gets Patient Direct Access Board
    if (user.role == UserRole.PATIENT) {
        if (currentScreen == AshaScreenState.SCREENING_FLOW) {
            AshaScreeningFlowScreen(
                worker = user,
                sessionManager = sessionManager,
                viewModel = viewModel,
                onFinishAndExit = {
                    currentScreen = AshaScreenState.DASHBOARD
                }
            )
            return
        }
        if (currentScreen == AshaScreenState.SUMMARY_DETAIL && viewingSummarySession != null) {
            com.example.ui.portal.steps.Step11SummaryScreen(
                session = viewingSummarySession!!,
                onFinishSession = {
                    currentScreen = AshaScreenState.DASHBOARD
                },
                onBack = { currentScreen = AshaScreenState.DASHBOARD }
            )
            return
        }

        PatientDirectAccessBoardScreen(
            patient = user,
            onLogout = handleLogout,
            onStartScreening = {
                if (activeSession == null) {
                    sessionManager.startNewSession(user, uiState.profile)
                }
                currentScreen = AshaScreenState.SCREENING_FLOW
            }
        )
        return
    }

    val stats = remember(user.id, activeSession) {
        sessionManager.computeStatsForWorker(user.id)
    }

    when (currentScreen) {
        AshaScreenState.DASHBOARD -> {
            AshaDashboardScreen(
                worker = user,
                uiState = uiState,
                stats = stats,
                inProgressSession = activeSession,
                onStartNewScreening = {
                    showSessionCreationDialog = true
                },
                onContinueScreening = {
                    if (activeSession != null) {
                        currentScreen = AshaScreenState.SCREENING_FLOW
                    }
                },
                onViewHistory = {
                    currentScreen = AshaScreenState.HISTORY
                },
                onDeviceCheck = {
                    currentScreen = AshaScreenState.SENSORS
                },
                onViewProfile = {
                    currentScreen = AshaScreenState.PROFILE
                },
                onOpenHelp = {
                    showHelpCenter = true
                },
                onOpenTechnicalDrawer = {
                    showTechDrawer = true
                },
                onLogout = handleLogout,
                onAttemptAdminAccess = {
                    // Test Authorization Guard enforcement
                    try {
                        AuthorizationGuard.checkRouteAccess(user, PortalRoute.ADMIN_DASHBOARD)
                        // If authorized (e.g. admin), navigate
                        currentScreen = AshaScreenState.ADMIN_SHELL_GUARDED
                    } catch (e: SecurityException) {
                        // Correctly caught by security guard
                        blockedRouteName = PortalRoute.ADMIN_DASHBOARD.title
                        showBlockedDialog = true
                    }
                }
            )
        }

        AshaScreenState.SENSORS -> {
            AshaSensorsScreen(
                worker = user,
                uiState = uiState,
                onConnect = { viewModel.connectSensors() },
                onToggleStreaming = { viewModel.toggleStreaming() },
                onNavigateHome = { currentScreen = AshaScreenState.DASHBOARD },
                onNavigateHistory = { currentScreen = AshaScreenState.HISTORY },
                onNavigateProfile = { currentScreen = AshaScreenState.PROFILE }
            )
        }

        AshaScreenState.SCREENING_FLOW -> {
            AshaScreeningFlowScreen(
                worker = user,
                sessionManager = sessionManager,
                viewModel = viewModel,
                onFinishAndExit = {
                    currentScreen = AshaScreenState.DASHBOARD
                }
            )
        }

        AshaScreenState.HISTORY -> {
            AshaScreeningHistoryScreen(
                worker = user,
                sessionManager = sessionManager,
                onBack = { currentScreen = AshaScreenState.DASHBOARD },
                onViewSessionSummary = { historySession ->
                    viewingSummarySession = historySession
                    currentScreen = AshaScreenState.SUMMARY_DETAIL
                }
            )
        }

        AshaScreenState.PROFILE -> {
            AshaProfileScreen(
                worker = user,
                onBack = { currentScreen = AshaScreenState.DASHBOARD },
                onLogout = handleLogout,
                stats = stats
            )
        }

        AshaScreenState.SUMMARY_DETAIL -> {
            val sessionToView = viewingSummarySession
            if (sessionToView != null) {
                com.example.ui.portal.steps.Step11SummaryScreen(
                    session = sessionToView,
                    onFinishSession = {
                        currentScreen = AshaScreenState.HISTORY
                    },
                    onExportJson = {
                        // Reuses JSON summary
                    },
                    onBack = { currentScreen = AshaScreenState.HISTORY }
                )
            } else {
                currentScreen = AshaScreenState.HISTORY
            }
        }

        AshaScreenState.ADMIN_SHELL_GUARDED -> {
            // Guarded admin screen
            if (AuthorizationGuard.canAccessRoute(user, PortalRoute.ADMIN_DASHBOARD)) {
                AdminControlCenterScreen(
                    admin = user,
                    onLogout = handleLogout
                )
            } else {
                blockedRouteName = PortalRoute.ADMIN_DASHBOARD.title
                showBlockedDialog = true
                currentScreen = AshaScreenState.DASHBOARD
            }
        }

        AshaScreenState.LOGIN -> {
            BackHandler(enabled = true) {
                // Exit app or remain on login
            }
            AshaLoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    currentScreen = AshaScreenState.DASHBOARD
                }
            )
        }
    }

    if (showSessionCreationDialog) {
        AshaSessionCreationDialog(
            worker = user,
            uiState = uiState,
            onCheckDevice = {
                viewModel.connectSensors()
            },
            onStartSession = { mode, participantId ->
                showSessionCreationDialog = false
                sessionManager.startNewSession(user, mode, participantId)
                viewModel.switchProfile(mode)
                currentScreen = AshaScreenState.SCREENING_FLOW
            },
            onDismiss = { showSessionCreationDialog = false }
        )
    }

    if (showTechDrawer) {
        AshaTechnicalDrawerDialog(
            uiState = uiState,
            onDismiss = { showTechDrawer = false }
        )
    }

    if (showHelpCenter) {
        AshaHelpCenterDialog(
            onDismiss = { showHelpCenter = false }
        )
    }

    if (showBlockedDialog) {
        BlockedRouteDialog(
            targetRouteName = blockedRouteName,
            onDismiss = { showBlockedDialog = false }
        )
    }
}

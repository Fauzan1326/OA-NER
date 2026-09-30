package com.example

import com.example.ai.*
import com.example.auth.*
import com.example.core.config.ProfileType
import com.example.core.contract.Modality
import com.example.core.contract.ReferralRecommendation
import com.example.core.contract.RiskTier
import com.example.portal.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 20 SECURITY & OPERATIONAL BOUNDARY TESTS FOR ASHA FIELD PORTAL
 * ARTHROSCAN-NER | SIH26004 | SECTION 27
 */
class AshaPortalSecurityTest {

    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: AshaSessionManager

    @Before
    fun setUp() {
        authRepo = AuthRepository()
        sessionManager = AshaSessionManager()
    }

    // 01: Public registration must never allow selecting ADMIN or SUPER_ADMIN
    @Test
    fun test01_publicRegistrationDefaultsToAshaWorkerAndPending() {
        val result = authRepo.registerPublicAshaWorker(
            username = "new_field_nurse",
            fullName = "Ananya Devi",
            email = "ananya.devi@health.gov.in",
            password = "Password123!",
            center = "Guwahati Sub-Center",
            region = "Kamrup Metro"
        )

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.ASHA_WORKER, user?.role)
        assertEquals(UserStatus.PENDING_APPROVAL, user?.status)
    }

    // 02: Pending users cannot log in
    @Test
    fun test02_pendingUserCannotLogin() {
        authRepo.registerPublicAshaWorker(
            username = "pending_nurse",
            fullName = "Pending User",
            email = "pending@health.gov.in",
            password = "Password123!",
            center = "Center X",
            region = "Region Y"
        )

        val loginResult = authRepo.login("pending_nurse", "Password123!")
        assertTrue(loginResult.isFailure)
        assertTrue(loginResult.exceptionOrNull()?.message?.contains("pending", ignoreCase = true) == true)
    }

    // 03: Suspended or rejected users cannot log in
    @Test
    fun test03_rejectedUserCannotLogin() {
        authRepo.registerPublicAshaWorker(
            username = "rejected_user",
            fullName = "Rejected User",
            email = "rejected@health.gov.in",
            password = "Password123!",
            center = "Center A",
            region = "Region B"
        )
        val admin = authRepo.getAccountByUsername("admin")!!
        authRepo.approveOrRejectUser(admin, "rejected_user", UserStatus.REJECTED)

        val loginResult = authRepo.login("rejected_user", "Password123!")
        assertTrue(loginResult.isFailure)
        assertTrue(loginResult.exceptionOrNull()?.message?.contains("rejected", ignoreCase = true) == true)
    }

    // 04: Active approved ASHA worker can log in
    @Test
    fun test04_activeAshaWorkerCanLogin() {
        val loginResult = authRepo.login("asha_priya", "AshaWorker2026!")
        assertTrue(loginResult.isSuccess)
        val account = loginResult.getOrNull()
        assertEquals(UserRole.ASHA_WORKER, account?.role)
        assertEquals(UserStatus.ACTIVE, account?.status)
    }

    // 05: ASHA worker cannot access Admin Dashboard
    @Test
    fun test05_ashaWorkerCannotAccessAdminDashboard() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        assertFalse(AuthorizationGuard.canAccessRoute(asha, PortalRoute.ADMIN_DASHBOARD))

        try {
            AuthorizationGuard.checkRouteAccess(asha, PortalRoute.ADMIN_DASHBOARD)
            fail("Expected SecurityException when ASHA worker tries to access ADMIN_DASHBOARD")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("ACCESS DENIED", ignoreCase = true) == true)
        }
    }

    // 06: ASHA worker cannot access Model Configuration
    @Test
    fun test06_ashaWorkerCannotAccessModelConfig() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        assertFalse(AuthorizationGuard.canAccessRoute(asha, PortalRoute.MODEL_CONFIG))

        assertThrows(SecurityException::class.java) {
            AuthorizationGuard.checkRouteAccess(asha, PortalRoute.MODEL_CONFIG)
        }
    }

    // 07: ASHA worker cannot access Scientific Threshold Editor
    @Test
    fun test07_ashaWorkerCannotAccessThresholdEditor() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        assertFalse(AuthorizationGuard.canAccessRoute(asha, PortalRoute.THRESHOLD_EDITOR))

        assertThrows(SecurityException::class.java) {
            AuthorizationGuard.checkRouteAccess(asha, PortalRoute.THRESHOLD_EDITOR)
        }
    }

    // 08: ASHA worker cannot modify RF hardware registers
    @Test
    fun test08_ashaWorkerCannotAccessRfRegisters() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        assertFalse(AuthorizationGuard.canAccessRoute(asha, PortalRoute.RF_HARDWARE_CONFIG))

        assertThrows(SecurityException::class.java) {
            AuthorizationGuard.checkRouteAccess(asha, PortalRoute.RF_HARDWARE_CONFIG)
        }
    }

    // 09: ASHA worker cannot edit or delete audit logs
    @Test
    fun test09_ashaWorkerCannotEditAuditRecords() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        assertFalse(AuthorizationGuard.canAccessRoute(asha, PortalRoute.AUDIT_LOGS))

        assertThrows(SecurityException::class.java) {
            AuthorizationGuard.checkRouteAccess(asha, PortalRoute.AUDIT_LOGS)
        }
    }

    // 10: ASHA worker cannot approve or reject other users
    @Test
    fun test10_ashaWorkerCannotApproveOrRejectUsers() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        assertFalse(AuthorizationGuard.canAccessRoute(asha, PortalRoute.USER_MANAGEMENT))

        val result = authRepo.approveOrRejectUser(asha, "asha_priya", UserStatus.ACTIVE)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("SUPER_ADMIN or ADMIN") == true)
    }

    // 11: ASHA worker cannot change user roles
    @Test
    fun test11_ashaWorkerCannotChangeRoles() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        val result = authRepo.changeUserRole(asha, "asha_priya", UserRole.ADMIN)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("SUPER_ADMIN") == true)
    }

    // 12: Admin can access Admin Dashboard
    @Test
    fun test12_adminCanAccessAdminDashboard() {
        val admin = authRepo.getAccountByUsername("admin")!!
        assertTrue(AuthorizationGuard.canAccessRoute(admin, PortalRoute.ADMIN_DASHBOARD))
        AuthorizationGuard.checkRouteAccess(admin, PortalRoute.ADMIN_DASHBOARD)
    }

    // 13: Super Admin has global clearance
    @Test
    fun test13_superAdminCanAccessAllRoutes() {
        val superAdmin = authRepo.getAccountByUsername("superadmin")!!
        PortalRoute.values().forEach { route ->
            assertTrue("SuperAdmin must have access to ${route.name}", AuthorizationGuard.canAccessRoute(superAdmin, route))
        }
    }

    // 14: Session creation requires authenticated worker and creates valid state
    @Test
    fun test14_sessionCreationRequiresAuthenticatedWorker() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        val session = sessionManager.startNewSession(asha, ProfileType.DEMO, "PART-TEST-001")

        assertNotNull(session)
        assertEquals(asha.id, session.workerId)
        assertEquals("PART-TEST-001", session.participantId)
        assertEquals(AshaStep.DEVICE, session.currentStep)
        assertEquals(StepStatus.ACTIVE, session.stepStatuses[AshaStep.DEVICE])
    }

    // 15: Session history enforces worker isolation
    @Test
    fun test15_sessionHistoryWorkerIsolation() {
        val asha1 = authRepo.getAccountByUsername("asha_priya")!!
        val asha2 = UserAccount(
            id = "WORKER-9999",
            username = "asha_second",
            fullName = "Second Worker",
            email = "second@health.gov.in",
            role = UserRole.ASHA_WORKER,
            status = UserStatus.ACTIVE,
            assignedCenter = "Sub-Center 2",
            assignedRegion = "Region 2"
        )

        sessionManager.startNewSession(asha1, ProfileType.DEMO, "PART-1")
        sessionManager.completeSession()

        val historyAsha1 = sessionManager.getHistoryForWorker(asha1.id)
        val historyAsha2 = sessionManager.getHistoryForWorker(asha2.id)

        assertTrue(historyAsha1.any { it.workerId == asha1.id })
        assertTrue(historyAsha2.none { it.workerId == asha1.id })
    }

    // 16: Step status transitions function predictably
    @Test
    fun test16_stepStatusTransitionsFunctionPredictably() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        sessionManager.startNewSession(asha, ProfileType.DEMO)

        sessionManager.updateStepStatus(AshaStep.DEVICE, StepStatus.PASS)
        sessionManager.setStep(AshaStep.SLEEVE)

        val active = sessionManager.activeSession.value!!
        assertEquals(StepStatus.PASS, active.stepStatuses[AshaStep.DEVICE])
        assertEquals(StepStatus.ACTIVE, active.stepStatuses[AshaStep.SLEEVE])
        assertEquals(AshaStep.SLEEVE, active.currentStep)
    }

    // 17: Sleeve placement requires all checks before proceed
    @Test
    fun test17_sleevePlacementCheckRequirements() {
        val incompleteCheck = SleevePlacementCheck(
            sensorAlignment = "REPOSITION",
            sleeveTension = "PASS",
            contactQuality = "PASS",
            orientation = "PASS",
            isConfirmed = false
        )
        assertFalse(incompleteCheck.isAllPass)

        val validCheck = SleevePlacementCheck(
            sensorAlignment = "PASS",
            sleeveTension = "PASS",
            contactQuality = "PASS",
            orientation = "PASS",
            isConfirmed = true
        )
        assertTrue(validCheck.isAllPass)
    }

    // 18: Calibration failure records failure message and prevents silent pass
    @Test
    fun test18_calibrationFailureHandling() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        sessionManager.startNewSession(asha, ProfileType.DEMO)

        sessionManager.recordCalibrationResult(false, "Decoupled electrode detected on Vastus Medialis")

        val active = sessionManager.activeSession.value!!
        assertFalse(active.calibrationPassed)
        assertEquals(StepStatus.FAIL, active.stepStatuses[AshaStep.CALIBRATION])
        assertEquals("Decoupled electrode detected on Vastus Medialis", active.calibrationMessage)
    }

    // 19: High uncertainty flags retest required and remediation guidance
    @Test
    fun test19_highUncertaintyTriggersRetest() {
        val asha = authRepo.getAccountByUsername("asha_priya")!!
        sessionManager.startNewSession(asha, ProfileType.DEMO)

        val highUncertaintyDecision = MultimodalScreeningDecision(
            sessionId = "SES-TEST",
            subjectId = "SUB-TEST",
            fusionState = FusionState.INSUFFICIENT,
            availableModalities = listOf(Modality.IMU, Modality.VAG),
            excludedModalities = listOf(Modality.SEMG, Modality.RF),
            modalityValidationResults = emptyMap(),
            modalityPredictions = emptyMap(),
            participatingWeights = emptyMap(),
            fusedScore = 0.55,
            uncertaintyResult = UncertaintyEvaluationResult(
                tier = UncertaintyTier.HIGH_UNCERTAINTY,
                uncertaintyScore = 0.65,
                contributingFactors = listOf("High modality disagreement")
            ),
            screeningRiskTier = RiskTier.HIGH_UNCERTAINTY_RETEST,
            referralRecommendation = ReferralRecommendation.RETEST_REQUIRED,
            screeningRecommendationText = "Retest required"
        )

        sessionManager.attachScreeningDecision(highUncertaintyDecision)

        val active = sessionManager.activeSession.value!!
        assertTrue(active.retestRequired)
        assertNotNull(active.retestReason)
        assertTrue(active.retestGuidance.isNotEmpty())
    }

    // 20: Screening results never claim diagnostic confirmation
    @Test
    fun test20_screeningResultsNeverClaimDiagnosticConfirmation() {
        ReferralRecommendation.values().forEach { rec ->
            val label = rec.label.lowercase()
            assertFalse("Referral label must never say 'diagnosed'", label.contains("diagnosed"))
            assertFalse("Referral label must never say 'confirmed'", label.contains("confirmed"))
            assertFalse("Referral label must never say 'x-ray not needed'", label.contains("x-ray not needed"))
        }

        RiskTier.values().forEach { tier ->
            val label = tier.label.lowercase()
            assertFalse("Tier label must never say 'osteoarthritis positive'", label.contains("positive"))
            assertFalse("Tier label must never say 'definitive'", label.contains("definitive"))
            assertTrue("Tier label must include 'screening risk' or 'retest'", label.contains("screening risk") || label.contains("retest"))
        }
    }

    // 21: Patient direct registration yields ACTIVE account and instant login
    @Test
    fun test21_patientRegistrationFlowAndDirectAccess() {
        val regResult = authRepo.registerPatientUser(
            username = "citizen.rahim",
            fullName = "Rahim Ali",
            email = "rahim.ali@patient.org",
            password = "SecurePass#2026"
        )

        assertTrue(regResult.isSuccess)
        val patient = regResult.getOrThrow()
        assertEquals(com.example.auth.UserRole.PATIENT, patient.role)
        assertEquals(com.example.auth.UserStatus.ACTIVE, patient.status)
        assertEquals(com.example.auth.AccountState.PATIENT_ACTIVE, patient.accountState)

        // Patient can log in directly without admin approval
        val loginResult = authRepo.login("citizen.rahim", "SecurePass#2026")
        assertTrue(loginResult.isSuccess)
        val loggedIn = loginResult.getOrThrow()
        assertEquals("citizen.rahim", loggedIn.username)
        assertEquals(com.example.auth.UserRole.PATIENT, loggedIn.role)
        assertEquals(loggedIn, authRepo.currentUser.value)
    }

    // 22: ASHA registration yields PENDING_APPROVAL and blocks login until admin approval
    @Test
    fun test22_ashaCadreRegistrationPendingApprovalFlow() {
        val regResult = authRepo.registerAshaWorker(
            username = "asha.kanika",
            fullName = "Kanika Saikia",
            email = "kanika.s@nhm.assam.gov.in",
            password = "AshaPass#2026"
        )

        assertTrue(regResult.isSuccess)
        val asha = regResult.getOrThrow()
        assertEquals(com.example.auth.UserRole.ASHA_WORKER, asha.role)
        assertEquals(com.example.auth.UserStatus.PENDING_APPROVAL, asha.status)
        assertEquals(com.example.auth.AccountState.ASHA_PENDING, asha.accountState)

        // Login before approval must be blocked
        val blockedLogin = authRepo.login("asha.kanika", "AshaPass#2026")
        assertTrue(blockedLogin.isFailure)
        val errMsg = blockedLogin.exceptionOrNull()?.message
        assertTrue(errMsg?.contains("Your account is awaiting administrator approval", ignoreCase = true) == true)

        // Admin approves the worker
        val admin = authRepo.getAccountByUsername("admin")!!
        val approvalResult = authRepo.approveOrRejectUser(admin, "asha.kanika", com.example.auth.UserStatus.ACTIVE)
        assertTrue(approvalResult.isSuccess)

        val approvedUser = authRepo.getAccountByUsername("asha.kanika")!!
        assertEquals(com.example.auth.UserStatus.ACTIVE, approvedUser.status)
        assertEquals(com.example.auth.AccountState.ASHA_APPROVED, approvedUser.accountState)

        // Now login must succeed
        val approvedLogin = authRepo.login("asha.kanika", "AshaPass#2026")
        assertTrue(approvedLogin.isSuccess)
        assertEquals(com.example.auth.UserRole.ASHA_WORKER, approvedLogin.getOrThrow().role)
    }
}

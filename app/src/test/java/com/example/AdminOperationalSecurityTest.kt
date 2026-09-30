package com.example

import com.example.admin.AdminRepository
import com.example.admin.ScientificConfigSnapshot
import com.example.auth.*
import com.example.core.config.ProfileType
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaSessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 24 SECURITY & RBAC TESTS FOR SECTION 6 — ADMIN OPERATIONAL CONTROL CENTER
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 */
class AdminOperationalSecurityTest {

    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: AshaSessionManager
    private lateinit var adminRepo: AdminRepository

    private lateinit var adminUser: UserAccount
    private lateinit var ashaUser: UserAccount
    private lateinit var superAdminUser: UserAccount

    @Before
    fun setUp() {
        authRepo = AuthRepository()
        sessionManager = AshaSessionManager()
        adminRepo = AdminRepository(authRepo, sessionManager)

        adminUser = authRepo.getAccountByUsername("admin")!!
        ashaUser = authRepo.getAccountByUsername("asha_worker")!!
        superAdminUser = authRepo.getAccountByUsername("super_admin")!!
    }

    // 01: Admin cannot access scientific-model edit privilege
    @Test
    fun test01_adminCannotAccessScientificModelEditPrivilege() {
        assertFalse("ADMIN must not have scientific model editing privileges", AuthorizationGuard.canModifyScientificModel(adminUser))
        assertTrue("SUPER_ADMIN alone has model configuration authority", AuthorizationGuard.canModifyScientificModel(superAdminUser))
    }

    // 02: Admin cannot access Super Admin creation
    @Test
    fun test02_adminCannotAccessSuperAdminCreation() {
        assertFalse("ADMIN must not create SUPER_ADMIN accounts", AuthorizationGuard.canCreateSuperAdmin(adminUser))
        val creationAttempt = authRepo.createAccount(
            caller = adminUser,
            username = "rogue_super",
            fullName = "Rogue Super",
            email = "rogue@health.gov.in",
            password = "SecretPassword123!",
            role = UserRole.SUPER_ADMIN
        )
        assertTrue(creationAttempt.isFailure)
        assertTrue(creationAttempt.exceptionOrNull() is SecurityException)
    }

    // 03: Admin cannot access Super Admin system settings
    @Test
    fun test03_adminCannotAccessSuperAdminSystemSettings() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.SUPER_ADMIN_SYSTEM_SETTINGS)
        assertFalse(accessCheck.isAllowed)
        assertTrue(accessCheck.reason.contains("SUPER_ADMIN"))
    }

    // 04: Admin can access Admin Dashboard
    @Test
    fun test04_adminCanAccessAdminDashboard() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_DASHBOARD)
        assertTrue(accessCheck.isAllowed)
        val kpis = adminRepo.computeAdminKpis(adminUser)
        assertNotNull(kpis)
    }

    // 05: Admin can access ASHA Worker Management
    @Test
    fun test05_adminCanAccessAshaWorkerManagement() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_WORKERS)
        assertTrue(accessCheck.isAllowed)
        val workers = adminRepo.getWorkers(adminUser)
        assertTrue(workers.isNotEmpty())
    }

    // 06: Admin can access Device Operations
    @Test
    fun test06_adminCanAccessDeviceOperations() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_DEVICES)
        assertTrue(accessCheck.isAllowed)
        val devices = adminRepo.getDevices(adminUser)
        assertTrue(devices.isNotEmpty())
    }

    // 07: Admin can access Screening Sessions
    @Test
    fun test07_adminCanAccessScreeningSessions() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_SESSIONS)
        assertTrue(accessCheck.isAllowed)
        val sessions = adminRepo.getAuthorizedSessions(adminUser)
        assertNotNull(sessions)
    }

    // 08: Admin can access Retest Queue
    @Test
    fun test08_adminCanAccessRetestQueue() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_RETEST)
        assertTrue(accessCheck.isAllowed)
        val retests = adminRepo.getRetestQueue(adminUser)
        assertNotNull(retests)
    }

    // 09: Admin can access Quality Operations
    @Test
    fun test09_adminCanAccessQualityOperations() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_QUALITY)
        assertTrue(accessCheck.isAllowed)
        val quality = adminRepo.getSignalQualityMetrics(adminUser)
        assertEquals(4, quality.size) // RF, VAG, IMU, sEMG
    }

    // 10: Admin can access Uncertainty Operations
    @Test
    fun test10_adminCanAccessUncertaintyOperations() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_UNCERTAINTY)
        assertTrue(accessCheck.isAllowed)
        val uncertaintyMap = adminRepo.getUncertaintyBreakdown(adminUser)
        assertNotNull(uncertaintyMap)
    }

    // 11: Admin can access Operational Reports
    @Test
    fun test11_adminCanAccessOperationalReports() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_REPORTS)
        assertTrue(accessCheck.isAllowed)
        val json = adminRepo.exportOperationalReportJson(adminUser)
        assertTrue(json.contains("ARTHROSCAN-NER Operational Deployment Summary"))
        assertTrue(json.contains("NON-DIAGNOSTIC"))
    }

    // 12: Admin can access Audit Logs
    @Test
    fun test12_adminCanAccessAuditLogs() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_AUDIT)
        assertTrue(accessCheck.isAllowed)
        val auditLogs = adminRepo.getAuditLogs(adminUser)
        assertTrue(auditLogs.isNotEmpty())
    }

    // 13: Admin can access System Health
    @Test
    fun test13_adminCanAccessSystemHealth() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_SYSTEM_HEALTH)
        assertTrue(accessCheck.isAllowed)
        val health = adminRepo.getSystemHealth(adminUser)
        assertNotNull(health)
    }

    // 14: Admin can access Scientific Config Read-Only
    @Test
    fun test14_adminCanAccessScientificConfigReadOnly() {
        val accessCheck = AuthorizationGuard.evaluateRouteAccess(adminUser, PortalRoute.ADMIN_SCIENTIFIC_CONFIG)
        assertTrue(accessCheck.isAllowed)
        val config = adminRepo.getScientificConfiguration(adminUser)
        assertTrue(config.isReadOnly)
        assertEquals("v1.0", config.schemaVersion)
        assertEquals(26004L, config.deterministicSeed)
    }

    // 15: Admin cannot modify Scientific Config
    @Test
    fun test15_adminCannotModifyScientificConfig() {
        try {
            adminRepo.attemptModifyScientificConfig(adminUser)
            fail("Should have thrown SecurityException")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("READ-ONLY"))
        }
    }

    // 16: Admin cannot modify Uncertainty Calibration
    @Test
    fun test16_adminCannotModifyUncertaintyCalibration() {
        assertFalse(AuthorizationGuard.canModifyUncertaintyCalibration(adminUser))
        try {
            adminRepo.attemptModifyUncertaintyCalibration(adminUser)
            fail("Should have thrown SecurityException")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("READ-ONLY"))
        }
    }

    // 17: Admin cannot modify Risk Tier Definitions
    @Test
    fun test17_adminCannotModifyRiskTierDefinitions() {
        assertFalse(AuthorizationGuard.canModifyRiskTierDefinitions(adminUser))
        try {
            adminRepo.attemptModifyRiskTierDefinitions(adminUser)
            fail("Should have thrown SecurityException")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("READ-ONLY"))
        }
    }

    // 18: ASHA Worker cannot access Admin Routes
    @Test
    fun test18_ashaWorkerCannotAccessAdminRoutes() {
        val adminRoutes = listOf(
            PortalRoute.ADMIN_DASHBOARD,
            PortalRoute.ADMIN_WORKERS,
            PortalRoute.ADMIN_DEVICES,
            PortalRoute.ADMIN_REGIONS,
            PortalRoute.ADMIN_SESSIONS,
            PortalRoute.ADMIN_RETEST,
            PortalRoute.ADMIN_QUALITY,
            PortalRoute.ADMIN_UNCERTAINTY,
            PortalRoute.ADMIN_REPORTS,
            PortalRoute.ADMIN_AUDIT,
            PortalRoute.ADMIN_SYSTEM_HEALTH,
            PortalRoute.ADMIN_SCIENTIFIC_CONFIG
        )
        for (route in adminRoutes) {
            val check = AuthorizationGuard.evaluateRouteAccess(ashaUser, route)
            assertFalse("ASHA Worker must be blocked from $route", check.isAllowed)
        }
    }

    // 19: ASHA Worker cannot access Admin Path URLs
    @Test
    fun test19_ashaWorkerCannotAccessAdminPathUrls() {
        val restrictedPaths = listOf(
            "/admin",
            "/admin/dashboard",
            "/admin/workers",
            "/admin/devices",
            "/admin/regions",
            "/admin/sessions",
            "/admin/audit",
            "/super-admin/tenants"
        )
        for (path in restrictedPaths) {
            val check = AuthorizationGuard.evaluatePathAccess(ashaUser, path)
            assertFalse("ASHA Worker must be blocked from path $path", check.isAllowed)
        }
    }

    // 20: Public registration cannot create Admin or Super Admin
    @Test
    fun test20_publicRegistrationCannotCreateAdminOrSuperAdmin() {
        val reg = authRepo.registerPublicUser(
            username = "attempted_admin",
            fullName = "Fake Admin",
            email = "fake@admin.com",
            password = "Password123!",
            assignedCenter = "PHC Rampur",
            assignedRegion = "Kamrup Rural"
        )
        assertTrue(reg.isSuccess)
        val created = reg.getOrNull()
        assertNotNull(created)
        assertEquals(UserRole.ASHA_WORKER, created!!.role)
        assertEquals(UserStatus.PENDING_APPROVAL, created.status)
        assertNotEquals(UserRole.ADMIN, created.role)
        assertNotEquals(UserRole.SUPER_ADMIN, created.role)
    }

    // 21: Unauthenticated user cannot access Admin or ASHA portal
    @Test
    fun test21_unauthenticatedUserCannotAccessAdminOrAshaPortal() {
        val unauthCheckAdmin = AuthorizationGuard.evaluateRouteAccess(null, PortalRoute.ADMIN_DASHBOARD)
        assertFalse(unauthCheckAdmin.isAllowed)
        assertTrue(unauthCheckAdmin.reason.contains("Unauthenticated"))

        val unauthCheckAsha = AuthorizationGuard.evaluateRouteAccess(null, PortalRoute.ASHA_DASHBOARD)
        assertFalse(unauthCheckAsha.isAllowed)
        assertTrue(unauthCheckAsha.reason.contains("Unauthenticated"))

        val pathCheck = AuthorizationGuard.evaluatePathAccess(null, "/admin/dashboard")
        assertFalse(pathCheck.isAllowed)
    }

    // 22: Pending user cannot access operational portal
    @Test
    fun test22_pendingUserCannotAccessOperationalPortal() {
        val reg = authRepo.registerPublicAshaWorker(
            username = "pending_worker_99",
            fullName = "Pending Worker",
            email = "pending99@health.gov.in",
            password = "Password123!",
            center = "Center Z",
            region = "Region Z"
        )
        val pendingUser = reg.getOrNull()!!
        assertEquals(UserStatus.PENDING_APPROVAL, pendingUser.status)

        val ashaRouteCheck = AuthorizationGuard.evaluateRouteAccess(pendingUser, PortalRoute.ASHA_DASHBOARD)
        assertFalse(ashaRouteCheck.isAllowed)
        assertTrue(ashaRouteCheck.reason.contains("Pending account approval"))

        val adminRouteCheck = AuthorizationGuard.evaluateRouteAccess(pendingUser, PortalRoute.ADMIN_DASHBOARD)
        assertFalse(adminRouteCheck.isAllowed)
    }

    // 23: Suspended user cannot access operational portal
    @Test
    fun test23_suspendedUserCannotAccessOperationalPortal() {
        val suspendResult = authRepo.suspendUser(adminUser, "asha_worker")
        assertTrue(suspendResult.isSuccess)
        val suspendedWorker = authRepo.getAccountByUsername("asha_worker")!!
        assertEquals(UserStatus.SUSPENDED, suspendedWorker.status)

        val ashaRouteCheck = AuthorizationGuard.evaluateRouteAccess(suspendedWorker, PortalRoute.ASHA_DASHBOARD)
        assertFalse(ashaRouteCheck.isAllowed)
        assertTrue(ashaRouteCheck.reason.contains("Account suspended"))

        // Reactivate for subsequent tests
        authRepo.reactivateUser(adminUser, "asha_worker")
    }

    // 24: Admin can only access authorized session records
    @Test
    fun test24_adminCanOnlyAccessAuthorizedSessionRecords() {
        val session1 = AshaScreeningSession(
            sessionId = "SES-NER-001",
            participantId = "SUB-001",
            workerId = "ASHA-NER-26004-01",
            workerName = "Anita Deka",
            centerName = "PHC Rampur - Sub-Center 04",
            mode = ProfileType.RESEARCH,
            isCompleted = true
        )
        sessionManager.addSessionToHistory(session1)

        assertTrue(AuthorizationGuard.canAccessSession(adminUser, session1))
        val fetched = adminRepo.getSessionDetail(adminUser, "SES-NER-001")
        assertNotNull(fetched)

        // Session outside authorized center / jurisdiction for a restricted worker
        val foreignSession = AshaScreeningSession(
            sessionId = "SES-NER-999-FOREIGN",
            participantId = "SUB-999",
            workerId = "FOREIGN_WORKER",
            workerName = "Unknown Worker",
            centerName = "UNAUTHORIZED_FACILITY_OVERSEAS",
            mode = ProfileType.RESEARCH,
            isCompleted = true
        )
        sessionManager.addSessionToHistory(foreignSession)

        // ASHA Worker cannot access foreign worker's session
        assertFalse(AuthorizationGuard.canAccessSession(ashaUser, foreignSession))
    }
}

package com.example.superadmin

import com.example.auth.AuthRepository
import com.example.auth.UserAccount
import com.example.auth.UserRole
import com.example.auth.UserStatus
import com.example.ui.theme.ThemeManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * SECTION 7 — SUPER ADMIN CONTROL PLANE & SCIENTIFIC GOVERNANCE TEST SUITE
 * ARTHROSCAN-NER | SIH26004 | TEAM GOD'S PLAN
 *
 * Verifies that:
 * 1. Super Admin has full administrative authority across users, deployment zones, and policies.
 * 2. Super Admin CANNOT fabricate scientific validity (e.g. increase accuracy, disable uncertainty, fake PASS).
 * 3. Parameter provenance, change history, and IRB protocol compliance are rigorously governed.
 * 4. Audit chain SHA-256 block integrity is verified tamper-evident.
 * 5. Theme toggle correctly switches between Light Mode and Dark Mode.
 */
class SuperAdminControlPlaneTest {

    private lateinit var superAdminRepo: SuperAdminRepository
    private lateinit var authRepo: AuthRepository
    private lateinit var rootSuperAdmin: UserAccount
    private lateinit var fieldAdmin: UserAccount
    private lateinit var ashaWorker: UserAccount

    @Before
    fun setUp() {
        superAdminRepo = SuperAdminRepository.getInstance()
        authRepo = AuthRepository.getInstance()

        rootSuperAdmin = authRepo.getAccountByUsername("superadmin")
            ?: UserAccount(
                id = "ROOT-001",
                username = "superadmin",
                fullName = "Dr. Root SuperAdmin",
                email = "superadmin@arthroscan.gov.in",
                role = UserRole.SUPER_ADMIN,
                status = UserStatus.ACTIVE,
                assignedCenter = "Consortium HQ",
                assignedRegion = "NER Central"
            )

        fieldAdmin = authRepo.getAccountByUsername("admin")
            ?: UserAccount(
                id = "ADM-001",
                username = "admin",
                fullName = "Field Admin",
                email = "admin@arthroscan.gov.in",
                role = UserRole.ADMIN,
                status = UserStatus.ACTIVE,
                assignedCenter = "AMCH Dibrugarh",
                assignedRegion = "Dibrugarh, Assam-NER"
            )

        ashaWorker = authRepo.getAccountByUsername("asha.anita")
            ?: UserAccount(
                id = "ASHA-001",
                username = "asha.anita",
                fullName = "Anita Deka",
                email = "anita.deka@nhm.assam.gov.in",
                role = UserRole.ASHA_WORKER,
                status = UserStatus.ACTIVE,
                assignedCenter = "PHC Rampur",
                assignedRegion = "Kamrup Rural, Assam-NER"
            )
    }

    @Test
    fun testSuperAdminRoleEnforcement_UnauthenticatedOrNonSuperAdminDenied() {
        // Null user denied
        try {
            superAdminRepo.getGovernedConfig(null)
            fail("Expected SecurityException for null caller")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("ACCESS DENIED"))
        }

        // Admin role denied from Super Admin control plane
        try {
            superAdminRepo.getGovernedConfig(fieldAdmin)
            fail("Expected SecurityException for ADMIN caller accessing Super Admin plane")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("ACCESS DENIED"))
        }

        // ASHA role denied
        try {
            superAdminRepo.getGovernedConfig(ashaWorker)
            fail("Expected SecurityException for ASHA caller")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("ACCESS DENIED"))
        }

        // Active Super Admin succeeds
        val config = superAdminRepo.getGovernedConfig(rootSuperAdmin)
        assertNotNull(config)
    }

    @Test
    fun testGovernedScientificConfig_LockedVersionAndDeterministicSeed() {
        val config = superAdminRepo.getGovernedConfig(rootSuperAdmin)
        assertEquals("v1.0-SIH26004-FROZEN", config.version)
        assertEquals(26004L, config.deterministicSeed)
        assertEquals("v1.0", config.schemaVersion)
        assertEquals(ConfigApprovalState.RATIFIED_FROZEN, config.approvalState)
        assertTrue(config.isLocked)
        assertTrue(config.author.contains("Debojit Barman"))
        assertTrue(config.irbProtocol.contains("AMCH-NER-ETH-2026-081B"))
        assertEquals(4, config.fusionWeights.size)
        assertEquals(0.35, config.fusionWeights["RF Resonance"]!!, 0.001)
        assertEquals(0.30, config.fusionWeights["VAG Acoustic Crepitus"]!!, 0.001)
    }

    @Test
    fun testFabricationAttempt_IncreaseAccuracy_BlockedByScientificGovernance() {
        // Super Admin attempts to propose "increase accuracy"
        val result = superAdminRepo.submitScientificProposal(
            user = rootSuperAdmin,
            targetVersion = "v1.1-FABRICATED",
            irbProtocolAmendment = "AMCH-NER-ETH-2026-081B-AMD",
            justification = "Attempting to increase accuracy manually to achieve desired sensitivity metric",
            diffItems = emptyList()
        )

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is IllegalArgumentException)
        assertTrue(ex!!.message!!.contains("SCIENTIFIC GOVERNANCE VIOLATION"))
        assertTrue(ex.message!!.contains("Attempt to fabricate scientific validity"))
    }

    @Test
    fun testFabricationAttempt_MarkPassOrDisableUncertainty_BlockedByScientificGovernance() {
        // Super Admin attempts to disable uncertainty
        val result = superAdminRepo.submitScientificProposal(
            user = rootSuperAdmin,
            targetVersion = "v1.2-TAMPERED",
            irbProtocolAmendment = "AMCH-NER-ETH-2026-081B-AMD",
            justification = "Disable uncertainty to speed up screening throughput and mark pass on borderline cases",
            diffItems = emptyList()
        )

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex!!.message!!.contains("SCIENTIFIC GOVERNANCE VIOLATION"))
        assertTrue(superAdminRepo.getBlockedFabricationCount() >= 1)
    }

    @Test
    fun testFormalIrbScientificProposal_ValidAmendment_AcceptedForReview() {
        val result = superAdminRepo.submitScientificProposal(
            user = rootSuperAdmin,
            targetVersion = "v1.1-EXPANSION",
            irbProtocolAmendment = "AMCH-NER-ETH-2026-081B-AMD-04",
            justification = "Formal multi-site validation expansion across 4 additional primary health centers in Dibrugarh",
            diffItems = listOf(
                ConfigDiffItem(
                    parameterKey = "vagKurtosisMin",
                    frozenValue = "4.5",
                    proposedValue = "4.6",
                    scientificJustification = "Statistically adjusted for acoustic damping in high-humidity tea garden micro-climates"
                )
            )
        )

        assertTrue(result.isSuccess)
        val proposal = result.getOrNull()!!
        assertEquals("v1.1-EXPANSION", proposal.targetVersion)
        assertEquals(ConfigApprovalState.PROPOSED_IN_REVIEW, proposal.reviewStatus)
        assertEquals("AMCH-NER-ETH-2026-081B-AMD-04", proposal.irbProtocolAmendment)
    }

    @Test
    fun testUserProvisioning_SuperAdminCreatesAdmin_SucceedsWithAudit() {
        val uniqueUser = "admin_test_${System.currentTimeMillis() % 10000}"
        val result = superAdminRepo.createAdminOrStaffUser(
            superAdmin = rootSuperAdmin,
            username = uniqueUser,
            fullName = "Dr. Test Administrator",
            email = "$uniqueUser@amch.gov.in",
            role = UserRole.ADMIN,
            initialPassword = "SecurePass2026!",
            assignedCenter = "PHC Rampur",
            assignedRegion = "Kamrup Rural, Assam-NER"
        )

        assertTrue(result.isSuccess)
        val created = result.getOrNull()!!
        assertEquals(UserRole.ADMIN, created.role)
        assertEquals(UserStatus.ACTIVE, created.status)

        // Duplicate username is rejected
        val dupResult = superAdminRepo.createAdminOrStaffUser(
            superAdmin = rootSuperAdmin,
            username = uniqueUser,
            fullName = "Duplicate Admin",
            email = "dup@amch.gov.in",
            role = UserRole.ADMIN,
            initialPassword = "SecurePass2026!",
            assignedCenter = "PHC Rampur",
            assignedRegion = "Kamrup Rural, Assam-NER"
        )
        assertTrue(dupResult.isFailure)
    }

    @Test
    fun testSelfProtection_SuperAdminCannotSuspendOwnRootAccount() {
        val result = superAdminRepo.updateUserStatus(
            superAdmin = rootSuperAdmin,
            targetUsername = rootSuperAdmin.username,
            newStatus = UserStatus.SUSPENDED
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("cannot suspend or deactivate their own active root account"))
    }

    @Test
    fun testUserStatusAndRolePromotion_SuperAdminControlsLifecycle() {
        val testUser = "worker_promo_${System.currentTimeMillis() % 10000}"
        superAdminRepo.createAdminOrStaffUser(
            superAdmin = rootSuperAdmin,
            username = testUser,
            fullName = "Promotee Worker",
            email = "$testUser@nhm.gov.in",
            role = UserRole.ASHA_WORKER,
            initialPassword = "InitialPass123!",
            assignedCenter = "PHC Chaygaon",
            assignedRegion = "Kamrup Rural, Assam-NER"
        )

        // Suspend
        val suspendResult = superAdminRepo.updateUserStatus(rootSuperAdmin, testUser, UserStatus.SUSPENDED)
        assertTrue(suspendResult.isSuccess)
        assertEquals(UserStatus.SUSPENDED, suspendResult.getOrNull()!!.status)

        // Reactivate
        val reactivateResult = superAdminRepo.updateUserStatus(rootSuperAdmin, testUser, UserStatus.ACTIVE)
        assertTrue(reactivateResult.isSuccess)
        assertEquals(UserStatus.ACTIVE, reactivateResult.getOrNull()!!.status)

        // Promote to Clinician
        val promoteResult = superAdminRepo.updateUserRole(rootSuperAdmin, testUser, UserRole.CLINICIAN)
        assertTrue(promoteResult.isSuccess)
        assertEquals(UserRole.CLINICIAN, promoteResult.getOrNull()!!.role)
    }

    @Test
    fun testDeploymentGovernance_MultiDistrictZonesAndHealthCenterRegistration() {
        val zones = superAdminRepo.getDeploymentZones(rootSuperAdmin)
        assertTrue(zones.size >= 4)
        val kamrupZone = zones.first { it.zoneId == "ZONE-NER-01" }
        assertEquals("Kamrup Rural", kamrupZone.district)
        assertEquals("Assam", kamrupZone.state)
        assertTrue(kamrupZone.healthCenters.isNotEmpty())

        // Register new sub-center
        val newCenter = HealthCenterNode(
            centerId = "HC-KMP-99",
            name = "Boko West Tea Estate Sub-Center",
            type = CenterType.SUB_CENTER,
            villageOrWard = "Boko Tea Estate",
            pinCode = "781123",
            activeWorkersCount = 2,
            assignedHardwareUnits = 1
        )
        val regResult = superAdminRepo.addHealthCenterToZone(rootSuperAdmin, "ZONE-NER-01", newCenter)
        assertTrue(regResult.isSuccess)

        val updatedZones = superAdminRepo.getDeploymentZones(rootSuperAdmin)
        val updatedKamrup = updatedZones.first { it.zoneId == "ZONE-NER-01" }
        assertTrue(updatedKamrup.healthCenters.any { it.centerId == "HC-KMP-99" })
    }

    @Test
    fun testSystemSecurityPolicy_SessionTimeoutAndBiometricExport() {
        val initialPolicy = superAdminRepo.getSecurityPolicy(rootSuperAdmin)
        assertEquals(30, initialPolicy.sessionTimeoutMinutes)
        assertTrue(initialPolicy.enforceBiometricForExport)
        assertTrue(initialPolicy.localAes256GcmEncrypted)

        val updated = initialPolicy.copy(sessionTimeoutMinutes = 15, enforceBiometricForExport = false)
        val res = superAdminRepo.updateSecurityPolicy(rootSuperAdmin, updated)
        assertTrue(res.isSuccess)
        assertEquals(15, superAdminRepo.getSecurityPolicy(rootSuperAdmin).sessionTimeoutMinutes)
        assertFalse(superAdminRepo.getSecurityPolicy(rootSuperAdmin).enforceBiometricForExport)
    }

    @Test
    fun testGlobalEmergencyMaintenanceLock_EngagementAndRelease() {
        val engageRes = superAdminRepo.toggleMaintenanceLock(
            superAdmin = rootSuperAdmin,
            lock = true,
            reason = "Mandatory IMU firmware patch deployment"
        )
        assertTrue(engageRes.isSuccess)
        assertTrue(superAdminRepo.getSecurityPolicy(rootSuperAdmin).globalMaintenanceLock)
        assertEquals("Mandatory IMU firmware patch deployment", superAdminRepo.getSecurityPolicy(rootSuperAdmin).maintenanceReason)

        val releaseRes = superAdminRepo.toggleMaintenanceLock(
            superAdmin = rootSuperAdmin,
            lock = false,
            reason = ""
        )
        assertTrue(releaseRes.isSuccess)
        assertFalse(superAdminRepo.getSecurityPolicy(rootSuperAdmin).globalMaintenanceLock)
    }

    @Test
    fun testAuditChainVerification_Sha256TamperEvidentProof() {
        val report = superAdminRepo.verifyAuditChainIntegrity(rootSuperAdmin)
        assertTrue(report.chainIntegrityPass)
        assertTrue(report.totalAuditRecords > 0)
        assertNotNull(report.genesisBlockHash)
        assertNotNull(report.latestBlockHash)
        assertTrue(report.icmrBioethicsCompliant)
        assertTrue(report.dpdpActCompliant)
        assertTrue(report.fdaCad2021Adherence)
    }

    @Test
    fun testThemeManager_ToggleLightAndDarkMode() {
        // Check default theme state
        val initialMode = ThemeManager.isDarkMode.value
        ThemeManager.toggleTheme()
        assertEquals(!initialMode, ThemeManager.isDarkMode.value)

        // Set explicit mode
        ThemeManager.setDarkMode(false)
        assertFalse(ThemeManager.isDarkMode.value)

        ThemeManager.setDarkMode(true)
        assertTrue(ThemeManager.isDarkMode.value)

        // Reset to default (Light mode)
        ThemeManager.setDarkMode(false)
        assertFalse(ThemeManager.isDarkMode.value)
    }

    @Test
    fun testSuperAdminKpis_CalculationMatchesRegisteredEntities() {
        val kpis = superAdminRepo.computeSuperAdminKpis(rootSuperAdmin)
        assertTrue(kpis.totalRegisteredUsers > 0)
        assertTrue(kpis.totalAdmins > 0)
        assertTrue(kpis.totalAshaWorkers > 0)
        assertTrue(kpis.totalDeploymentZones >= 4)
        assertTrue(kpis.totalHealthCenters >= 10)
        assertTrue(kpis.activeHardwareFleet > 0)
        assertTrue(kpis.totalAuditEventsLogged > 0)
        assertEquals("v1.0-SIH26004-FROZEN", kpis.governedConfigVersion)
    }
}

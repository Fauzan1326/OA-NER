package com.example.ui

import com.example.auth.AuthRepository
import com.example.auth.AuthorizationGuard
import com.example.auth.PortalRoute
import com.example.auth.UserRole
import com.example.portal.AshaSessionManager
import com.example.ui.theme.ThemeManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * UNIT TESTS FOR LOGOUT, NAVIGATION & THEME ARCHITECTURE
 * ARTHROSCAN-NER | SIH26004
 */
class PortalLogoutAndNavigationTest {

    private lateinit var authRepo: AuthRepository
    private lateinit var sessionManager: AshaSessionManager

    @Before
    fun setUp() {
        authRepo = AuthRepository()
        sessionManager = AshaSessionManager()
    }

    @Test
    fun test01_superAdminLoginAndRealLogout() {
        val loginResult = authRepo.login("superadmin", "superadmin@123")
        assertTrue(loginResult.isSuccess)
        val user = authRepo.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.SUPER_ADMIN, user?.role)

        // Perform real logout
        sessionManager.resetSession()
        authRepo.logout()

        assertNull("Current user must be null after logout", authRepo.currentUser.value)
        assertNull("Active screening session must be null after reset", sessionManager.activeSession.value)
    }

    @Test
    fun test02_adminLoginAndRealLogout() {
        val loginResult = authRepo.login("admin", "admin@123")
        assertTrue(loginResult.isSuccess)
        val user = authRepo.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.ADMIN, user?.role)

        // Perform real logout
        sessionManager.resetSession()
        authRepo.logout()

        assertNull("Current user must be null after logout", authRepo.currentUser.value)
    }

    @Test
    fun test03_ashaWorkerLoginAndRealLogout() {
        val loginResult = authRepo.login("asha_priya", "AshaWorker2026!")
        assertTrue(loginResult.isSuccess)
        val user = authRepo.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.ASHA_WORKER, user?.role)

        // Start session
        sessionManager.startNewSession(user!!, com.example.core.config.ProfileType.DEMO, "PART-123")
        assertNotNull(sessionManager.activeSession.value)

        // Perform real logout
        sessionManager.resetSession()
        authRepo.logout()

        assertNull("Current user must be null after logout", authRepo.currentUser.value)
        assertNull("Session must be reset on logout", sessionManager.activeSession.value)
    }

    @Test
    fun test04_themeManagerToggle() {
        val initialMode = ThemeManager.isDarkMode.value
        ThemeManager.toggleTheme()
        assertNotEquals(initialMode, ThemeManager.isDarkMode.value)
        ThemeManager.toggleTheme()
        assertEquals(initialMode, ThemeManager.isDarkMode.value)
    }

    @Test
    fun test05_authorizationBlockedWhenLoggedOut() {
        authRepo.logout()
        val user = authRepo.currentUser.value
        assertNull(user)
        assertFalse(AuthorizationGuard.canAccessRoute(user, PortalRoute.ADMIN_DASHBOARD))
        assertFalse(AuthorizationGuard.canAccessRoute(user, PortalRoute.SUPER_ADMIN_DASHBOARD))
    }

    @Test
    fun test06_patientLoginAndLogout() {
        val loginResult = authRepo.login("patient.user", "asha@123")
        assertTrue(loginResult.isSuccess)
        val user = authRepo.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.PATIENT, user?.role)

        sessionManager.resetSession()
        authRepo.logout()
        assertNull("Current user must be null after logout", authRepo.currentUser.value)
    }

    @Test
    fun test07_patientRegistrationAndDirectAccessSession() {
        val reg = authRepo.registerPatientUser(
            username = "citizen.baruah",
            fullName = "Bhaben Baruah",
            email = "bhaben@patient.gov.in",
            password = "SecurePass#2026"
        )
        assertTrue(reg.isSuccess)
        val patient = reg.getOrThrow()
        assertEquals(UserRole.PATIENT, patient.role)
        assertEquals(com.example.auth.AccountState.PATIENT_ACTIVE, patient.accountState)

        // Set as current user upon registration
        authRepo.setCurrentUser(patient)
        assertEquals(patient, authRepo.currentUser.value)

        // Patient can start screening session
        sessionManager.startNewSession(patient, com.example.core.config.ProfileType.DEMO, "PAT-BARUAH-01")
        assertNotNull(sessionManager.activeSession.value)

        // Logout
        sessionManager.resetSession()
        authRepo.logout()
        assertNull(authRepo.currentUser.value)
    }

    @Test
    fun test08_ashaRegistrationPendingApprovalAndSubsequentApprovalLogin() {
        val reg = authRepo.registerAshaWorker(
            username = "asha.manju",
            fullName = "Manju Nath",
            email = "manju.n@nhm.assam.gov.in",
            password = "AshaPassword#2026"
        )
        assertTrue(reg.isSuccess)
        val asha = reg.getOrThrow()
        assertEquals(UserRole.ASHA_WORKER, asha.role)
        assertEquals(com.example.auth.AccountState.ASHA_PENDING, asha.accountState)

        // Blocked before admin approval
        val blocked = authRepo.login("asha.manju", "AshaPassword#2026")
        assertTrue(blocked.isFailure)
        assertTrue(blocked.exceptionOrNull()?.message?.contains("Your account is awaiting administrator approval") == true)

        // Admin approves
        val admin = authRepo.getAccountByUsername("admin")!!
        val approved = authRepo.approveOrRejectUser(admin, "asha.manju", com.example.auth.UserStatus.ACTIVE)
        assertTrue(approved.isSuccess)

        // Now login succeeds
        val loginSuccess = authRepo.login("asha.manju", "AshaPassword#2026")
        assertTrue(loginSuccess.isSuccess)
        assertEquals(com.example.auth.AccountState.ASHA_APPROVED, loginSuccess.getOrThrow().accountState)
    }
}

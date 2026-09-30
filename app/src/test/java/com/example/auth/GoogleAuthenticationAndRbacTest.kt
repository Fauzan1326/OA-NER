package com.example.auth

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GOOGLE AUTHENTICATION & RBAC DIRECTORY TESTS
 * ARTHROSCAN-NER | SIH26004 | SECTION 1, 3, 4
 */
class GoogleAuthenticationAndRbacTest {

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        authRepository = AuthRepository()
    }

    @Test
    fun test01_initialStartupStateIsUnauthenticated() {
        assertNull("Startup state must be null to present login screen", authRepository.currentUser.value)
    }

    @Test
    fun test02_googleAuthResolvesAshaWorker() {
        val result = authRepository.loginWithGoogleEmail("anita.deka@nhm.assam.gov.in")
        assertTrue("Authorized ASHA Worker Google account must succeed", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.ASHA_WORKER, user?.role)
        assertEquals("Anita Deka", user?.fullName)
        assertEquals(authRepository.currentUser.value, user)
    }

    @Test
    fun test03_googleAuthResolvesAdminPreservingRbac() {
        val result = authRepository.loginWithGoogleEmail("dho.kamrup@nhm.assam.gov.in")
        assertTrue("Authorized Admin Google account must succeed", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.ADMIN, user?.role)
        assertEquals(authRepository.currentUser.value, user)
    }

    @Test
    fun test04_googleAuthResolvesSuperAdminPreservingRbac() {
        val result = authRepository.loginWithGoogleEmail("statelead@nhm.assam.gov.in")
        assertTrue("Authorized Super Admin Google account must succeed", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.SUPER_ADMIN, user?.role)
        assertEquals(authRepository.currentUser.value, user)
    }

    @Test
    fun test05_googleAuthRejectsUnauthorizedAccount() {
        val result = authRepository.loginWithGoogleEmail("unregistered.intruder@gmail.com")
        assertTrue("Unauthorized Google account must fail", result.isFailure)
        val message = result.exceptionOrNull()?.message
        assertTrue(
            "Error must specify account not authorized and contact administrator",
            message?.contains("Account not authorized", ignoreCase = true) == true &&
                    message?.contains("Contact your administrator", ignoreCase = true) == true
        )
        assertNull("Current user must remain null on failed auth", authRepository.currentUser.value)
    }

    @Test
    fun test06_googleAuthRejectsPendingApprovalAccount() {
        val result = authRepository.loginWithGoogleEmail("sunita.b@nhm.assam.gov.in")
        assertTrue("Pending approval Google account must fail", result.isFailure)
        val message = result.exceptionOrNull()?.message
        assertTrue(message?.contains("pending supervisor approval", ignoreCase = true) == true)
    }

    @Test
    fun test07_logoutCompletelyClearsUserSession() {
        // Log in
        authRepository.login("asha.anita", "asha@123")
        assertNotNull(authRepository.currentUser.value)

        // Perform logout
        authRepository.logout()
        assertNull("Current user must be null after logout", authRepository.currentUser.value)
    }
}

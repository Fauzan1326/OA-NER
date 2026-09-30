package com.example.ui

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import android.graphics.Bitmap
import com.example.auth.AuthRepository
import com.example.auth.PortalRoute
import com.example.auth.UserRole
import com.example.ui.components.UserAvatarStorage
import com.example.ui.superadmin.SuperAdminTab
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdminSuperAdminProfileAndUiTest {

    @Test
    fun testUserAvatarStorage_saveLoadRemove() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val username = "testadmin"

        // Create a 10x10 test bitmap
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        UserAvatarStorage.saveAvatar(context, username, bitmap)

        val loaded = UserAvatarStorage.loadAvatar(context, username)
        assertNotNull("Saved avatar should be readable", loaded)
        assertEquals(10, loaded?.width)
        assertEquals(10, loaded?.height)

        UserAvatarStorage.removeAvatar(context, username)
        val afterRemove = UserAvatarStorage.loadAvatar(context, username)
        assertNull("Avatar should be null after removal", afterRemove)
    }

    @Test
    fun testSuperAdminTabs_includesProfile() {
        val tabs = SuperAdminTab.values()
        assertTrue("SuperAdminTab must contain PROFILE tab", tabs.any { it == SuperAdminTab.PROFILE })
        assertEquals("Profile", SuperAdminTab.PROFILE.label)
    }

    @Test
    fun testPortalRoutes_adminProfileRouteExists() {
        assertEquals("ADMIN_PROFILE", PortalRoute.ADMIN_PROFILE.name)
        assertEquals("/admin/profile", PortalRoute.ADMIN_PROFILE.path)
    }

    @Test
    fun testAdminAndSuperAdminRoles() {
        val authRepo = AuthRepository.getInstance()
        val admin = authRepo.getAccountByUsername("admin")
        val superAdmin = authRepo.getAccountByUsername("superadmin")

        assertNotNull(admin)
        assertNotNull(superAdmin)
        assertEquals(UserRole.ADMIN, admin?.role)
        assertEquals(UserRole.SUPER_ADMIN, superAdmin?.role)
    }
}

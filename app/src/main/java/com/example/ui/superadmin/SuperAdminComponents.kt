package com.example.ui.superadmin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.auth.UserAccount
import com.example.ui.components.UserAvatarStorage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.ThemeManager

// ============================================================
// DYNAMIC THEME COLORS FOR SUPER ADMIN (LIGHT / DARK MODES)
// ============================================================

val SaDarkBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF040608) else Color(0xFFF8FAFC)
val SaSurfaceBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF090D12) else Color(0xFFFFFFFF)
val SaCardBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF101721) else Color(0xFFFFFFFF)
val SaActiveBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF162436) else Color(0xFFEFF6FF)
val SaBorder @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1F2E40) else Color(0xFFE2E8F0)
val SaTextPrimary @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFFF1F5F9) else Color(0xFF0F172A)
val SaTextSecondary @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF94A3B8) else Color(0xFF64748B)
val SaTextMuted @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF64748B) else Color(0xFF94A3B8)

// Status / Accent Colors (Adhering to Section 9)
val SaCyan = Color(0xFF0891B2)
val SaBlue = Color(0xFF2563EB)
val SaGreen = Color(0xFF059669)
val SaPurple = Color(0xFF7C3AED)
val SaGold = Color(0xFFD97706)
val SaAmber = Color(0xFFD97706)
val SaRed = Color(0xFFDC2626)

/**
 * Modern, responsive Super Admin Top App Bar with ASHA Design Language.
 * LEFT: Biotech / Security Icon, ARTHROSCAN-NER + SIH26004 + SUPER ADMIN badges
 * SUBTITLE: System & Scientific Governance • Root Level 4
 * RIGHT: Theme toggle + Diagnostics + Logout + Clickable Profile Avatar
 */
@Composable
fun SuperAdminTopAppBar(
    superAdmin: UserAccount,
    onOpenDiagnostics: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDark = ThemeManager.isDarkMode.value
    val avatarBitmap = remember(superAdmin.username) {
        UserAvatarStorage.loadAvatar(context, superAdmin.username)
    }

    Surface(
        color = SaSurfaceBg,
        border = BorderStroke(1.dp, SaBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Icon + Titles & Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaPurple.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ARTHROSCAN-NER",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = (-0.1).sp,
                                color = SaTextPrimary
                            ),
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            border = BorderStroke(0.5.dp, SaBorder)
                        ) {
                            Text(
                                text = "SIH26004",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = SaTextSecondary
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SaPurple.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, SaPurple.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "SUPER ADMIN",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = SaPurple
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    Text(
                        text = "System & Scientific Governance • Root Level 4",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            color = SaTextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // RIGHT: Theme toggle, Diagnostics, Logout, Clickable Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { ThemeManager.toggleTheme() },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Light/Dark Theme",
                        tint = if (isDark) SaGold else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "System Diagnostics",
                        tint = SaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("superadmin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = SaRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Profile Avatar Photo
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap!!.asImageBitmap(),
                        contentDescription = "Super Admin Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, SaPurple, CircleShape)
                            .clickable { onOpenProfile() }
                            .testTag("superadmin_profile_button")
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = SaPurple,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onOpenProfile() }
                            .testTag("superadmin_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initials = superAdmin.fullName.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .ifBlank { "SA" }
                            Text(
                                text = initials,
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 4: Compact Governance Sub-Banner
 * Example: SUPER ADMIN • SCIENTIFIC GOVERNANCE | Protocol: AMCH-NER-ETH-2026-081B
 */
@Composable
fun SuperAdminCompactBanner() {
    Surface(
        color = SaCardBg,
        border = BorderStroke(0.5.dp, SaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUPER ADMIN • SCIENTIFIC GOVERNANCE",
                    color = SaPurple,
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = SaPurple.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(3.dp),
                    border = BorderStroke(1.dp, SaPurple.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "SECURE CONSOLE",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        color = SaPurple,
                        fontSize = 8.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "Protocol: AMCH-NER-ETH-2026-081B • Deterministic Seed: 26004",
                color = SaTextSecondary,
                fontSize = 9.sp,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun SuperAdminPersistentBanners() {
    SuperAdminCompactBanner()
}

@Composable
fun SuperAdminHeaderBar(
    superAdmin: UserAccount,
    onOpenDiagnostics: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit
) {
    SuperAdminTopAppBar(
        superAdmin = superAdmin,
        onOpenDiagnostics = onOpenDiagnostics,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout
    )
}

@Composable
fun SuperAdminStatusBadge(label: String, value: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = tint.copy(alpha = if (ThemeManager.isDarkMode.value) 0.15f else 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                color = SaTextPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun SuperAdminSectionHeader(
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = SaCyan
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = SoraFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaTextPrimary,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            color = SaTextSecondary
        )
    }
}

/**
 * Cryptographic Audit Chain Card conforming to Section 3.
 * AUDIT CHAIN
 * Integrity: VERIFIED
 * Records: 1234
 * Latest Hash: xxxxxxxxx...
 * Status: Tamper Evident
 */
@Composable
fun SuperAdminAuditChainCard(
    integrityStatus: String = "VERIFIED",
    totalRecords: Int = 1234,
    latestHash: String = "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
    tamperStatus: String = "Tamper Evident",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SaCardBg,
        border = BorderStroke(1.dp, SaBorder),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SaGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AUDIT CHAIN",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaTextPrimary
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SaGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SaGreen.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = integrityStatus,
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

            HorizontalDivider(color = SaBorder, thickness = 0.7.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Integrity:",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = SaTextSecondary
                    )
                )
                Text(
                    text = integrityStatus,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SaGreen
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Records:",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = SaTextSecondary
                    )
                )
                Text(
                    text = "$totalRecords",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SaTextPrimary
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latest Hash:",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = SaTextSecondary
                    )
                )
                Text(
                    text = if (latestHash.length > 20) "${latestHash.take(16)}..." else latestHash,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp,
                        color = SaCyan
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = SaTextSecondary
                    )
                )
                Text(
                    text = tamperStatus,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SaGreen
                    )
                )
            }
        }
    }
}

/**
 * PRODUCTION SUPER ADMINISTRATOR PROFILE SCREEN
 * ARTHROSCAN-NER | Authoritative Root Scientific Governance & Security Identity
 */
@Composable
fun SuperAdminProfileScreen(
    superAdmin: UserAccount,
    onLogout: () -> Unit,
    onSwitchToOperationalAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    var avatarBitmap by remember(superAdmin.username) {
        mutableStateOf(UserAvatarStorage.loadAvatar(context, superAdmin.username))
    }
    var showPhotoModal by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            UserAvatarStorage.saveAvatar(context, superAdmin.username, bitmap)
            avatarBitmap = bitmap
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        UserAvatarStorage.saveAvatar(context, superAdmin.username, bitmap)
                        avatarBitmap = bitmap
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SaDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "SUPER ADMINISTRATOR PROFILE",
                    style = TextStyle(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        color = SaTextPrimary
                    )
                )
                Text(
                    text = "Scientific Governance, Root Cryptographic Authority & Security Vault",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = SaTextSecondary
                    )
                )
            }
        }

        // Section 2: Profile Hero Card with Interactive Photo
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Clickable Avatar with Camera Overlay
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clickable { showPhotoModal = true }
                                .testTag("superadmin_profile_photo_button")
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap!!.asImageBitmap(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, SaPurple, CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = SaPurple,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initials = superAdmin.fullName.split(" ")
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .take(2)
                                            .joinToString("")
                                            .ifBlank { "SA" }
                                        Text(
                                            text = initials,
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            // Camera edit badge overlay
                            Surface(
                                shape = CircleShape,
                                color = SaPurple,
                                border = BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Identity Info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = superAdmin.fullName,
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.2).sp,
                                    color = SaTextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "@${superAdmin.username}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = SaPurple
                                )
                            )
                            Text(
                                text = superAdmin.email,
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    color = SaTextSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                            fontSize = 9.sp,
                                            color = SaPurple
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = SaGreen.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, SaGreen.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(SaGreen)
                                        )
                                        Text(
                                            text = "VERIFIED",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = SaGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Scientific Governance & Root Security Clearance
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SaPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ROOT GOVERNANCE & JURISDICTION",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.5.sp,
                                color = SaTextSecondary
                            )
                        )
                    }

                    SaProfileItemRow("Account ID", superAdmin.id, isMono = true)
                    HorizontalDivider(color = SaBorder, thickness = 0.7.dp)
                    SaProfileItemRow("Official Role", "Super Administrator / Scientific Authority")
                    HorizontalDivider(color = SaBorder, thickness = 0.7.dp)
                    SaProfileItemRow("Authority Level", "Root Level 4 (Highest Clearance)")
                    HorizontalDivider(color = SaBorder, thickness = 0.7.dp)
                    SaProfileItemRow("Deterministic Seed", "26004L • Cryptographically Locked", isMono = true)
                    HorizontalDivider(color = SaBorder, thickness = 0.7.dp)
                    SaProfileItemRow("Protocol Consensus", "AMCH-NER-ETH-2026-081B", isMono = true)
                    HorizontalDivider(color = SaBorder, thickness = 0.7.dp)
                    SaProfileItemRow("Global Jurisdiction", "All NER Zones (Assam, Meghalaya, Tripura, Mizoram)")
                }
            }
        }

        // Section 4: Root Privilege & Governance Matrix
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = SaPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ROOT PRIVILEGES & GOVERNANCE MATRIX",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.5.sp,
                                color = SaTextSecondary
                            )
                        )
                    }

                    SaGrantRow("Consortium Scientific Governance & Version Freezing", true)
                    SaGrantRow("Master Keystore AES-256-GCM Cryptographic Seed Management", true)
                    SaGrantRow("Multi-Zone Regional Health Center Provisioning & Gating", true)
                    SaGrantRow("Cryptographic Tamper-Proof Chain Audit & Invariant Enforcement", true)
                    SaGrantRow("Global Recall & Emergency System Quarantine Lockout", true)
                    SaGrantRow("Direct Database Mutation Without Audit Trail", false)
                }
            }
        }

        // Section 5: Cryptographic Provenance & Security Trace
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SaPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SYSTEM AUDIT & PROVENANCE",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.5.sp,
                                    color = SaTextSecondary
                                )
                            )
                        }
                        Text(
                            text = "Node: NER-ROOT-01",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = SaTextSecondary
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (ThemeManager.isDarkMode.value) Color(0xFF0F1E30) else Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ROOT TRACE HASH (SHA-256)",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.5.sp,
                                    color = SaTextSecondary
                                )
                            )
                            Text(
                                text = "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = SaTextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSwitchToOperationalAdmin,
                    border = BorderStroke(1.dp, SaBlue.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SaBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "OPEN OPERATIONAL ADMIN CENTER",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SaBlue
                            )
                        )
                    }
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = SaRed),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("superadmin_logout_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "LOGOUT OF ROOT SESSION",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }

    // Photo Selection Dialog
    if (showPhotoModal) {
        Dialog(onDismissRequest = { showPhotoModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SaCardBg,
                border = BorderStroke(1.dp, SaBorder),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SUPER ADMINISTRATOR PROFILE PHOTO",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SaTextPrimary
                        )
                    )
                    Text(
                        text = "Choose a photo source or remove the existing profile avatar.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            color = SaTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            showPhotoModal = false
                            cameraLauncher.launch(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TAKE PHOTO WITH CAMERA", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showPhotoModal = false
                            galleryLauncher.launch("image/*")
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = SaTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CHOOSE FROM GALLERY", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SaTextPrimary)
                    }

                    if (avatarBitmap != null) {
                        OutlinedButton(
                            onClick = {
                                UserAvatarStorage.removeAvatar(context, superAdmin.username)
                                avatarBitmap = null
                                showPhotoModal = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SaRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = SaRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REMOVE PHOTO", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SaRed)
                        }
                    }

                    TextButton(
                        onClick = { showPhotoModal = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CANCEL", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SaTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaProfileItemRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                color = SaTextSecondary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = if (isMono) JetBrainsMonoFontFamily else SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = SaTextPrimary
            )
        )
    }
}

@Composable
private fun SaGrantRow(text: String, isAllowed: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isAllowed) SaGreen.copy(alpha = 0.08f) else SaRed.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, if (isAllowed) SaGreen.copy(alpha = 0.25f) else SaRed.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isAllowed) SaGreen else SaRed,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.5.sp,
                    color = SaTextPrimary
                )
            )
        }
    }
}

package com.example.ui.admin

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.DeviceConnectionStatus
import com.example.auth.UserAccount
import com.example.ui.components.UserAvatarStorage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.ThemeManager

// ============================================================
// ADMIN THEME PALETTE (LIGHT-FIRST ADAPTIVE WITH FULL DARK MODE)
// ============================================================
val AdminDarkBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF05070A) else Color(0xFFF8FAFC)
val AdminSurfaceBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF0B1015) else Color(0xFFFFFFFF)
val AdminCardBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF171E26) else Color(0xFFFFFFFF)
val AdminActiveBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF0D1E33) else Color(0xFFEFF6FF)
val AdminRfBg @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E142B) else Color(0xFFF3E8FF)
val AdminBorder @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF222E3C) else Color(0xFFE2E8F0)

val AdminCyan = Color(0xFF0891B2)
val AdminBlue = Color(0xFF2563EB)
val AdminGreen = Color(0xFF059669)
val AdminPurple = Color(0xFF7C3AED)
val AdminAmber = Color(0xFFD97706)
val AdminRed = Color(0xFFDC2626)

val AdminTextDim @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF8A9BA8) else Color(0xFF64748B)
val AdminTextMain @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFFF8FAFC) else Color(0xFF0F172A)

/**
 * Modern, responsive Admin Top App Bar with ASHA Design Language.
 * Includes Left Biotech Icon, Title, Subtitle, SIH26004 & ADMIN badges,
 * Theme Toggle, Diagnostics, Logout, and Clickable Profile Photo.
 */
@Composable
fun AdminTopAppBar(
    admin: UserAccount,
    onOpenDiagnostics: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDark = ThemeManager.isDarkMode.value
    val avatarBitmap = remember(admin.username) {
        UserAvatarStorage.loadAvatar(context, admin.username)
    }

    Surface(
        color = AdminSurfaceBg,
        border = BorderStroke(1.dp, AdminBorder),
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
                    color = AdminBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = AdminBlue,
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
                                color = AdminTextMain
                            ),
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            border = BorderStroke(0.5.dp, AdminBorder)
                        ) {
                            Text(
                                text = "SIH26004",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = AdminTextDim
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AdminBlue.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, AdminBlue.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "ADMIN",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = AdminBlue
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    Text(
                        text = "District Administration • Operational Control",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            color = AdminTextDim
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
                        tint = if (isDark) AdminAmber else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Technical Diagnostics",
                        tint = AdminCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = AdminRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Profile Avatar Photo
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = "Admin Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, AdminBlue, CircleShape)
                            .clickable { onOpenProfile() }
                            .testTag("admin_profile_button")
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = AdminBlue,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onOpenProfile() }
                            .testTag("admin_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initials = admin.fullName.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .ifBlank { "AD" }
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
 * Compact Operational Telemetry Sub-Bar
 */
@Composable
fun AdminCompactSubBar(admin: UserAccount) {
    Surface(
        color = AdminSurfaceBg,
        border = BorderStroke(0.5.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminStatusBadge(label = "ADMIN", value = admin.username, tint = AdminBlue)
            AdminStatusBadge(label = "CENTER", value = admin.assignedCenter, tint = AdminCyan)
            AdminStatusBadge(label = "STATUS", value = "ONLINE", tint = AdminGreen)
            AdminStatusBadge(label = "SYNC", value = "REALTIME", tint = AdminGreen)
        }
    }
}

@Composable
fun AdminPersistentBanners() {
    AdminCompactSubBar(
        admin = UserAccount(
            id = "ADMIN-NER-01",
            username = "admin.dho",
            fullName = "Admin DHO",
            email = "admin@nhm.assam.gov.in",
            role = com.example.auth.UserRole.ADMIN,
            status = com.example.auth.UserStatus.ACTIVE,
            assignedCenter = "District Health Office",
            assignedRegion = "Assam-NER"
        )
    )
}

@Composable
fun AdminHeaderBar(
    admin: UserAccount,
    onOpenDiagnostics: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit
) {
    AdminTopAppBar(
        admin = admin,
        onOpenDiagnostics = onOpenDiagnostics,
        onOpenProfile = onOpenProfile,
        onLogout = onLogout
    )
}

@Composable
fun AdminStatusBadge(
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = tint.copy(alpha = if (ThemeManager.isDarkMode.value) 0.15f else 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                color = AdminTextDim,
                fontSize = 10.sp,
                fontFamily = SpaceGroteskFontFamily,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = value,
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGroteskFontFamily,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun AdminKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtext: String? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AdminCardBg,
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.4.sp,
                        color = AdminTextDim
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = value,
                style = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AdminTextMain
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtext != null) {
                Text(
                    text = subtext,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.5.sp,
                        color = AdminTextDim
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Standard Responsive Audit Chain Card.
 * Complies with Section 3:
 * AUDIT CHAIN
 * Integrity: VERIFIED
 * Records: 1234
 * Latest Hash: xxxxxxxxx...
 * Status: Tamper Evident
 */
@Composable
fun AuditChainCard(
    integrityStatus: String = "VERIFIED",
    totalRecords: Int = 1234,
    latestHash: String = "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
    tamperStatus: String = "Tamper Evident",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AdminCardBg,
        border = BorderStroke(1.dp, AdminBorder),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = AdminGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AUDIT CHAIN",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminTextMain
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AdminGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AdminGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = integrityStatus,
                        color = AdminGreen,
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Integrity:",
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    color = AdminTextDim
                )
                Text(
                    text = integrityStatus,
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AdminGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Records:",
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    color = AdminTextDim
                )
                Text(
                    text = "$totalRecords",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AdminTextMain
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latest Hash:",
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    color = AdminTextDim
                )
                Text(
                    text = if (latestHash.length > 20) "${latestHash.take(16)}..." else latestHash,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = AdminCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    color = AdminTextDim
                )
                Text(
                    text = tamperStatus,
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AdminGreen
                )
            }
        }
    }
}

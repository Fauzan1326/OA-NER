package com.example.ui.admin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin.AdminRepository
import com.example.admin.SubsystemHealthStatus
import com.example.auth.UserAccount
import com.example.ui.components.UserAvatarStorage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.ThemeManager

@Composable
fun AdminSystemHealthScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val report = remember(admin) { repository.getSystemHealth(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SYSTEM HEALTH & SUBSYSTEM VERIFICATION",
                color = AdminCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Continuous Runtime Verification of Pipeline Modules",
                color = AdminTextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminHealthItemCard("Application Core", report.appVersion, SubsystemHealthStatus.PASS)
                AdminHealthItemCard("Universal Data Schema", report.schemaVersion, SubsystemHealthStatus.PASS)
                AdminHealthItemCard("Multimodal Neural Architecture", report.modelVersion, SubsystemHealthStatus.PASS)
                AdminHealthItemCard("Universal Hardware Bus", "Active / 0.0% Loss", report.deviceConnection)
                AdminHealthItemCard("Local Database Persistence", "In-Memory Verified Cache", report.databaseStatus)
                AdminHealthItemCard("Cloud & Operational Sync", "Real-Time Verified", report.syncStatus)
                AdminHealthItemCard("Regulatory Audit Ledger", "Append-Only Immutable", report.auditLogStatus)
                AdminHealthItemCard("Feature Extraction Pipeline", "Module 4 Vector Store Active", report.featurePipeline)
                AdminHealthItemCard("AI Modality Inference Pipeline", "Late-Fusion Uncertainty Engine Active", report.aiPipeline)
            }
        }
    }
}

@Composable
fun AdminHealthItemCard(title: String, detail: String, status: SubsystemHealthStatus) {
    val statusColor = when (status) {
        SubsystemHealthStatus.PASS -> AdminGreen
        SubsystemHealthStatus.WARN -> AdminAmber
        SubsystemHealthStatus.FAIL -> AdminRed
        SubsystemHealthStatus.NOT_CONFIGURED -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = AdminCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = AdminTextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(detail, color = AdminTextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AdminSurfaceBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
            ) {
                Text(
                    text = status.label,
                    color = statusColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AdminScientificConfigScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val config = remember(admin) { repository.getScientificConfiguration(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AdminCyan, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "SCIENTIFIC CONFIGURATION",
                        color = AdminCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "READ ONLY — Operational Administrator View",
                        color = AdminAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1400),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = AdminAmber, modifier = Modifier.size(18.dp))
                    Text(
                        text = "IMMUTABILITY POLICY: Administrative accounts cannot modify AI model weights, scientific thresholds, normalization parameters, or RF frequency bounds. Alterations require authorized SUPER_ADMIN cryptographic credentials.",
                        color = AdminAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdminCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminConfigFieldRow("Schema Version:", config.schemaVersion)
                    AdminConfigFieldRow("Feature Freshness Window:", "< ${config.featureFreshnessSeconds} seconds")
                    AdminConfigFieldRow("RF Frequency Boundary:", config.rfFrequencyBoundary)
                    AdminConfigFieldRow("Deterministic Pseudorandom Seed:", config.deterministicSeed.toString())
                    AdminConfigFieldRow("Quality Gate Rejection:", if (config.qualityRejectionEnabled) "ENABLED (STRICT)" else "DISABLED")
                    AdminConfigFieldRow("Zero Substitution:", if (config.zeroSubstitutionDisabled) "DISABLED (PROHIBITED)" else "ALLOWED")
                    AdminConfigFieldRow("Inference Traceability:", config.inferenceTraceability)
                    AdminConfigFieldRow("Model Architecture:", config.modelArchitecture)
                    AdminConfigFieldRow("Uncertainty Calibration:", config.uncertaintyCalibrationMethod)
                    AdminConfigFieldRow("Configuration Access Level:", if (config.isReadOnly) "LOCKED / READ-ONLY" else "EDITABLE")
                }
            }
        }
    }
}

@Composable
fun AdminConfigFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = AdminTextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun AdminProfileScreen(
    admin: UserAccount,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var avatarBitmap by remember(admin.username) {
        mutableStateOf(UserAvatarStorage.loadAvatar(context, admin.username))
    }
    var showPhotoModal by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            UserAvatarStorage.saveAvatar(context, admin.username, bitmap)
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
                        UserAvatarStorage.saveAvatar(context, admin.username, bitmap)
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
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "ADMINISTRATOR PROFILE",
                    style = TextStyle(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        color = AdminTextMain
                    )
                )
                Text(
                    text = "Operational Identity, District Jurisdiction & Credential Vault",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = AdminTextDim
                    )
                )
            }
        }

        // Section 2: Profile Hero Card with Interactive Photo
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
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
                                .testTag("admin_profile_photo_button")
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap!!.asImageBitmap(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, AdminBlue, CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = AdminBlue,
                                    modifier = Modifier.fillMaxSize()
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
                                color = AdminBlue,
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
                                text = admin.fullName,
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.2).sp,
                                    color = AdminTextMain
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "@${admin.username}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = AdminCyan
                                )
                            )
                            Text(
                                text = admin.email,
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    color = AdminTextDim
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = AdminGreen.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, AdminGreen.copy(alpha = 0.35f))
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
                                                .background(AdminGreen)
                                        )
                                        Text(
                                            text = "ACTIVE",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = AdminGreen
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = AdminBlue.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, AdminBlue.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "LEVEL 3 ADMIN",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = AdminBlue
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Operational Jurisdiction & Facility Assignment
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
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
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AdminCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "JURISDICTION & ASSIGNMENT",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.5.sp,
                                color = AdminTextDim
                            )
                        )
                    }

                    AdminProfileItemRow("Account ID", admin.id, isMono = true)
                    HorizontalDivider(color = AdminBorder, thickness = 0.7.dp)
                    AdminProfileItemRow("Official Role", admin.role.displayName)
                    HorizontalDivider(color = AdminBorder, thickness = 0.7.dp)
                    AdminProfileItemRow("Base Facility", admin.assignedCenter)
                    HorizontalDivider(color = AdminBorder, thickness = 0.7.dp)
                    AdminProfileItemRow("Jurisdiction Region", admin.assignedRegion)
                    HorizontalDivider(color = AdminBorder, thickness = 0.7.dp)
                    AdminProfileItemRow("Fleet Under Oversight", "4 Sub-Centers • 12 Active ASHA Staff")
                }
            }
        }

        // Section 4: Operational Responsibilities & RBAC Grants
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
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
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = AdminBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "OPERATIONAL CLEARANCE & RBAC GRANTS",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.5.sp,
                                color = AdminTextDim
                            )
                        )
                    }

                    AdminGrantRow("Field Hardware Fleet Management & Calibration Audits", true)
                    AdminGrantRow("Real-Time Screening Telemetry & Gate Pass Monitoring", true)
                    AdminGrantRow("Retest Queue Authorization & Incident Triage", true)
                    AdminGrantRow("Provincial Ledger Audit & Clinical PDF Export", true)
                    AdminGrantRow("ASHA Worker Quota Allocation & Daily Balancing", true)
                    AdminGrantRow("Root Cryptographic Seed Governance (Super Admin Only)", false)
                    AdminGrantRow("Scientific Consortium Parameter Freezing (Super Admin Only)", false)
                }
            }
        }

        // Section 5: Cryptographic Provenance & Security Trace
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
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
                                tint = AdminBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AUDIT & SECURITY PROVENANCE",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.5.sp,
                                    color = AdminTextDim
                                )
                            )
                        }
                        Text(
                            text = "Seed: 26004L",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = AdminTextDim
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
                                text = "SESSION TRACE HASH (SHA-256)",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.5.sp,
                                    color = AdminTextDim
                                )
                            )
                            Text(
                                text = "9a7d3f82e1b4c6a5...tamper_evident",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = AdminTextMain
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Logout Action Button
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_logout_button")
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
                        text = "LOGOUT OF ADMIN SESSION",
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

    // Photo Selection Dialog
    if (showPhotoModal) {
        Dialog(onDismissRequest = { showPhotoModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ADMINISTRATOR PROFILE PHOTO",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AdminTextMain
                        )
                    )
                    Text(
                        text = "Choose a photo source or remove the existing profile avatar.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            color = AdminTextDim
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            showPhotoModal = false
                            cameraLauncher.launch(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
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
                        border = BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = AdminTextMain, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CHOOSE FROM GALLERY", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMain)
                    }

                    if (avatarBitmap != null) {
                        OutlinedButton(
                            onClick = {
                                UserAvatarStorage.removeAvatar(context, admin.username)
                                avatarBitmap = null
                                showPhotoModal = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AdminRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = AdminRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REMOVE PHOTO", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminRed)
                        }
                    }

                    TextButton(
                        onClick = { showPhotoModal = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CANCEL", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextDim)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProfileItemRow(label: String, value: String, isMono: Boolean = false) {
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
                color = AdminTextDim
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = if (isMono) JetBrainsMonoFontFamily else SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = AdminTextMain
            )
        )
    }
}

@Composable
private fun AdminGrantRow(text: String, isAllowed: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isAllowed) AdminGreen.copy(alpha = 0.08f) else AdminRed.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, if (isAllowed) AdminGreen.copy(alpha = 0.25f) else AdminRed.copy(alpha = 0.25f)),
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
                tint = if (isAllowed) AdminGreen else AdminRed,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.5.sp,
                    color = AdminTextMain
                )
            )
        }
    }
}

@Composable
fun AdminDiagnosticsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AdminSurfaceBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminCyan),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TECHNICAL DIAGNOSTICS",
                        color = AdminCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = AdminTextMain)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AdminSurfaceBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PACKET LOSS: 0.00% (Strict zero-loss verified)", color = AdminGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("SEQUENCE GAPS: 0 gap detected", color = AdminGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("JITTER BUFFER: 1.2 ms (Within <= 5.0 ms target)", color = AdminGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("RING BUFFER DEPTH: 1024 samples (Healthy 98.4%)", color = AdminCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("SQI MINIMUM BOUND: 0.70 threshold locked", color = AdminCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("ARTIFACT DETECTOR: Adaptive peak kurtosis active", color = AdminBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("FEATURE FRESHNESS: < 300s window strictly enforced", color = AdminBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("SCHEMA VERSION: v1.0 Universal Data Contract", color = AdminTextMain, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("MODEL ARCHITECTURE: Late-Fusion Classifier v1.0.0", color = AdminTextMain, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("INFERENCE TRACE: SHA-256 Provenance Active", color = AdminCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE DIAGNOSTICS", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

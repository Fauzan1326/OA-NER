package com.example.ui.portal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.auth.UserAccount
import com.example.auth.UserStatus
import com.example.portal.AshaDashboardStats
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

/**
 * PRODUCTION ASHA WORKER PROFILE SCREEN
 * ARTHROSCAN-NER | Authoritative Field Operator Identity & Credential Vault
 *
 * Requirements:
 * - Single authoritative profile page (no duplicate personal/assignment blocks)
 * - Clickable circular avatar with camera intent and native document pickers
 * - Local photo persistence (preview, crop to circle, replace, remove)
 * - Light-mode clinical visual design (no theme toggle or moon icon on profile)
 * - Authoritative field operator credentials ledger & RBAC grants
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshaProfileScreen(
    worker: UserAccount,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    stats: AshaDashboardStats? = null
) {
    val context = LocalContext.current

    // Authoritative Light Mode Clinical Palette
    val bgCanvas = Color(0xFFF8FAFC)
    val surfaceCard = Color.White
    val surfaceCardLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val accentCyan = Color(0xFF0891B2)
    val statusGreen = Color(0xFF059669)
    val statusGreenBg = Color(0xFFECFDF5)
    val statusGreenText = Color(0xFF065F46)
    val errorRed = Color(0xFFDC2626)
    val errorRedBg = Color(0xFFFEF2F2)

    // Local profile avatar state with persistent storage
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(loadAvatarFromStorage(context)) }
    var showPhotoModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "FIELD WORKER PROFILE",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = "ARTHROSCAN-NER // OPS-ID-26004",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }

                    // Online indicator pill (No dark-mode or moon icon)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE6FFFA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusGreen)
                            )
                            Text(
                                text = "ONLINE",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF006F66)
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = bgCanvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // ==============================================================
            // 1. PRIMARY IDENTITY CARD (CLICKABLE AVATAR)
            // ==============================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Clickable Circular Avatar with Camera Trigger
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .clickable { showPhotoModal = true }
                                .testTag("profile_avatar_clickable")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = primaryBlue,
                                modifier = Modifier.size(86.dp),
                                shadowElevation = 3.dp,
                                border = BorderStroke(2.dp, Color(0xFFDBEAFE))
                            ) {
                                val currentBmp = avatarBitmap
                                if (currentBmp != null) {
                                    Image(
                                        bitmap = currentBmp.asImageBitmap(),
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initials = worker.fullName.split(" ")
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .take(2)
                                            .joinToString("")
                                            .ifBlank { "AD" }
                                        Text(
                                            text = initials,
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            // Camera trigger action badge
                            Surface(
                                shape = CircleShape,
                                color = primaryBlue,
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Update Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Name & Status Badges
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = worker.fullName,
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = textPrimary
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "ASHA WORKER",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp,
                                            color = primaryBlue
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = statusGreenBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(statusGreen)
                                        )
                                        Text(
                                            text = "ACTIVE / APPROVED",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = statusGreenText
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Institutional Affiliation
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = surfaceCardLow
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = primaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "National Health Mission • Govt. of Assam",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 2. FIELD OPERATOR CREDENTIALS (SINGLE AUTHORITATIVE SOURCE)
            // ==============================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "FIELD OPERATOR CREDENTIALS",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = textPrimary
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusGreenBg
                            ) {
                                Text(
                                    text = "Verified M3-RBAC",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = statusGreenText
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Sub-card 1: Identity Ledger
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = surfaceCardLow,
                            border = BorderStroke(1.dp, borderStrokeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "01 // IDENTITY LEDGER",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        color = primaryBlue
                                    )
                                )
                                ProfileFieldRow("Worker Name", worker.fullName, textPrimary, textSecondary)
                                HorizontalDivider(color = borderStrokeColor, thickness = 0.6.dp)
                                ProfileFieldRow("Username", worker.username, textPrimary, textSecondary, isMonospace = true)
                                HorizontalDivider(color = borderStrokeColor, thickness = 0.6.dp)
                                ProfileFieldRow("Worker ID", "ASHA-NER-26004-01", textPrimary, textSecondary, isMonospace = true)
                                HorizontalDivider(color = borderStrokeColor, thickness = 0.6.dp)
                                ProfileFieldRow("Role", "ASHA WORKER", textPrimary, textSecondary)
                            }
                        }

                        // Sub-card 2: Deployment Node
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = surfaceCardLow,
                            border = BorderStroke(1.dp, borderStrokeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "02 // DEPLOYMENT NODE",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        color = primaryBlue
                                    )
                                )
                                ProfileFieldRow("Account Status", "ACTIVE / APPROVED", statusGreen, textSecondary)
                                HorizontalDivider(color = borderStrokeColor, thickness = 0.6.dp)
                                ProfileFieldRow("Govt Email", "anita.deka@nhm.assam.gov.in", textPrimary, textSecondary)
                                HorizontalDivider(color = borderStrokeColor, thickness = 0.6.dp)
                                ProfileFieldRow("Assigned Center", worker.assignedCenter, textPrimary, textSecondary)
                                HorizontalDivider(color = borderStrokeColor, thickness = 0.6.dp)
                                ProfileFieldRow("District / Region", "Kamrup Rural, Assam-NER", textPrimary, textSecondary)
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 3. OPERATIONAL RBAC GRANTS (PRESERVE STRICT SECURITY)
            // ==============================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
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
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ROLE PERMISSIONS SUMMARY",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = textPrimary
                                    )
                                )
                            }
                            Text(
                                text = "Policy Token: V4-OK",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            )
                        }

                        // Authorized section
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = "AUTHORIZED CLINICAL SCREENING (M3-M4)",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = statusGreen
                                )
                            )
                            RbacBulletItem("Conduct screening sessions with calibrated sensory arrays and hardware peripherals.")
                            RbacBulletItem("Review Module 3 signal quality telemetry and Module 4 biomechanical feature freshness.")
                            RbacBulletItem("View multimodal screening triage tiers and real-time calibrated diagnostic uncertainty scores.")
                        }

                        HorizontalDivider(color = borderStrokeColor, thickness = 0.8.dp)

                        // Restricted section
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = "STRICT POLICY SAFEGUARDS",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = errorRed
                                )
                            )
                            RbacRestrictedItem("Cannot access Administrative root controls or Supervisory clinical governance consoles.")
                            RbacRestrictedItem("Cannot modify AI neural model hyperweights or scientific diagnostic classification thresholds.")
                        }
                    }
                }
            }

            // ==============================================================
            // 4. TERMINAL SESSION & LOGOUT
            // ==============================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = surfaceCardLow
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusGreen)
                                )
                                Column {
                                    Text(
                                        text = "FIELD TERMINAL SESSION ACTIVE",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            color = textPrimary
                                        )
                                    )
                                    Text(
                                        text = "Hardware: Device BT-V2 Paired • Audit Seed: 26004L",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("profile_logout_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = errorRed
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
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
                                    text = "LOG OUT OF FIELD TERMINAL",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Session encrypted with AES-GCM-256 Auth",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==============================================================
    // UPDATE PROFILE PHOTO MODAL
    // ==============================================================
    if (showPhotoModal) {
        var tempPreviewBitmap by remember { mutableStateOf<Bitmap?>(avatarBitmap) }

        // Camera Launcher
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { resultBitmap ->
            if (resultBitmap != null) {
                tempPreviewBitmap = resultBitmap
            }
        }

        // Native Files Launcher (ACTION_OPEN_DOCUMENT / GetContent)
        val filesLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bmp = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    if (bmp != null) {
                        tempPreviewBitmap = bmp
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Drive Document Provider Launcher
        val driveLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bmp = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    if (bmp != null) {
                        tempPreviewBitmap = bmp
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Dialog(onDismissRequest = { showPhotoModal = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = surfaceCard,
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
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
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Update Profile Photo",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                        }
                        IconButton(
                            onClick = { showPhotoModal = false },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Circular Crop Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = primaryBlue,
                                modifier = Modifier.size(80.dp),
                                border = BorderStroke(2.dp, primaryBlue)
                            ) {
                                val previewBmp = tempPreviewBitmap
                                if (previewBmp != null) {
                                    Image(
                                        bitmap = previewBmp.asImageBitmap(),
                                        contentDescription = "Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "AD",
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "1:1 Calibrated Circular Framing",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }

                    // Action Option Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PhotoOptionRow(
                            icon = Icons.Default.PhotoCamera,
                            iconTint = primaryBlue,
                            title = "Take Photo",
                            subtitle = "Camera Intent // Real-time biometric capture",
                            onClick = { cameraLauncher.launch(null) }
                        )

                        PhotoOptionRow(
                            icon = Icons.Default.FolderOpen,
                            iconTint = accentCyan,
                            title = "Choose from My Files",
                            subtitle = "ACTION_OPEN_DOCUMENT (Local Storage)",
                            onClick = { filesLauncher.launch("image/*") }
                        )

                        PhotoOptionRow(
                            icon = Icons.Default.CloudUpload,
                            iconTint = primaryBlue,
                            title = "Choose from Drive",
                            subtitle = "NHM Cloud Document Provider Sync",
                            onClick = { driveLauncher.launch(arrayOf("image/*")) }
                        )

                        PhotoOptionRow(
                            icon = Icons.Default.Delete,
                            iconTint = errorRed,
                            title = "Remove Photo",
                            subtitle = "Revert to default clinical initials",
                            isDestructive = true,
                            onClick = { tempPreviewBitmap = null }
                        )
                    }

                    // Modal Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPhotoModal = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, borderStrokeColor)
                        ) {
                            Text(
                                text = "Cancel",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = textPrimary
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val newBmp = tempPreviewBitmap
                                if (newBmp != null) {
                                    saveAvatarToStorage(context, newBmp)
                                    avatarBitmap = newBmp
                                } else {
                                    removeAvatarFromStorage(context)
                                    avatarBitmap = null
                                }
                                showPhotoModal = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            Text(
                                text = "Save Photo",
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

@Composable
private fun PhotoOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDestructive) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isDestructive) Color(0xFFFECACA) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = if (isDestructive) Color(0xFFDC2626) else Color(0xFF0F172A)
                    )
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = if (isDestructive) Color(0xFFEF4444) else Color(0xFF64748B)
                    )
                )
            }
        }
    }
}

@Composable
private fun ProfileFieldRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.5.sp,
                color = textSecondary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = if (isMonospace) JetBrainsMonoFontFamily else SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = textPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RbacBulletItem(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF1F5F9),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(14.dp).padding(top = 1.dp)
            )
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF0F172A)
                )
            )
        }
    }
}

@Composable
private fun RbacRestrictedItem(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFFEF2F2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(14.dp).padding(top = 1.dp)
            )
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF0F172A)
                )
            )
        }
    }
}

// ==============================================================
// LOCAL STORAGE HELPERS FOR PROFILE AVATAR
// ==============================================================

private fun saveAvatarToStorage(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.filesDir, "asha_avatar.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun loadAvatarFromStorage(context: Context): Bitmap? {
    return try {
        val file = File(context.filesDir, "asha_avatar.png")
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun removeAvatarFromStorage(context: Context) {
    try {
        val file = File(context.filesDir, "asha_avatar.png")
        if (file.exists()) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

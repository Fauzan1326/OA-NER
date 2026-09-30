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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.auth.UserAccount
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PatientNavTab {
    DASHBOARD,
    REPORTS,
    ASHA_SUPPORT,
    PROFILE
}

/**
 * ARTHROSCAN-NER PATIENT DIRECT ACCESS BOARD
 * Clinical Diagnostic Precision Design System
 * Matches Google Stitch Patient UI Export
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDirectAccessBoardScreen(
    patient: UserAccount,
    onLogout: () -> Unit,
    onStartScreening: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(PatientNavTab.DASHBOARD) }
    var showScreeningModal by remember { mutableStateOf(false) }
    var showPhotoModal by remember { mutableStateOf(false) }

    // Avatar state
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(loadPatientAvatar(context)) }

    // Preferences state
    var isTextLarge by remember { mutableStateOf(false) }
    var selectedDialect by remember { mutableStateOf("Assamese") }
    var reducedMotion by remember { mutableStateOf(false) }

    // Stitch Design Tokens
    val bgSurface = Color(0xFFFAF8FF)
    val cardSurface = Color(0xFFFFFFFF)
    val containerLow = Color(0xFFF2F3FF)
    val containerHigh = Color(0xFFE2E7FF)
    val primaryColor = Color(0xFF004AC6)
    val primaryContainer = Color(0xFF2563EB)
    val secondaryColor = Color(0xFF006A61)
    val secondaryFixed = Color(0xFF89F5E7)
    val onSecondaryFixed = Color(0xFF00201D)
    val textPrimary = Color(0xFF131B2E)
    val textSecondary = Color(0xFF434655)
    val outlineBorder = Color(0xFFC3C6D7)
    val errorColor = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)

    Scaffold(
        topBar = {
            Surface(
                color = bgSurface.copy(alpha = 0.95f),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo & Breadcrumb
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = containerHigh,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MedicalInformation,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "ARTHROSCAN-NER",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = (-0.2).sp,
                                    color = textPrimary
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Patient Direct Access",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                )
                                Text("•", fontSize = 10.sp, color = textSecondary)
                                Text(
                                    text = when (currentTab) {
                                        PatientNavTab.DASHBOARD -> "Dashboard"
                                        PatientNavTab.REPORTS -> "Reports"
                                        PatientNavTab.ASHA_SUPPORT -> "Support"
                                        PatientNavTab.PROFILE -> "Profile"
                                    },
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = primaryColor
                                    )
                                )
                            }
                        }
                    }

                    // Top Bar Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Language Selector Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = containerLow,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = secondaryColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "English",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = textPrimary
                                    )
                                )
                            }
                        }

                        // Avatar with Active Indicator
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { currentTab = PatientNavTab.PROFILE }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = primaryContainer,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (avatarBitmap != null) {
                                    Image(
                                        bitmap = avatarBitmap!!.asImageBitmap(),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "RA",
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(secondaryColor)
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = bgSurface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PatientBottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Dashboard",
                        isSelected = currentTab == PatientNavTab.DASHBOARD,
                        onClick = { currentTab = PatientNavTab.DASHBOARD }
                    )
                    PatientBottomNavItem(
                        icon = Icons.Default.Assessment,
                        label = "Reports",
                        isSelected = currentTab == PatientNavTab.REPORTS,
                        onClick = { currentTab = PatientNavTab.REPORTS }
                    )
                    PatientBottomNavItem(
                        icon = Icons.Default.MedicalServices,
                        label = "ASHA Support",
                        isSelected = currentTab == PatientNavTab.ASHA_SUPPORT,
                        onClick = { currentTab = PatientNavTab.ASHA_SUPPORT }
                    )
                    PatientBottomNavItem(
                        icon = Icons.Default.Badge,
                        label = "Profile",
                        isSelected = currentTab == PatientNavTab.PROFILE,
                        onClick = { currentTab = PatientNavTab.PROFILE }
                    )
                }
            }
        },
        containerColor = bgSurface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                PatientNavTab.DASHBOARD -> {
                    PatientDashboardContent(
                        patient = patient,
                        onOpenScreeningModal = { showScreeningModal = true },
                        onNavigateReports = { currentTab = PatientNavTab.REPORTS },
                        onNavigateSupport = { currentTab = PatientNavTab.ASHA_SUPPORT }
                    )
                }
                PatientNavTab.PROFILE -> {
                    PatientProfileContent(
                        patient = patient,
                        avatarBitmap = avatarBitmap,
                        onBack = { currentTab = PatientNavTab.DASHBOARD },
                        onOpenPhotoModal = { showPhotoModal = true },
                        onLogout = onLogout,
                        isTextLarge = isTextLarge,
                        onToggleTextSize = { isTextLarge = it },
                        selectedDialect = selectedDialect,
                        onSelectDialect = { selectedDialect = it },
                        reducedMotion = reducedMotion,
                        onToggleReducedMotion = { reducedMotion = it }
                    )
                }
                PatientNavTab.REPORTS -> {
                    PatientReportsContent(
                        patient = patient,
                        onBack = { currentTab = PatientNavTab.DASHBOARD }
                    )
                }
                PatientNavTab.ASHA_SUPPORT -> {
                    PatientSupportContent(
                        patient = patient,
                        onBack = { currentTab = PatientNavTab.DASHBOARD }
                    )
                }
            }
        }
    }

    // ==============================================================
    // START SCREENING MODAL / SHEET
    // ==============================================================
    if (showScreeningModal) {
        val formattedDate = remember {
            SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.US).format(Date())
        }

        Dialog(onDismissRequest = { showScreeningModal = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardSurface,
                border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = containerLow,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Start Screening",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = textPrimary
                                )
                            )
                        }

                        IconButton(
                            onClick = { showScreeningModal = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Participant Metadata Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Participant ID",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = cardSurface
                                ) {
                                    Text(
                                        text = "PART-ANON-420",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = textPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Screening Type",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                )
                                Text(
                                    text = "Knee Mobility Acoustic",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = primaryColor
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Timestamp",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                )
                                Text(
                                    text = formattedDate,
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Non-Diagnostic Advisory
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = containerHigh.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp).padding(top = 1.dp)
                            )
                            Text(
                                text = "This is a non-diagnostic screening assessment. It measures joint audio vibrations and does not replace evaluation by a qualified medical professional.",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showScreeningModal = false },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Cancel",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = textPrimary
                                )
                            )
                        }

                        Button(
                            onClick = {
                                showScreeningModal = false
                                onStartScreening()
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Continue",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==============================================================
    // UPDATE PROFILE PHOTO MODAL
    // ==============================================================
    if (showPhotoModal) {
        var tempPreviewBitmap by remember { mutableStateOf<Bitmap?>(avatarBitmap) }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { resultBitmap ->
            if (resultBitmap != null) {
                tempPreviewBitmap = resultBitmap
            }
        }

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
                color = cardSurface,
                border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Update Profile Photo",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = "Citizen Identity Biometrics",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            )
                        }
                        IconButton(onClick = { showPhotoModal = false }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Preview Circle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = primaryContainer,
                                modifier = Modifier.size(76.dp),
                                border = BorderStroke(2.dp, primaryContainer)
                            ) {
                                if (tempPreviewBitmap != null) {
                                    Image(
                                        bitmap = tempPreviewBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("RA", style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White))
                                    }
                                }
                            }
                            Text(
                                text = "Circular Frame Preview (1:1 Ratio)",
                                style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = textSecondary)
                            )
                        }
                    }

                    // Options
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PatientPhotoOptionRow(
                            icon = Icons.Default.PhotoCamera,
                            iconTint = primaryColor,
                            title = "Take Photo",
                            subtitle = "Launch device front/rear camera",
                            onClick = { cameraLauncher.launch(null) }
                        )
                        PatientPhotoOptionRow(
                            icon = Icons.Default.FolderOpen,
                            iconTint = primaryColor,
                            title = "Choose from My Files",
                            subtitle = "ACTION_OPEN_DOCUMENT (Local Storage)",
                            onClick = { filesLauncher.launch("image/*") }
                        )
                        PatientPhotoOptionRow(
                            icon = Icons.Default.CloudUpload,
                            iconTint = secondaryColor,
                            title = "Choose from Drive",
                            subtitle = "Cloud Document Storage",
                            onClick = { driveLauncher.launch(arrayOf("image/*")) }
                        )
                        PatientPhotoOptionRow(
                            icon = Icons.Default.DeleteOutline,
                            iconTint = errorColor,
                            title = "Remove Photo",
                            subtitle = "Revert to standard silhouette badge",
                            isDestructive = true,
                            onClick = { tempPreviewBitmap = null }
                        )
                    }

                    // Save / Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPhotoModal = false },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp))
                        }
                        Button(
                            onClick = {
                                val newBmp = tempPreviewBitmap
                                if (newBmp != null) {
                                    savePatientAvatar(context, newBmp)
                                    avatarBitmap = newBmp
                                } else {
                                    deletePatientAvatar(context)
                                    avatarBitmap = null
                                }
                                showPhotoModal = false
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Save", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================
// PATIENT DASHBOARD CONTENT
// ==============================================================
@Composable
private fun PatientDashboardContent(
    patient: UserAccount,
    onOpenScreeningModal: () -> Unit,
    onNavigateReports: () -> Unit,
    onNavigateSupport: () -> Unit
) {
    val cardSurface = Color(0xFFFFFFFF)
    val containerLow = Color(0xFFF2F3FF)
    val containerHigh = Color(0xFFE2E7FF)
    val primaryColor = Color(0xFF004AC6)
    val secondaryColor = Color(0xFF006A61)
    val secondaryFixed = Color(0xFF89F5E7)
    val onSecondaryFixed = Color(0xFF00201D)
    val textPrimary = Color(0xFF131B2E)
    val textSecondary = Color(0xFF434655)
    val outlineBorder = Color(0xFFC3C6D7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Patient Welcome Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome, ${patient.fullName.ifBlank { "Rahim Ali" }}",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Citizen ID:",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = containerLow
                            ) {
                                Text(
                                    text = patient.username.ifBlank { "patient.user" },
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = primaryColor
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // PATIENT ACTIVE pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = secondaryFixed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(secondaryColor)
                            )
                            Text(
                                text = "PATIENT ACTIVE",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = onSecondaryFixed
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Your citizen mobility account is active. Access your knee health self-check tools, multimodal acoustic crepitus analysis, and verified longitudinal screening reports from this secure patient portal.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = textSecondary
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Verified National Telehealth Link",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = secondaryColor
                        )
                    )
                }
            }
        }

        // Primary Screening Trigger Button
        Button(
            onClick = onOpenScreeningModal,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF004AC6),
                                Color(0xFF006A61)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "START KNEE MOBILITY SCREENING",
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

        // 2x2 Bento Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: My Reports
                PatientBentoTile(
                    title = "My Reports",
                    subtitle = "View verified knee joint acoustic assessments & history",
                    icon = Icons.Default.BarChart,
                    iconTint = primaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateReports
                )

                // Card 2: Field Support
                PatientBentoTile(
                    title = "Field Support",
                    subtitle = "Assigned ASHA: Anita Deka (PHC Rampur)",
                    icon = Icons.Default.MedicalServices,
                    iconTint = secondaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateSupport
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 3: Session Logs
                PatientBentoTile(
                    title = "Session Logs",
                    subtitle = "View previous longitudinal mobility scans",
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateReports
                )

                // Card 4: Information
                PatientBentoTile(
                    title = "Information",
                    subtitle = "Learn about multi-physics acoustic sensors",
                    icon = Icons.Default.MenuBook,
                    iconTint = Color(0xFF6A1EDB),
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
        }

        // Security Protocol & NHM Integrated Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = containerLow,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = containerHigh,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "ARTHROSCAN-NER Citizen Gateway is encrypted and integrated with National Health Mission digital protocols.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SEED: 26004L • END-TO-END VERIFIED",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==============================================================
// PATIENT PROFILE CONTENT
// ==============================================================
@Composable
private fun PatientProfileContent(
    patient: UserAccount,
    avatarBitmap: Bitmap?,
    onBack: () -> Unit,
    onOpenPhotoModal: () -> Unit,
    onLogout: () -> Unit,
    isTextLarge: Boolean,
    onToggleTextSize: (Boolean) -> Unit,
    selectedDialect: String,
    onSelectDialect: (String) -> Unit,
    reducedMotion: Boolean,
    onToggleReducedMotion: (Boolean) -> Unit
) {
    val cardSurface = Color(0xFFFFFFFF)
    val containerLow = Color(0xFFF2F3FF)
    val containerHigh = Color(0xFFE2E7FF)
    val primaryColor = Color(0xFF004AC6)
    val secondaryColor = Color(0xFF006A61)
    val secondaryFixed = Color(0xFF89F5E7)
    val onSecondaryFixed = Color(0xFF00201D)
    val textPrimary = Color(0xFF131B2E)
    val textSecondary = Color(0xFF434655)
    val outlineBorder = Color(0xFFC3C6D7)
    val errorColor = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)
    val onErrorContainer = Color(0xFF93000A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sub-Header / Context Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = containerLow
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "NHM INTEGRATED",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = secondaryColor
                        )
                    )
                }
            }
        }

        // Title Block
        Column {
            Text(
                text = "Patient Profile",
                style = TextStyle(
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textPrimary
                )
            )
            Text(
                text = "Citizen Health Identity & Screening Preferences",
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    color = textSecondary
                )
            )
        }

        // Profile Identity Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clickable Avatar with Camera Overlay
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .size(68.dp)
                            .clickable { onOpenPhotoModal() }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "RA",
                                        style = TextStyle(
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = primaryColor,
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Identity Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = patient.fullName.ifBlank { "Rahim Ali" },
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = textPrimary
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = secondaryFixed
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(secondaryColor)
                                    )
                                    Text(
                                        text = "ACTIVE",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = onSecondaryFixed
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = containerHigh
                            ) {
                                Text(
                                    text = "PATIENT / CITIZEN",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = textSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "ID: ${patient.username.ifBlank { "patient.user" }}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = secondaryColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Kamrup Rural • PHC Rampur",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }

                // Quick Telemetry Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = containerLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = primaryColor, modifier = Modifier.size(12.dp))
                                Text("Enrolled", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 10.5.sp, color = textSecondary))
                            }
                            Text("12 May 2026", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary), modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = containerLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(12.dp))
                                Text("Last Access", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 10.5.sp, color = textSecondary))
                            }
                            Text("Today, 12:16 PM", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary), modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }

        // Citizen Credentials Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
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
                        Icon(Icons.Default.Badge, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Citizen Credentials",
                            style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = textPrimary)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = containerHigh) {
                        Text(
                            text = "VERIFIED",
                            style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = primaryColor),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = outlineBorder.copy(alpha = 0.3f), thickness = 0.7.dp)

                PatientInfoRow("Full Legal Name", patient.fullName.ifBlank { "Rahim Ali" })
                PatientInfoRow("Citizen Portal ID", patient.username.ifBlank { "patient.user" }, isMono = true, valueColor = primaryColor)
                PatientInfoRow("Registered Contact", "+91 98765 43210\n${patient.email.ifBlank { "rahim@example.com" }}")
                PatientInfoRow("Date of Birth & Age", "14/08/1982 (44 Yrs)")
                PatientInfoRow("Gender", "Male")
                PatientInfoRow("Health Sub-Center", "PHC Rampur (Block 04)\nKamrup Rural District, Assam")
                PatientInfoRow("Default Language", "English 🌐")
            }
        }

        // Preferences & Accessibility Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Preferences & Accessibility",
                        style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = textPrimary)
                    )
                }

                // Text Size Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Text Sizing", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textPrimary))
                        Text("Screen readability for reports", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textSecondary))
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(containerLow)
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isTextLarge) cardSurface else Color.Transparent,
                            modifier = Modifier.clickable { onToggleTextSize(false) }
                        ) {
                            Text(
                                text = "Standard",
                                style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = if (!isTextLarge) primaryColor else textSecondary),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isTextLarge) cardSurface else Color.Transparent,
                            modifier = Modifier.clickable { onToggleTextSize(true) }
                        ) {
                            Text(
                                text = "Large",
                                style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = if (isTextLarge) primaryColor else textSecondary),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = outlineBorder.copy(alpha = 0.3f), thickness = 0.7.dp)

                // Voice Narration Dialect
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Voice Narration Dialect", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textPrimary))
                        Text("NER Field Pack", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = secondaryColor))
                    }

                    val dialects = listOf("Assamese" to "অসমীয়া (Assamese)", "Bodo" to "बर' (Bodo)", "Hindi" to "हिन्दी (Hindi)", "English" to "English")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        dialects.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { (key, label) ->
                                    val isSel = selectedDialect == key
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) containerHigh else containerLow,
                                        border = BorderStroke(1.dp, if (isSel) primaryColor else Color.Transparent),
                                        modifier = Modifier.weight(1f).clickable { onSelectDialect(key) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSel,
                                                onClick = { onSelectDialect(key) },
                                                modifier = Modifier.size(16.dp),
                                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                            )
                                            Text(
                                                text = label,
                                                style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.5.sp, color = textPrimary),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = outlineBorder.copy(alpha = 0.3f), thickness = 0.7.dp)

                // Reduced Motion Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reduced Motion", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textPrimary))
                        Text("Minimize biomechanical scan animations", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textSecondary))
                    }
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = onToggleReducedMotion,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = secondaryColor)
                    )
                }
            }
        }

        // Privacy & Governance Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Privacy & Governance",
                        style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = textPrimary)
                    )
                }

                // Consent Pill Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = containerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                Icon(Icons.Default.Lock, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(15.dp))
                                Text("NHM Screening Data Consent", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textPrimary))
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = secondaryFixed) {
                                Text("ACTIVE", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = onSecondaryFixed), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Text(
                            text = "Acoustic joint signatures and range-of-motion telemetry are cryptographically sealed and accessible only by assigned ASHA health workers and medical officers.",
                            style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, lineHeight = 15.sp, color = textSecondary)
                        )
                    }
                }

                // Action Links
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = textSecondary, modifier = Modifier.size(16.dp))
                        Text("Data Usage & Digital Health Protocol", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textPrimary))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = textSecondary, modifier = Modifier.size(16.dp))
                        Text("Cryptographic Audit Certificate", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textPrimary))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                }

                // Sign Out Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = errorContainer)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = onErrorContainer, modifier = Modifier.size(16.dp))
                        Text(
                            text = "SIGN OUT OF CITIZEN PORTAL",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.5.sp,
                                color = onErrorContainer
                            )
                        )
                    }
                }

                Text(
                    text = "ARTHROSCAN-NER v2.4.2 • Device Node Assam-NER-0881",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = textSecondary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==============================================================
// PATIENT REPORTS CONTENT
// ==============================================================
@Composable
private fun PatientReportsContent(
    patient: UserAccount,
    onBack: () -> Unit
) {
    val cardSurface = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF131B2E)
    val textSecondary = Color(0xFF434655)
    val outlineBorder = Color(0xFFC3C6D7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to Dashboard", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = textSecondary))
        }

        Text(
            text = "Knee Health Reports",
            style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
        )
        Text(
            text = "Longitudinal acoustic assessments and mobility screening logs",
            style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textSecondary)
        )

        // Mock screening record 1
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SES-NER-43818", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary))
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFECFDF5)) {
                        Text("LOW RISK (0.182)", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF059669)), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text("Bilateral Knee Acoustic Crepitus Profiling", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textSecondary))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Conducted: 29 Sep 2026, 10:14 AM", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textSecondary))
                    Text("Operator: Anita Deka (ASHA)", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textSecondary))
                }
            }
        }

        // Mock screening record 2
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SES-NER-39210", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary))
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFFBEB)) {
                        Text("MODERATE (0.493)", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFFD97706)), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text("Bilateral Knee Acoustic Crepitus Profiling", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textSecondary))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Conducted: 14 Aug 2026, 09:30 AM", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textSecondary))
                    Text("Operator: Anita Deka (ASHA)", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textSecondary))
                }
            }
        }
    }
}

// ==============================================================
// PATIENT ASHA SUPPORT CONTENT
// ==============================================================
@Composable
private fun PatientSupportContent(
    patient: UserAccount,
    onBack: () -> Unit
) {
    val cardSurface = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF004AC6)
    val textPrimary = Color(0xFF131B2E)
    val textSecondary = Color(0xFF434655)
    val outlineBorder = Color(0xFFC3C6D7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to Dashboard", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = textSecondary))
        }

        Text(
            text = "Field Cadre Support",
            style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
        )
        Text(
            text = "Direct assistance and screening coordination with your local ASHA worker",
            style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textSecondary)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, outlineBorder.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = CircleShape, color = Color(0xFF2563EB), modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("AD", style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                        }
                    }
                    Column {
                        Text("Anita Deka", style = TextStyle(fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary))
                        Text("ASHA Health Worker • Kamrup-04", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textSecondary))
                    }
                }

                HorizontalDivider(color = outlineBorder.copy(alpha = 0.3f), thickness = 0.7.dp)

                Text("Assigned Sub-Center: PHC Rampur - Sub-Center 04", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = textPrimary))
                Text("Contact Phone: +91 94350 26004", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = primaryColor))
                Text("Consultation Shift: Morning Shift (09:00 AM – 01:00 PM)", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.5.sp, color = textSecondary))
            }
        }
    }
}

// ==============================================================
// SUB-COMPONENTS
// ==============================================================

@Composable
private fun PatientBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = Color(0xFF004AC6)
    val textSecondary = Color(0xFF434655)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) primaryColor else textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.5.sp,
                color = if (isSelected) primaryColor else textSecondary
            )
        )
    }
}

@Composable
private fun PatientBentoTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFC3C6D7).copy(alpha = 0.5f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconTint.copy(alpha = 0.1f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF131B2E)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                        color = Color(0xFF434655)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PatientInfoRow(
    label: String,
    value: String,
    isMono: Boolean = false,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.5.sp,
                color = Color(0xFF434655)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = if (isMono) JetBrainsMonoFontFamily else SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = valueColor ?: Color(0xFF131B2E)
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PatientPhotoOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDestructive) Color(0xFFFFDAD6).copy(alpha = 0.5f) else Color(0xFFF2F3FF),
        border = BorderStroke(1.dp, if (isDestructive) Color(0xFFBA1A1A).copy(alpha = 0.3f) else Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isDestructive) Color(0xFFBA1A1A) else Color(0xFF131B2E)
                    )
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Color(0xFF434655)
                    )
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF737686), modifier = Modifier.size(14.dp))
        }
    }
}

// ==============================================================
// LOCAL STORAGE HELPERS FOR PATIENT AVATAR
// ==============================================================

private fun savePatientAvatar(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.filesDir, "patient_avatar.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun loadPatientAvatar(context: Context): Bitmap? {
    return try {
        val file = File(context.filesDir, "patient_avatar.png")
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun deletePatientAvatar(context: Context) {
    try {
        val file = File(context.filesDir, "patient_avatar.png")
        if (file.exists()) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

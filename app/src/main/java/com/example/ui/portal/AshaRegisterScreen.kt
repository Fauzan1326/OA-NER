package com.example.ui.portal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.auth.AuthRepository
import com.example.auth.UserAccount
import com.example.ui.theme.*

/**
 * ARTHROSCAN-NER REGISTRATION & ACCESS REQUEST SCREEN
 * High-fidelity clinical diagnostic access provisioning console
 * Smart India Hackathon 2026 | Team GOD'S PLAN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshaRegisterScreen(
    authRepository: AuthRepository,
    onBackToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    onPatientRegistered: ((UserAccount) -> Unit)? = null
) {
    // Intercept back button to return to login
    BackHandler(onBack = onBackToLogin)

    val context = LocalContext.current

    var selectedCadre by remember { mutableStateOf("patient") } // "asha" or "patient"
    var fullName by remember { mutableStateOf("Anita Deka") }
    var username by remember { mutableStateOf("rahim.ali") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("rahim.ali@patient.org") }
    var idCode by remember { mutableStateOf("ABHA-9821-4412") }
    var selectedGender by remember { mutableStateOf("Female") }
    var age by remember { mutableIntStateOf(37) }
    var dob by remember { mutableStateOf("14 / 08 / 1988") }

    var password by remember { mutableStateOf("KneeScan#2026NER") }
    var confirmPassword by remember { mutableStateOf("KneeScan#2026NER") }
    var passVisible1 by remember { mutableStateOf(false) }
    var passVisible2 by remember { mutableStateOf(false) }

    var complianceChecked by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAshaPendingDialog by remember { mutableStateOf(false) }

    val isDark = ThemeManager.isDarkMode.value

    val validateUsername = { userStr: String ->
        val trimmed = userStr.trim()
        when {
            trimmed.isBlank() -> "Username is required."
            trimmed.length < 3 -> "Username must be at least 3 characters."
            !trimmed.matches(Regex("^[a-zA-Z0-9._-]+$")) -> "Username can only contain letters, numbers, dots, hyphens, and underscores."
            else -> null
        }
    }

    // Auto-update defaults based on selected cadre
    LaunchedEffect(selectedCadre) {
        if (selectedCadre == "asha") {
            if (idCode.startsWith("ABHA")) idCode = "ASHA-NER-4402"
            if (fullName == "Rahim Ali") fullName = "Anita Deka"
            if (username == "rahim.ali" || username.isBlank()) username = "anita.deka"
            if (email == "rahim.ali@patient.org" || email.isBlank()) email = "anita.deka@nhm.assam.gov.in"
        } else {
            if (idCode.startsWith("ASHA")) idCode = "ABHA-9821-4412"
            if (fullName == "Anita Deka") fullName = "Rahim Ali"
            if (username == "anita.deka" || username.isBlank()) username = "rahim.ali"
            if (email == "anita.deka@nhm.assam.gov.in" || email.isBlank()) email = "rahim.ali@patient.org"
        }
        usernameError = null
    }

    val submitRegistration = {
        val uErr = validateUsername(username)
        if (uErr != null) {
            usernameError = uErr
            errorMessage = uErr
        } else if (!complianceChecked) {
            errorMessage = "Please confirm the compliance statement to continue."
        } else if (password != confirmPassword) {
            errorMessage = "Passwords do not match."
        } else if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
        } else if (fullName.isBlank() || email.isBlank()) {
            errorMessage = "Please fill in all required profile fields."
        } else {
            isSubmitting = true
            errorMessage = null
            usernameError = null

            val cleanUsername = username.trim().lowercase()

            if (selectedCadre == "asha") {
                val result = authRepository.registerAshaWorker(
                    username = cleanUsername,
                    fullName = fullName,
                    email = email,
                    password = password,
                    assignedCenter = "PHC Rampur - Sub-Center 04",
                    assignedRegion = "Kamrup Rural, Assam-NER"
                )
                isSubmitting = false
                if (result.isSuccess) {
                    showAshaPendingDialog = true
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed. Please check inputs."
                }
            } else {
                val result = authRepository.registerPatientUser(
                    username = cleanUsername,
                    fullName = fullName,
                    email = email,
                    password = password,
                    assignedCenter = "Citizen Self-Care Portal",
                    assignedRegion = "Kamrup Rural, Assam-NER"
                )
                isSubmitting = false
                if (result.isSuccess) {
                    val registeredPatient = result.getOrThrow()
                    authRepository.setCurrentUser(registeredPatient)
                    Toast.makeText(
                        context,
                        "Registration successful. Welcome to ARTHROSCAN-NER.",
                        Toast.LENGTH_LONG
                    ).show()
                    if (onPatientRegistered != null) {
                        onPatientRegistered(registeredPatient)
                    } else {
                        onRegisterSuccess("Registration successful. Welcome to ARTHROSCAN-NER.")
                    }
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed. Please check inputs."
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFFF8FAFC)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork Backdrop with Light Clinical Scrim
            Image(
                painter = painterResource(id = R.drawable.bg_arthroscan_knee),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFAFBFF).copy(alpha = 0.90f),
                                Color(0xFFF1F5F9).copy(alpha = 0.88f),
                                Color(0xFFFAFBFF).copy(alpha = 0.94f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ==============================================================
                // 1. TOP APP BAR
                // ==============================================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.85f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onBackToLogin,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to Login",
                                    tint = Color(0xFF131B2E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "ARTHROSCAN-NER",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF004AC6),
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "Register / Request Access",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // SIH26004 tag (hidden on very small widths, compact badge)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFE2E8FF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF006A61), CircleShape)
                                    )
                                    Text(
                                        text = "SIH26004",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF004AC6)
                                        )
                                    )
                                }
                            }

                            // Theme toggle button
                            IconButton(
                                onClick = { ThemeManager.toggleTheme() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Toggle Theme",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Profile avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF004AC6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AD",
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ==============================================================
                    // 2. HERO BANNER CARD
                    // ==============================================================
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEBF3FF),
                        border = BorderStroke(1.dp, Color(0xFFD6E4FF))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF004AC6), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HowToReg,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Registration & Access Portal",
                                        style = TextStyle(
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = Color(0xFF131B2E)
                                        )
                                    )
                                    Text(
                                        text = "Submit cadre credentials or register for citizen mobility screening",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF434655)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 3. ROLE SELECTOR TABS
                    // ==============================================================
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // ASHA Cadre Tab
                            val isAsha = selectedCadre == "asha"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCadre = "asha" },
                                shape = RoundedCornerShape(9.dp),
                                color = if (isAsha) Color.White else Color.Transparent,
                                shadowElevation = if (isAsha) 2.dp else 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = if (isAsha) Color(0xFF004AC6) else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "ASHA Cadre",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isAsha) Color(0xFF131B2E) else Color(0xFF64748B)
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "District Approval",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 9.5.sp,
                                            color = Color(0xFF004AC6)
                                        )
                                    )
                                }
                            }

                            // Patient / User Tab
                            val isPatient = selectedCadre == "patient"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCadre = "patient" },
                                shape = RoundedCornerShape(9.dp),
                                color = if (isPatient) Color.White else Color.Transparent,
                                shadowElevation = if (isPatient) 2.dp else 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isPatient) Color(0xFF004AC6) else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Patient / User",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isPatient) Color(0xFF131B2E) else Color(0xFF64748B)
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF86F2E4).copy(alpha = 0.45f)
                                    ) {
                                        Text(
                                            text = "Instant Sign-Up",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF006F66)
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Error banner if any
                    if (errorMessage != null) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Text(errorMessage!!, style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.5.sp, color = Color(0xFFB91C1C)))
                            }
                        }
                    }

                    // ==============================================================
                    // 4. DEMOGRAPHIC & CADRE PROFILE CARD
                    // ==============================================================
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = Color(0xFF004AC6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Demographic & Cadre Profile",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                )
                            }

                            // Full Legal Name
                            Column {
                                Text(
                                    text = "Full Legal Name",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF434655)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    leadingIcon = {
                                        Icon(Icons.Default.PersonOutline, null, tint = Color(0xFF737686), modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF004AC6),
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF131B2E),
                                        unfocusedTextColor = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Required Username Field (for both ASHA and Patient)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Username",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF434655)
                                        )
                                    )
                                    Text(
                                        text = "Required",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF004AC6)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it
                                        usernameError = validateUsername(it)
                                    },
                                    isError = usernameError != null,
                                    placeholder = {
                                        Text(
                                            text = if (selectedCadre == "asha") "e.g. anita.deka" else "e.g. rahim.ali",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 12.5.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AlternateEmail,
                                            null,
                                            tint = if (usernameError != null) Color(0xFFDC2626) else Color(0xFF737686),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_username_input"),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF004AC6),
                                        unfocusedBorderColor = Color.Transparent,
                                        errorContainerColor = Color(0xFFFEE2E2).copy(alpha = 0.5f),
                                        errorBorderColor = Color(0xFFDC2626),
                                        focusedTextColor = Color(0xFF131B2E),
                                        unfocusedTextColor = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                if (usernameError != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = usernameError!!,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFDC2626)
                                        ),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            // Email
                            Column {
                                Text(
                                    text = "Official / Personal Email",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF434655)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    leadingIcon = {
                                        Icon(Icons.Default.MailOutline, null, tint = Color(0xFF737686), modifier = Modifier.size(18.dp))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF004AC6),
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF131B2E),
                                        unfocusedTextColor = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Cadre / Citizen ID
                            Column {
                                Text(
                                    text = if (selectedCadre == "asha") "NHM Cadre Identifier / License Code" else "Citizen Portal ID / ABHA ID",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF434655)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = idCode,
                                    onValueChange = { idCode = it },
                                    leadingIcon = {
                                        Icon(Icons.Default.AssignmentInd, null, tint = Color(0xFF737686), modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = if (selectedCadre == "asha") null else {
                                        {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFECFDF5)
                                            ) {
                                                Text(
                                                    text = "VERIFIED",
                                                    style = TextStyle(
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF006A61)
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF004AC6),
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF131B2E),
                                        unfocusedTextColor = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Biometric Gender Segmented Control
                            Column {
                                Text(
                                    text = "Biometric Gender",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF434655)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val genders = listOf("Female", "Male", "Other")
                                    genders.forEach { gender ->
                                        val isSelected = selectedGender == gender
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedGender = gender },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xFF004AC6) else Color(0xFFF2F3FF)
                                        ) {
                                            Text(
                                                text = gender,
                                                style = TextStyle(
                                                    fontFamily = SpaceGroteskFontFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.5.sp,
                                                    color = if (isSelected) Color.White else Color(0xFF131B2E)
                                                ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 9.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Age & Date of Birth
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Age Stepper
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Patient Age",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF434655)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF2F3FF)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton(
                                                onClick = { if (age > 18) age-- },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color.White, RoundedCornerShape(6.dp))
                                            ) {
                                                Icon(Icons.Default.Remove, null, tint = Color(0xFF131B2E), modifier = Modifier.size(16.dp))
                                            }

                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = "$age",
                                                    style = TextStyle(
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = Color(0xFF131B2E)
                                                    )
                                                )
                                                Text(
                                                    text = "Yrs",
                                                    style = TextStyle(
                                                        fontFamily = SpaceGroteskFontFamily,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF737686)
                                                    )
                                                )
                                            }

                                            IconButton(
                                                onClick = { if (age < 100) age++ },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color.White, RoundedCornerShape(6.dp))
                                            ) {
                                                Icon(Icons.Default.Add, null, tint = Color(0xFF131B2E), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                // Date of Birth
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Date of Birth",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF434655)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = dob,
                                        onValueChange = { dob = it },
                                        leadingIcon = {
                                            Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF737686), modifier = Modifier.size(16.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFF2F3FF),
                                            unfocusedContainerColor = Color(0xFFF2F3FF),
                                            focusedBorderColor = Color(0xFF004AC6),
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = Color(0xFF131B2E),
                                            unfocusedTextColor = Color(0xFF131B2E)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 5. SCREENING GATEWAY CREDENTIALS CARD
                    // ==============================================================
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF004AC6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Screening Gateway Credentials",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = Color(0xFF131B2E)
                                    )
                                )
                            }

                            // Create Passcode
                            Column {
                                Text(
                                    text = "Create Secure PIN / Passcode",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF434655)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, null, tint = Color(0xFF737686), modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passVisible1 = !passVisible1 }) {
                                            Icon(
                                                imageVector = if (passVisible1) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = Color(0xFF737686),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    visualTransformation = if (passVisible1) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF004AC6),
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF131B2E),
                                        unfocusedTextColor = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Confirm Passcode
                            Column {
                                Text(
                                    text = "Confirm Gateway Passcode",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF434655)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    leadingIcon = {
                                        Icon(Icons.Default.EnhancedEncryption, null, tint = Color(0xFF737686), modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passVisible2 = !passVisible2 }) {
                                            Icon(
                                                imageVector = if (passVisible2) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = Color(0xFF737686),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    visualTransformation = if (passVisible2) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF2F3FF),
                                        unfocusedContainerColor = Color(0xFFF2F3FF),
                                        focusedBorderColor = Color(0xFF004AC6),
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF131B2E),
                                        unfocusedTextColor = Color(0xFF131B2E)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // ==============================================================
                    // 6. ASHA WORKER SECTION (CONDITIONAL)
                    // ==============================================================
                    if (selectedCadre == "asha") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF004AC6), modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Aadhaar Card Proof",
                                            style = TextStyle(
                                                fontFamily = SoraFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF86F2E4).copy(alpha = 0.4f)
                                    ) {
                                        Text(
                                            text = "Mandatory",
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFF006F66)
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF2F3FF)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color(0xFFDAE2FD), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.CloudUpload, null, tint = Color(0xFF004AC6), modifier = Modifier.size(22.dp))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Upload PDF",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF131B2E)
                                            )
                                        )
                                        Text(
                                            text = "Scanned Aadhaar (PDF ONLY, MAX 10 MB)",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF737686)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFDAE2FD)
                                        ) {
                                            Text(
                                                text = "Select Document",
                                                style = TextStyle(
                                                    fontFamily = SpaceGroteskFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF004AC6)
                                                ),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Attached document status
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEAEDFF)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFF006F66), modifier = Modifier.size(20.dp))
                                            Text(
                                                text = "aadhaar_verification_anita_deka.pdf",
                                                style = TextStyle(
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF131B2E)
                                                ),
                                                maxLines = 1
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Sync, null, tint = Color(0xFF434655), modifier = Modifier.size(16.dp))
                                            Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==============================================================
                    // 7. COMPLIANCE STATEMENT
                    // ==============================================================
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable { complianceChecked = !complianceChecked },
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = complianceChecked,
                                onCheckedChange = { complianceChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF004AC6),
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = "I confirm that the information provided is accurate and complete and may be used for official health-service purposes.",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF131B2E)
                                )
                            )
                        }
                    }

                    // ==============================================================
                    // 8. PRIMARY SUBMIT CTA
                    // ==============================================================
                    Button(
                        onClick = submitRegistration,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004AC6)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isSubmitting) {
                                    "PROCESSING SIGN-UP..."
                                } else if (selectedCadre == "asha") {
                                    "SUBMIT REGISTRATION FOR APPROVAL"
                                } else {
                                    "COMPLETE INSTANT SIGN-UP"
                                },
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // ==============================================================
                    // 9. RETURN TO LOGIN LINK
                    // ==============================================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBackToLogin() }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color(0xFF004AC6),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Already registered? Return to Login Gate",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF004AC6)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showAshaPendingDialog) {
        AlertDialog(
            onDismissRequest = {
                showAshaPendingDialog = false
                onRegisterSuccess("Your ASHA Cadre registration has been submitted successfully. Approval is pending by the Administrator.")
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF006A61),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Registration Successful",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF131B2E)
                        )
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your ASHA Cadre registration has been submitted successfully.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF131B2E)
                        )
                    )
                    Text(
                        text = "Approval is pending by the Administrator.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAshaPendingDialog = false
                        onRegisterSuccess("Your ASHA Cadre registration has been submitted successfully. Approval is pending by the Administrator.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004AC6)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("asha_pending_dialog_confirm_button")
                ) {
                    Text(
                        text = "OK / CONTINUE",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

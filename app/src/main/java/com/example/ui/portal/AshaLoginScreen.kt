package com.example.ui.portal

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.AuthRepository
import com.example.auth.GoogleAuthService
import com.example.auth.UserAccount
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ARTHROSCAN-NER LOGIN GATE — FINAL PREMIUM REDESIGN
 * Official clinical diagnostic access portal
 * Clean, unified, accessible, and high-fidelity interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshaLoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (UserAccount) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthService = remember { GoogleAuthService(context) }
    var isDarkState by remember { mutableStateOf(false) } // Default clean Light Mode
    val isDark = isDarkState

    var isRegisterMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("superadmin") }
    var selectedRoleName by remember { mutableStateOf("Super Admin") }
    var isDropdownOpen by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("ASHA-NER-4402") }
    var password by remember { mutableStateOf("asha@123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Registration state (preserved for compatibility)
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regCenter by remember { mutableStateOf("PHC Rampur - Sub-Center 04") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isDropdownOpen) 180f else 0f,
        label = "chevronRotation"
    )

    val performLogin = {
        errorMessage = null
        infoMessage = null
        isAuthenticating = true
        if (isRegisterMode) {
            val regResult = authRepository.registerPublicUser(
                username = username,
                fullName = regFullName.ifBlank { "Field Health Worker" },
                email = regEmail.ifBlank { "$username@nhm.assam.gov.in" },
                password = password,
                assignedCenter = regCenter
            )
            isAuthenticating = false
            if (regResult.isSuccess) {
                infoMessage = "Registration submitted successfully. Account is pending supervisor approval."
                isRegisterMode = false
            } else {
                errorMessage = regResult.exceptionOrNull()?.message ?: "Registration failed."
            }
        } else {
            val loginResult = authRepository.login(username, password)
            isAuthenticating = false
            if (loginResult.isSuccess) {
                onLoginSuccess(loginResult.getOrThrow())
            } else {
                errorMessage = loginResult.exceptionOrNull()?.message ?: "Invalid username or password."
            }
        }
    }

    val performGoogleLogin = {
        errorMessage = null
        infoMessage = null
        isAuthenticating = true
        coroutineScope.launch {
            val googleResult = googleAuthService.signIn()
            if (googleResult.isSuccess) {
                val email = googleResult.getOrThrow()
                val userResult = authRepository.loginWithGoogleEmail(email)
                isAuthenticating = false
                if (userResult.isSuccess) {
                    onLoginSuccess(userResult.getOrThrow())
                } else {
                    errorMessage = userResult.exceptionOrNull()?.message ?: "Account not authorized.\nContact your administrator."
                }
            } else {
                isAuthenticating = false
                val exception = googleResult.exceptionOrNull()
                errorMessage = exception?.message ?: "Google authentication could not be completed."
            }
        }
        Unit
    }

    // Role switcher helper
    val switchRole = { roleId: String, roleTitle: String, defaultUser: String, defaultPass: String ->
        selectedRole = roleId
        selectedRoleName = roleTitle
        username = defaultUser
        password = defaultPass
        isDropdownOpen = false
        errorMessage = null
    }

    // If user clicked registration, route cleanly to registration screen
    if (isRegisterMode) {
        AshaRegisterScreen(
            authRepository = authRepository,
            onBackToLogin = {
                isRegisterMode = false
                errorMessage = null
            },
            onRegisterSuccess = { msg ->
                isRegisterMode = false
                infoMessage = msg
                errorMessage = null
            },
            onPatientRegistered = { patient ->
                isRegisterMode = false
                onLoginSuccess(patient)
            }
        )
        return
    }

    // Unified Design Palette (Consistent Tokens)
    val bgBase = if (isDark) Color(0xFF07111F) else Color(0xFFF7FAFC)
    val cardSurface = if (isDark) Color(0xFF0B172A).copy(alpha = 0.92f) else Color(0xFFFFFFFF).copy(alpha = 0.93f)
    val secondarySurface = if (isDark) Color(0xFF10233D) else Color(0xFFF1F5F9)
    val borderStrokeColor = if (isDark) Color(0xFF164E63).copy(alpha = 0.65f) else Color(0xFFCBD5E1)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val accentPrimary = Color(0xFF2563EB)
    val accentCyan = if (isDark) Color(0xFF22D3EE) else Color(0xFF0891B2)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = bgBase
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // ==============================================================
            // 1. FULL-BLEED KNEE TECH BACKGROUND WITH SUBTLE READABILITY OVERLAY
            // ==============================================================
            Image(
                painter = painterResource(id = R.drawable.bg_arthroscan_firstpage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopEnd
            )

            // Scrim Overlay: Soft gradient preserving background details while ensuring AAA contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFF07111F).copy(alpha = 0.88f),
                                    Color(0xFF07111F).copy(alpha = 0.92f),
                                    Color(0xFF07111F).copy(alpha = 0.97f)
                                )
                            } else {
                                listOf(
                                    Color(0xFFF7FAFC).copy(alpha = 0.88f),
                                    Color(0xFFF7FAFC).copy(alpha = 0.92f),
                                    Color(0xFFF7FAFC).copy(alpha = 0.97f)
                                )
                            }
                        )
                    )
            )

            // ==============================================================
            // 2. UNIFIED CONTENT COLUMN
            // ==============================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ----------------------------------------------------------
                // TOP: THEME TOGGLE ONLY (CLEAN, NO HACKATHON BADGES)
                // ----------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = secondarySurface,
                        border = BorderStroke(1.dp, borderStrokeColor),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { isDarkState = !isDarkState }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF1E293B),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ----------------------------------------------------------
                // 3. BRANDING SECTION (COMPACT ~18-22% SCREEN HEIGHT)
                // Logo transparent over background — NO white rectangular box
                // ----------------------------------------------------------
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arthroscan_emblem),
                        contentDescription = "ARTHROSCAN Logo",
                        modifier = Modifier
                            .size(76.dp)
                            .padding(bottom = 4.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "ARTHROSCAN-NER",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp,
                            letterSpacing = 1.5.sp,
                            color = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "KNEE HEALTH MONITORING",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 2.sp,
                            color = accentCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ----------------------------------------------------------
                // 4. MAIN LOGIN HEADLINE
                // ----------------------------------------------------------
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "WELCOME TO ARTHROSCAN",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Secure access to your health-service workspace",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ----------------------------------------------------------
                // 6. ACCESS PORTAL (COMPACT PREVIEW SELECTOR)
                // ----------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ACCESS PORTAL",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = accentCyan
                        ),
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownOpen = !isDropdownOpen },
                        shape = RoundedCornerShape(18.dp),
                        color = cardSurface,
                        border = BorderStroke(1.dp, borderStrokeColor),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = accentPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (selectedRole) {
                                                    "asha" -> Icons.Default.MedicalServices
                                                    "district" -> Icons.Default.AdminPanelSettings
                                                    "superadmin" -> Icons.Default.Shield
                                                    else -> Icons.Default.Person
                                                },
                                                contentDescription = null,
                                                tint = accentPrimary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = selectedRoleName,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = textPrimary
                                        )
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Role Dropdown",
                                    tint = textSecondary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(chevronRotation)
                                )
                            }

                            // Expandable Role Items
                            AnimatedVisibility(
                                visible = isDropdownOpen,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val roles = listOf(
                                        Triple("patient", "User / Patient", Pair("patient.user", "asha@123")),
                                        Triple("asha", "ASHA Worker", Pair("ASHA-NER-4402", "asha@123")),
                                        Triple("district", "District Admin", Pair("admin.dho", "admin@123")),
                                        Triple("superadmin", "Super Admin", Pair("superadmin", "superadmin@123"))
                                    )

                                    roles.forEach { (roleId, title, creds) ->
                                        val isCurrent = selectedRole == roleId
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    switchRole(roleId, title, creds.first, creds.second)
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isCurrent) {
                                                accentPrimary.copy(alpha = if (isDark) 0.20f else 0.10f)
                                            } else {
                                                Color.Transparent
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = TextStyle(
                                                        fontFamily = SpaceGroteskFontFamily,
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp,
                                                        color = if (isCurrent) accentPrimary else textPrimary
                                                    )
                                                )

                                                if (isCurrent) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = accentPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ----------------------------------------------------------
                // 7. GOOGLE AUTHENTICATION BUTTON
                // ----------------------------------------------------------
                Button(
                    onClick = performGoogleLogin,
                    enabled = !isAuthenticating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_login_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardSurface,
                        contentColor = textPrimary
                    ),
                    border = BorderStroke(1.dp, borderStrokeColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ----------------------------------------------------------
                // 8. SUBTLE DIVIDER: ────────  OR  ────────
                // ----------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(borderStrokeColor.copy(alpha = 0.7f))
                    )
                    Text(
                        text = "OR",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = textSecondary
                        )
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(borderStrokeColor.copy(alpha = 0.7f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ----------------------------------------------------------
                // 9. COMPACT UNIFIED AUTHENTICATION SURFACE
                // ----------------------------------------------------------
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardSurface,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Error Banner (if any)
                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = errorMessage ?: "",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF991B1B)
                                        )
                                    )
                                }
                            }
                        }

                        // Info Banner (if any)
                        if (infoMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = infoMessage ?: "",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF166534)
                                        )
                                    )
                                }
                            }
                        }

                        // USERNAME / CADRE ID
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "USERNAME / CADRE ID",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textSecondary
                                )
                            )

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = {
                                    Text(
                                        "e.g. ASHA-NER-4402",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 13.sp,
                                        color = textSecondary.copy(alpha = 0.6f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = accentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_username_input"),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = secondarySurface,
                                    unfocusedContainerColor = secondarySurface,
                                    focusedBorderColor = accentPrimary,
                                    unfocusedBorderColor = borderStrokeColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                        }

                        // SECURITY PIN / PASSWORD
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "SECURITY PIN / PASSWORD",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textSecondary
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = {
                                    Text(
                                        "Enter password",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 13.sp,
                                        color = textSecondary.copy(alpha = 0.6f)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = accentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = secondarySurface,
                                    unfocusedContainerColor = secondarySurface,
                                    focusedBorderColor = accentPrimary,
                                    unfocusedBorderColor = borderStrokeColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 16. PRIMARY LOGIN CTA: SIGN IN TO ARTHROSCAN →
                        Button(
                            onClick = performLogin,
                            enabled = !isAuthenticating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(15.dp),
                                    ambientColor = accentPrimary.copy(alpha = 0.35f),
                                    spotColor = accentCyan.copy(alpha = 0.40f)
                                )
                                .testTag("login_submit_btn"),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF2563EB),
                                                Color(0xFF0891B2),
                                                Color(0xFF0D9488)
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
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SIGN IN TO ARTHROSCAN",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            letterSpacing = 0.8.sp,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ----------------------------------------------------------
                // BOTTOM: NEW USER / FIELD WORKER ACCESS REQUEST LINK
                // ----------------------------------------------------------
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            isRegisterMode = true
                            errorMessage = null
                            infoMessage = null
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = cardSurface,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = accentPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New User/Field Worker? Request Access",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = textPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Official Google Multi-Color 'G' Logo rendered vectorially
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = w * 0.22f
        val radius = (w - stroke) / 2f

        // Google 4 Official Colors
        val blue = Color(0xFF4285F4)
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)

        // Red top arc
        drawArc(
            color = red,
            startAngle = 180f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke)
        )

        // Yellow left arc
        drawArc(
            color = yellow,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke)
        )

        // Green bottom arc
        drawArc(
            color = green,
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke)
        )

        // Blue right arc
        drawArc(
            color = blue,
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke)
        )

        // Blue horizontal crossbar
        val barHeight = stroke * 0.95f
        drawRect(
            color = blue,
            topLeft = Offset(cx - stroke * 0.1f, cy - barHeight / 2f),
            size = Size(radius + stroke * 0.5f, barHeight)
        )
    }
}

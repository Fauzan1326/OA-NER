package com.example.ui.portal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

/**
 * ARTHROSCAN-NER FIRST PAGE / ONBOARDING SCREEN
 * High-fidelity implementation matching exact user spec & media_1790655205377.png
 * Smart India Hackathon 2026 | Team GOD'S PLAN
 */
@Composable
fun ArthroscanFirstScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDark by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var isLangMenuExpanded by remember { mutableStateOf(false) }

    val languages = listOf("English", "हिंदी", "অসমীয়া", "বাংলা")

    // Theme color palette matching exact HTML specification
    val bgBase = if (isDark) Color(0xFF050B14) else Color(0xFFFAFBFF)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val quoteCardBg = if (isDark) Color(0xFF0A1628).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f)
    val quoteCardBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFFBFDBFE).copy(alpha = 0.9f)
    val quoteTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val quoteAuthorColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = bgBase
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==============================================================
            // 1. KNEE SENSING ARTWORK BACKGROUND WITH CALIBRATED SCRIM
            // ==============================================================
            Image(
                painter = painterResource(id = R.drawable.bg_arthroscan_firstpage),
                contentDescription = "Medical Knee Visual Background",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterEnd),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopEnd
            )

            // Scrim Overlay: Gradient from solid on left to translucent on right
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to (if (isDark) Color(0xFF050B14).copy(alpha = 0.98f) else Color(0xFFFFFFFF).copy(alpha = 0.96f)),
                                0.45f to (if (isDark) Color(0xFF071525).copy(alpha = 0.92f) else Color(0xFFFFFFFF).copy(alpha = 0.90f)),
                                0.80f to (if (isDark) Color(0xFF071525).copy(alpha = 0.50f) else Color(0xFFFFFFFF).copy(alpha = 0.40f)),
                                1.0f to (if (isDark) Color(0xFF071525).copy(alpha = 0.20f) else Color(0xFFFFFFFF).copy(alpha = 0.10f))
                            )
                        )
                    )
            )

            // Vertical gradient for top & bottom contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to (if (isDark) Color(0xFF050B14).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.40f)),
                                0.40f to Color.Transparent,
                                0.85f to (if (isDark) Color(0xFF050B14).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                                1.0f to (if (isDark) Color(0xFF050B14) else Color.White)
                            )
                        )
                    )
            )

            // ==============================================================
            // 2. MAIN SCROLLABLE CONTENT (Z-INDEX 10)
            // ==============================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ----------------------------------------------------------
                // TOP BAR: LANGUAGE SELECTOR & THEME TOGGLE
                // ----------------------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Dropdown Pill
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier.clickable { isLangMenuExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Language",
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = selectedLanguage,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isLangMenuExpanded,
                            onDismissRequest = { isLangMenuExpanded = false },
                            modifier = Modifier.background(
                                if (isDark) Color(0xFF0F172A) else Color.White
                            )
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = lang,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedLanguage == lang) {
                                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                                                } else {
                                                    if (isDark) Color(0xFFCBD5E1) else Color(0xFF1E293B)
                                                }
                                            )
                                            if (selectedLanguage == lang) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedLanguage = lang
                                        isLangMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Theme Toggle Circular Button
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { isDark = !isDark }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF1E293B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ----------------------------------------------------------
                // CENTER HERO: EMBLEM LOGO & BRAND HEADERS
                // ----------------------------------------------------------
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emblem Logo with soft luminous elevation
                    Image(
                        painter = painterResource(id = R.drawable.ic_arthroscan_emblem),
                        contentDescription = "ARTHROSCAN Emblem Logo",
                        modifier = Modifier
                            .size(190.dp)
                            .padding(bottom = 6.dp),
                        contentScale = ContentScale.Fit
                    )

                    // ARTHROSCAN Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ARTHRO",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 27.sp,
                                letterSpacing = 2.sp,
                                color = textPrimary
                            )
                        )
                        Text(
                            text = "SCAN",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 27.sp,
                                letterSpacing = 2.sp,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // KNEE HEALTH MONITORING with subtle horizontal lines
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(1.dp)
                                .background(if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF2563EB).copy(alpha = 0.4f))
                        )
                        Text(
                            text = "KNEE HEALTH MONITORING",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 2.5.sp,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(1.dp)
                                .background(if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF2563EB).copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Move Better Live Brighter Headlines
                    Text(
                        text = "Move Better",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            lineHeight = 42.sp,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    // "Live Brighter" with Multi-color Gradient
                    Text(
                        text = "Live Brighter",
                        style = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            lineHeight = 42.sp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2563EB),
                                    Color(0xFF0891B2),
                                    Color(0xFF059669)
                                )
                            ),
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Supporting description
                    Text(
                        text = "Bringing intelligent knee health screening closer to every community, supporting earlier attention and informed care.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Refined Borderless Translucent Editorial Quote Treatment
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDark) Color(0xFF0A182F).copy(alpha = 0.35f)
                                else Color(0xFFFFFFFF).copy(alpha = 0.40f)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Large Opening Quotation Mark (Top-Left)
                        Text(
                            text = "“",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                lineHeight = 36.sp,
                                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)).copy(alpha = 0.30f)
                            ),
                            modifier = Modifier.align(Alignment.TopStart)
                        )

                        // Main Quote Text & Author (Centered)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "A healthy outside starts from inside",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.5.sp,
                                    lineHeight = 20.sp,
                                    color = quoteTextColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— ROBERT URICH",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp,
                                    color = quoteAuthorColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }

                        // Large Closing Quotation Mark (Bottom-Right)
                        Text(
                            text = "”",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                lineHeight = 36.sp,
                                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)).copy(alpha = 0.30f)
                            ),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ----------------------------------------------------------
                // BOTTOM ACTION: VIBRANT GRADIENT GET STARTED BUTTON
                // ----------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(31.dp),
                                ambientColor = Color(0xFF2563EB).copy(alpha = 0.4f),
                                spotColor = Color(0xFF0891B2).copy(alpha = 0.5f)
                            )
                            .testTag("get_started_btn"),
                        shape = RoundedCornerShape(31.dp),
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
                                            Color(0xFF0D9488),
                                            Color(0xFF10B981)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(31.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "GET STARTED",
                                    style = TextStyle(
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        letterSpacing = 2.sp,
                                        color = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                // Circular disc with Arrow icon
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color.White.copy(alpha = 0.22f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Get Started",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
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

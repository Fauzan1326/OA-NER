package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// ARTHROSCAN-NER LIGHT / DARK ADAPTIVE PALETTE
// Light-First default with dark mode support
// ============================================================

val SurfaceLevel0_Background: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF0F172A) else Color(0xFFFFFFFF)

val SurfaceLevel1_Primary: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF111827) else Color(0xFFFFFFFF)

val SurfaceLevel2_Elevated: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E293B) else Color(0xFFF8FAFC)

val SurfaceLevel2_Card: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF172033) else Color(0xFFFFFFFF)

val SurfaceLevel3_Active: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF0D1E33) else Color(0xFFEFF6FF)

val SurfaceLevel3_ActiveBorder: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E3A5F) else Color(0xFFBFDBFE)

val SurfaceLevel4_Warning: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF261810) else Color(0xFFFFFBEB)

val SurfaceLevel4_Error: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF261214) else Color(0xFFFEF2F2)

val SurfaceLevel5_Info: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF151426) else Color(0xFFFAF5FF)

val SurfaceControl: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val SurfaceControlBorder: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF334155) else Color(0xFFE5E7EB)

val SurfaceSubtleBorder: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E293B) else Color(0xFFE5E7EB)

val ArthroscanBlue = Color(0xFF2563EB)
val ArthroscanBlueBright: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF3B82F6) else Color(0xFF2563EB)

val ArthroscanBlueDim = Color(0xFF1D4ED8)
val RfAccentPurple = Color(0xFF7C3AED)

val RfSurfaceDark: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF1E142B) else Color(0xFFFAF5FF)

val MedicalAccentTeal = Color(0xFF06B6D4)

val MedicalTealDark: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF08262E) else Color(0xFFF0FDFA)

val StatusPassGreen = Color(0xFF059669)

val StatusPassGreenDark: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF0A291E) else Color(0xFFECFDF5)

val StatusWarningAmber = Color(0xFFD97706)

val StatusWarningDark: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF2E1F0A) else Color(0xFFFFFBEB)

val StatusFailRed = Color(0xFFDC2626)

val StatusFailRedDark: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF2D0F12) else Color(0xFFFEF2F2)

val StatusStandbyGray = Color(0xFF64748B)

val StatusStandbyDark: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF17202A) else Color(0xFFF1F5F9)

val StatusExperimentalPurple = Color(0xFF7C3AED)

val TextNearWhite: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFFF8FAFC) else Color(0xFF0F172A)

val TextCoolGray: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFFCBD5E1) else Color(0xFF475569)

val TextGrayBlue: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF94A3B8) else Color(0xFF64748B)

val TextTechnicalLabel: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF38BDF8) else Color(0xFF2563EB)

val TextMonospaceHighlight: Color
    @Composable get() = if (ThemeManager.isDarkMode.value) Color(0xFF38BDF8) else Color(0xFF0284C7)

// Legacy compatibility
val Purple80 = RfAccentPurple
val PurpleGrey80 = Color(0xFFCBD5E1)
val Pink80 = StatusFailRed
val Purple40 = ArthroscanBlue
val PurpleGrey40 = Color(0xFF172033)
val Pink40 = StatusWarningAmber


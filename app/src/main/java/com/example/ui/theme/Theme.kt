package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Typography

// ============================================================
// SCIENTIFIC LIGHT COLOR SCHEME (LIGHT MODE DEFAULT)
// Crisp, clean scientific research interface
// ============================================================
private val ScientificLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),          // Primary Accent Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFF6FF), // Soft blue container
    onPrimaryContainer = Color(0xFF1D4ED8),
    inversePrimary = Color(0xFF93C5FD),

    secondary = Color(0xFF06B6D4),        // Secondary Accent Teal
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0FDFA),
    onSecondaryContainer = Color(0xFF0E7490),

    tertiary = Color(0xFF7C3AED),         // Research Accent Purple
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAF5FF),
    onTertiaryContainer = Color(0xFF6D28D9),

    background = Color(0xFFF5F7FA),       // Background #F5F7FA
    onBackground = Color(0xFF0F172A),     // Primary text #0F172A

    surface = Color(0xFFFFFFFF),          // Primary Surface #FFFFFF
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),   // Secondary Surface #F8FAFC
    onSurfaceVariant = Color(0xFF475569), // Secondary text #475569

    surfaceTint = Color(0xFF2563EB),
    inverseSurface = Color(0xFF0F172A),
    inverseOnSurface = Color(0xFFF5F7FA),

    error = Color(0xFFDC2626),            // Error #DC2626
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEF2F2),
    onErrorContainer = Color(0xFF991B1B),

    outline = Color(0xFFE2E8F0),          // Borders #E2E8F0
    outlineVariant = Color(0xFFCBD5E1),
    scrim = Color(0xFF000000)
)

// ============================================================
// PREMIUM AI CONTROL CENTER COLOR SCHEME (DARK MODE)
// Deep dark surfaces #0F172A, #111827, #1E293B
// ============================================================
private val PremiumDarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFF8FAFC),
    primaryContainer = Color(0xFF172033),
    onPrimaryContainer = Color(0xFF38BDF8),
    inversePrimary = Color(0xFF2563EB),

    secondary = Color(0xFF06B6D4),
    onSecondary = Color(0xFFF8FAFC),
    secondaryContainer = Color(0xFF08262E),
    onSecondaryContainer = Color(0xFF22D3EE),

    tertiary = Color(0xFFA855F7),
    onTertiary = Color(0xFFF8FAFC),
    tertiaryContainer = Color(0xFF1E142B),
    onTertiaryContainer = Color(0xFFC084FC),

    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),

    surfaceTint = Color(0xFF3B82F6),
    inverseSurface = Color(0xFFF8FAFC),
    inverseOnSurface = Color(0xFF0F172A),

    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF2D0F12),
    onErrorContainer = Color(0xFFF87171),

    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    scrim = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = when (ThemeManager.themeMode.value) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        else -> false
    },
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Keep isDarkMode updated for dynamic color getters
    ThemeManager.setDarkMode(darkTheme)

    val colorScheme = if (darkTheme) PremiumDarkColorScheme else ScientificLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


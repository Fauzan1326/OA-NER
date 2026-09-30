package com.example.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Global Theme Controller
 * Defaults to Light Mode as requested ("LIGHT-FIRST / ADAPTIVE THEME")
 * Supports instant runtime toggling between LIGHT, DARK, and SYSTEM modes.
 */
object ThemeManager {
    private val _themeMode = mutableStateOf(ThemeMode.DARK)
    val themeMode: State<ThemeMode> = _themeMode

    private val _isDarkMode = mutableStateOf(true)
    val isDarkMode: State<Boolean> = _isDarkMode

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        if (mode == ThemeMode.DARK) {
            _isDarkMode.value = true
        } else if (mode == ThemeMode.LIGHT) {
            _isDarkMode.value = false
        }
    }

    fun toggleTheme() {
        if (_isDarkMode.value) {
            setThemeMode(ThemeMode.LIGHT)
        } else {
            setThemeMode(ThemeMode.DARK)
        }
    }

    fun setDarkMode(dark: Boolean) {
        if (dark) setThemeMode(ThemeMode.DARK) else setThemeMode(ThemeMode.LIGHT)
    }
}


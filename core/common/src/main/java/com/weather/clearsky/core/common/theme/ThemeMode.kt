package com.weather.clearsky.core.common.theme

/**
 * Theme mode enum for app-wide theme management
 */
enum class ThemeMode(val value: String, val displayName: String) {
    SYSTEM("system", "Follow system"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark")
} 
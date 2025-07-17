package com.weather.clearsky.core.common.theme

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app theme changes and applies them system-wide
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Apply theme based on ThemeMode
     */
    fun applyTheme(themeMode: ThemeMode) {
        val nightMode = when (themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
    
    /**
     * Get current theme mode from AppCompatDelegate
     */
    fun getCurrentThemeMode(): ThemeMode {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> ThemeMode.DARK
            AppCompatDelegate.MODE_NIGHT_NO -> ThemeMode.LIGHT
            else -> ThemeMode.SYSTEM
        }
    }
    
    /**
     * Check if current theme is dark mode
     */
    fun isDarkMode(): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * Apply theme and recreate activity
     */
    fun applyThemeAndRecreate(activity: Activity, themeMode: ThemeMode) {
        applyTheme(themeMode)
        activity.recreate()
    }
} 
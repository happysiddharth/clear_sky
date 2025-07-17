package com.weather.clearsky.feature.settings.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.weather.clearsky.feature.settings.presentation.model.*
import com.weather.clearsky.core.common.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing application settings
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "weather_app_settings"
        
        // Keys
        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_UPDATE_FREQUENCY = "update_frequency"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SHOW_TEMPERATURE_CARD = "show_temperature_card"
        private const val KEY_SHOW_AIR_QUALITY_CARD = "show_air_quality_card"
        private const val KEY_SHOW_HUMIDITY_CARD = "show_humidity_card"
        private const val KEY_SHOW_WIND_CARD = "show_wind_card"
        private const val KEY_SHOW_UV_INDEX_CARD = "show_uv_index_card"
        private const val KEY_SHOW_FORECAST_CARD = "show_forecast_card"
        private const val KEY_ENABLE_NOTIFICATIONS = "enable_notifications"
        private const val KEY_ENABLE_LOCATION_SERVICES = "enable_location_services"
        private const val KEY_ENABLE_WIDGET_UPDATES = "enable_widget_updates"
        private const val KEY_DEFAULT_LOCATION = "default_location"
        private const val KEY_API_KEY = "4e9980a4cbc984227ad965f925e120bd"
        private const val KEY_ENABLE_ANALYTICS = "enable_analytics"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _preferencesFlow = MutableStateFlow(getCurrentPreferences())
    val preferencesFlow: Flow<SettingsPreferences> = _preferencesFlow.asStateFlow()

    /**
     * Get current preferences
     */
    fun getCurrentPreferences(): SettingsPreferences {
        return SettingsPreferences(
            temperatureUnit = getTemperatureUnit(),
            updateFrequency = getUpdateFrequency(),
            themeMode = getThemeMode(),
            showTemperatureCard = prefs.getBoolean(KEY_SHOW_TEMPERATURE_CARD, true),
            showAirQualityCard = prefs.getBoolean(KEY_SHOW_AIR_QUALITY_CARD, true),
            showHumidityCard = prefs.getBoolean(KEY_SHOW_HUMIDITY_CARD, true),
            showWindCard = prefs.getBoolean(KEY_SHOW_WIND_CARD, true),
            showUvIndexCard = prefs.getBoolean(KEY_SHOW_UV_INDEX_CARD, true),
            showForecastCard = prefs.getBoolean(KEY_SHOW_FORECAST_CARD, true),
            enableNotifications = prefs.getBoolean(KEY_ENABLE_NOTIFICATIONS, true),
            enableLocationServices = prefs.getBoolean(KEY_ENABLE_LOCATION_SERVICES, true),
            enableWidgetUpdates = prefs.getBoolean(KEY_ENABLE_WIDGET_UPDATES, true),
            defaultLocation = prefs.getString(KEY_DEFAULT_LOCATION, "") ?: "",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            enableAnalytics = prefs.getBoolean(KEY_ENABLE_ANALYTICS, false)
        )
    }

    /**
     * Update preferences and emit to flow
     */
    private fun updatePreferences(updater: SettingsPreferences.() -> SettingsPreferences) {
        val currentPreferences = getCurrentPreferences()
        val newPreferences = currentPreferences.updater()
        _preferencesFlow.value = newPreferences
    }

    // Temperature Unit
    fun getTemperatureUnit(): TemperatureUnit {
        val unitName = prefs.getString(KEY_TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.name)
        return try {
            TemperatureUnit.valueOf(unitName ?: TemperatureUnit.CELSIUS.name)
        } catch (e: IllegalArgumentException) {
            TemperatureUnit.CELSIUS
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        prefs.edit {
            putString(KEY_TEMPERATURE_UNIT, unit.name)
        }
        updatePreferences { copy(temperatureUnit = unit) }
    }

    // Update Frequency
    fun getUpdateFrequency(): UpdateFrequency {
        val frequencyName = prefs.getString(KEY_UPDATE_FREQUENCY, UpdateFrequency.THIRTY_MINUTES.name)
        return try {
            UpdateFrequency.valueOf(frequencyName ?: UpdateFrequency.THIRTY_MINUTES.name)
        } catch (e: IllegalArgumentException) {
            UpdateFrequency.THIRTY_MINUTES
        }
    }

    fun setUpdateFrequency(frequency: UpdateFrequency) {
        prefs.edit {
            putString(KEY_UPDATE_FREQUENCY, frequency.name)
        }
        updatePreferences { copy(updateFrequency = frequency) }
    }

    // Theme Mode
    fun getThemeMode(): ThemeMode {
        val themeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeName ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(theme: ThemeMode) {
        prefs.edit {
            putString(KEY_THEME_MODE, theme.name)
        }
        updatePreferences { copy(themeMode = theme) }
    }

    // Card Visibility
    fun setCardVisibility(cardType: String, isVisible: Boolean) {
        val key = when (cardType) {
            "temperature" -> KEY_SHOW_TEMPERATURE_CARD
            "air_quality" -> KEY_SHOW_AIR_QUALITY_CARD
            "humidity" -> KEY_SHOW_HUMIDITY_CARD
            "wind" -> KEY_SHOW_WIND_CARD
            "uv_index" -> KEY_SHOW_UV_INDEX_CARD
            "forecast" -> KEY_SHOW_FORECAST_CARD
            else -> return
        }
        
        prefs.edit {
            putBoolean(key, isVisible)
        }
        
        updatePreferences {
            when (cardType) {
                "temperature" -> copy(showTemperatureCard = isVisible)
                "air_quality" -> copy(showAirQualityCard = isVisible)
                "humidity" -> copy(showHumidityCard = isVisible)
                "wind" -> copy(showWindCard = isVisible)
                "uv_index" -> copy(showUvIndexCard = isVisible)
                "forecast" -> copy(showForecastCard = isVisible)
                else -> this
            }
        }
    }

    // Notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_ENABLE_NOTIFICATIONS, enabled)
        }
        updatePreferences { copy(enableNotifications = enabled) }
    }

    // Location Services
    fun setLocationServicesEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_ENABLE_LOCATION_SERVICES, enabled)
        }
        updatePreferences { copy(enableLocationServices = enabled) }
    }

    // Widget Updates
    fun setWidgetUpdatesEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_ENABLE_WIDGET_UPDATES, enabled)
        }
        updatePreferences { copy(enableWidgetUpdates = enabled) }
    }

    // Default Location
    fun setDefaultLocation(location: String) {
        prefs.edit {
            putString(KEY_DEFAULT_LOCATION, location)
        }
        updatePreferences { copy(defaultLocation = location) }
    }

    // API Key
    fun setApiKey(apiKey: String) {
        prefs.edit {
            putString(KEY_API_KEY, apiKey)
        }
        updatePreferences { copy(apiKey = "4e9980a4cbc984227ad965f925e120bd") }
    }

    // Analytics
    fun setAnalyticsEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_ENABLE_ANALYTICS, enabled)
        }
        updatePreferences { copy(enableAnalytics = enabled) }
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit {
            clear()
        }
        updatePreferences { SettingsPreferences() }
    }

    /**
     * Export settings as a map
     */
    fun exportSettings(): Map<String, Any?> {
        return prefs.all
    }

    /**
     * Import settings from a map
     */
    fun importSettings(settings: Map<String, Any?>) {
        prefs.edit {
            clear()
            settings.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                }
            }
        }
        updatePreferences { getCurrentPreferences() }
    }
} 
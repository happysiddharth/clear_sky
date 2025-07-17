package com.weather.clearsky.feature.settings.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.weather.clearsky.core.common.theme.ThemeMode

/**
 * Sealed class representing different types of settings
 */
sealed class SettingItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int? = null,
    @DrawableRes val iconRes: Int? = null
) {
    
    data class SwitchSetting(
        val key: String,
        @StringRes val title: Int,
        @StringRes val description: Int? = null,
        @DrawableRes val icon: Int? = null,
        val isEnabled: Boolean = true,
        val value: Boolean = false
    ) : SettingItem(key, title, description, icon)
    
    data class ListSetting(
        val key: String,
        @StringRes val title: Int,
        @StringRes val description: Int? = null,
        @DrawableRes val icon: Int? = null,
        val entries: List<String> = emptyList(),
        val selectedIndex: Int = 0
    ) : SettingItem(key, title, description, icon)
    
    data class ActionSetting(
        val key: String,
        @StringRes val title: Int,
        @StringRes val description: Int? = null,
        @DrawableRes val icon: Int? = null,
        val action: SettingAction = SettingAction.NONE
    ) : SettingItem(key, title, description, icon)
    
    data class HeaderSetting(
        val key: String,
        @StringRes val title: Int
    ) : SettingItem(key, title)
    
    data class InfoSetting(
        val key: String,
        @StringRes val title: Int,
        val value: String = "",
        @DrawableRes val icon: Int? = null
    ) : SettingItem(key, title, null, icon)
}

/**
 * Enum for setting actions
 */
enum class SettingAction {
    NONE,
    OPEN_ABOUT,
    RESET_SETTINGS,
    CLEAR_CACHE,
    EXPORT_SETTINGS,
    IMPORT_SETTINGS,
    RATE_APP,
    SHARE_APP,
    PRIVACY_POLICY
}

/**
 * Temperature unit enum
 */
enum class TemperatureUnit(val symbol: String, val displayName: String) {
    CELSIUS("°C", "Celsius"),
    FAHRENHEIT("°F", "Fahrenheit"),
    KELVIN("K", "Kelvin")
}

/**
 * Update frequency enum
 */
enum class UpdateFrequency(val minutes: Int, val displayName: String) {
    NEVER(0, "Manual only"),
    FIVE_MINUTES(5, "Every 5 minutes"),
    FIFTEEN_MINUTES(15, "Every 15 minutes"),
    THIRTY_MINUTES(30, "Every 30 minutes"),
    ONE_HOUR(60, "Every hour"),
    THREE_HOURS(180, "Every 3 hours"),
    SIX_HOURS(360, "Every 6 hours")
}



/**
 * Settings preferences data class
 */
data class SettingsPreferences(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val updateFrequency: UpdateFrequency = UpdateFrequency.THIRTY_MINUTES,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showTemperatureCard: Boolean = true,
    val showAirQualityCard: Boolean = true,
    val showHumidityCard: Boolean = true,
    val showWindCard: Boolean = true,
    val showUvIndexCard: Boolean = true,
    val showForecastCard: Boolean = true,
    val enableNotifications: Boolean = true,
    val enableLocationServices: Boolean = true,
    val enableWidgetUpdates: Boolean = true,
    val defaultLocation: String = "",
    val apiKey: String = "",
    val enableAnalytics: Boolean = false
)

/**
 * UI state for settings screen
 */
data class SettingsUiState(
    val settings: List<SettingItem> = emptyList(),
    val preferences: SettingsPreferences = SettingsPreferences(),
    val isLoading: Boolean = false,
    val error: String? = null
) 
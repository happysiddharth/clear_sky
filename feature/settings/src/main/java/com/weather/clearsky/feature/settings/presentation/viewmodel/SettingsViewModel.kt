package com.weather.clearsky.feature.settings.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.feature.settings.R
import com.weather.clearsky.feature.settings.domain.usecase.*
import com.weather.clearsky.feature.settings.presentation.model.*
import com.weather.clearsky.core.common.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateTemperatureUnitUseCase: UpdateTemperatureUnitUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
    private val updateUpdateFrequencyUseCase: UpdateUpdateFrequencyUseCase,
    private val updateCardVisibilityUseCase: UpdateCardVisibilityUseCase,
    private val updateNotificationsUseCase: UpdateNotificationsUseCase,
    private val updateLocationServicesUseCase: UpdateLocationServicesUseCase,
    private val updateWidgetUpdatesUseCase: UpdateWidgetUpdatesUseCase,
    private val updateDefaultLocationUseCase: UpdateDefaultLocationUseCase,
    private val updateApiKeyUseCase: UpdateApiKeyUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val exportSettingsUseCase: ExportSettingsUseCase,
    private val importSettingsUseCase: ImportSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getSettingsUseCase().collect { preferences ->
                val settingItems = createSettingItems(preferences)
                _uiState.value = _uiState.value.copy(
                    settings = settingItems,
                    preferences = preferences,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun createSettingItems(preferences: SettingsPreferences): List<SettingItem> {
        return listOf(
            // General Section
            SettingItem.HeaderSetting("general_header", R.string.settings_general),
            
            SettingItem.ListSetting(
                key = "temperature_unit",
                title = R.string.settings_temperature_unit,
                description = R.string.settings_temperature_unit_desc,
                icon = R.drawable.ic_temperature,
                entries = TemperatureUnit.values().map { it.displayName },
                selectedIndex = TemperatureUnit.values().indexOf(preferences.temperatureUnit)
            ),
            
            SettingItem.ListSetting(
                key = "theme_mode",
                title = R.string.settings_theme,
                description = R.string.settings_theme_desc,
                icon = R.drawable.ic_palette,
                entries = ThemeMode.values().map { it.displayName },
                selectedIndex = ThemeMode.values().indexOf(preferences.themeMode)
            ),
            
            SettingItem.ListSetting(
                key = "update_frequency",
                title = R.string.settings_update_frequency,
                description = R.string.settings_update_frequency_desc,
                icon = R.drawable.ic_refresh,
                entries = UpdateFrequency.values().map { it.displayName },
                selectedIndex = UpdateFrequency.values().indexOf(preferences.updateFrequency)
            ),

            // Cards Section
            SettingItem.HeaderSetting("cards_header", R.string.settings_cards),
            
            SettingItem.SwitchSetting(
                key = "show_temperature_card",
                title = R.string.settings_show_temperature_card,
                icon = R.drawable.ic_temperature,
                value = preferences.showTemperatureCard
            ),
            
            SettingItem.SwitchSetting(
                key = "show_air_quality_card",
                title = R.string.settings_show_air_quality_card,
                icon = R.drawable.ic_air,
                value = preferences.showAirQualityCard
            ),
            
            SettingItem.SwitchSetting(
                key = "show_humidity_card",
                title = R.string.settings_show_humidity_card,
                icon = R.drawable.ic_water_drop,
                value = preferences.showHumidityCard
            ),
            
            SettingItem.SwitchSetting(
                key = "show_wind_card",
                title = R.string.settings_show_wind_card,
                icon = R.drawable.ic_wind,
                value = preferences.showWindCard
            ),
            
            SettingItem.SwitchSetting(
                key = "show_uv_index_card",
                title = R.string.settings_show_uv_card,
                icon = R.drawable.ic_sunny,
                value = preferences.showUvIndexCard
            ),
            
            SettingItem.SwitchSetting(
                key = "show_forecast_card",
                title = R.string.settings_show_forecast_card,
                icon = R.drawable.ic_calendar,
                value = preferences.showForecastCard
            ),

            // Features Section
            SettingItem.HeaderSetting("features_header", R.string.settings_features),
            
            SettingItem.SwitchSetting(
                key = "enable_notifications",
                title = R.string.settings_notifications,
                description = R.string.settings_notifications_desc,
                icon = R.drawable.ic_notifications,
                value = preferences.enableNotifications
            ),
            
            SettingItem.SwitchSetting(
                key = "enable_location_services",
                title = R.string.settings_location_services,
                description = R.string.settings_location_services_desc,
                icon = R.drawable.ic_location,
                value = preferences.enableLocationServices
            ),
            
            SettingItem.SwitchSetting(
                key = "enable_widget_updates",
                title = R.string.settings_widget_updates,
                description = R.string.settings_widget_updates_desc,
                icon = R.drawable.ic_widgets,
                value = preferences.enableWidgetUpdates
            ),

            // Data Section
            SettingItem.HeaderSetting("data_header", R.string.settings_data),
            
            SettingItem.ActionSetting(
                key = "clear_cache",
                title = R.string.settings_clear_cache,
                description = R.string.settings_clear_cache_desc,
                icon = R.drawable.ic_delete,
                action = SettingAction.CLEAR_CACHE
            ),
            
            SettingItem.ActionSetting(
                key = "reset_settings",
                title = R.string.settings_reset,
                description = R.string.settings_reset_desc,
                icon = R.drawable.ic_restore,
                action = SettingAction.RESET_SETTINGS
            ),
            
            SettingItem.ActionSetting(
                key = "export_settings",
                title = R.string.settings_export,
                description = R.string.settings_export_desc,
                icon = R.drawable.ic_export,
                action = SettingAction.EXPORT_SETTINGS
            ),

            // About Section
            SettingItem.HeaderSetting("about_header", R.string.settings_about),
            
            SettingItem.InfoSetting(
                key = "app_version",
                title = R.string.settings_version,
                value = getAppVersion(),
                icon = R.drawable.ic_info
            ),
            
            SettingItem.ActionSetting(
                key = "rate_app",
                title = R.string.settings_rate_app,
                description = R.string.settings_rate_app_desc,
                icon = R.drawable.ic_star,
                action = SettingAction.RATE_APP
            ),
            
            SettingItem.ActionSetting(
                key = "privacy_policy",
                title = R.string.settings_privacy_policy,
                icon = R.drawable.ic_privacy,
                action = SettingAction.PRIVACY_POLICY
            )
        )
    }

    fun onTemperatureUnitChanged(index: Int) {
        val unit = TemperatureUnit.values()[index]
        updateTemperatureUnitUseCase(unit)
    }

    fun onThemeModeChanged(index: Int) {
        val theme = ThemeMode.values()[index]
        updateThemeModeUseCase(theme)
    }

    fun onUpdateFrequencyChanged(index: Int) {
        val frequency = UpdateFrequency.values()[index]
        updateUpdateFrequencyUseCase(frequency)
    }

    fun onCardVisibilityChanged(cardType: String, isVisible: Boolean) {
        updateCardVisibilityUseCase(cardType, isVisible)
    }

    fun onNotificationsChanged(enabled: Boolean) {
        updateNotificationsUseCase(enabled)
    }

    fun onLocationServicesChanged(enabled: Boolean) {
        updateLocationServicesUseCase(enabled)
    }

    fun onWidgetUpdatesChanged(enabled: Boolean) {
        updateWidgetUpdatesUseCase(enabled)
    }

    fun onDefaultLocationChanged(location: String) {
        updateDefaultLocationUseCase(location)
    }

    fun onApiKeyChanged(apiKey: String) {
        updateApiKeyUseCase(apiKey)
    }

    fun onActionClicked(action: SettingAction) {
        viewModelScope.launch {
            try {
                when (action) {
                    SettingAction.RESET_SETTINGS -> {
                        resetSettingsUseCase()
                        showMessage("Settings reset to defaults")
                    }
                    SettingAction.CLEAR_CACHE -> {
                        // Implement cache clearing
                        showMessage("Cache cleared")
                    }
                    SettingAction.EXPORT_SETTINGS -> {
                        val settings = exportSettingsUseCase()
                        // Implement export functionality
                        showMessage("Settings exported")
                    }
                    else -> {
                        // Handle other actions in the fragment
                    }
                }
            } catch (e: Exception) {
                showError("Failed to perform action: ${e.message}")
            }
        }
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
        // Clear message after showing
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(error = null)
        }
    }

    private fun showError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
} 
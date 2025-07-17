package com.weather.clearsky.feature.settings.domain.usecase

import com.weather.clearsky.feature.settings.data.repository.SettingsRepository
import com.weather.clearsky.feature.settings.presentation.model.*
import com.weather.clearsky.core.common.theme.ThemeMode
import com.weather.clearsky.core.common.theme.ThemeManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing settings
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<SettingsPreferences> {
        return settingsRepository.preferencesFlow
    }
}

/**
 * Use case for updating temperature unit
 */
class UpdateTemperatureUnitUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(unit: TemperatureUnit) {
        settingsRepository.setTemperatureUnit(unit)
    }
}

/**
 * Use case for updating theme mode
 */
class UpdateThemeModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val themeManager: ThemeManager
) {
    operator fun invoke(theme: ThemeMode) {
        settingsRepository.setThemeMode(theme)
        themeManager.applyTheme(theme)
    }
}

/**
 * Use case for updating update frequency
 */
class UpdateUpdateFrequencyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(frequency: UpdateFrequency) {
        settingsRepository.setUpdateFrequency(frequency)
    }
}

/**
 * Use case for updating card visibility
 */
class UpdateCardVisibilityUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(cardType: String, isVisible: Boolean) {
        settingsRepository.setCardVisibility(cardType, isVisible)
    }
}

/**
 * Use case for updating notifications
 */
class UpdateNotificationsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
    }
}

/**
 * Use case for updating location services
 */
class UpdateLocationServicesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        settingsRepository.setLocationServicesEnabled(enabled)
    }
}

/**
 * Use case for updating widget updates
 */
class UpdateWidgetUpdatesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        settingsRepository.setWidgetUpdatesEnabled(enabled)
    }
}

/**
 * Use case for updating default location
 */
class UpdateDefaultLocationUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(location: String) {
        settingsRepository.setDefaultLocation(location)
    }
}

/**
 * Use case for updating API key
 */
class UpdateApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(apiKey: String) {
        settingsRepository.setApiKey(apiKey)
    }
}

/**
 * Use case for resetting settings
 */
class ResetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke() {
        settingsRepository.resetToDefaults()
    }
}

/**
 * Use case for exporting settings
 */
class ExportSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Map<String, Any?> {
        return settingsRepository.exportSettings()
    }
}

/**
 * Use case for importing settings
 */
class ImportSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(settings: Map<String, Any?>) {
        settingsRepository.importSettings(settings)
    }
} 
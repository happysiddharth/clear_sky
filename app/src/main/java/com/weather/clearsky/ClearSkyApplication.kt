package com.weather.clearsky

import android.app.Application
import com.pluto.BuildConfig
import com.pluto.Pluto
import com.pluto.plugins.logger.PlutoLoggerPlugin
import com.pluto.plugins.network.PlutoNetworkPlugin
import com.pluto.plugins.preferences.PlutoSharePreferencesPlugin
import com.weather.clearsky.feature.alerts.data.work.AlertWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClearSkyApplication: Application() {
    
    @Inject
    lateinit var alertWorkScheduler: AlertWorkScheduler
    
    @Inject
    lateinit var themeManager: com.weather.clearsky.core.common.theme.ThemeManager
    
    @Inject
    lateinit var settingsRepository: com.weather.clearsky.feature.settings.data.repository.SettingsRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Pluto for debugging (only in debug builds)
        if (BuildConfig.DEBUG) {
            Pluto.Installer(this)
                .addPlugin(PlutoNetworkPlugin("network"))
                .addPlugin(PlutoLoggerPlugin())
                .addPlugin(PlutoSharePreferencesPlugin())
                .install()
        }
        
        // Initialize theme
        initializeTheme()
        
        // Initialize alerts background checking
        initializeAlerts()
        
        // LeakCanary is automatically initialized - no setup needed
    }
    
    private fun initializeTheme() {
        // Apply saved theme on app startup
        val savedTheme = settingsRepository.getThemeMode()
        themeManager.applyTheme(savedTheme)
    }
    
    private fun initializeAlerts() {
        // Schedule periodic alert checking
        alertWorkScheduler.schedulePeriodicAlertCheck()
    }
}
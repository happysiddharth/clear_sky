package com.weather.clearsky.feature.settings.navigation

import androidx.navigation.NavController
import com.weather.clearsky.core.common.navigation.FeatureNavigator
import com.weather.clearsky.core.common.navigation.NavigationOptions
import com.weather.clearsky.core.common.navigation.NavigationResult
import com.weather.clearsky.core.common.navigation.NavigationRoute
import javax.inject.Inject

/**
 * Navigator for the Settings feature module
 */
class SettingsFeatureNavigator @Inject constructor() : FeatureNavigator {
    
    override fun canHandle(route: NavigationRoute): Boolean {
        return when (route) {
            is NavigationRoute.Settings -> true
            is NavigationRoute.SettingsCategory -> true
            else -> false
        }
    }
    
    override suspend fun navigate(
        navController: NavController,
        route: NavigationRoute,
        options: NavigationOptions
    ): NavigationResult {
        return try {
            when (route) {
                is NavigationRoute.Settings -> {
                    // This will be handled by the app-level navigation
                    // since the action is defined in app module
                    NavigationResult.Error("Use app-level navigation for settings")
                }
                is NavigationRoute.SettingsCategory -> {
                    // Navigate to specific settings category
                    // Implementation would depend on having category-specific fragments
                    NavigationResult.Success
                }
                else -> NavigationResult.Error("Route not supported by SettingsFeatureNavigator")
            }
        } catch (e: Exception) {
            NavigationResult.Error(e.message ?: "Navigation failed in SettingsFeatureNavigator")
        }
    }
} 
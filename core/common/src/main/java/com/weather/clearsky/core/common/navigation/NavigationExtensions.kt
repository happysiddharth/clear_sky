package com.weather.clearsky.core.common.navigation

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Extension functions to make navigation easier from fragments and activities
 */

/**
 * Navigate to a route from a fragment using the injected NavigationManager
 */
fun Fragment.navigateTo(
    navigationManager: NavigationManager,
    route: NavigationRoute,
    options: NavigationOptions = NavigationOptions.DEFAULT
) {
    viewLifecycleOwner.lifecycleScope.launch {
        navigationManager.navigateTo(route, options)
    }
}

/**
 * Navigate back using the injected NavigationManager
 */
fun Fragment.navigateBack(navigationManager: NavigationManager): Boolean {
    return navigationManager.navigateBack()
}

/**
 * Common navigation routes for easy access
 */
object CommonRoutes {
    fun toSettings() = NavigationRoute.Settings
    fun toMain() = NavigationRoute.Main
    fun toWeatherDetails() = NavigationRoute.WeatherDetails
    fun toForecast() = NavigationRoute.Forecast
    fun toMaps() = NavigationRoute.Maps
    fun toProfile() = NavigationRoute.Profile
    fun toNotifications() = NavigationRoute.Notifications
    
    fun toWeatherLocation(locationId: String) = NavigationRoute.WeatherLocation(locationId)
    fun toSettingsCategory(category: String) = NavigationRoute.SettingsCategory(category)
    fun toExternalUrl(url: String) = NavigationRoute.ExternalUrl(url)
    fun toDeepLink(uri: String) = NavigationRoute.DeepLink(uri)
} 
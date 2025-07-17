package com.weather.clearsky.core.common.navigation

import android.os.Bundle

/**
 * Sealed class representing all possible navigation destinations in the app.
 * This provides type-safe navigation and makes it easy to add new features.
 */
sealed class NavigationRoute(
    val routeId: String,
    val arguments: Bundle = Bundle()
) {
    
    // Main Feature Routes
    data object Main : NavigationRoute("main")
    
    // Settings Feature Routes  
    data object Settings : NavigationRoute("settings")
    data class SettingsCategory(val category: String) : NavigationRoute(
        "settings_category", 
        Bundle().apply { putString("category", category) }
    )
    
    // Weather Feature Routes
    data object WeatherDetails : NavigationRoute("weather_details")
    data class WeatherLocation(val locationId: String) : NavigationRoute(
        "weather_location",
        Bundle().apply { putString("location_id", locationId) }
    )
    
    // Future Feature Routes (examples)
    data object Forecast : NavigationRoute("forecast")
    data object Maps : NavigationRoute("maps")
    data object Profile : NavigationRoute("profile")
    data object Notifications : NavigationRoute("notifications")
    
    // External Routes
    data class ExternalUrl(val url: String) : NavigationRoute(
        "external_url",
        Bundle().apply { putString("url", url) }
    )
    
    data class DeepLink(val uri: String) : NavigationRoute(
        "deep_link",
        Bundle().apply { putString("uri", uri) }
    )
}

/**
 * Navigation options for controlling how navigation is performed
 */
data class NavigationOptions(
    val clearBackStack: Boolean = false,
    val singleTop: Boolean = false,
    val animated: Boolean = true,
    val popUpTo: String? = null,
    val inclusive: Boolean = false
) {
    companion object {
        val DEFAULT = NavigationOptions()
        val CLEAR_STACK = NavigationOptions(clearBackStack = true)
        val SINGLE_TOP = NavigationOptions(singleTop = true)
    }
} 
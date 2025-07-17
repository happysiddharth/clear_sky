package com.weather.clearsky.core.common.navigation

interface NavigationCallback {
    fun navigateToSettings()
    fun navigateToMain()
    fun navigateToAlerts()
    fun navigateToLocation(locationId: String)
    fun navigateToLocationManager()
} 
package com.weather.clearsky.feature.main.data.model

/**
 * Data class representing a saved location for weather tracking
 */
data class SavedLocation(
    val id: String = "",
    val name: String,
    val country: String = "",
    val area: String = "", // Area/locality/neighborhood name
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isCurrentLocation: Boolean = false,
    val order: Int = 0
) {
    /**
     * Display name for the location (e.g., "New York, US")
     */
    val displayName: String
        get() = if (country.isNotEmpty()) "$name, $country" else name
    
    /**
     * Full display name with area for current location (e.g., "Downtown, New York")
     */
    val fullDisplayName: String
        get() = when {
            isCurrentLocation && area.isNotEmpty() -> if (name != "Current Location") "$area, $name" else area
            country.isNotEmpty() -> "$name, $country"
            else -> name
        }
    
    /**
     * Location coordinates as a string for API calls
     */
    val coordinates: String
        get() = "$latitude,$longitude"
} 
package com.weather.clearsky.feature.alerts.domain.entity

data class AlertLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryName: String,
    val displayName: String = "$cityName, $countryName"
) {
    companion object {
        fun fromCoordinates(
            latitude: Double,
            longitude: Double,
            cityName: String = "Custom Location",
            countryName: String = ""
        ): AlertLocation {
            return AlertLocation(
                latitude = latitude,
                longitude = longitude,
                cityName = cityName,
                countryName = countryName
            )
        }
    }
} 
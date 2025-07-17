package com.weather.clearsky.core.common.model

/**
 * Domain models representing business entities
 * These are independent of any external data source
 */

data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val icon: String,
    val humidity: Int,
    val pressure: Int,
    val cityName: String,
    val tempMin: Double,
    val tempMax: Double
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

data class WeatherInfo(
    val temperature: String,
    val description: String,
    val cityName: String,
    val icon: String,
    val humidity: String,
    val feelsLike: String
) 
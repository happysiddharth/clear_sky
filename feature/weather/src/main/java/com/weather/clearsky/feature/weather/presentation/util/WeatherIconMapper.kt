package com.weather.clearsky.feature.weather.presentation.util

/**
 * Utility object to map weather icon codes to emoji representations
 */
object WeatherIconMapper {
    
    fun getWeatherIcon(iconCode: String): String {
        return when (iconCode) {
            "01d" -> "☀️" // clear sky day
            "01n" -> "🌙" // clear sky night
            "02d", "02n" -> "⛅" // few clouds
            "03d", "03n" -> "☁️" // scattered clouds
            "04d", "04n" -> "☁️" // broken clouds
            "09d", "09n" -> "🌧️" // shower rain
            "10d" -> "🌦️" // rain day
            "10n" -> "🌧️" // rain night
            "11d", "11n" -> "⛈️" // thunderstorm
            "13d", "13n" -> "🌨️" // snow
            "50d", "50n" -> "🌫️" // mist
            else -> "🌤️" // default
        }
    }
} 
package com.weather.clearsky.core.network.mapper

import com.weather.clearsky.core.common.model.Weather
import com.weather.clearsky.core.common.model.WeatherInfo
import com.weather.clearsky.core.network.model.WeatherResponse

/**
 * Mappers to convert between Network DTOs and Domain models
 */

fun WeatherResponse.toDomain(): Weather {
    val weatherDto = weather.firstOrNull()
    
    return Weather(
        temperature = main.temp,
        feelsLike = main.feelsLike,
        description = weatherDto?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown",
        icon = weatherDto?.icon ?: "01d",
        humidity = main.humidity,
        pressure = main.pressure,
        cityName = cityName,
        tempMin = main.tempMin,
        tempMax = main.tempMax
    )
}

fun Weather.toDisplayModel(): WeatherInfo {
    val temp = temperature.toInt()
    val feelsLikeTemp = feelsLike.toInt()
    
    return WeatherInfo(
        temperature = "${temp}°C",
        description = description,
        cityName = cityName,
        icon = icon,
        humidity = "${humidity}%",
        feelsLike = "${feelsLikeTemp}°C"
    )
} 
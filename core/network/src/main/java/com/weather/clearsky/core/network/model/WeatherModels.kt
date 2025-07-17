package com.weather.clearsky.core.network.model

import com.google.gson.annotations.SerializedName

/**
 * Network Data Transfer Objects (DTOs) for Weather API responses
 */

data class WeatherResponse(
    @SerializedName("weather")
    val weather: List<WeatherDto>,
    @SerializedName("main")
    val main: MainDto,
    @SerializedName("name")
    val cityName: String,
    @SerializedName("cod")
    val cod: Int
)

data class WeatherDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("main")
    val main: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String
)

data class MainDto(
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    @SerializedName("pressure")
    val pressure: Int,
    @SerializedName("humidity")
    val humidity: Int
) 
package com.weather.clearsky.feature.main.presentation.model

import androidx.annotation.DrawableRes

/**
 * Data class for hero weather section
 */
data class HeroWeatherData(
    val location: String,
    val temperature: String,
    val condition: String,
    val high: String,
    val low: String
)

/**
 * Sealed class representing different types of weather cards
 */
sealed class WeatherCard(
    val id: String,
    val title: String,
    val cardType: CardType,
    var position: Int = 0
) {
    data class TemperatureCard(
        val temperature: String = "--°C",
        val feelsLike: String = "--°C",
        val location: String = "Loading...",
        val description: String = "Loading...",
        val icon: String = "🌤️"
    ) : WeatherCard("temperature", "Temperature", CardType.TEMPERATURE)

    data class AirQualityCard(
        val aqi: Int = 0,
        val quality: String = "Loading...",
        val pm25: String = "--",
        val pm10: String = "--",
        val co: String = "--",
        val no2: String = "--"
    ) : WeatherCard("air_quality", "Air Quality", CardType.AIR_QUALITY)

    data class HumidityCard(
        val humidity: String = "--%",
        val dewPoint: String = "--°C",
        val pressure: String = "-- hPa"
    ) : WeatherCard("humidity", "Humidity & Pressure", CardType.HUMIDITY)

    data class WindCard(
        val windSpeed: String = "-- km/h",
        val windDirection: String = "--",
        val windGust: String = "-- km/h",
        val visibility: String = "-- km"
    ) : WeatherCard("wind", "Wind & Visibility", CardType.WIND)

    data class UvIndexCard(
        val uvIndex: Int = 0,
        val uvLevel: String = "Low",
        val uvDescription: String = "Loading...",
        val sunriseTime: String = "--:--",
        val sunsetTime: String = "--:--"
    ) : WeatherCard("uv_index", "UV Index & Sun", CardType.UV_INDEX)

    data class ForecastCard(
        val todayHigh: String = "--°C",
        val todayLow: String = "--°C",
        val tomorrowHigh: String = "--°C",
        val tomorrowLow: String = "--°C",
        val hourlyForecast: List<HourlyForecast> = emptyList()
    ) : WeatherCard("forecast", "Forecast", CardType.FORECAST)

    data class HourlyWeatherCard(
        val currentHour: String = "--:--",
        val hourlyForecast: List<HourlyForecast> = emptyList(),
        val location: String = "Loading..."
    ) : WeatherCard("hourly_weather", "Hourly Weather", CardType.HOURLY_WEATHER)
}

/**
 * Enum for different card types
 */
enum class CardType {
    TEMPERATURE,
    AIR_QUALITY,
    HUMIDITY,
    WIND,
    UV_INDEX,
    FORECAST,
    HOURLY_WEATHER
}

/**
 * Data class for hourly forecast
 */
data class HourlyForecast(
    val time: String,
    val temperature: String,
    val icon: String,
    val precipitationChance: Int = 0
)

/**
 * UI state for cards
 */
data class WeatherCardUiState(
    val cards: List<WeatherCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
) 
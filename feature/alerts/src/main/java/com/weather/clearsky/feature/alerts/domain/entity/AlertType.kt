package com.weather.clearsky.feature.alerts.domain.entity

enum class AlertType(val displayName: String, val icon: String) {
    TEMPERATURE("Temperature", "🌡️"),
    RAIN("Rain", "🌧️"),
    SNOW("Snow", "❄️"),
    WIND("Wind Speed", "💨"),
    HUMIDITY("Humidity", "💧"),
    WEATHER_CONDITION("Weather Condition", "🌤️"),
    UV_INDEX("UV Index", "☀️"),
    PRESSURE("Atmospheric Pressure", "📊"),
    VISIBILITY("Visibility", "👁️")
} 
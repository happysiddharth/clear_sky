package com.weather.clearsky.feature.alerts.domain.entity

import java.time.LocalDateTime

data class AlertNotification(
    val alertId: String,
    val title: String,
    val message: String,
    val triggeredAt: LocalDateTime = LocalDateTime.now(),
    val weatherData: WeatherData,
    val location: AlertLocation,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Int?,
    val weatherCondition: String,
    val description: String,
    val rain: Double? = null,
    val snow: Double? = null
)

enum class NotificationPriority(val level: Int) {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4)
} 
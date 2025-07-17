package com.weather.clearsky.feature.alerts.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.weather.clearsky.feature.alerts.data.local.converter.AlertConverters
import com.weather.clearsky.feature.alerts.domain.entity.*
import java.time.LocalDateTime

@Entity(tableName = "weather_alerts")
@TypeConverters(AlertConverters::class)
data class WeatherAlertEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val alertType: AlertType,
    val condition: AlertCondition,
    val location: AlertLocation,
    val targetDateTime: LocalDateTime,
    val expiryDateTime: LocalDateTime?,
    val status: AlertStatus,
    val isRepeating: Boolean,
    val repeatInterval: RepeatInterval?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastTriggeredAt: LocalDateTime?,
    val isNotificationEnabled: Boolean,
    val notificationSound: Boolean,
    val notificationVibration: Boolean,
    val customMessage: String?
)

// Extension functions to convert between domain and entity
fun WeatherAlert.toEntity(): WeatherAlertEntity {
    return WeatherAlertEntity(
        id = id,
        title = title,
        description = description,
        alertType = alertType,
        condition = condition,
        location = location,
        targetDateTime = targetDateTime,
        expiryDateTime = expiryDateTime,
        status = status,
        isRepeating = isRepeating,
        repeatInterval = repeatInterval,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastTriggeredAt = lastTriggeredAt,
        isNotificationEnabled = isNotificationEnabled,
        notificationSound = notificationSound,
        notificationVibration = notificationVibration,
        customMessage = customMessage
    )
}

fun WeatherAlertEntity.toDomain(): WeatherAlert {
    return WeatherAlert(
        id = id,
        title = title,
        description = description,
        alertType = alertType,
        condition = condition,
        location = location,
        targetDateTime = targetDateTime,
        expiryDateTime = expiryDateTime,
        status = status,
        isRepeating = isRepeating,
        repeatInterval = repeatInterval,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastTriggeredAt = lastTriggeredAt,
        isNotificationEnabled = isNotificationEnabled,
        notificationSound = notificationSound,
        notificationVibration = notificationVibration,
        customMessage = customMessage
    )
} 
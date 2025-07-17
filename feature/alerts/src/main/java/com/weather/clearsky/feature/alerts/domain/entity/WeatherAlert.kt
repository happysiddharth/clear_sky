package com.weather.clearsky.feature.alerts.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class WeatherAlert(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val alertType: AlertType,
    val condition: AlertCondition,
    val location: AlertLocation,
    val targetDateTime: LocalDateTime,
    val expiryDateTime: LocalDateTime? = null,
    val status: AlertStatus = AlertStatus.ACTIVE,
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastTriggeredAt: LocalDateTime? = null,
    val isNotificationEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val customMessage: String? = null
) {
    fun isExpired(): Boolean {
        val now = LocalDateTime.now()
        return expiryDateTime?.isBefore(now) == true || 
               (!isRepeating && targetDateTime.isBefore(now))
    }
    
    fun shouldCheck(): Boolean {
        val now = LocalDateTime.now()
        return status == AlertStatus.ACTIVE && 
               !isExpired() && 
               (targetDateTime.isBefore(now) || targetDateTime.isEqual(now))
    }
    
    fun getFormattedDateTime(): String {
        return targetDateTime.toString() // Can be formatted as needed
    }
}

enum class RepeatInterval(val displayName: String, val daysToAdd: Long) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    YEARLY("Yearly", 365)
} 
package com.weather.clearsky.feature.alerts.domain.entity

enum class AlertStatus(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    TRIGGERED("Triggered"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled")
} 
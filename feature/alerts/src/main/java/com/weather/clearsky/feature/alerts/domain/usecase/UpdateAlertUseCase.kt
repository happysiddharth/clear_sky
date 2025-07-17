package com.weather.clearsky.feature.alerts.domain.usecase

import com.weather.clearsky.feature.alerts.domain.entity.AlertStatus
import com.weather.clearsky.feature.alerts.domain.entity.WeatherAlert
import com.weather.clearsky.feature.alerts.domain.repository.AlertRepository
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateAlertUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    suspend fun updateAlert(alert: WeatherAlert): Result<WeatherAlert> {
        return repository.updateAlert(alert)
    }
    
    suspend fun deleteAlert(alertId: String): Result<Unit> {
        return repository.deleteAlert(alertId)
    }
    
    suspend fun updateAlertStatus(alertId: String, status: AlertStatus): Result<Unit> {
        return repository.updateAlertStatus(alertId, status)
    }
    
    suspend fun markAlertAsTriggered(alertId: String): Result<Unit> {
        return repository.markAlertAsTriggered(alertId)
    }
    
    suspend fun updateLastTriggeredAt(alertId: String, triggeredAt: LocalDateTime): Result<Unit> {
        return repository.updateLastTriggeredAt(alertId, triggeredAt)
    }
    
    suspend fun deleteAllAlerts(): Result<Unit> {
        return repository.deleteAllAlerts()
    }
} 
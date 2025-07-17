package com.weather.clearsky.feature.alerts.domain.usecase

import com.weather.clearsky.feature.alerts.domain.entity.AlertStatus
import com.weather.clearsky.feature.alerts.domain.entity.AlertType
import com.weather.clearsky.feature.alerts.domain.entity.WeatherAlert
import com.weather.clearsky.feature.alerts.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlertsUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    fun getAllAlerts(): Flow<List<WeatherAlert>> {
        return repository.getAllAlerts()
    }
    
    fun getActiveAlerts(): Flow<List<WeatherAlert>> {
        return repository.getActiveAlerts()
    }
    
    fun getAlertsByStatus(status: AlertStatus): Flow<List<WeatherAlert>> {
        return repository.getAlertsByStatus(status)
    }
    
    fun getAlertsByType(type: AlertType): Flow<List<WeatherAlert>> {
        return repository.getAlertsByType(type)
    }
    
    suspend fun getAlertById(alertId: String): Result<WeatherAlert?> {
        return repository.getAlertById(alertId)
    }
    
    fun getAlertByIdFlow(alertId: String): Flow<WeatherAlert?> {
        return repository.getAlertByIdFlow(alertId)
    }
} 
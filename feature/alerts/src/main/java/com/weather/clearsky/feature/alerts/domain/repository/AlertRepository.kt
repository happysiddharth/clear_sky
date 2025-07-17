package com.weather.clearsky.feature.alerts.domain.repository

import com.weather.clearsky.feature.alerts.domain.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AlertRepository {
    
    // Basic CRUD operations
    suspend fun createAlert(alert: WeatherAlert): Result<WeatherAlert>
    suspend fun updateAlert(alert: WeatherAlert): Result<WeatherAlert>
    suspend fun deleteAlert(alertId: String): Result<Unit>
    suspend fun getAlertById(alertId: String): Result<WeatherAlert?>
    
    // Flow-based operations for real-time updates
    fun getAllAlerts(): Flow<List<WeatherAlert>>
    fun getActiveAlerts(): Flow<List<WeatherAlert>>
    fun getAlertsByStatus(status: AlertStatus): Flow<List<WeatherAlert>>
    fun getAlertsByType(type: AlertType): Flow<List<WeatherAlert>>
    fun getAlertByIdFlow(alertId: String): Flow<WeatherAlert?>
    
    // Alert checking and status management
    suspend fun getActiveAlertsToCheck(currentTime: LocalDateTime): Result<List<WeatherAlert>>
    suspend fun updateAlertStatus(alertId: String, status: AlertStatus): Result<Unit>
    suspend fun markAlertAsTriggered(alertId: String): Result<Unit>
    suspend fun updateLastTriggeredAt(alertId: String, triggeredAt: LocalDateTime): Result<Unit>
    
    // Cleanup operations
    suspend fun getExpiredAlerts(currentTime: LocalDateTime): Result<List<WeatherAlert>>
    suspend fun deleteExpiredAlerts(): Result<Unit>
    suspend fun deleteAlertsByStatus(status: AlertStatus): Result<Unit>
    
    // Statistics
    suspend fun getActiveAlertCount(): Result<Int>
    suspend fun getTriggeredAlertCount(): Result<Int>
    
    // Bulk operations
    suspend fun createAlerts(alerts: List<WeatherAlert>): Result<List<WeatherAlert>>
    suspend fun deleteAllAlerts(): Result<Unit>
} 
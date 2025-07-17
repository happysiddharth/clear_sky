package com.weather.clearsky.feature.alerts.data.repository

import com.weather.clearsky.feature.alerts.data.local.dao.WeatherAlertDao
import com.weather.clearsky.feature.alerts.data.local.entity.toDomain
import com.weather.clearsky.feature.alerts.data.local.entity.toEntity
import com.weather.clearsky.feature.alerts.domain.entity.*
import com.weather.clearsky.feature.alerts.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val alertDao: WeatherAlertDao
) : AlertRepository {
    
    override suspend fun createAlert(alert: WeatherAlert): Result<WeatherAlert> {
        return try {
            Log.d("AlertRepositoryImpl", "Creating alert in database: ${alert.title}")
            alertDao.insertAlert(alert.toEntity())
            Log.d("AlertRepositoryImpl", "Alert created successfully in database: ${alert.title}")
            Result.success(alert)
        } catch (e: Exception) {
            Log.e("AlertRepositoryImpl", "Failed to create alert in database: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateAlert(alert: WeatherAlert): Result<WeatherAlert> {
        return try {
            val updatedAlert = alert.copy(updatedAt = LocalDateTime.now())
            alertDao.updateAlert(updatedAlert.toEntity())
            Result.success(updatedAlert)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAlert(alertId: String): Result<Unit> {
        return try {
            alertDao.deleteAlertById(alertId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAlertById(alertId: String): Result<WeatherAlert?> {
        return try {
            val alertEntity = alertDao.getAlertById(alertId)
            Result.success(alertEntity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllAlerts(): Flow<List<WeatherAlert>> {
        return alertDao.getAllAlerts().map { entities ->
            Log.d("AlertRepositoryImpl", "Retrieved ${entities.size} alert entities from database")
            entities.map { it.toDomain() }
        }
    }
    
    override fun getActiveAlerts(): Flow<List<WeatherAlert>> {
        return alertDao.getActiveAlerts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAlertsByStatus(status: AlertStatus): Flow<List<WeatherAlert>> {
        return alertDao.getAlertsByStatus(status).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAlertsByType(type: AlertType): Flow<List<WeatherAlert>> {
        return alertDao.getAlertsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAlertByIdFlow(alertId: String): Flow<WeatherAlert?> {
        return alertDao.getAlertByIdFlow(alertId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun getActiveAlertsToCheck(currentTime: LocalDateTime): Result<List<WeatherAlert>> {
        return try {
            val entities = alertDao.getActiveAlertsToCheck(currentTime)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAlertStatus(alertId: String, status: AlertStatus): Result<Unit> {
        return try {
            alertDao.updateAlertStatus(alertId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markAlertAsTriggered(alertId: String): Result<Unit> {
        return try {
            val now = LocalDateTime.now()
            alertDao.updateAlertStatus(alertId, AlertStatus.TRIGGERED)
            alertDao.updateLastTriggeredAt(alertId, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateLastTriggeredAt(alertId: String, triggeredAt: LocalDateTime): Result<Unit> {
        return try {
            alertDao.updateLastTriggeredAt(alertId, triggeredAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getExpiredAlerts(currentTime: LocalDateTime): Result<List<WeatherAlert>> {
        return try {
            val entities = alertDao.getExpiredAlerts(currentTime)
            Result.success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteExpiredAlerts(): Result<Unit> {
        return try {
            val now = LocalDateTime.now()
            val expiredAlerts = alertDao.getExpiredAlerts(now)
            expiredAlerts.forEach { alertDao.deleteAlert(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAlertsByStatus(status: AlertStatus): Result<Unit> {
        return try {
            alertDao.deleteAlertsByStatus(status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getActiveAlertCount(): Result<Int> {
        return try {
            val count = alertDao.getActiveAlertCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTriggeredAlertCount(): Result<Int> {
        return try {
            val count = alertDao.getTriggeredAlertCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createAlerts(alerts: List<WeatherAlert>): Result<List<WeatherAlert>> {
        return try {
            val entities = alerts.map { it.toEntity() }
            alertDao.insertAlerts(entities)
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAllAlerts(): Result<Unit> {
        return try {
            alertDao.deleteAllAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
package com.weather.clearsky.feature.alerts.data.local.dao

import androidx.room.*
import com.weather.clearsky.feature.alerts.data.local.entity.WeatherAlertEntity
import com.weather.clearsky.feature.alerts.domain.entity.AlertStatus
import com.weather.clearsky.feature.alerts.domain.entity.AlertType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WeatherAlertDao {
    
    @Query("SELECT * FROM weather_alerts ORDER BY targetDateTime ASC")
    fun getAllAlerts(): Flow<List<WeatherAlertEntity>>
    
    @Query("SELECT * FROM weather_alerts WHERE status = :status ORDER BY targetDateTime ASC")
    fun getAlertsByStatus(status: AlertStatus): Flow<List<WeatherAlertEntity>>
    
    @Query("SELECT * FROM weather_alerts WHERE status = 'ACTIVE' ORDER BY targetDateTime ASC")
    fun getActiveAlerts(): Flow<List<WeatherAlertEntity>>
    
    @Query("SELECT * FROM weather_alerts WHERE status = 'ACTIVE' AND targetDateTime <= :currentTime")
    suspend fun getActiveAlertsToCheck(currentTime: LocalDateTime): List<WeatherAlertEntity>
    
    @Query("SELECT * FROM weather_alerts WHERE alertType = :alertType ORDER BY targetDateTime ASC")
    fun getAlertsByType(alertType: AlertType): Flow<List<WeatherAlertEntity>>
    
    @Query("SELECT * FROM weather_alerts WHERE id = :alertId")
    suspend fun getAlertById(alertId: String): WeatherAlertEntity?
    
    @Query("SELECT * FROM weather_alerts WHERE id = :alertId")
    fun getAlertByIdFlow(alertId: String): Flow<WeatherAlertEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlertEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<WeatherAlertEntity>)
    
    @Update
    suspend fun updateAlert(alert: WeatherAlertEntity)
    
    @Delete
    suspend fun deleteAlert(alert: WeatherAlertEntity)
    
    @Query("DELETE FROM weather_alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: String)
    
    @Query("DELETE FROM weather_alerts WHERE status = :status")
    suspend fun deleteAlertsByStatus(status: AlertStatus)
    
    @Query("DELETE FROM weather_alerts")
    suspend fun deleteAllAlerts()
    
    @Query("UPDATE weather_alerts SET status = :newStatus WHERE id = :alertId")
    suspend fun updateAlertStatus(alertId: String, newStatus: AlertStatus)
    
    @Query("UPDATE weather_alerts SET lastTriggeredAt = :triggeredAt WHERE id = :alertId")
    suspend fun updateLastTriggeredAt(alertId: String, triggeredAt: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM weather_alerts WHERE status = 'ACTIVE'")
    suspend fun getActiveAlertCount(): Int
    
    @Query("SELECT COUNT(*) FROM weather_alerts WHERE status = 'TRIGGERED'")
    suspend fun getTriggeredAlertCount(): Int
    
    @Query("SELECT * FROM weather_alerts WHERE expiryDateTime < :currentTime AND status != 'EXPIRED'")
    suspend fun getExpiredAlerts(currentTime: LocalDateTime): List<WeatherAlertEntity>
} 
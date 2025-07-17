package com.weather.clearsky.feature.alerts.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weather.clearsky.feature.alerts.domain.entity.WeatherData
import com.weather.clearsky.feature.alerts.domain.usecase.CheckAlertsUseCase
import com.weather.clearsky.feature.alerts.domain.usecase.UpdateAlertUseCase
import com.weather.clearsky.feature.alerts.presentation.notification.NotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime

@HiltWorker
class AlertCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkAlertsUseCase: CheckAlertsUseCase,
    private val updateAlertUseCase: UpdateAlertUseCase,
    private val notificationManager: NotificationManager,
    private val weatherService: WeatherService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get all active alerts that need to be checked
            val alertsResult = checkAlertsUseCase.checkActiveAlerts()
            
            if (alertsResult.isFailure) {
                return Result.retry()
            }
            
            val alerts = alertsResult.getOrNull() ?: emptyList()
            
            // Process each alert
            for (alert in alerts) {
                try {
                    // Get weather data for the alert's location
                    val weatherData = getWeatherDataForLocation(
                        alert.location.latitude,
                        alert.location.longitude
                    )
                    
                    // Check if alert condition is met
                    val shouldTrigger = checkAlertsUseCase.shouldTriggerAlert(alert, weatherData)
                    
                    if (shouldTrigger) {
                        // Mark alert as triggered
                        updateAlertUseCase.markAlertAsTriggered(alert.id)
                        
                        // Send notification
                        notificationManager.sendAlertNotification(alert, weatherData)
                        
                        // Handle repeating alerts
                        if (alert.isRepeating && alert.repeatInterval != null) {
                            val nextDateTime = alert.targetDateTime.plusDays(alert.repeatInterval.daysToAdd)
                            val updatedAlert = alert.copy(
                                targetDateTime = nextDateTime,
                                status = com.weather.clearsky.feature.alerts.domain.entity.AlertStatus.ACTIVE
                            )
                            updateAlertUseCase.updateAlert(updatedAlert)
                        }
                    }
                } catch (e: Exception) {
                    // Log error but continue processing other alerts
                    android.util.Log.e("AlertCheckWorker", "Error processing alert ${alert.id}", e)
                }
            }
            
            // Clean up expired alerts
            checkAlertsUseCase.cleanupExpiredAlerts()
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("AlertCheckWorker", "Error in alert check worker", e)
            Result.retry()
        }
    }
    
    private suspend fun getWeatherDataForLocation(latitude: Double, longitude: Double): WeatherData {
        // This should use the existing weather API service
        // For now, returning mock data - this needs to be implemented with actual API call
        return WeatherData(
            temperature = 25.0,
            humidity = 60,
            windSpeed = 10.0,
            pressure = 1013.25,
            visibility = 10.0,
            uvIndex = 5,
            weatherCondition = "Clear",
            description = "Clear sky",
            rain = null,
            snow = null
        )
    }
    
    companion object {
        const val WORK_NAME = "alert_check_work"
    }
}

// Placeholder for WeatherService - this should be injected from the weather feature module
interface WeatherService {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData
} 
package com.weather.clearsky.feature.alerts.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.weather.clearsky.feature.alerts.R
import com.weather.clearsky.feature.alerts.domain.entity.WeatherAlert
import com.weather.clearsky.feature.alerts.domain.entity.WeatherData
import com.weather.clearsky.feature.alerts.domain.entity.NotificationPriority
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // High priority channel for urgent alerts
            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT_ALERTS,
                "Urgent Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical weather alerts that require immediate attention"
                enableVibration(true)
                enableLights(true)
            }
            
            // Normal priority channel for regular alerts
            val normalChannel = NotificationChannel(
                CHANNEL_NORMAL_ALERTS,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Regular weather alerts and notifications"
                enableVibration(true)
            }
            
            // Low priority channel for info alerts
            val infoChannel = NotificationChannel(
                CHANNEL_INFO_ALERTS,
                "Weather Information",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General weather information and updates"
            }
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannels(listOf(urgentChannel, normalChannel, infoChannel))
        }
    }
    
    fun sendAlertNotification(alert: WeatherAlert, weatherData: WeatherData) {
        val channelId = getChannelIdForAlert(alert)
        val notificationId = alert.id.hashCode()
        
        // Create intent to open the app when notification is tapped
        val intent = createMainActivityIntent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(alert.alertType))
            .setContentTitle(buildNotificationTitle(alert))
            .setContentText(buildNotificationText(alert, weatherData))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(buildDetailedNotificationText(alert, weatherData)))
            .setPriority(getNotificationPriority(alert))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                // Add sound and vibration based on alert settings
                if (alert.notificationSound) {
                    setDefaults(NotificationCompat.DEFAULT_SOUND)
                }
                if (alert.notificationVibration) {
                    setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                }
            }
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    private fun getChannelIdForAlert(alert: WeatherAlert): String {
        return when (alert.alertType) {
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.TEMPERATURE,
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.WEATHER_CONDITION -> CHANNEL_URGENT_ALERTS
            else -> CHANNEL_NORMAL_ALERTS
        }
    }
    
    private fun getNotificationIcon(alertType: com.weather.clearsky.feature.alerts.domain.entity.AlertType): Int {
        return when (alertType) {
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.TEMPERATURE -> android.R.drawable.ic_dialog_alert
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.RAIN -> android.R.drawable.ic_dialog_info
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.SNOW -> android.R.drawable.ic_dialog_info
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.WIND -> android.R.drawable.ic_dialog_alert
            else -> android.R.drawable.ic_dialog_info
        }
    }
    
    private fun buildNotificationTitle(alert: WeatherAlert): String {
        return "${alert.alertType.icon} ${alert.title}"
    }
    
    private fun buildNotificationText(alert: WeatherAlert, weatherData: WeatherData): String {
        return alert.customMessage ?: buildDefaultMessage(alert, weatherData)
    }
    
    private fun buildDetailedNotificationText(alert: WeatherAlert, weatherData: WeatherData): String {
        val location = "📍 ${alert.location.displayName}"
        val condition = formatConditionText(alert, weatherData)
        val currentWeather = formatCurrentWeather(weatherData)
        
        return "$location\n\n$condition\n\nCurrent conditions:\n$currentWeather"
    }
    
    private fun buildDefaultMessage(alert: WeatherAlert, weatherData: WeatherData): String {
        return when (alert.alertType) {
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.TEMPERATURE -> 
                "Temperature alert triggered! Current: ${weatherData.temperature}°C"
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.RAIN -> 
                "Rain alert triggered! Current: ${weatherData.rain ?: 0.0}mm/h"
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.WIND -> 
                "Wind alert triggered! Current: ${weatherData.windSpeed}km/h"
            else -> "Weather alert triggered!"
        }
    }
    
    private fun formatConditionText(alert: WeatherAlert, weatherData: WeatherData): String {
        return when (val condition = alert.condition) {
            is com.weather.clearsky.feature.alerts.domain.entity.AlertCondition.TemperatureCondition -> {
                "Alert condition: Temperature ${condition.operator.symbol} ${condition.value}${condition.unit.symbol}"
            }
            is com.weather.clearsky.feature.alerts.domain.entity.AlertCondition.RainCondition -> {
                "Alert condition: Rain ${condition.operator.symbol} ${condition.value}mm/h"
            }
            else -> "Weather condition met"
        }
    }
    
    private fun formatCurrentWeather(weatherData: WeatherData): String {
        return """
            🌡️ ${weatherData.temperature}°C
            💧 ${weatherData.humidity}%
            💨 ${weatherData.windSpeed}km/h
            📊 ${weatherData.pressure}hPa
        """.trimIndent()
    }
    
    private fun getNotificationPriority(alert: WeatherAlert): Int {
        return when (alert.alertType) {
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.TEMPERATURE,
            com.weather.clearsky.feature.alerts.domain.entity.AlertType.WEATHER_CONDITION -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
    
    private fun createMainActivityIntent(): Intent {
        // This should point to the main activity of the app
        return Intent().apply {
            setClassName(context, "com.weather.clearsky.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
    
    fun cancelNotification(alertId: String) {
        notificationManager.cancel(alertId.hashCode())
    }
    
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    companion object {
        private const val CHANNEL_URGENT_ALERTS = "urgent_weather_alerts"
        private const val CHANNEL_NORMAL_ALERTS = "normal_weather_alerts"
        private const val CHANNEL_INFO_ALERTS = "info_weather_alerts"
    }
} 
package com.weather.clearsky.feature.alerts.domain.usecase

import com.weather.clearsky.feature.alerts.domain.entity.*
import com.weather.clearsky.feature.alerts.domain.repository.AlertRepository
import java.time.LocalDateTime
import javax.inject.Inject

class CheckAlertsUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    suspend fun checkActiveAlerts(currentTime: LocalDateTime = LocalDateTime.now()): Result<List<WeatherAlert>> {
        return repository.getActiveAlertsToCheck(currentTime)
    }
    
    suspend fun shouldTriggerAlert(alert: WeatherAlert, weatherData: WeatherData): Boolean {
        if (!alert.shouldCheck()) return false
        
        return when (val condition = alert.condition) {
            is AlertCondition.TemperatureCondition -> {
                checkTemperatureCondition(condition, weatherData.temperature)
            }
            is AlertCondition.RainCondition -> {
                checkRainCondition(condition, weatherData.rain ?: 0.0)
            }
            is AlertCondition.SnowCondition -> {
                checkSnowCondition(condition, weatherData.snow ?: 0.0)
            }
            is AlertCondition.WindCondition -> {
                checkWindCondition(condition, weatherData.windSpeed)
            }
            is AlertCondition.HumidityCondition -> {
                checkHumidityCondition(condition, weatherData.humidity)
            }
            is AlertCondition.WeatherCondition -> {
                checkWeatherCondition(condition, weatherData.weatherCondition)
            }
            is AlertCondition.UvIndexCondition -> {
                checkUvIndexCondition(condition, weatherData.uvIndex ?: 0)
            }
            is AlertCondition.PressureCondition -> {
                checkPressureCondition(condition, weatherData.pressure)
            }
            is AlertCondition.VisibilityCondition -> {
                checkVisibilityCondition(condition, weatherData.visibility)
            }
        }
    }
    
    private fun checkTemperatureCondition(condition: AlertCondition.TemperatureCondition, temperature: Double): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> temperature > condition.value
            ComparisonOperator.LESS_THAN -> temperature < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> temperature >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> temperature <= condition.value
            ComparisonOperator.EQUAL -> temperature == condition.value
        }
    }
    
    private fun checkRainCondition(condition: AlertCondition.RainCondition, rain: Double): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> rain > condition.value
            ComparisonOperator.LESS_THAN -> rain < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> rain >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> rain <= condition.value
            ComparisonOperator.EQUAL -> rain == condition.value
        }
    }
    
    private fun checkSnowCondition(condition: AlertCondition.SnowCondition, snow: Double): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> snow > condition.value
            ComparisonOperator.LESS_THAN -> snow < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> snow >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> snow <= condition.value
            ComparisonOperator.EQUAL -> snow == condition.value
        }
    }
    
    private fun checkWindCondition(condition: AlertCondition.WindCondition, windSpeed: Double): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> windSpeed > condition.value
            ComparisonOperator.LESS_THAN -> windSpeed < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> windSpeed >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> windSpeed <= condition.value
            ComparisonOperator.EQUAL -> windSpeed == condition.value
        }
    }
    
    private fun checkHumidityCondition(condition: AlertCondition.HumidityCondition, humidity: Int): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> humidity > condition.value
            ComparisonOperator.LESS_THAN -> humidity < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> humidity >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> humidity <= condition.value
            ComparisonOperator.EQUAL -> humidity == condition.value
        }
    }
    
    private fun checkWeatherCondition(condition: AlertCondition.WeatherCondition, weatherCondition: String): Boolean {
        return weatherCondition.contains(condition.weatherType.displayName, ignoreCase = true)
    }
    
    private fun checkUvIndexCondition(condition: AlertCondition.UvIndexCondition, uvIndex: Int): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> uvIndex > condition.value
            ComparisonOperator.LESS_THAN -> uvIndex < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> uvIndex >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> uvIndex <= condition.value
            ComparisonOperator.EQUAL -> uvIndex == condition.value
        }
    }
    
    private fun checkPressureCondition(condition: AlertCondition.PressureCondition, pressure: Double): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> pressure > condition.value
            ComparisonOperator.LESS_THAN -> pressure < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> pressure >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> pressure <= condition.value
            ComparisonOperator.EQUAL -> pressure == condition.value
        }
    }
    
    private fun checkVisibilityCondition(condition: AlertCondition.VisibilityCondition, visibility: Double): Boolean {
        return when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> visibility > condition.value
            ComparisonOperator.LESS_THAN -> visibility < condition.value
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> visibility >= condition.value
            ComparisonOperator.LESS_THAN_OR_EQUAL -> visibility <= condition.value
            ComparisonOperator.EQUAL -> visibility == condition.value
        }
    }
    
    suspend fun cleanupExpiredAlerts(): Result<Unit> {
        return repository.deleteExpiredAlerts()
    }
} 
package com.weather.clearsky.feature.alerts.domain.usecase

import com.weather.clearsky.feature.alerts.domain.entity.WeatherAlert
import com.weather.clearsky.feature.alerts.domain.repository.AlertRepository
import java.time.LocalDateTime
import javax.inject.Inject

class CreateAlertUseCase @Inject constructor(
    private val repository: AlertRepository
) {
    suspend operator fun invoke(alert: WeatherAlert): Result<WeatherAlert> {
        // Validate the alert before creating
        val validationResult = validateAlert(alert)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        return repository.createAlert(alert)
    }
    
    private fun validateAlert(alert: WeatherAlert): Result<WeatherAlert> {
        return when {
            alert.title.isBlank() -> {
                Result.failure(IllegalArgumentException("Alert title cannot be empty"))
            }
            alert.targetDateTime.isBefore(LocalDateTime.now()) -> {
                Result.failure(IllegalArgumentException("Target date/time must be in the future"))
            }
            alert.expiryDateTime?.isBefore(alert.targetDateTime) == true -> {
                Result.failure(IllegalArgumentException("Expiry date/time must be after target date/time"))
            }
            else -> Result.success(alert)
        }
    }
} 
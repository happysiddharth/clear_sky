package com.weather.clearsky.feature.weather.domain.usecase

import com.weather.clearsky.core.common.model.LocationData
import com.weather.clearsky.core.common.model.Weather
import com.weather.clearsky.core.common.result.Resource
import com.weather.clearsky.core.network.repository.WeatherNetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting current weather data.
 * Encapsulates business logic for weather fetching.
 */
class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherNetworkRepository
) {
    
    suspend operator fun invoke(location: LocationData): Flow<Resource<Weather>> {
        return weatherRepository.getCurrentWeather(location)
    }
    
    suspend operator fun invoke(cityName: String): Flow<Resource<Weather>> {
        return weatherRepository.getCurrentWeatherByCity(cityName)
    }
} 
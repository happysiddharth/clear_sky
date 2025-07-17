package com.weather.clearsky.core.network.repository

import com.weather.clearsky.core.common.model.LocationData
import com.weather.clearsky.core.common.model.Weather
import com.weather.clearsky.core.common.result.Resource
import com.weather.clearsky.core.network.api.WeatherApiService
import com.weather.clearsky.core.network.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherNetworkRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {
    
    companion object {
        // You should replace this with your actual API key from OpenWeatherMap
        // For production, store this in BuildConfig or secure configuration
        private const val API_KEY = "4e9980a4cbc984227ad965f925e120bd"
    }
    
    suspend fun getCurrentWeather(location: LocationData): Flow<Resource<Weather>> = flow {
        emit(Resource.Loading)
        
        try {
            val response = weatherApiService.getCurrentWeather(
                latitude = location.latitude,
                longitude = location.longitude,
                apiKey = API_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                val weatherDomain = response.body()!!.toDomain()
                emit(Resource.Success(weatherDomain))
            } else {
                emit(Resource.Error(
                    exception = Exception("API Error: ${response.code()}"),
                    message = "Failed to fetch weather data"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(
                exception = e,
                message = e.message ?: "Network error occurred"
            ))
        }
    }
    
    suspend fun getCurrentWeatherByCity(cityName: String): Flow<Resource<Weather>> = flow {
        emit(Resource.Loading)
        
        try {
            val response = weatherApiService.getCurrentWeatherByCity(
                cityName = cityName,
                apiKey = API_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                val weatherDomain = response.body()!!.toDomain()
                emit(Resource.Success(weatherDomain))
            } else {
                emit(Resource.Error(
                    exception = Exception("API Error: ${response.code()}"),
                    message = "Failed to fetch weather data for $cityName"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(
                exception = e,
                message = e.message ?: "Network error occurred"
            ))
        }
    }
} 
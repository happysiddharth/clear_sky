package com.weather.clearsky.feature.weather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.core.common.model.Weather
import com.weather.clearsky.core.common.model.WeatherInfo
import com.weather.clearsky.core.common.result.Resource
import com.weather.clearsky.core.network.mapper.toDisplayModel
import com.weather.clearsky.feature.weather.domain.location.LocationManager
import com.weather.clearsky.feature.weather.domain.usecase.GetCurrentWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherWidgetViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<WeatherInfo>>(Resource.Loading)
    val weatherState: StateFlow<Resource<WeatherInfo>> = _weatherState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchWeatherData()
    }

    fun fetchWeatherData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            try {
                // Try to get current location first
                val locationResult = locationManager.getCurrentLocation()
                
                val weatherFlow = when (locationResult) {
                    is Resource.Success -> {
                        getCurrentWeatherUseCase(locationResult.data)
                    }
                    is Resource.Error -> {
                        // Fallback to last known location
                        val lastLocationResult = locationManager.getLastKnownLocation()
                        when (lastLocationResult) {
                            is Resource.Success -> {
                                getCurrentWeatherUseCase(lastLocationResult.data)
                            }
                            is Resource.Error -> {
                                // Fallback to default city
                                getCurrentWeatherUseCase("London")
                            }
                            is Resource.Loading -> getCurrentWeatherUseCase("London")
                        }
                    }
                    is Resource.Loading -> getCurrentWeatherUseCase("London")
                }
                
                // Collect weather data and transform to display model
                weatherFlow.collect { resource ->
                    _weatherState.value = when (resource) {
                        is Resource.Loading -> Resource.Loading
                        is Resource.Success -> {
                            Resource.Success(resource.data.toDisplayModel())
                        }
                        is Resource.Error -> {
                            Resource.Error(resource.exception, resource.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _weatherState.value = Resource.Error(e, "Failed to fetch weather data")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshWeather() {
        fetchWeatherData()
    }

    fun fetchWeatherForCity(cityName: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            try {
                getCurrentWeatherUseCase(cityName).collect { resource ->
                    _weatherState.value = when (resource) {
                        is Resource.Loading -> Resource.Loading
                        is Resource.Success -> {
                            Resource.Success(resource.data.toDisplayModel())
                        }
                        is Resource.Error -> {
                            Resource.Error(resource.exception, resource.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _weatherState.value = Resource.Error(e, "Failed to fetch weather for $cityName")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
} 
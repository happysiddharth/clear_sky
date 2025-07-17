package com.weather.clearsky.feature.main.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.core.common.model.Weather
import com.weather.clearsky.core.common.model.WeatherInfo
import com.weather.clearsky.core.common.result.Resource
import com.weather.clearsky.core.network.mapper.toDisplayModel
import com.weather.clearsky.feature.main.data.repository.CardOrderRepository
import com.weather.clearsky.feature.main.presentation.model.*
import com.weather.clearsky.feature.weather.domain.location.LocationManager
import com.weather.clearsky.feature.weather.domain.usecase.GetCurrentWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cardOrderRepository: CardOrderRepository,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherCardUiState())
    val uiState: StateFlow<WeatherCardUiState> = _uiState.asStateFlow()

    private val _permissionStatus = MutableStateFlow<PermissionStatus>(PermissionStatus.Checking)
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _heroWeatherData = MutableStateFlow<HeroWeatherData?>(null)
    val heroWeatherData: StateFlow<HeroWeatherData?> = _heroWeatherData.asStateFlow()

    // Store specific location data for this fragment instance
    private var specificLocationData: LocationData? = null

    init {
        checkPermissions()
        initializeCards()
    }

    /**
     * Set specific location data for this weather screen
     */
    fun setLocationData(locationArgs: com.weather.clearsky.feature.main.presentation.fragment.MainFragment.LocationArguments) {
        val oldLocationId = getCurrentLocationId()
        specificLocationData = LocationData(
            id = locationArgs.id,
            name = locationArgs.name,
            country = locationArgs.country,
            area = locationArgs.area,
            latitude = locationArgs.latitude,
            longitude = locationArgs.longitude,
            isCurrentLocation = locationArgs.isCurrentLocation
        )
        
        val newLocationId = getCurrentLocationId()
        
        // Reload cards if location changed
        if (oldLocationId != newLocationId) {
            initializeCards()
        }
        
        // Refresh weather data with new location
        refreshWeatherData()
    }

    // Data class for location information
    data class LocationData(
        val id: String,
        val name: String,
        val country: String,
        val area: String,
        val latitude: Double,
        val longitude: Double,
        val isCurrentLocation: Boolean
    )

    /**
     * Get current location ID for card ordering
     */
    private fun getCurrentLocationId(): String {
        return specificLocationData?.id ?: "default"
    }

    private fun initializeCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Create cards in saved order for this specific location
            val locationId = getCurrentLocationId()
            val cardOrder = cardOrderRepository.getVisibleCardOrder(locationId)
            val cards = createCardsFromOrder(cardOrder)
            
            _uiState.value = _uiState.value.copy(
                cards = cards,
                isLoading = false
            )
            
            // Load weather data
            refreshWeatherData()
        }
    }

    private fun createCardsFromOrder(cardOrder: List<CardType>): List<WeatherCard> {
        return cardOrder.mapIndexed { index, cardType ->
            when (cardType) {
                CardType.TEMPERATURE -> WeatherCard.TemperatureCard().apply { position = index }
                CardType.AIR_QUALITY -> WeatherCard.AirQualityCard().apply { position = index }
                CardType.HUMIDITY -> WeatherCard.HumidityCard().apply { position = index }
                CardType.WIND -> WeatherCard.WindCard().apply { position = index }
                CardType.UV_INDEX -> WeatherCard.UvIndexCard().apply { position = index }
                CardType.FORECAST -> WeatherCard.ForecastCard().apply { position = index }
                CardType.HOURLY_WEATHER -> WeatherCard.HourlyWeatherCard().apply { position = index }
            }
        }
    }

    fun refreshWeatherData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)

            try {
                val weatherFlow = if (specificLocationData != null && !specificLocationData!!.isCurrentLocation) {
                    // Use specific location coordinates for weather
                    val locationData = com.weather.clearsky.core.common.model.LocationData(
                        latitude = specificLocationData!!.latitude,
                        longitude = specificLocationData!!.longitude
                    )
                    getCurrentWeatherUseCase(locationData)
                } else {
                    // Use current location (original behavior)
                    val locationResult = locationManager.getCurrentLocation()
                    
                    when (locationResult) {
                        is Resource.Success -> {
                            getCurrentWeatherUseCase(locationResult.data)
                        }
                        is Resource.Error -> {
                            // Try last known location
                            val lastLocationResult = locationManager.getLastKnownLocation()
                            when (lastLocationResult) {
                                is Resource.Success -> {
                                    getCurrentWeatherUseCase(lastLocationResult.data)
                                }
                                else -> {
                                    // Fallback to city name
                                    getCurrentWeatherUseCase("London")
                                }
                            }
                        }
                        is Resource.Loading -> {
                            // Fallback to city name
                            getCurrentWeatherUseCase("London")
                        }
                    }
                }

                weatherFlow.collect { weatherResource ->
                    when (weatherResource) {
                        is Resource.Success -> {
                            updateCardsWithWeatherData(weatherResource.data)
                            updateHeroWeatherData(weatherResource.data)
                            _uiState.value = _uiState.value.copy(
                                isRefreshing = false,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isRefreshing = false,
                                error = weatherResource.exception.message
                            )
                        }
                        is Resource.Loading -> {
                            // Keep showing refreshing state
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun updateHeroWeatherData(weather: Weather) {
        val currentTemp = weather.temperature.toInt()
        val locationName = if (specificLocationData != null) {
            if (specificLocationData!!.country.isNotEmpty()) {
                "${specificLocationData!!.name}, ${specificLocationData!!.country}"
            } else {
                specificLocationData!!.name
            }
        } else {
            weather.cityName
        }
        
        val heroData = HeroWeatherData(
            location = locationName,
            temperature = "${currentTemp}°",
            condition = weather.description,
            high = "${weather.tempMax.toInt()}°",
            low = "${weather.tempMin.toInt()}°"
        )
        _heroWeatherData.value = heroData
    }

    private fun updateCardsWithWeatherData(weather: Weather) {
        val displayModel = weather.toDisplayModel()
        val locationDisplayName = if (specificLocationData != null) {
            if (specificLocationData!!.country.isNotEmpty()) {
                "${specificLocationData!!.name}, ${specificLocationData!!.country}"
            } else {
                specificLocationData!!.name
            }
        } else {
            displayModel.cityName
        }
        
        val updatedCards = _uiState.value.cards.map { card ->
            when (card) {
                is WeatherCard.TemperatureCard -> card.copy(
                    temperature = displayModel.temperature,
                    feelsLike = displayModel.feelsLike,
                    location = locationDisplayName,
                    description = displayModel.description,
                    icon = getWeatherEmoji(displayModel.icon)
                )
                is WeatherCard.AirQualityCard -> card.copy(
                    // Mock AQI data - in real app, you'd fetch from air quality API
                    aqi = (30..150).random(),
                    quality = getAirQualityDescription((30..150).random()),
                    pm25 = "${(5..35).random()}",
                    pm10 = "${(10..50).random()}",
                    co = "${(0.1..1.5)}",
                    no2 = "${(5..40).random()}"
                )
                is WeatherCard.HumidityCard -> card.copy(
                    humidity = displayModel.humidity,
                    dewPoint = "${(weather.temperature.toInt() - 5)}°C",
                    pressure = "${weather.pressure} hPa"
                )
                is WeatherCard.WindCard -> card.copy(
                    windSpeed = "${(5..25).random()} km/h",
                    windDirection = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW").random(),
                    windGust = "${(8..35).random()} km/h",
                    visibility = "${(8..15).random()} km"
                )
                is WeatherCard.UvIndexCard -> card.copy(
                    uvIndex = (1..11).random(),
                    uvLevel = getUvLevel((1..11).random()),
                    uvDescription = getUvDescription((1..11).random()),
                    sunriseTime = "06:${(30..50).random()}",
                    sunsetTime = "19:${(10..30).random()}"
                )
                is WeatherCard.ForecastCard -> card.copy(
                    todayHigh = "${weather.tempMax.toInt()}°C",
                    todayLow = "${weather.tempMin.toInt()}°C",
                    tomorrowHigh = "${(weather.temperature.toInt() + 1)}°C",
                    tomorrowLow = "${(weather.temperature.toInt() - 6)}°C"
                )
                is WeatherCard.HourlyWeatherCard -> card.copy(
                    currentHour = getCurrentHour(),
                    location = locationDisplayName,
                    hourlyForecast = generateMockHourlyForecast(weather.temperature.toInt())
                )
                else -> card
            }
        }
        
        _uiState.value = _uiState.value.copy(cards = updatedCards)
    }

    fun onCardMoved(reorderedCards: List<WeatherCard>) {
        viewModelScope.launch {
            // Save card order for the current location
            val locationId = getCurrentLocationId()
            cardOrderRepository.saveCardOrder(reorderedCards, locationId)
            _uiState.value = _uiState.value.copy(cards = reorderedCards)
        }
    }

    /**
     * Copy card order from another location to current location
     */
    fun copyCardOrderFromLocation(sourceLocationId: String) {
        viewModelScope.launch {
            val currentLocationId = getCurrentLocationId()
            if (currentLocationId != sourceLocationId) {
                cardOrderRepository.copyCardOrderToLocation(sourceLocationId, currentLocationId)
                // Reload cards with new order
                initializeCards()
            }
        }
    }

    /**
     * Reset card order to default for current location
     */
    fun resetCardOrderToDefault() {
        viewModelScope.launch {
            val locationId = getCurrentLocationId()
            cardOrderRepository.resetToDefaults(locationId)
            // Reload cards with default order
            initializeCards()
        }
    }

    /**
     * Check if current location has custom card order
     */
    fun hasCustomCardOrder(): Boolean {
        val locationId = getCurrentLocationId()
        return cardOrderRepository.hasCustomOrder(locationId)
    }

    fun checkPermissions() {
        viewModelScope.launch {
            val hasLocationPermission = hasLocationPermission()
            _permissionStatus.value = if (hasLocationPermission) {
                PermissionStatus.Granted("Location permission granted")
            } else {
                PermissionStatus.Denied("Location permission needed for accurate weather data")
            }
        }
    }

    fun refreshWidgets() {
        // Refresh the weather data which will also update widgets
        refreshWeatherData()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getWeatherEmoji(icon: String): String {
        return when {
            icon.contains("01") -> "☀️"
            icon.contains("02") -> "⛅"
            icon.contains("03") || icon.contains("04") -> "☁️"
            icon.contains("09") || icon.contains("10") -> "🌧️"
            icon.contains("11") -> "⛈️"
            icon.contains("13") -> "🌨️"
            icon.contains("50") -> "🌫️"
            else -> "🌤️"
        }
    }

    private fun getAirQualityDescription(aqi: Int): String {
        return when {
            aqi <= 50 -> "Good"
            aqi <= 100 -> "Moderate"
            aqi <= 150 -> "Unhealthy for Sensitive"
            aqi <= 200 -> "Unhealthy"
            aqi <= 300 -> "Very Unhealthy"
            else -> "Hazardous"
        }
    }

    private fun getUvLevel(uvIndex: Int): String {
        return when {
            uvIndex <= 2 -> "Low"
            uvIndex <= 5 -> "Moderate"
            uvIndex <= 7 -> "High"
            uvIndex <= 10 -> "Very High"
            else -> "Extreme"
        }
    }

    private fun getUvDescription(uvIndex: Int): String {
        return when {
            uvIndex <= 2 -> "No protection needed"
            uvIndex <= 5 -> "Some protection required"
            uvIndex <= 7 -> "Protection essential"
            uvIndex <= 10 -> "Extra protection required"
            else -> "Stay indoors"
        }
    }

    private fun getCurrentHour(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private fun generateMockHourlyForecast(currentTemp: Int): List<HourlyForecast> {
        val calendar = java.util.Calendar.getInstance()
        val forecasts = mutableListOf<HourlyForecast>()
        
        repeat(24) { index ->
            val hour = (calendar.get(java.util.Calendar.HOUR_OF_DAY) + index) % 24
            val temp = currentTemp + (-3..3).random()
            val precipChance = (0..30).random()
            
            forecasts.add(
                HourlyForecast(
                    time = String.format("%02d:00", hour),
                    temperature = "${temp}°",
                    icon = getHourlyWeatherIcon(hour, precipChance),
                    precipitationChance = precipChance
                )
            )
        }
        
        return forecasts
    }

    private fun getHourlyWeatherIcon(hour: Int, precipChance: Int): String {
        return when {
            precipChance > 70 -> "🌧️"
            precipChance > 40 -> "🌦️" 
            precipChance > 20 -> "⛅"
            hour in 6..18 -> "☀️"
            else -> "🌙"
        }
    }

    sealed class PermissionStatus {
        object Checking : PermissionStatus()
        data class Granted(val message: String) : PermissionStatus()
        data class Denied(val message: String) : PermissionStatus()
    }
} 
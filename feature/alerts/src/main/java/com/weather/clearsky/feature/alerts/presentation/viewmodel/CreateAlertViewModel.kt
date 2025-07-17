package com.weather.clearsky.feature.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.feature.alerts.domain.entity.*
import com.weather.clearsky.feature.alerts.domain.usecase.CreateAlertUseCase
import com.weather.clearsky.feature.alerts.domain.usecase.GetAlertsUseCase
import com.weather.clearsky.feature.alerts.domain.usecase.UpdateAlertUseCase
import com.weather.clearsky.feature.weather.domain.location.LocationManager
import com.weather.clearsky.core.common.result.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class CreateAlertViewModel @Inject constructor(
    private val createAlertUseCase: CreateAlertUseCase,
    private val getAlertsUseCase: GetAlertsUseCase,
    private val updateAlertUseCase: UpdateAlertUseCase,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAlertUiState())
    val uiState: StateFlow<CreateAlertUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<AlertLocation?>(null)
    val currentLocation: StateFlow<AlertLocation?> = _currentLocation.asStateFlow()

    init {
        setupDefaultValues()
        getCurrentLocation()
    }

    private fun setupDefaultValues() {
        val now = LocalDateTime.now().plusHours(1) // Default to 1 hour from now
        
        _uiState.value = _uiState.value.copy(
            selectedAlertType = AlertType.TEMPERATURE,
            selectedOperator = ComparisonOperator.GREATER_THAN,
            targetDateTime = now,
            targetDateString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            targetTimeString = now.format(DateTimeFormatter.ofPattern("HH:mm")),
            conditionValue = "25.0",
            isNotificationEnabled = true,
            notificationSound = true,
            notificationVibration = true
        )
    }

    private fun getCurrentLocation() {
        viewModelScope.launch {
            when (val locationResult = locationManager.getCurrentLocation()) {
                is Resource.Success -> {
                    _currentLocation.value = AlertLocation(
                        latitude = locationResult.data.latitude,
                        longitude = locationResult.data.longitude,
                        cityName = "Current Location",
                        countryName = ""
                    )
                }
                is Resource.Error -> {
                    // Use fallback location if current location fails
                    _currentLocation.value = AlertLocation(
                        latitude = 0.0,
                        longitude = 0.0,
                        cityName = "Current Location",
                        countryName = ""
                    )
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        validateForm()
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateAlertType(alertType: AlertType) {
        _uiState.value = _uiState.value.copy(
            selectedAlertType = alertType,
            // Reset condition when alert type changes
            conditionValue = getDefaultValueForAlertType(alertType),
            selectedWeatherType = null
        )
        validateForm()
    }

    fun updateOperator(operator: ComparisonOperator) {
        _uiState.value = _uiState.value.copy(selectedOperator = operator)
        validateForm()
    }

    fun updateConditionValue(value: String) {
        _uiState.value = _uiState.value.copy(conditionValue = value)
        validateForm()
    }

    fun updateWeatherType(weatherType: WeatherType) {
        _uiState.value = _uiState.value.copy(selectedWeatherType = weatherType)
        validateForm()
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(locationString = location)
        validateForm()
    }

    fun useCurrentLocation() {
        _currentLocation.value?.let { location ->
            _uiState.value = _uiState.value.copy(
                locationString = location.displayName,
                selectedLocation = location
            )
        }
    }

    fun updateTargetDate(year: Int, month: Int, dayOfMonth: Int) {
        val currentState = _uiState.value
        val time = currentState.targetDateTime.toLocalTime()
        val newDateTime = LocalDateTime.of(year, month, dayOfMonth, time.hour, time.minute)
        
        _uiState.value = currentState.copy(
            targetDateTime = newDateTime,
            targetDateString = newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        validateForm()
    }

    fun updateTargetTime(hourOfDay: Int, minute: Int) {
        val currentState = _uiState.value
        val date = currentState.targetDateTime.toLocalDate()
        val newDateTime = LocalDateTime.of(date, java.time.LocalTime.of(hourOfDay, minute))
        
        _uiState.value = currentState.copy(
            targetDateTime = newDateTime,
            targetTimeString = newDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        validateForm()
    }

    fun updateExpiryDate(year: Int, month: Int, dayOfMonth: Int) {
        val currentState = _uiState.value
        val time = currentState.expiryDateTime?.toLocalTime() ?: java.time.LocalTime.of(23, 59)
        val newDateTime = LocalDateTime.of(year, month, dayOfMonth, time.hour, time.minute)
        
        _uiState.value = currentState.copy(
            expiryDateTime = newDateTime,
            expiryDateString = newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        validateForm()
    }

    fun updateExpiryTime(hourOfDay: Int, minute: Int) {
        val currentState = _uiState.value
        val date = currentState.expiryDateTime?.toLocalDate() ?: currentState.targetDateTime.toLocalDate()
        val newDateTime = LocalDateTime.of(date, java.time.LocalTime.of(hourOfDay, minute))
        
        _uiState.value = currentState.copy(
            expiryDateTime = newDateTime,
            expiryTimeString = newDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        validateForm()
    }

    fun setHasExpiry(hasExpiry: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasExpiry = hasExpiry,
            expiryDateTime = if (!hasExpiry) null else _uiState.value.expiryDateTime
        )
    }

    fun setIsRepeating(isRepeating: Boolean) {
        _uiState.value = _uiState.value.copy(isRepeating = isRepeating)
    }

    fun updateRepeatInterval(interval: RepeatInterval) {
        _uiState.value = _uiState.value.copy(selectedRepeatInterval = interval)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNotificationEnabled = enabled)
    }

    fun setNotificationSound(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationSound = enabled)
    }

    fun setNotificationVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationVibration = enabled)
    }

    fun createAlert() {
        if (!_uiState.value.isFormValid) {
            _uiState.value = _uiState.value.copy(
                error = "Please fill in all required fields correctly"
            )
            return
        }

        val currentState = _uiState.value
        
        // Build alert condition based on alert type
        val condition = buildAlertCondition(currentState)
        if (condition == null) {
            _uiState.value = _uiState.value.copy(
                error = "Invalid alert condition"
            )
            return
        }

        // Use current location if no custom location specified
        val alertLocation = currentState.selectedLocation ?: _currentLocation.value
        if (alertLocation == null) {
            _uiState.value = _uiState.value.copy(
                error = "Location is required"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        if (currentState.isEditMode && currentState.editingAlertId != null) {
            // Update existing alert
            val updatedAlert = WeatherAlert(
                id = currentState.editingAlertId,
                title = currentState.title,
                description = currentState.description,
                alertType = currentState.selectedAlertType,
                condition = condition,
                location = alertLocation,
                targetDateTime = currentState.targetDateTime,
                expiryDateTime = currentState.expiryDateTime,
                isRepeating = currentState.isRepeating,
                repeatInterval = if (currentState.isRepeating) currentState.selectedRepeatInterval else null,
                isNotificationEnabled = currentState.isNotificationEnabled,
                notificationSound = currentState.notificationSound,
                notificationVibration = currentState.notificationVibration,
                updatedAt = LocalDateTime.now()
            )

            viewModelScope.launch {
                Log.d("CreateAlertViewModel", "Updating alert: ${updatedAlert.title}")
                updateAlertUseCase.updateAlert(updatedAlert)
                    .onSuccess {
                        Log.d("CreateAlertViewModel", "Alert updated successfully: ${updatedAlert.title}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAlertCreated = true
                        )
                    }
                    .onFailure { exception ->
                        Log.e("CreateAlertViewModel", "Failed to update alert: ${exception.message}", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update alert"
                        )
                    }
            }
        } else {
            // Create new alert
            val alert = WeatherAlert(
                title = currentState.title,
                description = currentState.description,
                alertType = currentState.selectedAlertType,
                condition = condition,
                location = alertLocation,
                targetDateTime = currentState.targetDateTime,
                expiryDateTime = currentState.expiryDateTime,
                isRepeating = currentState.isRepeating,
                repeatInterval = if (currentState.isRepeating) currentState.selectedRepeatInterval else null,
                isNotificationEnabled = currentState.isNotificationEnabled,
                notificationSound = currentState.notificationSound,
                notificationVibration = currentState.notificationVibration
            )

            viewModelScope.launch {
                Log.d("CreateAlertViewModel", "Creating alert: ${alert.title}")
                createAlertUseCase(alert)
                    .onSuccess {
                        Log.d("CreateAlertViewModel", "Alert created successfully: ${alert.title}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAlertCreated = true
                        )
                    }
                    .onFailure { exception ->
                        Log.e("CreateAlertViewModel", "Failed to create alert: ${exception.message}", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create alert"
                        )
                    }
            }
        }
    }

    private fun buildAlertCondition(state: CreateAlertUiState): AlertCondition? {
        return try {
            when (state.selectedAlertType) {
                AlertType.TEMPERATURE -> AlertCondition.TemperatureCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toDouble()
                )
                AlertType.RAIN -> AlertCondition.RainCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toDouble()
                )
                AlertType.SNOW -> AlertCondition.SnowCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toDouble()
                )
                AlertType.WIND -> AlertCondition.WindCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toDouble()
                )
                AlertType.HUMIDITY -> AlertCondition.HumidityCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toInt()
                )
                AlertType.WEATHER_CONDITION -> {
                    state.selectedWeatherType?.let { weatherType ->
                        AlertCondition.WeatherCondition(weatherType)
                    }
                }
                AlertType.UV_INDEX -> AlertCondition.UvIndexCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toInt()
                )
                AlertType.PRESSURE -> AlertCondition.PressureCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toDouble()
                )
                AlertType.VISIBILITY -> AlertCondition.VisibilityCondition(
                    operator = state.selectedOperator,
                    value = state.conditionValue.toDouble()
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getDefaultValueForAlertType(alertType: AlertType): String {
        return when (alertType) {
            AlertType.TEMPERATURE -> "25.0"
            AlertType.RAIN -> "1.0"
            AlertType.SNOW -> "1.0"
            AlertType.WIND -> "20.0"
            AlertType.HUMIDITY -> "70"
            AlertType.UV_INDEX -> "6"
            AlertType.PRESSURE -> "1013.0"
            AlertType.VISIBILITY -> "10.0"
            AlertType.WEATHER_CONDITION -> ""
        }
    }

    private fun validateForm() {
        val currentState = _uiState.value
        
        val isTitleValid = currentState.title.isNotBlank()
        val isLocationValid = currentState.locationString.isNotBlank()
        val isDateTimeValid = currentState.targetDateTime.isAfter(LocalDateTime.now())
        
        val isConditionValid = when (currentState.selectedAlertType) {
            AlertType.WEATHER_CONDITION -> currentState.selectedWeatherType != null
            else -> currentState.conditionValue.isNotBlank() && 
                    when (currentState.selectedAlertType) {
                        AlertType.HUMIDITY, AlertType.UV_INDEX -> currentState.conditionValue.toIntOrNull() != null
                        else -> currentState.conditionValue.toDoubleOrNull() != null
                    }
        }

        val isExpiryValid = if (currentState.hasExpiry && currentState.expiryDateTime != null) {
            currentState.expiryDateTime.isAfter(currentState.targetDateTime)
        } else true

        _uiState.value = currentState.copy(
            isFormValid = isTitleValid && isLocationValid && isDateTimeValid && 
                         isConditionValid && isExpiryValid
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetCreatedState() {
        _uiState.value = _uiState.value.copy(isAlertCreated = false)
    }
    
    fun loadAlertForEditing(alertId: String) {
        viewModelScope.launch {
            Log.d("CreateAlertViewModel", "Loading alert for editing: $alertId")
            getAlertsUseCase.getAlertById(alertId)
                .onSuccess { alert ->
                    alert?.let {
                        Log.d("CreateAlertViewModel", "Alert loaded for editing: ${it.title}")
                        populateFormWithAlert(it)
                    } ?: run {
                        Log.e("CreateAlertViewModel", "Alert not found: $alertId")
                        _uiState.value = _uiState.value.copy(error = "Alert not found")
                    }
                }
                .onFailure { exception ->
                    Log.e("CreateAlertViewModel", "Failed to load alert: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(error = "Failed to load alert: ${exception.message}")
                }
        }
    }
    
    private fun populateFormWithAlert(alert: WeatherAlert) {
        _uiState.value = _uiState.value.copy(
            editingAlertId = alert.id,
            isEditMode = true,
            title = alert.title,
            description = alert.description,
            selectedAlertType = alert.alertType,
            locationString = alert.location.displayName,
            selectedLocation = alert.location,
            targetDateTime = alert.targetDateTime,
            targetDateString = alert.targetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            targetTimeString = alert.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            hasExpiry = alert.expiryDateTime != null,
            expiryDateTime = alert.expiryDateTime,
            expiryDateString = alert.expiryDateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "",
            expiryTimeString = alert.expiryDateTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
            isRepeating = alert.isRepeating,
            selectedRepeatInterval = alert.repeatInterval ?: RepeatInterval.DAILY,
            isNotificationEnabled = alert.isNotificationEnabled,
            notificationSound = alert.notificationSound,
            notificationVibration = alert.notificationVibration
        )
        
        // Populate condition-specific fields
        when (val condition = alert.condition) {
            is AlertCondition.TemperatureCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.RainCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.SnowCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.WindCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.HumidityCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.WeatherCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedWeatherType = condition.weatherType
                )
            }
            is AlertCondition.UvIndexCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.PressureCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
            is AlertCondition.VisibilityCondition -> {
                _uiState.value = _uiState.value.copy(
                    selectedOperator = condition.operator,
                    conditionValue = condition.value.toString()
                )
            }
        }
        
        validateForm()
    }
}

data class CreateAlertUiState(
    val title: String = "",
    val description: String = "",
    val selectedAlertType: AlertType = AlertType.TEMPERATURE,
    val selectedOperator: ComparisonOperator = ComparisonOperator.GREATER_THAN,
    val conditionValue: String = "",
    val selectedWeatherType: WeatherType? = null,
    val locationString: String = "Current Location",
    val selectedLocation: AlertLocation? = null,
    val targetDateTime: LocalDateTime = LocalDateTime.now().plusHours(1),
    val targetDateString: String = "",
    val targetTimeString: String = "",
    val hasExpiry: Boolean = false,
    val expiryDateTime: LocalDateTime? = null,
    val expiryDateString: String = "",
    val expiryTimeString: String = "",
    val isRepeating: Boolean = false,
    val selectedRepeatInterval: RepeatInterval = RepeatInterval.DAILY,
    val isNotificationEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val isAlertCreated: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val editingAlertId: String? = null
) 
package com.weather.clearsky.feature.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.feature.alerts.domain.entity.AlertStatus
import com.weather.clearsky.feature.alerts.domain.entity.AlertType
import com.weather.clearsky.feature.alerts.domain.entity.WeatherAlert
import com.weather.clearsky.feature.alerts.domain.usecase.GetAlertsUseCase
import com.weather.clearsky.feature.alerts.domain.usecase.UpdateAlertUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val updateAlertUseCase: UpdateAlertUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()
    
    private val _filterType = MutableStateFlow<AlertType?>(null)
    private val _filterStatus = MutableStateFlow<AlertStatus?>(null)
    
    init {
        Log.d("AlertsViewModel", "AlertsViewModel initialized, loading alerts...")
        loadAlerts()
    }
    
    private fun loadAlerts() {
        viewModelScope.launch {
            combine(
                getAlertsUseCase.getAllAlerts(),
                _filterType,
                _filterStatus
            ) { alerts, typeFilter, statusFilter ->
                var filteredAlerts = alerts
                
                typeFilter?.let { type ->
                    filteredAlerts = filteredAlerts.filter { it.alertType == type }
                }
                
                statusFilter?.let { status ->
                    filteredAlerts = filteredAlerts.filter { it.status == status }
                }
                
                filteredAlerts
            }.catch { exception ->
                Log.e("AlertsViewModel", "Error loading alerts: ${exception.message}", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }.collect { alerts ->
                Log.d("AlertsViewModel", "Loaded ${alerts.size} alerts")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alerts = alerts,
                    error = null
                )
            }
        }
    }
    
    fun setTypeFilter(type: AlertType?) {
        _filterType.value = type
    }
    
    fun setStatusFilter(status: AlertStatus?) {
        _filterStatus.value = status
    }
    
    fun clearFilters() {
        _filterType.value = null
        _filterStatus.value = null
    }
    
    fun toggleAlertStatus(alertId: String, currentStatus: AlertStatus) {
        viewModelScope.launch {
            val newStatus = when (currentStatus) {
                AlertStatus.ACTIVE -> AlertStatus.INACTIVE
                AlertStatus.INACTIVE -> AlertStatus.ACTIVE
                else -> return@launch
            }
            
            updateAlertUseCase.updateAlertStatus(alertId, newStatus)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update alert: ${exception.message}"
                    )
                }
        }
    }
    
    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            updateAlertUseCase.deleteAlert(alertId)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete alert: ${exception.message}"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshAlerts() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadAlerts()
    }
    
    fun clearAllAlerts() {
        viewModelScope.launch {
            updateAlertUseCase.deleteAllAlerts()
                .onSuccess {
                    Log.d("AlertsViewModel", "All alerts cleared successfully")
                }
                .onFailure { exception ->
                    Log.e("AlertsViewModel", "Failed to clear alerts: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to clear alerts: ${exception.message}"
                    )
                }
        }
    }
}

data class AlertsUiState(
    val isLoading: Boolean = true,
    val alerts: List<WeatherAlert> = emptyList(),
    val error: String? = null
) 
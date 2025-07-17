package com.weather.clearsky.feature.main.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.feature.main.data.model.SavedLocation
import com.weather.clearsky.feature.main.data.repository.LocationRepository
import com.weather.clearsky.feature.weather.domain.location.LocationManager
import com.weather.clearsky.core.common.result.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddLocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<SavedLocation>>(emptyList())
    val searchResults: StateFlow<List<SavedLocation>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _addLocationResult = MutableStateFlow<Triple<Boolean, String, String?>?>(null)
    val addLocationResult: StateFlow<Triple<Boolean, String, String?>?> = _addLocationResult.asStateFlow()

    /**
     * Search for locations based on query
     * In a real app, this would integrate with a geocoding API
     */
    fun searchLocations(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Simulate API delay
                kotlinx.coroutines.delay(500)
                
                // Mock location search - in real app, use geocoding API
                val mockResults = searchMockLocations(query)
                _searchResults.value = mockResults
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    /**
     * Add a location to saved locations
     * Ensures new locations are placed after current location
     */
    fun addLocation(location: SavedLocation) {
        viewModelScope.launch {
            // Ensure the new location is not marked as current location
            val newLocation = location.copy(isCurrentLocation = false)
            val locationId = locationRepository.addLocation(newLocation)
            if (locationId != null) {
                _addLocationResult.value = Triple(true, "Location added successfully!", locationId)
            } else {
                _addLocationResult.value = Triple(false, "Location already exists or failed to add", null)
            }
        }
    }

    /**
     * Add current location to saved locations
     */
    fun addCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val locationResult = locationManager.getCurrentLocationWithAddress()
                when (locationResult) {
                    is Resource.Success -> {
                        val detailedLocation = locationResult.data
                        val currentLocation = SavedLocation(
                            name = if (detailedLocation.cityName.isNotEmpty()) detailedLocation.cityName else "Current Location",
                            country = detailedLocation.countryName,
                            area = detailedLocation.areaName,
                            latitude = detailedLocation.latitude,
                            longitude = detailedLocation.longitude,
                            isCurrentLocation = true
                        )
                        addLocation(currentLocation)
                    }
                    is Resource.Error -> {
                        _addLocationResult.value = Triple(false, "Unable to get current location. Please check location permissions.", null)
                    }
                    is Resource.Loading -> {
                        // Continue loading
                    }
                }
            } catch (e: Exception) {
                _addLocationResult.value = Triple(false, "Failed to get current location: ${e.message}", null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear the add location result
     */
    fun clearAddLocationResult() {
        _addLocationResult.value = null
    }

    /**
     * Mock location search function
     * In a real app, replace with actual geocoding API calls
     */
    private fun searchMockLocations(query: String): List<SavedLocation> {
        val mockLocations = listOf(
            SavedLocation("", "New York", "United States", "", 40.7128, -74.0060),
            SavedLocation("", "London", "United Kingdom", "", 51.5074, -0.1278),
            SavedLocation("", "Tokyo", "Japan", "", 35.6762, 139.6503),
            SavedLocation("", "Paris", "France", "", 48.8566, 2.3522),
            SavedLocation("", "Sydney", "Australia", "", -33.8688, 151.2093),
            SavedLocation("", "Mumbai", "India", "", 19.0760, 72.8777),
            SavedLocation("", "Dubai", "United Arab Emirates", "", 25.2048, 55.2708),
            SavedLocation("", "Singapore", "Singapore", "", 1.3521, 103.8198),
            SavedLocation("", "Toronto", "Canada", "", 43.6532, -79.3832),
            SavedLocation("", "Berlin", "Germany", "", 52.5200, 13.4050),
            SavedLocation("", "Madrid", "Spain", "", 40.4168, -3.7038),
            SavedLocation("", "Rome", "Italy", "", 41.9028, 12.4964),
            SavedLocation("", "Bangkok", "Thailand", "", 13.7563, 100.5018),
            SavedLocation("", "Seoul", "South Korea", "", 37.5665, 126.9780),
            SavedLocation("", "Cairo", "Egypt", "", 30.0444, 31.2357),
            SavedLocation("", "Cape Town", "South Africa", "", -33.9249, 18.4241),
            SavedLocation("", "São Paulo", "Brazil", "", -23.5558, -46.6396),
            SavedLocation("", "Mexico City", "Mexico", "", 19.4326, -99.1332),
            SavedLocation("", "Moscow", "Russia", "", 55.7558, 37.6176),
            SavedLocation("", "Istanbul", "Turkey", "", 41.0082, 28.9784),
            SavedLocation("", "Los Angeles", "United States", "", 34.0522, -118.2437),
            SavedLocation("", "Chicago", "United States", "", 41.8781, -87.6298),
            SavedLocation("", "Miami", "United States", "", 25.7617, -80.1918),
            SavedLocation("", "Las Vegas", "United States", "", 36.1699, -115.1398),
            SavedLocation("", "San Francisco", "United States", "", 37.7749, -122.4194)
        )

        return mockLocations.filter { location ->
            location.name.contains(query, ignoreCase = true) ||
            location.country.contains(query, ignoreCase = true)
        }.take(10)
    }
} 
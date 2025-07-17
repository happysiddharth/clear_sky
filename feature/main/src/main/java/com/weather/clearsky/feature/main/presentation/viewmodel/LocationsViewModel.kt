package com.weather.clearsky.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.clearsky.feature.main.data.model.SavedLocation
import com.weather.clearsky.feature.main.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    /**
     * StateFlow of saved locations
     */
    val savedLocations = locationRepository.savedLocations

    /**
     * Initialize locations - add default current location if no locations exist
     * Allow locations to maintain their natural order
     */
    fun initializeLocations() {
        viewModelScope.launch {
            // First, ensure current location is added if no locations exist
            locationRepository.addCurrentLocationIfEmpty()
            
            // Then refresh current location data if it's a placeholder
            locationRepository.refreshCurrentLocationIfNeeded()
        }
    }

    /**
     * Add a new location (can be current or regular location)
     * Maintains the specified order without forcing current location to be first
     */
    fun addNewLocation(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.addLocation(location)
        }
    }

    /**
     * Check if this is the first time user is opening the app
     */
    fun isFirstTimeUser(): Boolean {
        return locationRepository.getSavedLocations().isEmpty()
    }

    /**
     * Manually refresh current location with latest geocoded data
     */
    fun refreshCurrentLocation() {
        viewModelScope.launch {
            locationRepository.refreshCurrentLocationIfNeeded()
        }
    }

    /**
     * Remove a location by ID
     */
    fun removeLocation(locationId: String) {
        viewModelScope.launch {
            locationRepository.removeLocation(locationId)
        }
    }

    /**
     * Reorder locations
     */
    fun reorderLocations(newOrder: List<SavedLocation>) {
        viewModelScope.launch {
            locationRepository.reorderLocations(newOrder)
        }
    }

    /**
     * Get all saved locations
     */
    fun getSavedLocations(): List<SavedLocation> {
        return locationRepository.getSavedLocations()
    }

    /**
     * Handle location permission granted - especially important for first-time users
     * This ensures current location is properly set up when permission is given
     */
    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            // Force refresh of current location with real location data
            locationRepository.refreshCurrentLocationIfNeeded()
            
            // If no locations exist yet, add current location
            if (locationRepository.getSavedLocations().isEmpty()) {
                locationRepository.addCurrentLocationIfEmpty()
            }
            
            // Ensure current location is first
            // This logic is now handled by the new moveLocationToPosition method
        }
    }

    /**
     * Get the current location (first location that's marked as current)
     */
    fun getCurrentLocation(): SavedLocation? {
        return locationRepository.getSavedLocations().find { it.isCurrentLocation }
    }

    /**
     * Check if we have a current location set up
     */
    fun hasCurrentLocation(): Boolean {
        return locationRepository.getSavedLocations().any { it.isCurrentLocation }
    }

    /**
     * Move a location to a specific position
     * Updates all location orders to maintain sequence
     */
    fun moveLocationToPosition(locationId: String, newPosition: Int) {
        viewModelScope.launch {
            locationRepository.insertLocationAtPosition(locationId, newPosition)
        }
    }

    /**
     * Swap two locations by their IDs
     */
    fun swapLocations(locationId1: String, locationId2: String) {
        viewModelScope.launch {
            locationRepository.swapLocations(locationId1, locationId2)
        }
    }

    /**
     * Move a location up by one position
     */
    fun moveLocationUp(locationId: String) {
        viewModelScope.launch {
            val locations = locationRepository.getSavedLocations()
            val currentIndex = locations.indexOfFirst { it.id == locationId }
            if (currentIndex > 0) {
                locationRepository.insertLocationAtPosition(locationId, currentIndex - 1)
            }
        }
    }

    /**
     * Move a location down by one position
     */
    fun moveLocationDown(locationId: String) {
        viewModelScope.launch {
            val locations = locationRepository.getSavedLocations()
            val currentIndex = locations.indexOfFirst { it.id == locationId }
            if (currentIndex != -1 && currentIndex < locations.size - 1) {
                locationRepository.insertLocationAtPosition(locationId, currentIndex + 1)
            }
        }
    }

    /**
     * Normalize location orders to fix any gaps or duplicates
     */
    fun normalizeLocationOrders() {
        viewModelScope.launch {
            locationRepository.normalizeLocationOrders()
        }
    }
} 
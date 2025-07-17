package com.weather.clearsky.feature.main.data.repository

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather.clearsky.feature.main.data.model.SavedLocation
import com.weather.clearsky.feature.weather.domain.location.LocationManager
import com.weather.clearsky.core.common.result.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing saved weather locations
 */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager
) {
    companion object {
        private const val PREFS_NAME = "saved_locations_prefs"
        private const val KEY_LOCATIONS = "saved_locations"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _savedLocations = MutableStateFlow<List<SavedLocation>>(emptyList())
    val savedLocations: Flow<List<SavedLocation>> = _savedLocations.asStateFlow()

    init {
        loadSavedLocations()
    }

    /**
     * Get all saved locations
     */
    fun getSavedLocations(): List<SavedLocation> {
        return _savedLocations.value
    }

    /**
     * Add a new location
     * @return The ID of the added location if successful, null if failed
     */
    suspend fun addLocation(location: SavedLocation): String? {
        try {
            val currentLocations = _savedLocations.value.toMutableList()
            
            // Check if location already exists
            if (currentLocations.any { it.name.equals(location.name, ignoreCase = true) && 
                                     it.country.equals(location.country, ignoreCase = true) }) {
                return null // Location already exists
            }
            
            // Assign order based on what's specified or append to end
            val newOrder = if (location.order > 0) {
                // Use specified order if provided
                location.order
            } else {
                // If no order specified, append to end
                (currentLocations.maxOfOrNull { it.order } ?: -1) + 1
            }
            
            val newLocationId = UUID.randomUUID().toString()
            val newLocation = location.copy(
                id = newLocationId,
                order = newOrder
            )
            
            currentLocations.add(newLocation)
            saveLocationsList(currentLocations)
            _savedLocations.value = currentLocations.sortedBy { it.order }
            
            return newLocationId
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Remove a location
     */
    suspend fun removeLocation(locationId: String): Boolean {
        try {
            val currentLocations = _savedLocations.value.toMutableList()
            val removed = currentLocations.removeAll { it.id == locationId }
            
            if (removed) {
                // Reorder remaining locations
                val reorderedLocations = currentLocations.mapIndexed { index, location ->
                    location.copy(order = index)
                }
                saveLocationsList(reorderedLocations)
                _savedLocations.value = reorderedLocations
            }
            return removed
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Reorder locations
     */
    suspend fun reorderLocations(newOrder: List<SavedLocation>) {
        try {
            val reorderedLocations = newOrder.mapIndexed { index, location ->
                location.copy(order = index)
            }
            saveLocationsList(reorderedLocations)
            _savedLocations.value = reorderedLocations
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Swap two locations by their IDs
     */
    suspend fun swapLocations(locationId1: String, locationId2: String): Boolean {
        try {
            val locations = _savedLocations.value.toMutableList()
            val index1 = locations.indexOfFirst { it.id == locationId1 }
            val index2 = locations.indexOfFirst { it.id == locationId2 }
            
            if (index1 != -1 && index2 != -1) {
                val order1 = locations[index1].order
                val order2 = locations[index2].order
                
                locations[index1] = locations[index1].copy(order = order2)
                locations[index2] = locations[index2].copy(order = order1)
                
                saveLocationsList(locations)
                _savedLocations.value = locations.sortedBy { it.order }
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Insert location at specific position, shifting others as needed
     */
    suspend fun insertLocationAtPosition(locationId: String, newPosition: Int): Boolean {
        try {
            val locations = _savedLocations.value.toMutableList()
            val currentIndex = locations.indexOfFirst { it.id == locationId }
            
            if (currentIndex != -1 && newPosition >= 0 && newPosition < locations.size) {
                val locationToMove = locations.removeAt(currentIndex)
                locations.add(newPosition, locationToMove)
                
                // Reorder all locations
                val reorderedLocations = locations.mapIndexed { index, location ->
                    location.copy(order = index)
                }
                
                saveLocationsList(reorderedLocations)
                _savedLocations.value = reorderedLocations
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Fix any gaps or duplicates in location ordering
     */
    suspend fun normalizeLocationOrders() {
        try {
            val locations = _savedLocations.value.sortedBy { it.order }
            val normalizedLocations = locations.mapIndexed { index, location ->
                location.copy(order = index)
            }
            saveLocationsList(normalizedLocations)
            _savedLocations.value = normalizedLocations
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Add default current location if no locations exist
     */
    suspend fun addCurrentLocationIfEmpty() {
        if (_savedLocations.value.isEmpty()) {
            try {
                // Try to get detailed location data with geocoding
                val locationResult = locationManager.getCurrentLocationWithAddress()
                when (locationResult) {
                    is Resource.Success -> {
                        val detailedLocation = locationResult.data
                        val currentLocation = SavedLocation(
                            id = UUID.randomUUID().toString(),
                            name = if (detailedLocation.cityName.isNotEmpty()) detailedLocation.cityName else "Current Location",
                            country = detailedLocation.countryName,
                            area = detailedLocation.areaName,
                            latitude = detailedLocation.latitude,
                            longitude = detailedLocation.longitude,
                            isCurrentLocation = true,
                            order = 0
                        )
                        addLocation(currentLocation)
                    }
                    is Resource.Error -> {
                        // Fallback to basic current location if geocoding fails
                        val currentLocation = SavedLocation(
                            id = UUID.randomUUID().toString(),
                            name = "Current Location",
                            country = "",
                            area = "",
                            isCurrentLocation = true,
                            order = 0
                        )
                        addLocation(currentLocation)
                    }
                    is Resource.Loading -> {
                        // Handle loading state - create basic location
                        val currentLocation = SavedLocation(
                            id = UUID.randomUUID().toString(),
                            name = "Current Location",
                            country = "",
                            area = "",
                            isCurrentLocation = true,
                            order = 0
                        )
                        addLocation(currentLocation)
                    }
                }
            } catch (e: Exception) {
                // Fallback to basic current location if any error occurs
                val currentLocation = SavedLocation(
                    id = UUID.randomUUID().toString(),
                    name = "Current Location",
                    country = "",
                    area = "",
                    isCurrentLocation = true,
                    order = 0
                )
                addLocation(currentLocation)
            }
        } else {
            // Check if we have a placeholder current location and update it
            refreshCurrentLocationIfNeeded()
        }
    }

    /**
     * Refresh current location with real geocoded data if it's a placeholder
     */
    suspend fun refreshCurrentLocationIfNeeded() {
        val currentLocation = _savedLocations.value.find { it.isCurrentLocation }
        if (currentLocation != null && 
            (currentLocation.name == "Current Location" || currentLocation.area.isEmpty())) {
            
            try {
                val locationResult = locationManager.getCurrentLocationWithAddress()
                if (locationResult is Resource.Success) {
                    val detailedLocation = locationResult.data
                    val updatedLocation = currentLocation.copy(
                        name = if (detailedLocation.cityName.isNotEmpty()) detailedLocation.cityName else currentLocation.name,
                        country = detailedLocation.countryName.ifEmpty { currentLocation.country },
                        area = detailedLocation.areaName,
                        latitude = detailedLocation.latitude,
                        longitude = detailedLocation.longitude
                    )
                    
                    // Update the location in the list
                    val currentLocations = _savedLocations.value.toMutableList()
                    val index = currentLocations.indexOfFirst { it.id == currentLocation.id }
                    if (index != -1) {
                        currentLocations[index] = updatedLocation
                        saveLocationsList(currentLocations)
                        _savedLocations.value = currentLocations
                    }
                }
            } catch (e: Exception) {
                // Ignore errors in refresh
            }
        }
    }

    /**
     * Get location by ID
     */
    fun getLocationById(id: String): SavedLocation? {
        return _savedLocations.value.find { it.id == id }
    }

    /**
     * Get the first location (usually current location)
     */
    fun getFirstLocation(): SavedLocation? {
        return _savedLocations.value.minByOrNull { it.order }
    }

    private fun loadSavedLocations() {
        try {
            val locationsJson = prefs.getString(KEY_LOCATIONS, null)
            if (locationsJson != null) {
                val type = object : TypeToken<List<SavedLocation>>() {}.type
                val locations: List<SavedLocation> = gson.fromJson(locationsJson, type)
                _savedLocations.value = locations.sortedBy { it.order }
            }
        } catch (e: Exception) {
            _savedLocations.value = emptyList()
        }
    }

    private fun saveLocationsList(locations: List<SavedLocation>) {
        prefs.edit {
            putString(KEY_LOCATIONS, gson.toJson(locations))
        }
    }
} 
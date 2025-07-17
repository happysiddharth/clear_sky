package com.weather.clearsky.feature.weather.domain.location

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.weather.clearsky.core.common.extensions.hasPermission
import com.weather.clearsky.core.common.model.LocationData
import com.weather.clearsky.core.common.result.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Extended location data with address information
 */
data class DetailedLocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
    val countryName: String = "",
    val areaName: String = "", // Locality/neighborhood/area
    val fullAddress: String = ""
)

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    
    fun hasLocationPermission(): Boolean {
        return context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
               context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    
    suspend fun getCurrentLocation(): Resource<LocationData> {
        try {
            if (!hasLocationPermission()) {
                return Resource.Error(
                    SecurityException("Location permission not granted"),
                    "Location permission required"
                )
            }
            
            val location = getCurrentLocationInternal()
            return if (location != null) {
                Resource.Success(
                    LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
            } else {
                Resource.Error(
                    Exception("Unable to get current location"),
                    "Location not available"
                )
            }
        } catch (e: Exception) {
            return Resource.Error(e, e.message ?: "Location error")
        }
    }
    
    /**
     * Get current location with detailed address information
     */
    suspend fun getCurrentLocationWithAddress(): Resource<DetailedLocationData> {
        try {
            if (!hasLocationPermission()) {
                return Resource.Error(
                    SecurityException("Location permission not granted"),
                    "Location permission required"
                )
            }
            
            val location = getCurrentLocationInternal()
            return if (location != null) {
                val addressInfo = getAddressFromCoordinates(location.latitude, location.longitude)
                Resource.Success(addressInfo)
            } else {
                Resource.Error(
                    Exception("Unable to get current location"),
                    "Location not available"
                )
            }
        } catch (e: Exception) {
            return Resource.Error(e, e.message ?: "Location error")
        }
    }
    
    /**
     * Get address details from coordinates using Geocoder
     */
    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): DetailedLocationData {
        return try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new async API for Android 13+
                getAddressesAsync(latitude, longitude)
            } else {
                // Use legacy sync API
                geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            }
            
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                DetailedLocationData(
                    latitude = latitude,
                    longitude = longitude,
                    cityName = address.locality ?: address.subAdminArea ?: "",
                    countryName = address.countryName ?: "",
                    areaName = address.subLocality ?: address.thoroughfare ?: address.featureName ?: "",
                    fullAddress = address.getAddressLine(0) ?: ""
                )
            } else {
                DetailedLocationData(latitude = latitude, longitude = longitude)
            }
        } catch (e: Exception) {
            DetailedLocationData(latitude = latitude, longitude = longitude)
        }
    }
    
    /**
     * Async address retrieval for Android 13+
     */
    private suspend fun getAddressesAsync(latitude: Double, longitude: Double): List<Address> = 
        suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                ) { addresses ->
                    continuation.resume(addresses)
                }
            } else {
                continuation.resume(emptyList())
            }
        }
    
    suspend fun getLastKnownLocation(): Resource<LocationData> {
        try {
            if (!hasLocationPermission()) {
                return Resource.Error(
                    SecurityException("Location permission not granted"),
                    "Location permission required"
                )
            }
            
            val location = getLastKnownLocationInternal()
            return if (location != null) {
                Resource.Success(
                    LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
            } else {
                Resource.Error(
                    Exception("No last known location available"),
                    "No cached location found"
                )
            }
        } catch (e: Exception) {
            return Resource.Error(e, e.message ?: "Location error")
        }
    }
    
    private suspend fun getCurrentLocationInternal(): android.location.Location? = 
        suspendCancellableCoroutine { continuation ->
            try {
                val cancellationTokenSource = CancellationTokenSource()
                
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
                
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    
    private suspend fun getLastKnownLocationInternal(): android.location.Location? = 
        suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
} 
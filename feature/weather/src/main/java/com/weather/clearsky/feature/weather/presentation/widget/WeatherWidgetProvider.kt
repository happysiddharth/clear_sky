package com.weather.clearsky.feature.weather.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.weather.clearsky.core.common.model.WeatherInfo
import com.weather.clearsky.core.common.result.Resource
import com.weather.clearsky.feature.weather.R
import com.weather.clearsky.feature.weather.domain.location.LocationManager
import com.weather.clearsky.feature.weather.domain.usecase.GetCurrentWeatherUseCase
import com.weather.clearsky.feature.weather.presentation.util.WeatherIconMapper
import com.weather.clearsky.core.network.mapper.toDisplayModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class WeatherWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherWidgetEntryPoint {
        fun getCurrentWeatherUseCase(): GetCurrentWeatherUseCase
        fun getLocationManager(): LocationManager
    }
    
    private fun getEntryPoint(context: Context): WeatherWidgetEntryPoint {
        return EntryPointAccessors.fromApplication(context, WeatherWidgetEntryPoint::class.java)
    }

    companion object {
        private const val TAG = "WeatherWidgetProvider"
        private const val ACTION_REFRESH = "com.weather.clearsky.widget.REFRESH"
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_REFRESH -> {
                Log.d(TAG, "Refresh action received")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, WeatherWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        Log.d(TAG, "Updating widget $appWidgetId")
        
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        
        // Set up click to open main app
        setupMainAppClick(context, views)
        
        // Set up refresh click on last updated text
        setupRefreshClick(context, views)
        
        // Update the widget immediately with loading state
        setLoadingState(views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        
        // Fetch weather data asynchronously with enhanced error handling
        scope.launch {
            try {
                Log.d(TAG, "Starting weather data fetch for widget $appWidgetId")
                val weatherInfo = fetchWeatherData(context)
                Log.d(TAG, "Weather data fetched successfully: ${weatherInfo.cityName}, ${weatherInfo.temperature}")
                updateWidgetWithWeatherData(context, appWidgetManager, appWidgetId, weatherInfo)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather data for widget $appWidgetId", e)
                // Show fallback data instead of just error
                updateWidgetWithFallback(context, appWidgetManager, appWidgetId, e.message ?: "Unknown error")
            }
        }
    }

    private fun setupMainAppClick(context: Context, views: RemoteViews) {
        // Create intent to open main activity
        val intent = Intent().apply {
            setClassName(context, "com.weather.clearsky.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set click on the main temperature area to open app
        views.setOnClickPendingIntent(R.id.tv_temperature, pendingIntent)
        views.setOnClickPendingIntent(R.id.tv_city_name, pendingIntent)
        views.setOnClickPendingIntent(R.id.tv_weather_icon, pendingIntent)
    }
    
    private fun setupRefreshClick(context: Context, views: RemoteViews) {
        val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.tv_last_updated, refreshPendingIntent)
    }

    private suspend fun fetchWeatherData(context: Context): WeatherInfo {
        Log.d(TAG, "Fetching weather data...")
        
        try {
            val entryPoint = getEntryPoint(context)
            val getCurrentWeatherUseCase = entryPoint.getCurrentWeatherUseCase()
            val locationManager = entryPoint.getLocationManager()
            
            Log.d(TAG, "Got entry point and dependencies")
            
            // Try to get current location first
            val locationResult = locationManager.getCurrentLocation()
            Log.d(TAG, "Location result: $locationResult")
            
            val weatherFlow = when (locationResult) {
                is Resource.Success -> {
                    Log.d(TAG, "Using current location: ${locationResult.data}")
                    getCurrentWeatherUseCase(locationResult.data)
                }
                is Resource.Error -> {
                    Log.w(TAG, "Current location failed: ${locationResult.message}")
                    // Fallback to last known location
                    val lastLocationResult = locationManager.getLastKnownLocation()
                    when (lastLocationResult) {
                        is Resource.Success -> {
                            Log.d(TAG, "Using last known location: ${lastLocationResult.data}")
                            getCurrentWeatherUseCase(lastLocationResult.data)
                        }
                        else -> {
                            Log.d(TAG, "Using fallback city: London")
                            // Fallback to default city
                            getCurrentWeatherUseCase("London")
                        }
                    }
                }
                is Resource.Loading -> {
                    Log.d(TAG, "Location loading, using fallback city: London")
                    getCurrentWeatherUseCase("London")
                }
            }
            
            // Get the first emission from the flow
            Log.d(TAG, "Collecting weather data from flow...")
            val weatherResource = weatherFlow.first()
            Log.d(TAG, "Weather resource result: $weatherResource")

            return when (weatherResource) {
                is Resource.Success -> {
                    Log.d(TAG, "Weather data successful: ${weatherResource.data}")
                    weatherResource.data.toDisplayModel()
                }
                is Resource.Error -> {
                    Log.e(TAG, "Weather data error: ${weatherResource.message}", weatherResource.exception)
                    throw weatherResource.exception
                }
                is Resource.Loading -> {
                    Log.w(TAG, "Weather data still loading")
                    throw Exception("Still loading weather data")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchWeatherData", e)
            throw e
        }
    }

    private fun setLoadingState(views: RemoteViews) {
        views.setTextViewText(R.id.tv_city_name, "Loading...")
        views.setTextViewText(R.id.tv_temperature, "--°C")
        views.setTextViewText(R.id.tv_description, "Fetching weather...")
        views.setTextViewText(R.id.tv_feels_like, "Feels like --°C")
        views.setTextViewText(R.id.tv_humidity, "Humidity --%")
        views.setTextViewText(R.id.tv_weather_icon, "🌤️")
        views.setTextViewText(R.id.tv_last_updated, "Updating...")
    }

    private fun updateWidgetWithWeatherData(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weatherInfo: WeatherInfo
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        
        // Update all text views with weather data
        views.setTextViewText(R.id.tv_city_name, weatherInfo.cityName)
        views.setTextViewText(R.id.tv_temperature, weatherInfo.temperature)
        views.setTextViewText(R.id.tv_description, weatherInfo.description)
        views.setTextViewText(R.id.tv_feels_like, "Feels like ${weatherInfo.feelsLike}")
        views.setTextViewText(R.id.tv_humidity, "Humidity ${weatherInfo.humidity}")
        
        // Set weather icon based on weather condition
        val iconText = WeatherIconMapper.getWeatherIcon(weatherInfo.icon)
        views.setTextViewText(R.id.tv_weather_icon, iconText)
        
        // Update last updated time
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.tv_last_updated, "Updated at $currentTime • Tap to refresh")
        
        // Set up click actions
        setupMainAppClick(context, views)
        setupRefreshClick(context, views)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Widget $appWidgetId updated with weather data for ${weatherInfo.cityName}")
    }

    private fun updateWidgetWithFallback(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        errorMessage: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        
        // Show fallback data that still looks decent
        views.setTextViewText(R.id.tv_city_name, "Clear Sky")
        views.setTextViewText(R.id.tv_temperature, "24°C")
        views.setTextViewText(R.id.tv_description, "Partly Cloudy")
        views.setTextViewText(R.id.tv_feels_like, "Feels like 26°C")
        views.setTextViewText(R.id.tv_humidity, "Humidity 65%")
        views.setTextViewText(R.id.tv_weather_icon, "⛅")
        views.setTextViewText(R.id.tv_last_updated, "Tap to refresh • Network error")
        
        // Set up click actions
        setupMainAppClick(context, views)
        setupRefreshClick(context, views)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Widget $appWidgetId updated with fallback data due to: $errorMessage")
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        scope.cancel()
    }
} 
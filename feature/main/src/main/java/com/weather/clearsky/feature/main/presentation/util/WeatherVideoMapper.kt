package com.weather.clearsky.feature.main.presentation.util

import androidx.annotation.RawRes
import com.weather.clearsky.feature.main.R

/**
 * Utility object to map weather conditions to background video resources
 */
object WeatherVideoMapper {
    
    /**
     * Map weather condition to appropriate background video resource
     * @param condition Weather condition string (e.g., "Rain", "Clear", "Clouds", etc.)
     * @return Raw resource ID for the video file
     */
    @RawRes
    fun getWeatherVideo(condition: String): Int {
        return when (condition.lowercase()) {
            // Rain conditions
            "rain", "drizzle", "shower rain", "light rain", "moderate rain", "heavy rain" -> 
                R.raw.weather_rain
            
            // Thunderstorm conditions  
            "thunderstorm", "storm", "lightning" -> 
                R.raw.weather_thunderstorm
            
            // Snow conditions
            "snow", "light snow", "heavy snow", "sleet", "blizzard" -> 
                R.raw.weather_snow
            
            // Clear/Sunny conditions
            "clear", "sunny", "clear sky" -> 
                R.raw.weather_sunny
            
            // Cloudy conditions
            "clouds", "cloudy", "overcast", "partly cloudy", "broken clouds", "scattered clouds" -> 
                R.raw.weather_cloudy
            
            // Foggy/Misty conditions
            "mist", "fog", "haze", "dust", "sand" -> 
                R.raw.weather_foggy
            
            // Night conditions (if time-based detection is added later)
            "clear night", "night" -> 
                R.raw.weather_night
            
            // Wind conditions
            "windy", "tornado", "hurricane" -> 
                R.raw.weather_windy
                
            // Default fallback
            else -> R.raw.weather_default
        }
    }
    
    /**
     * Map weather icon code to video resource (alternative method for icon-based mapping)
     * @param iconCode Weather icon code from API (e.g., "01d", "02n", etc.)
     * @return Raw resource ID for the video file
     */
    @RawRes
    fun getWeatherVideoByIcon(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.raw.weather_sunny        // clear sky day
            "01n" -> R.raw.weather_night        // clear sky night
            "02d", "02n" -> R.raw.weather_cloudy // few clouds
            "03d", "03n" -> R.raw.weather_cloudy // scattered clouds
            "04d", "04n" -> R.raw.weather_cloudy // broken clouds
            "09d", "09n" -> R.raw.weather_rain  // shower rain
            "10d", "10n" -> R.raw.weather_rain  // rain
            "11d", "11n" -> R.raw.weather_thunderstorm // thunderstorm
            "13d", "13n" -> R.raw.weather_snow  // snow
            "50d", "50n" -> R.raw.weather_foggy // mist
            else -> R.raw.weather_default
        }
    }
    
    /**
     * Get all available weather video resources
     * @return List of all video resource IDs
     */
    @RawRes
    fun getAllWeatherVideos(): List<Int> {
        return listOf(
            R.raw.weather_rain,
            R.raw.weather_thunderstorm,
            R.raw.weather_snow,
            R.raw.weather_sunny,
            R.raw.weather_cloudy,
            R.raw.weather_foggy,
            R.raw.weather_night,
            R.raw.weather_windy,
            R.raw.weather_default
        )
    }
} 
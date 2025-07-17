package com.weather.clearsky.feature.main.data.repository

import android.content.Context
import androidx.core.content.edit
import com.weather.clearsky.feature.main.presentation.model.WeatherCard
import com.weather.clearsky.feature.main.presentation.model.CardType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing weather card order and visibility preferences per location
 */
@Singleton
class CardOrderRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "weather_cards_prefs"
        private const val KEY_CARD_ORDER = "card_order"
        private const val KEY_CARD_VISIBILITY = "card_visibility"
        private const val SEPARATOR = ","
        private const val DEFAULT_LOCATION_ID = "default"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save the order of cards for a specific location
     */
    fun saveCardOrder(cards: List<WeatherCard>, locationId: String = DEFAULT_LOCATION_ID) {
        val orderString = cards.joinToString(SEPARATOR) { it.cardType.name }
        prefs.edit {
            putString("${KEY_CARD_ORDER}_$locationId", orderString)
        }
    }

    /**
     * Get the saved card order for a specific location
     */
    fun getSavedCardOrder(locationId: String = DEFAULT_LOCATION_ID): List<CardType> {
        val orderString = prefs.getString("${KEY_CARD_ORDER}_$locationId", null)
        return if (orderString != null) {
            orderString.split(SEPARATOR)
                .mapNotNull { typeName ->
                    try {
                        CardType.valueOf(typeName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
        } else {
            // Default order
            getDefaultCardOrder()
        }
    }

    /**
     * Save card visibility preferences for a specific location
     */
    fun saveCardVisibility(cardType: CardType, isVisible: Boolean, locationId: String = DEFAULT_LOCATION_ID) {
        prefs.edit {
            putBoolean("${KEY_CARD_VISIBILITY}_${cardType.name}_$locationId", isVisible)
        }
    }

    /**
     * Get card visibility preference for a specific location
     */
    fun isCardVisible(cardType: CardType, locationId: String = DEFAULT_LOCATION_ID): Boolean {
        return prefs.getBoolean("${KEY_CARD_VISIBILITY}_${cardType.name}_$locationId", true)
    }

    /**
     * Get all visible card types in saved order for a specific location
     */
    fun getVisibleCardOrder(locationId: String = DEFAULT_LOCATION_ID): List<CardType> {
        return getSavedCardOrder(locationId).filter { isCardVisible(it, locationId) }
    }

    /**
     * Copy card order from one location to another
     */
    fun copyCardOrderToLocation(fromLocationId: String, toLocationId: String) {
        val fromOrder = getSavedCardOrder(fromLocationId)
        val cards = fromOrder.mapIndexed { index, cardType ->
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
        saveCardOrder(cards, toLocationId)
        
        // Copy visibility settings too
        fromOrder.forEach { cardType ->
            val isVisible = isCardVisible(cardType, fromLocationId)
            saveCardVisibility(cardType, isVisible, toLocationId)
        }
    }

    /**
     * Reset to default settings for a specific location
     */
    fun resetToDefaults(locationId: String = DEFAULT_LOCATION_ID) {
        val allKeys = prefs.all.keys.filter { 
            it.endsWith("_$locationId") 
        }
        prefs.edit {
            allKeys.forEach { key ->
                remove(key)
            }
        }
    }

    /**
     * Reset all settings for all locations
     */
    fun resetAllToDefaults() {
        prefs.edit {
            clear()
        }
    }

    /**
     * Get default card order
     */
    private fun getDefaultCardOrder(): List<CardType> {
        return listOf(
            CardType.TEMPERATURE,
            CardType.HOURLY_WEATHER,
            CardType.AIR_QUALITY,
            CardType.HUMIDITY,
            CardType.WIND,
            CardType.UV_INDEX,
            CardType.FORECAST
        )
    }

    /**
     * Check if location has custom card order
     */
    fun hasCustomOrder(locationId: String): Boolean {
        return prefs.contains("${KEY_CARD_ORDER}_$locationId")
    }
} 
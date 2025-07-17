package com.weather.clearsky.feature.alerts.domain.entity

sealed class AlertCondition {
    data class TemperatureCondition(
        val operator: ComparisonOperator,
        val value: Double,
        val unit: TemperatureUnit = TemperatureUnit.CELSIUS
    ) : AlertCondition()
    
    data class RainCondition(
        val operator: ComparisonOperator,
        val value: Double // mm per hour
    ) : AlertCondition()
    
    data class SnowCondition(
        val operator: ComparisonOperator,
        val value: Double // mm per hour
    ) : AlertCondition()
    
    data class WindCondition(
        val operator: ComparisonOperator,
        val value: Double // km/h
    ) : AlertCondition()
    
    data class HumidityCondition(
        val operator: ComparisonOperator,
        val value: Int // percentage
    ) : AlertCondition()
    
    data class WeatherCondition(
        val weatherType: WeatherType
    ) : AlertCondition()
    
    data class UvIndexCondition(
        val operator: ComparisonOperator,
        val value: Int
    ) : AlertCondition()
    
    data class PressureCondition(
        val operator: ComparisonOperator,
        val value: Double // hPa
    ) : AlertCondition()
    
    data class VisibilityCondition(
        val operator: ComparisonOperator,
        val value: Double // km
    ) : AlertCondition()
}

enum class ComparisonOperator(val symbol: String, val displayName: String) {
    GREATER_THAN(">", "Greater than"),
    LESS_THAN("<", "Less than"),
    GREATER_THAN_OR_EQUAL(">=", "Greater than or equal"),
    LESS_THAN_OR_EQUAL("<=", "Less than or equal"),
    EQUAL("=", "Equal to")
}

enum class TemperatureUnit(val symbol: String, val displayName: String) {
    CELSIUS("°C", "Celsius"),
    FAHRENHEIT("°F", "Fahrenheit"),
    KELVIN("K", "Kelvin")
}

enum class WeatherType(val displayName: String, val description: String) {
    CLEAR("Clear", "Clear sky"),
    CLOUDS("Cloudy", "Cloudy weather"),
    RAIN("Rainy", "Rain is expected"),
    SNOW("Snowy", "Snow is expected"),
    THUNDERSTORM("Thunderstorm", "Thunderstorm is expected"),
    DRIZZLE("Drizzle", "Light rain/drizzle"),
    MIST("Misty", "Misty/foggy weather"),
    EXTREME("Extreme", "Extreme weather conditions")
} 
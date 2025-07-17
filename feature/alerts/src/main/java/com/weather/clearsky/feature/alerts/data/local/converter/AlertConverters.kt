package com.weather.clearsky.feature.alerts.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.weather.clearsky.feature.alerts.domain.entity.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlertConverters {
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(AlertCondition::class.java, AlertConditionAdapter())
        .create()
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
    
    // AlertType converter
    @TypeConverter
    fun fromAlertType(alertType: AlertType): String {
        return alertType.name
    }
    
    @TypeConverter
    fun toAlertType(alertTypeString: String): AlertType {
        return AlertType.valueOf(alertTypeString)
    }
    
    // AlertStatus converter
    @TypeConverter
    fun fromAlertStatus(status: AlertStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toAlertStatus(statusString: String): AlertStatus {
        return AlertStatus.valueOf(statusString)
    }
    
    // RepeatInterval converter
    @TypeConverter
    fun fromRepeatInterval(interval: RepeatInterval?): String? {
        return interval?.name
    }
    
    @TypeConverter
    fun toRepeatInterval(intervalString: String?): RepeatInterval? {
        return intervalString?.let { RepeatInterval.valueOf(it) }
    }
    
    // AlertCondition converter (using custom JSON)
    @TypeConverter
    fun fromAlertCondition(condition: AlertCondition): String {
        return gson.toJson(condition)
    }
    
    @TypeConverter
    fun toAlertCondition(conditionJson: String): AlertCondition {
        return try {
            gson.fromJson(conditionJson, AlertCondition::class.java)
        } catch (e: Exception) {
            // Fallback for corrupted data - return a default temperature condition
            AlertCondition.TemperatureCondition(ComparisonOperator.GREATER_THAN, 25.0, TemperatureUnit.CELSIUS)
        }
    }
    
    // AlertLocation converter (using JSON)
    @TypeConverter
    fun fromAlertLocation(location: AlertLocation): String {
        return gson.toJson(location)
    }
    
    @TypeConverter
    fun toAlertLocation(locationJson: String): AlertLocation {
        val type = object : TypeToken<AlertLocation>() {}.type
        return gson.fromJson(locationJson, type)
    }
}

// Custom type adapter for AlertCondition sealed class
class AlertConditionAdapter : JsonSerializer<AlertCondition>, JsonDeserializer<AlertCondition> {
    
    override fun serialize(src: AlertCondition, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        
        when (src) {
            is AlertCondition.TemperatureCondition -> {
                jsonObject.addProperty("type", "temperature")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
                jsonObject.addProperty("unit", src.unit.name)
            }
            is AlertCondition.RainCondition -> {
                jsonObject.addProperty("type", "rain")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
            is AlertCondition.SnowCondition -> {
                jsonObject.addProperty("type", "snow")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
            is AlertCondition.WindCondition -> {
                jsonObject.addProperty("type", "wind")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
            is AlertCondition.HumidityCondition -> {
                jsonObject.addProperty("type", "humidity")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
            is AlertCondition.WeatherCondition -> {
                jsonObject.addProperty("type", "weather")
                jsonObject.addProperty("weatherType", src.weatherType.name)
            }
            is AlertCondition.UvIndexCondition -> {
                jsonObject.addProperty("type", "uv")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
            is AlertCondition.PressureCondition -> {
                jsonObject.addProperty("type", "pressure")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
            is AlertCondition.VisibilityCondition -> {
                jsonObject.addProperty("type", "visibility")
                jsonObject.addProperty("operator", src.operator.name)
                jsonObject.addProperty("value", src.value)
            }
        }
        
        return jsonObject
    }
    
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AlertCondition {
        val jsonObject = json.asJsonObject
        
        // Handle new format with "type" field
        val typeElement = jsonObject.get("type")
        if (typeElement != null && !typeElement.isJsonNull) {
            val type = typeElement.asString
            return when (type) {
            "temperature" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asDouble
                val unit = if (jsonObject.has("unit")) {
                    TemperatureUnit.valueOf(jsonObject.get("unit").asString)
                } else {
                    TemperatureUnit.CELSIUS
                }
                AlertCondition.TemperatureCondition(operator, value, unit)
            }
            "rain" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asDouble
                AlertCondition.RainCondition(operator, value)
            }
            "snow" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asDouble
                AlertCondition.SnowCondition(operator, value)
            }
            "wind" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asDouble
                AlertCondition.WindCondition(operator, value)
            }
            "humidity" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asInt
                AlertCondition.HumidityCondition(operator, value)
            }
            "weather" -> {
                val weatherType = WeatherType.valueOf(jsonObject.get("weatherType").asString)
                AlertCondition.WeatherCondition(weatherType)
            }
            "uv" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asInt
                AlertCondition.UvIndexCondition(operator, value)
            }
            "pressure" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asDouble
                AlertCondition.PressureCondition(operator, value)
            }
            "visibility" -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value").asDouble
                AlertCondition.VisibilityCondition(operator, value)
            }
                else -> throw IllegalArgumentException("Unknown AlertCondition type: $type")
            }
        }
        
        // Handle legacy format (fallback for old data)
        // Try to infer the type from the available fields
        return when {
            jsonObject.has("weatherType") -> {
                val weatherType = WeatherType.valueOf(jsonObject.get("weatherType").asString)
                AlertCondition.WeatherCondition(weatherType)
            }
            jsonObject.has("operator") && jsonObject.has("value") -> {
                val operator = ComparisonOperator.valueOf(jsonObject.get("operator").asString)
                val value = jsonObject.get("value")
                
                // Default to temperature condition for legacy data
                if (value.isJsonPrimitive && value.asJsonPrimitive.isNumber) {
                    if (value.asDouble == value.asDouble.toInt().toDouble()) {
                        // Integer value - could be humidity or UV index, default to humidity
                        AlertCondition.HumidityCondition(operator, value.asInt)
                    } else {
                        // Double value - default to temperature
                        AlertCondition.TemperatureCondition(operator, value.asDouble, TemperatureUnit.CELSIUS)
                    }
                } else {
                    // Fallback to temperature
                    AlertCondition.TemperatureCondition(operator, 25.0, TemperatureUnit.CELSIUS)
                }
            }
            else -> {
                // Ultimate fallback - create a default temperature condition
                AlertCondition.TemperatureCondition(ComparisonOperator.GREATER_THAN, 25.0, TemperatureUnit.CELSIUS)
            }
        }
    }
} 
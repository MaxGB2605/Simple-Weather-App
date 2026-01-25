package com.example.simpleweatherappv2.data

/**
 * Unified forecast period model used throughout the app
 * Can be populated from WeatherAPI data
 */
data class ForecastPeriod(
    val name: String,
    val startTime: String, // ISO 8601 or local string
    val icon: String? = null,
    val isDaytime: Boolean = true,
    val temperature: Double,
    val temperatureUnit: String,
    val shortForecast: String,
    val detailedForecast: String,
    val windSpeed: String,
    val windDirection: String,
    val relativeHumidity: ForecastUnitValue?,
    val probabilityOfPrecipitation: ForecastUnitValue?,
    // Additional fields for enhanced display
    val feelsLike: Double? = null,
    val clouds: Int? = null,
    val uvIndex: Double? = null,
    val windGust: String? = null,
    val maxTemp: Double? = null,
    val minTemp: Double? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val airQualityIndex: Int? = null, // EPA Index 1-6
    val snowDepth: Double? = null, // in cm
    val snowChance: Int? = null,
    val visibility: String? = null,
    val pressure: String? = null,
    val dewPoint: String? = null,
    val windChill: String? = null,
    val heatIndex: String? = null,
    val precipitation: String? = null
)

/**
 * Generic unit value wrapper
 * Used for humidity, precipitation, etc.
 */
data class ForecastUnitValue(
    val value: Double?,
)

package com.example.simpleweatherappv2.data

data class PointsResponse(
    val properties: PointsProperties,
)

data class PointsProperties(
    val gridId: String,
    val gridX: Int,
    val gridY: Int,
    val forecast: String,
)

data class ForecastResponse(
    val properties: ForecastProperties,
)

data class ForecastProperties(
    val periods: List<ForecastPeriod>,
)

data class ForecastPeriod(
    val name: String,
    val startTime: String, // ISO 8601: "2023-10-25T14:00:00-04:00"
    val isDaytime: Boolean = true, // NEW: Distinguishes day vs night periods
    val temperature: Double,
    val temperatureUnit: String,
    val shortForecast: String,
    val detailedForecast: String,
    val windSpeed: String,
    val windDirection: String,
    val relativeHumidity: ForecastUnitValue?,
    val probabilityOfPrecipitation: ForecastUnitValue?,
)

data class ForecastUnitValue(
    val value: Double?,
)

// --- STATION & OBSERVATION MODELS ---

data class StationsResponse(
    val features: List<StationFeature>
)

data class StationFeature(
    val properties: StationProperties
)

data class StationProperties(
    val stationIdentifier: String, // e.g., "KJFK"
    val name: String
)

data class ObservationResponse(
    val properties: ObservationProperties
)

data class ObservationProperties(
    val textDescription: String?,
    val temperature: ForecastUnitValue?,
    val relativeHumidity: ForecastUnitValue?,
    val windSpeed: ForecastUnitValue?,
    val windDirection: ForecastUnitValue?,
    val heatIndex: ForecastUnitValue?,        // ADD THIS
    val windChill: ForecastUnitValue?,        // ADD THIS
    val barometricPressure: ForecastUnitValue?, // ADD THIS
    val probabilityOfPrecipitation: ForecastUnitValue? = null
)
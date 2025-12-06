package com.example.simpleweatherappv2.data

import com.google.gson.annotations.SerializedName

data class PointsResponse(
    val properties: PointsProperties
)

data class PointsProperties(
    val gridId: String,
    val gridX: Int,
    val gridY: Int,
    val forecast: String
)

data class ForecastResponse(
    val properties: ForecastProperties
)

data class ForecastProperties(
    val periods: List<ForecastPeriod>
)

data class ForecastPeriod(
    val name: String,
    val temperature: Int,
    val temperatureUnit: String,
    val shortForecast: String,
    val detailedForecast: String
)
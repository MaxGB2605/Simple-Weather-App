package com.example.simpleweatherappv2.ui

import com.example.simpleweatherappv2.data.ForecastPeriod

data class WeatherUiState(
    val cityName: String = "Unknown",
    val temperature: String = "--°F",
    val condition: String = "Loading...",
    val humidity: String = "--%",
    val wind: String = "-- mph",
    val rainChance: String = "--%",
    val dailyForecasts: List<ForecastPeriod> = emptyList(),

    val isLoading: Boolean = true,
)
package com.example.simpleweatherappv2.ui

data class WeatherUiState(
    val cityName: String = "Unknown",
    val temperature: String = "--°F",
    val condition: String = "Loading...",
    val isLoading: Boolean = true,
)
package com.example.simpleweatherappv2.ui

data class WeatherUiState(
    val cityName: String = "Unknown",
    val temperature: String = "--°F",
    val condition: String = "Loading...",
    val humidity: String = "--%",
    val wind: String = "-- mph",
    val rainChance: String = "--%",

    val isLoading: Boolean = true,
)
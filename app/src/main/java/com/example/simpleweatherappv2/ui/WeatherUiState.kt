package com.example.simpleweatherappv2.ui

import com.example.simpleweatherappv2.data.ForecastPeriod

data class WeatherUiState(
    val cityName: String = "Unknown",
    val temperature: String = "--°F",
    val condition: String = "Loading...",
    val humidity: String = "--%",
    val wind: String = "-- mph",
    val rainChance: String = "--%",
    val feelsLike: String = "--°F",
    val pressure: String = "-- mb",
    
    // NEW: High/Low Temperature
    val highTemp: String = "--°F",
    val lowTemp: String = "--°F",
    
    // NEW: Current Date
    val currentDate: String = "",
    
    // NEW: Sun/Moon Data
    val sunrise: String = "--:-- AM",
    val sunset: String = "--:-- PM",
    val uvIndex: String = "--",
    val moonPhase: String = "Unknown",
    
    // NEW: Air Quality Data
    val aqi: String = "--",
    val aqiStatus: String = "Unknown",
    val pm25: String = "--",
    val pm10: String = "--",
    val ozone: String = "--",
    
    val dailyForecasts: List<ForecastPeriod> = emptyList(),
    val hourlyForecasts: List<ForecastPeriod> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isUsingGps: Boolean = false,
)
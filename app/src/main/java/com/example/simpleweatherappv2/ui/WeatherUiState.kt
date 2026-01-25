package com.example.simpleweatherappv2.ui

import com.example.simpleweatherappv2.data.ForecastPeriod
import com.example.simpleweatherappv2.data.SearchSuggestion

data class WeatherUiState(
    val cityName: String = "Unknown",
    val temperature: String = "--°F",
    val condition: String = "Loading...",
    val humidity: String = "--%",
    val wind: String = "-- mph",
    val rainChance: String = "--%",
    val feelsLike: String = "--°F",
    val pressure: String = "-- mb",
    val visibility: String = "--",
    val cloudCover: String = "--",
    val precipitation: String = "--",
    val dewPoint: String = "--°F",
    val snowChance: String = "--%",
    val windChill: String = "--°F",
    val heatIndex: String = "--°F",
    
    // NEW: High/Low Temperature
    val highTemp: String = "--°F",
    val lowTemp: String = "--°F",
    
    // NEW: Current Date
    val currentDate: String = "",
    
    // NEW: Sun/Moon Data
    val sunrise: String = "--:-- AM",
    val sunset: String = "--:-- PM",
    val daylightDuration: String = "--",
    val moonrise: String = "--:--",
    val moonset: String = "--:--",
    val moonIllumination: String = "0",
    val uvIndex: String = "--",
    val moonPhase: String = "Unknown",
    val moonPhaseImageUrl: String? = null,
    val starChartImageUrl: String? = null,
    val constellationName: String = "Sky Directly Overhead", 
    
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
    val isDaytime: Boolean = true,
    
    // Search Autocomplete
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    
    // Settings
    val isDarkTheme: Boolean = true,
    val tempUnit: String = "°F", // "°F" or "°C"
    val speedUnit: String = "mph", // "mph" or "km/h"
    val dataSource: String = "WeatherAPI", // "WeatherAPI" or "NWS"
    
    // Favorites
    val favorites: List<String> = emptyList(),
    val alerts: List<com.example.simpleweatherappv2.data.WeatherAlert> = emptyList()
)
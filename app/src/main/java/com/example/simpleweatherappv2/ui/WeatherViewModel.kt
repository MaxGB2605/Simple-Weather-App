package com.example.simpleweatherappv2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleweatherappv2.data.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// CHANGE: Inherit from AndroidViewModel to get 'application' context
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    // Pass the application context to the repository
    private val repository = WeatherRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchCurrentLocation() // Loads NYC on startup!
    }

    // Search by City Name
    fun updateWeather(locationSearch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                cityName = locationSearch,
                condition = "Loading...",
                isLoading = true,
                error = null
            )

            // 1. Get Coordinates from the city name
            val coords = repository.getCoordinates(locationSearch)

            if (coords != null) {
                // 2. Fetch and Display Weather using Helper
                val finalCityName = repository.getCityName(coords.first, coords.second)
                fetchAndDisplayWeather(finalCityName, coords.first, coords.second)
            } else {
                showError("City not found")
            }
        }
    }

    // Search by GPS
    fun fetchCurrentLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                cityName = "Locating...",
                condition = "Loading...",
                isLoading = true,
                error = null
            )

            // 1. Get GPS Location
            val coords = repository.getCurrentLocation()

            if (coords != null) {
                // 2. Fetch and Display Weather using Helper
                val finalCityName = repository.getCityName(coords.first, coords.second)
                fetchAndDisplayWeather(finalCityName, coords.first, coords.second)
            } else {
                showError("Location denied or not found")
            }
        }
    }

    // --- SHARED HELPER FUNCTION ---
    // This implements the "Hybrid Fallback" logic
    private suspend fun fetchAndDisplayWeather(city: String, lat: Double, lon: Double) {

        // Plan A: Get Real-Time Observation
        val observation = repository.getRealTimeWeather(lat, lon)

        // Plan B: Get Hourly Forecast (Backup)
        val forecast = repository.getWeather(lat, lon)

        // NEW: Get 7-Day Forecast (For the new screen)
        val dailyList = repository.getDailyForecasts(lat, lon) ?: emptyList()

        // DECISION LOGIC
        if (observation != null && observation.temperature?.value != null) {
            // ... (Same Observation logic as before) ...
            val tempC = observation.temperature.value
            val tempF = (tempC * 9 / 5) + 32
            val windKmh = observation.windSpeed?.value ?: 0.0
            val windMph = windKmh * 0.621371

            _uiState.value = WeatherUiState(
                cityName = city,
                temperature = "${tempF.toInt()}°F",
                condition = observation.textDescription ?: "Unknown",
                humidity = "${observation.relativeHumidity?.value?.toInt() ?: 0}%",
                wind = "${windMph.toInt()} mph",
                rainChance = "--",
                dailyForecasts = dailyList, // <--- SAVE THE LIST!
                isLoading = false
            )
        } else if (forecast != null) {
            // ... (Same Forecast logic as before) ...
            _uiState.value = WeatherUiState(
                cityName = city,
                temperature = "${forecast.temperature}°${forecast.temperatureUnit}",
                condition = forecast.shortForecast,
                humidity = "${forecast.relativeHumidity?.value ?: 0}%",
                wind = "${forecast.windSpeed ?: "--"} ${forecast.windDirection ?: ""}",
                rainChance = "${forecast.probabilityOfPrecipitation?.value ?: 0}%",
                dailyForecasts = dailyList, // <--- SAVE THE LIST!
                isLoading = false
            )
        } else {
            showError("Weather data unavailable")
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            error = message,
            isLoading = false
        )
    }

    // Simple refresh function
    fun refreshWeather() {
        val currentCity = _uiState.value.cityName
        if (currentCity != "Unknown" && currentCity != "Locating...") {
            updateWeather(currentCity)
        } else {
            fetchCurrentLocation()
        }
    }
}
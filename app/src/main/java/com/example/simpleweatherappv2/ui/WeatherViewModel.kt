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
        updateWeather("New York") // Loads NYC on startup!
    }
    fun updateWeather(locationSearch: String) {
        viewModelScope.launch(Dispatchers.IO) { // Run on IO thread (background)
            // Set loading state
            _uiState.value = _uiState.value.copy(
                cityName = locationSearch,
                condition = "Loading...",
                isLoading = true
            )

            // 1. Get Coordinates from the city name
            val coords = repository.getCoordinates(locationSearch)

            if (coords != null) {
                // 2. Get Weather using those coordinates
                val weather = repository.getWeather(coords.first, coords.second)

                if (weather != null) {
                    _uiState.value = WeatherUiState(
                        cityName = locationSearch.uppercase(), // Make it look nice
                        temperature = "${weather.temperature}°${weather.temperatureUnit}",
                        condition = weather.shortForecast,
                        humidity = "${weather.relativeHumidity?.value ?: 0}%",
                        wind = "${weather.windSpeed ?: "--"} ${weather.windDirection ?: ""}",
                        rainChance = "${weather.probabilityOfPrecipitation?.value ?: 0}%",
                        isLoading = false
                    )
                } else {
                    showError("Weather not found")
                }
            } else {
                showError("City not found")
            }
        }
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            condition = message,
            isLoading = false
        )
    }
}
package com.example.simpleweatherappv2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleweatherappv2.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    fun updateWeather(city: String) {
        viewModelScope.launch {
            // Set loading state
            _uiState.value = _uiState.value.copy(
                cityName = city,
                condition = "Loading...",
                isLoading = true
            )

            // HARDCODED Coordinates for New York City (40.7128, -74.0060)
            // Later we can make this dynamic
            val weather = repository.getWeather(40.7128, -74.0060)

            if (weather != null) {
                _uiState.value = WeatherUiState(
                    cityName = city,
                    temperature = "${weather.temperature}°${weather.temperatureUnit}",
                    condition = weather.shortForecast,
                    humidity = "${weather.relativeHumidity?.value ?: 0}%",
                    wind = "${weather.windSpeed} ${weather.windDirection}",
                    rainChance = "${weather.probabilityOfPrecipitation?.value ?: 0}%",
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    condition = "Error loading",
                    isLoading = false
                )
            }
        }
    }
}
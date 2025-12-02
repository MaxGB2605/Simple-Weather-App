package com.example.simpleweatherappv2.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())

    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun updateWeather(city: String, temp: String, condition: String) {
        _uiState.value = WeatherUiState(
            cityName = city,
            temperature = temp,
            condition = condition,
            isLoading = false
        )
    }
}
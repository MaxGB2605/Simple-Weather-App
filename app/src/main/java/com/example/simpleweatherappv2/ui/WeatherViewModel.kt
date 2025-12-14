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

        // NEW: Get Hourly Forecast (For the horizontal list)
        val fullHourlyList = repository.getHourlyForecasts(lat, lon) ?: emptyList()

        // Filter: Start from the NEXT hour
        val now = java.time.ZonedDateTime.now()
        val hourlyList = fullHourlyList
            .filter { period ->
                try {
                    val periodTime = java.time.ZonedDateTime.parse(period.startTime)
                    periodTime.isAfter(now)
                } catch (e: Exception) {
                    false
                }
            }
            .take(24) // Take next 24 hours

        android.util.Log.d("WeatherApp", "Hourly List Size: ${hourlyList.size}")

        // --- EXTRACT HIGH/LOW FROM DAILY FORECAST ---
        val todayHigh = dailyList
            .filter { it.isDaytime }
            .maxByOrNull { it.temperature }
            ?.temperature?.let { "${it}°F" } ?: "--°F"
        
        val todayLow = dailyList
            .filter { !it.isDaytime }
            .minByOrNull { it.temperature }
            ?.temperature?.let { "${it}°F" } ?: "--°F"

        // --- FORMAT CURRENT DATE ---
        val currentDate = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))

        // --- PLACEHOLDER VALUES FOR SUN/MOON/AQI ---
        // TODO: Integrate real APIs for these values later
        val sunriseTime = "6:45 AM"
        val sunsetTime = "5:30 PM"
        val uvIndexValue = "3"
        val moonPhaseValue = "Waxing Gibbous"
        
        val aqiValue = "42"
        val aqiStatusValue = "Good"
        val pm25Value = "8"
        val pm10Value = "15"
        val ozoneValue = "32"

        // DECISION LOGIC
        if (observation != null && observation.temperature?.value != null) {
            // ... (Same Observation logic as before) ...
            val tempC = observation.temperature.value
            val tempF = (tempC * 9 / 5) + 32
            val windKmh = observation.windSpeed?.value ?: 0.0
            val windMph = windKmh * 0.621371
// Calculate "Feels Like" (use heat index or wind chill)
            val feelsLikeC = observation.heatIndex?.value
                ?: observation.windChill?.value
                ?: tempC
            val feelsLikeF = (feelsLikeC * 9 / 5) + 32
// Get Pressure (convert from Pascals to millibars)
            val pressurePa = observation.barometricPressure?.value ?: 0.0
            val pressureMb = pressurePa / 100 // 1 mb = 100 Pa

            _uiState.value = WeatherUiState(
                cityName = city,
                temperature = "${tempF.toInt()}°F",
                condition = observation.textDescription ?: "Unknown",
                humidity = "${observation.relativeHumidity?.value?.toInt() ?: 0}%",
                wind = "${windMph.toInt()} mph",
                rainChance = "${forecast?.probabilityOfPrecipitation?.value?.toInt() ?: 0}%",
                feelsLike = "${feelsLikeF.toInt()}°F",
                pressure = "${pressureMb.toInt()} mb",
                highTemp = todayHigh,
                lowTemp = todayLow,
                currentDate = currentDate,
                sunrise = sunriseTime,
                sunset = sunsetTime,
                uvIndex = uvIndexValue,
                moonPhase = moonPhaseValue,
                aqi = aqiValue,
                aqiStatus = aqiStatusValue,
                pm25 = pm25Value,
                pm10 = pm10Value,
                ozone = ozoneValue,
                dailyForecasts = dailyList,
                hourlyForecasts = hourlyList,
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
                feelsLike = "--°F",
                pressure = "-- mb",
                highTemp = todayHigh,
                lowTemp = todayLow,
                currentDate = currentDate,
                sunrise = sunriseTime,
                sunset = sunsetTime,
                uvIndex = uvIndexValue,
                moonPhase = moonPhaseValue,
                aqi = aqiValue,
                aqiStatus = aqiStatusValue,
                pm25 = pm25Value,
                pm10 = pm10Value,
                ozone = ozoneValue,
                dailyForecasts = dailyList,
                hourlyForecasts = hourlyList,
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
package com.example.simpleweatherappv2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleweatherappv2.data.FavoriteLocation
import com.example.simpleweatherappv2.data.ForecastPeriod
import com.example.simpleweatherappv2.data.ForecastUnitValue
import com.example.simpleweatherappv2.data.WeatherApiResponse
import com.example.simpleweatherappv2.data.WeatherDatabase
import com.example.simpleweatherappv2.data.WeatherRepository
import com.example.simpleweatherappv2.utils.SunCalc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    private val favoriteDao = WeatherDatabase.getDatabase(application).favoriteDao()

    init {
        // Observe Favorites from Database
        viewModelScope.launch {
            favoriteDao.getAllFavorites().collectLatest { locations ->
                _uiState.value = _uiState.value.copy(
                    favorites = locations.map { it.cityName }
                )
            }
        }
        fetchCurrentLocation()
    }

    // --- SETTINGS STATE ---
    private var isDarkTheme = true
    private var tempUnit = "°F" // "°F" or "°C"
    private var speedUnit = "mph" // "mph" or "km/h"

    // --- DATA CACHE ---
    private var lastWeatherData: WeatherApiResponse? = null
    private var lastMoonPhaseImage: String? = null
    private var lastStarChartImage: String? = null

    // ==================== SETTINGS METHODS ====================

    fun toggleTheme(isDark: Boolean) {
        if (isDarkTheme != isDark) {
            isDarkTheme = isDark
            refreshUiState()
            reapplySettings()
        }
    }

    fun setTempUnit(unit: String) {
        if (tempUnit != unit) {
            tempUnit = unit
            refreshUiState()
            reapplySettings()
        }
    }

    fun setSpeedUnit(unit: String) {
        if (speedUnit != unit) {
            speedUnit = unit
            refreshUiState()
            reapplySettings()
        }
    }

    // ==================== FAVORITES METHODS ====================

    fun addFavorite(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteDao.insert(FavoriteLocation(location))
        }
    }

    fun removeFavorite(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteDao.delete(FavoriteLocation(location))
        }
    }

    fun toggleFavorite(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value.favorites.contains(location)) {
                favoriteDao.delete(FavoriteLocation(location))
            } else {
                favoriteDao.insert(FavoriteLocation(location))
            }
        }
    }

    // ==================== WEATHER FETCHING METHODS ====================

    /**
     * Search weather by city name
     */
    fun updateWeather(locationSearch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                cityName = locationSearch,
                condition = "Loading...",
                isLoading = true,
                error = null,
                isDarkTheme = isDarkTheme,
                tempUnit = tempUnit,
                speedUnit = speedUnit
            )

            // Try to get weather data directly by city name
            val weatherData = repository.getWeatherData(locationSearch)

            if (weatherData != null) {
                // Update UI with weather data IMMEDIATELY so user doesn't wait
                updateUiStateFromWeatherApi(weatherData, null, null)
                lastWeatherData = weatherData

                // Then fetch astronomy data in parallel without blocking the main weather display
                val moonImageDeferred = async { repository.getMoonPhaseImage(weatherData.location.lat, weatherData.location.lon) }
                val starChartDeferred = async { repository.getStarChartImage(weatherData.location.lat, weatherData.location.lon) }
                
                val moonImage = moonImageDeferred.await()
                val starChart = starChartDeferred.await()

                lastMoonPhaseImage = moonImage
                lastStarChartImage = starChart

                // Update UI again with astronomy images
                updateUiStateFromWeatherApi(weatherData, moonImage, starChart)
            } else {
                // Fallback: Try geocoding the city name to coordinates
                val coords = repository.getCoordinates(locationSearch)
                if (coords != null) {
                    val cityName = repository.getCityName(coords.first, coords.second)
                    fetchAndDisplayWeather(cityName, coords.first, coords.second)
                } else {
                    showError("City not found")
                }
            }
        }
    }

    /**
     * Fetch weather using current GPS location
     */
    fun fetchCurrentLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                cityName = "Locating...",
                condition = "Loading...",
                isLoading = true,
                error = null,
                isDarkTheme = isDarkTheme,
                tempUnit = tempUnit,
                speedUnit = speedUnit
            )

            val coords = repository.getCurrentLocation()

            if (coords != null) {
                val cityName = repository.getCityName(coords.first, coords.second)
                fetchAndDisplayWeather(cityName, coords.first, coords.second)
            } else {
                showError("Location denied or not found")
            }
        }
    }

    /**
     * Refresh current weather data
     */
    fun refreshWeather() {
        val currentCity = _uiState.value.cityName
        if (currentCity != "Unknown" && currentCity != "Locating...") {
            updateWeather(currentCity)
        } else {
            fetchCurrentLocation()
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Fetch weather data and update UI state
     */
    private suspend fun fetchAndDisplayWeather(
        city: String,
        lat: Double,
        lon: Double
    ) = coroutineScope {
        // 1. Fetch main weather data FIRST
        val weatherData = repository.getWeatherData(lat, lon)
        
        if (weatherData != null) {
            // Update UI immediately with weather data
            lastWeatherData = weatherData
            updateUiStateFromWeatherApi(weatherData, null, null)
            
            // 2. Then fetch astronomy data in parallel
            val moonImageDeferred = async { repository.getMoonPhaseImage(lat, lon) }
            val starChartDeferred = async { repository.getStarChartImage(lat, lon) }

            val moonImage = moonImageDeferred.await()
            val starChart = starChartDeferred.await()

            lastMoonPhaseImage = moonImage
            lastStarChartImage = starChart
            
            // 3. Update UI again with astronomy data
            updateUiStateFromWeatherApi(weatherData, moonImage, starChart)
        } else {
            showError("Weather data unavailable")
        }
    }

    /**
     * Update UI state from WeatherAPI response
     */
    private fun updateUiStateFromWeatherApi(
        data: WeatherApiResponse,
        moonPhaseImageUrl: String? = null,
        starChartImageUrl: String? = null
    ) {
        val current = data.current
        val forecastDay = data.forecast.forecastDay.firstOrNull()
        val astro = forecastDay?.astro

        val isMetricTemp = tempUnit == "°C"
        val isMetricSpeed = speedUnit == "km/h"

        // Build hourly forecast list
        val hourlyList = ArrayList<ForecastPeriod>()
        val allHours = data.forecast.forecastDay.flatMap { it.hour }
        val zoneId = try {
            ZoneId.of(data.location.tzId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }

        allHours.forEach { hour ->
            // Filter to show only future hours
            if (hour.timeEpoch > data.location.localtimeEpoch) {
                val zdt = java.time.ZonedDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(hour.timeEpoch),
                    zoneId
                )

                hourlyList.add(
                    ForecastPeriod(
                        name = "",
                        startTime = zdt.toString(),
                        temperature = if (isMetricTemp) hour.tempC else hour.tempF,
                        temperatureUnit = tempUnit.replace("°", ""),
                        windSpeed = "${(if (isMetricSpeed) hour.windKph else hour.windMph).toInt()} $speedUnit",
                        windDirection = "",
                        icon = "https:${hour.condition.icon}",
                        shortForecast = hour.condition.text,
                        detailedForecast = "",
                        isDaytime = hour.isDay == 1,
                        probabilityOfPrecipitation = ForecastUnitValue(hour.chanceOfRain.toDouble()),
                        relativeHumidity = ForecastUnitValue(hour.humidity.toDouble()),
                        feelsLike = if (isMetricTemp) hour.feelslikeC else hour.feelslikeF,
                        clouds = hour.cloud,
                        uvIndex = hour.uv,
                        windGust = if (isMetricSpeed) {
                            "${hour.windKph * 1.2} $speedUnit"
                        } else {
                            "${hour.windMph * 1.2} $speedUnit"
                        },
                        airQualityIndex = 1
                    )
                )
            }
        }

        // Build daily forecast list
        val dailyList = data.forecast.forecastDay.map { day ->
            ForecastPeriod(
                name = java.time.LocalDate.parse(day.date).dayOfWeek.name,
                startTime = "${day.date}T12:00:00-00:00",
                temperature = if (isMetricTemp) day.day.avgTempC else day.day.avgTempF,
                temperatureUnit = tempUnit.replace("°", ""),
                windSpeed = "${(if (isMetricSpeed) day.day.maxWindKph else day.day.maxWindMph).toInt()} $speedUnit",
                windDirection = "",
                icon = "https:${day.day.condition.icon}",
                shortForecast = day.day.condition.text,
                detailedForecast = "High near ${if (isMetricTemp) day.day.maxTempC.toInt() else day.day.maxTempF.toInt()}$tempUnit. Night low around ${if (isMetricTemp) day.day.minTempC.toInt() else day.day.minTempF.toInt()}$tempUnit.",
                isDaytime = true,
                probabilityOfPrecipitation = ForecastUnitValue(day.day.dailyChanceOfRain.toDouble()),
                relativeHumidity = ForecastUnitValue(0.0),
                uvIndex = day.day.uv,
                sunrise = day.astro.sunrise,
                sunset = day.astro.sunset,
                maxTemp = if (isMetricTemp) day.day.maxTempC else day.day.maxTempF,
                minTemp = if (isMetricTemp) day.day.minTempC else day.day.minTempF,
                airQualityIndex = day.day.airQuality?.usEpaIndex
            )
        }

        // Process AQI data (fix the bug - don't default to 1)
        val usEpaIndex = current.airQuality?.usEpaIndex
        val aqiStatus = when (usEpaIndex) {
            1 -> "Good"
            2 -> "Moderate"
            3 -> "Unhealthy for Sensitive Groups"
            4 -> "Unhealthy"
            5 -> "Very Unhealthy"
            6 -> "Hazardous"
            else -> "Unavailable"
        }

        // Get temperature and other values
        val tempVal = if (isMetricTemp) current.tempC else current.tempF
        val feelsLikeVal = if (isMetricTemp) current.feelslikeC else current.feelslikeF
        val windVal = if (isMetricSpeed) current.windKph else current.windMph

        val highTempVal = if (isMetricTemp) {
            forecastDay?.day?.maxTempC
        } else {
            forecastDay?.day?.maxTempF
        }
        val lowTempVal = if (isMetricTemp) {
            forecastDay?.day?.minTempC
        } else {
            forecastDay?.day?.minTempF
        }

        // Update UI state
        _uiState.value = _uiState.value.copy(
            cityName = "${data.location.name}, ${data.location.region}",
            temperature = "${tempVal.toInt()}$tempUnit",
            condition = current.condition.text,
            isDaytime = current.isDay == 1,
            humidity = "${current.humidity}%",
            wind = "${windVal.toInt()} $speedUnit",
            rainChance = "${forecastDay?.day?.dailyChanceOfRain ?: 0}%",
            feelsLike = "${feelsLikeVal.toInt()}$tempUnit",
            pressure = "${current.pressureMb.toInt()} mb",
            highTemp = "${highTempVal?.toInt() ?: "--"}$tempUnit",
            lowTemp = "${lowTempVal?.toInt() ?: "--"}$tempUnit",
            currentDate = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            sunrise = astro?.sunrise ?: "--:--",
            sunset = astro?.sunset ?: "--:--",
            moonPhase = astro?.moonPhase ?: "Unknown",
            moonPhaseImageUrl = moonPhaseImageUrl,
            starChartImageUrl = starChartImageUrl,
            constellationName = "Sky Directly Overhead",
            uvIndex = "${current.uv.toInt()}",
            // Fixed AQI - no longer defaults to 1
            aqi = usEpaIndex?.toString() ?: "--",
            aqiStatus = aqiStatus,
            pm25 = "${current.airQuality?.pm25?.toInt() ?: "--"}",
            pm10 = "${current.airQuality?.pm10?.toInt() ?: "--"}",
            ozone = "${current.airQuality?.o3?.toInt() ?: "--"}",
            dailyForecasts = dailyList,
            hourlyForecasts = hourlyList.take(24),
            isLoading = false,
            isDarkTheme = isDarkTheme,
            tempUnit = tempUnit,
            speedUnit = speedUnit,
            dataSource = "WeatherAPI"
        )
    }

    /**
     * Show error message
     */
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            error = message,
            isLoading = false
        )
    }

    /**
     * Refresh UI state with current settings
     */
    private fun refreshUiState() {
        _uiState.value = _uiState.value.copy(
            isDarkTheme = isDarkTheme,
            tempUnit = tempUnit,
            speedUnit = speedUnit,
            dataSource = "WeatherAPI"
        )
    }

    /**
     * Reapply settings to cached data
     */
    private fun reapplySettings() {
        if (lastWeatherData != null) {
            updateUiStateFromWeatherApi(
                lastWeatherData!!,
                lastMoonPhaseImage,
                lastStarChartImage
            )
        } else {
            refreshWeather()
        }
    }
}

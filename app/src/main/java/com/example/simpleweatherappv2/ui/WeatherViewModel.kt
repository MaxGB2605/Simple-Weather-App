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
     * Update search suggestions as the user types
     */
    fun onSearchQueryChanged(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchSuggestions = emptyList(),
                isSearching = false
            )
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val suggestions = repository.getSearchSuggestions(query)
            _uiState.value = _uiState.value.copy(
                searchSuggestions = suggestions,
                isSearching = false
            )
        }
    }

    /**
     * Clear all search results
     */
    fun clearSuggestions() {
        _uiState.value = _uiState.value.copy(
            searchSuggestions = emptyList(),
            isSearching = false
        )
    }

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
                val localDate = weatherData.location.localtime.split(" ")[0]
                val moonImageDeferred = async { repository.getMoonPhaseImage(weatherData.location.lat, weatherData.location.lon, localDate) }
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
            val localDate = weatherData.location.localtime.split(" ")[0]
            val moonImageDeferred = async { repository.getMoonPhaseImage(lat, lon, localDate) }
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
                        windDirection = hour.windDir,
                        icon = "https:${hour.condition.icon}",
                        shortForecast = hour.condition.text,
                        detailedForecast = "",
                        isDaytime = hour.isDay == 1,
                        probabilityOfPrecipitation = ForecastUnitValue(hour.chanceOfRain.toDouble()),
                        relativeHumidity = ForecastUnitValue(hour.humidity.toDouble()),
                        feelsLike = if (isMetricTemp) hour.feelslikeC else hour.feelslikeF,
                        clouds = hour.cloud,
                        uvIndex = hour.uv,
                        snowChance = hour.chanceOfSnow,
                        windGust = if (isMetricSpeed) {
                            "${hour.gustKph.toInt()} $speedUnit"
                        } else {
                            "${hour.gustMph.toInt()} $speedUnit"
                        },
                        visibility = if (isMetricSpeed) "%.1f km".format(hour.visKm) else "%.1f mi".format(hour.visMiles),
                        pressure = if (isMetricSpeed) "${hour.pressureMb.toInt()} mb" else "%.2f in".format(hour.pressureIn),
                        dewPoint = "${(if (isMetricTemp) hour.dewpointC else hour.dewpointF)?.toInt() ?: "--"}$tempUnit",
                        windChill = "${(if (isMetricTemp) hour.windchillC else hour.windchillF)?.toInt() ?: "--"}$tempUnit",
                        heatIndex = "${(if (isMetricTemp) hour.heatindexC else hour.heatindexF)?.toInt() ?: "--"}$tempUnit",
                        precipitation = if (isMetricSpeed) "%.1f mm".format(hour.precipMm) else "%.2f in".format(hour.precipIn),
                        snowDepth = if (isMetricSpeed) hour.snowCm else (hour.snowCm?.let { it / 2.54 }),
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
                relativeHumidity = ForecastUnitValue(day.day.avgHumidity),
                uvIndex = day.day.uv,
                sunrise = day.astro.sunrise,
                sunset = day.astro.sunset,
                maxTemp = if (isMetricTemp) day.day.maxTempC else day.day.maxTempF,
                minTemp = if (isMetricTemp) day.day.minTempC else day.day.minTempF,
                airQualityIndex = day.day.airQuality?.usEpaIndex,
                snowDepth = if (isMetricSpeed) day.day.totalSnowCm else (day.day.totalSnowCm?.let { it / 2.54 }),
                snowChance = day.day.dailyChanceOfSnow,
                visibility = if (isMetricSpeed) "%.1f km".format(day.day.avgVisKm) else "%.1f mi".format(day.day.avgVisMiles),
                precipitation = if (isMetricSpeed) "%.1f mm".format(day.day.totalPrecipMm) else "%.2f in".format(day.day.totalPrecipIn)
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
            pressure = if (speedUnit == "km/h") "${current.pressureMb.toInt()} mb" else "%.2f inHg".format(current.pressureIn),
            visibility = if (speedUnit == "km/h") "%.1f km".format(current.visKm) else "%.1f mi".format(current.visMiles),
            precipitation = if (speedUnit == "km/h") "%.1f mm".format(current.precipMm) else "%.2f in".format(current.precipIn),
            cloudCover = "${current.clouds}%",
            dewPoint = "${(if (isMetricTemp) current.dewpointC else current.dewpointF)?.toInt() ?: "--"}$tempUnit",
            snowChance = "${forecastDay?.day?.dailyChanceOfSnow ?: 0}%",
            windChill = "${(if (isMetricTemp) current.windchillC else current.windchillF)?.toInt() ?: "--"}$tempUnit",
            heatIndex = "${(if (isMetricTemp) current.heatindexC else current.heatindexF)?.toInt() ?: "--"}$tempUnit",
            highTemp = "${highTempVal?.toInt() ?: "--"}$tempUnit",
            lowTemp = "${lowTempVal?.toInt() ?: "--"}$tempUnit",
            currentDate = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            sunrise = astro?.sunrise ?: "--:--",
            sunset = astro?.sunset ?: "--:--",
            daylightDuration = calculateDaylightDuration(astro?.sunrise, astro?.sunset),
            moonrise = astro?.moonrise ?: "--:--",
            moonset = astro?.moonset ?: "--:--",
            moonIllumination = "${astro?.moonIllumination ?: 0}%",
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
            alerts = data.alerts?.alert ?: emptyList(),
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
    
    private fun calculateDaylightDuration(sunrise: String?, sunset: String?): String {
        if (sunrise == null || sunset == null) return "--"
        return try {
            // WeatherAPI returns time like "07:12 AM"
            val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
            val sunriseTime = java.time.LocalTime.parse(sunrise.uppercase(), formatter)
            val sunsetTime = java.time.LocalTime.parse(sunset.uppercase(), formatter)
            
            val duration = java.time.Duration.between(sunriseTime, sunsetTime)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            
            "$hours hrs $minutes mins"
        } catch (e: Exception) {
            "--"
        }
    }
}

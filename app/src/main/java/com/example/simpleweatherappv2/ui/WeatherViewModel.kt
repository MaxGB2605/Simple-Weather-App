package com.example.simpleweatherappv2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleweatherappv2.data.FavoriteLocation
import com.example.simpleweatherappv2.data.ForecastPeriod
import com.example.simpleweatherappv2.data.ForecastUnitValue
import com.example.simpleweatherappv2.data.LassAqiFeed
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
    private var weatherProvider = "WeatherAPI" // "WeatherAPI" or "NWS"

    // --- DATA CACHE ---
    private var lastWeatherApiData: WeatherApiResponse? = null

    // --- SETTERS ---
    fun toggleTheme(isDark: Boolean) {
        if (isDarkTheme != isDark) {
            isDarkTheme = isDark
            refreshUiState() // Update UI immediately
            reapplySettings()
        }
    }

    fun setTempUnit(unit: String) {
        if (tempUnit != unit) {
            tempUnit = unit
            refreshUiState() // Update UI immediately
            reapplySettings()
        }
    }

    fun setSpeedUnit(unit: String) {
        if (speedUnit != unit) {
            speedUnit = unit
            refreshUiState() // Update UI immediately
            reapplySettings()
        }
    }

    fun setDataSource(source: String) {
        if (weatherProvider != source) {
            weatherProvider = source
            refreshUiState() // Update UI immediately
            refreshWeather()
        }
    }
    
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
            // Since we need to know the state to toggle, we could check DB or UI state. 
            // Using UI state for simplicity as it's kept in sync.
            if (_uiState.value.favorites.contains(location)) {
                favoriteDao.delete(FavoriteLocation(location))
            } else {
                favoriteDao.insert(FavoriteLocation(location))
            }
        }
    }
    
    private fun refreshUiState() {
        _uiState.value = _uiState.value.copy(
            isDarkTheme = isDarkTheme,
            tempUnit = tempUnit,
            speedUnit = speedUnit,
            dataSource = weatherProvider
        )
    }

    private fun reapplySettings() {
        if (lastWeatherApiData != null && weatherProvider == "WeatherAPI") {
            updateUiStateFromWeatherApi(lastWeatherApiData!!)
        } else {
            refreshWeather()
        }
    }

    // Search by City Name
    fun updateWeather(locationSearch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                cityName = locationSearch,
                condition = "Loading...",
                isLoading = true,
                error = null,
                // Ensure settings are preserved during loading
                isDarkTheme = isDarkTheme,
                tempUnit = tempUnit,
                speedUnit = speedUnit,
                dataSource = weatherProvider
            )

            if (weatherProvider == "WeatherAPI") {
                // Direct calls, no need for extra coroutineScope since we are already in launch
                val apiData = repository.getWeatherApiForecast(locationSearch)
                
                if (apiData != null) {
                    val aqiData = repository.getAqiData(apiData.location.lat, apiData.location.lon)
                    lastWeatherApiData = apiData
                    updateUiStateFromWeatherApi(apiData, aqiData)
                    return@launch
                }
            }

            // Fallback or NWS path
            val coords = repository.getCoordinates(locationSearch)
            if (coords != null) {
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
                error = null,
                // Ensure settings are preserved during loading
                isDarkTheme = isDarkTheme,
                tempUnit = tempUnit,
                speedUnit = speedUnit,
                dataSource = weatherProvider
            )

            val coords = repository.getCurrentLocation()

            if (coords != null) {
                val finalCityName = repository.getCityName(coords.first, coords.second)
                fetchAndDisplayWeather(finalCityName, coords.first, coords.second)
            } else {
                showError("Location denied or not found")
            }
        }
    }

    private suspend fun fetchAndDisplayWeather(city: String, lat: Double, lon: Double) = coroutineScope {
        // Use user selected provider
        if (weatherProvider == "WeatherAPI") {
            val weatherApiDataDeferred = async { repository.getWeatherApiForecast(lat, lon) }
            val aqiDataDeferred = async { repository.getAqiData(lat, lon) }
            
            val weatherApiData = weatherApiDataDeferred.await()
            val aqiData = aqiDataDeferred.await()
            
            if (weatherApiData != null) {
                lastWeatherApiData = weatherApiData
                updateUiStateFromWeatherApi(weatherApiData, aqiData)
                return@coroutineScope
            }
        }

        // --- FALLBACK TO NWS ---
        val observationDeferred = async { repository.getRealTimeWeather(lat, lon) }
        val forecastDeferred = async { repository.getWeather(lat, lon) }
        val dailyListDeferred = async { repository.getDailyForecasts(lat, lon) ?: emptyList() }
        val fullHourlyListDeferred = async { repository.getHourlyForecasts(lat, lon) ?: emptyList() }
        val aqiDataDeferred = async { repository.getAqiData(lat, lon) }

        val observation = observationDeferred.await()
        val forecast = forecastDeferred.await()
        val dailyList = dailyListDeferred.await()
        val fullHourlyList = fullHourlyListDeferred.await()
        val aqiData = aqiDataDeferred.await()

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
            .take(24)

        val todayHigh = dailyList
            .filter { it.isDaytime }
            .maxByOrNull { it.temperature }
            ?.temperature?.let { "${it}°F" } ?: "--°F"
        
        val todayLow = dailyList
            .filter { !it.isDaytime }
            .minByOrNull { it.temperature }
            ?.temperature?.let { "${it}°F" } ?: "--°F"

        val currentDate = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))

        val timeZoneId = repository.getTimeZone(lat, lon)
        val zoneId = try { ZoneId.of(timeZoneId) } catch (e: Exception) { ZoneId.systemDefault() }
        
        val sunTimes = SunCalc.calculateSunriseSunset(lat, lon, java.time.LocalDate.now(), zoneId)
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        
        val sunriseTime = sunTimes.first?.format(timeFormatter) ?: "--:--"
        val sunsetTime = sunTimes.second?.format(timeFormatter) ?: "--:--"

        val uvIndexValue = "3"
        val moonPhaseValue = "Waxing Gibbous"
        
        val aqiValueInt = aqiData?.aqi?.toIntOrNull() 
            ?: aqiData?.pm25?.let { calculateAqiFromPm25(it) }

        // LASS AQI Mapping
        val aqiValue = aqiValueInt?.toString() ?: "--"
        val pm25Value = aqiData?.pm25?.toInt()?.toString() ?: "--"
        val pm10Value = "--" // LASS API might not provide PM10 directly in this feed
        val ozoneValue = "--"

        val aqiStatusValue = when {
            aqiValueInt == null -> "Unknown"
            aqiValueInt <= 50 -> "Good"
            aqiValueInt <= 100 -> "Moderate"
            aqiValueInt <= 150 -> "Unhealthy for Sensitive Groups"
            aqiValueInt <= 200 -> "Unhealthy"
            aqiValueInt <= 300 -> "Very Unhealthy"
            else -> "Hazardous"
        }
        
        // Settings for NWS
        val isMetricTemp = tempUnit == "°C"
        val isMetricSpeed = speedUnit == "km/h"

        if (observation != null && observation.temperature?.value != null) {
            val tempC = observation.temperature.value
            val tempF = (tempC * 9 / 5) + 32
            val tempDisplay = if (isMetricTemp) "${tempC.toInt()}°C" else "${tempF.toInt()}°F"
            
            val windKmh = observation.windSpeed?.value ?: 0.0
            val windMph = windKmh * 0.621371
            val windDisplay = if (isMetricSpeed) "${windKmh.toInt()} km/h" else "${windMph.toInt()} mph"

            val feelsLikeC = observation.heatIndex?.value
                ?: observation.windChill?.value
                ?: tempC
            val feelsLikeF = (feelsLikeC * 9 / 5) + 32
            val feelsLikeDisplay = if (isMetricTemp) "${feelsLikeC.toInt()}°C" else "${feelsLikeF.toInt()}°F"
            
            val pressurePa = observation.barometricPressure?.value ?: 0.0
            val pressureMb = pressurePa / 100

            _uiState.value = _uiState.value.copy(
                cityName = city,
                temperature = tempDisplay,
                condition = observation.textDescription ?: "Unknown",
                humidity = "${observation.relativeHumidity?.value?.toInt() ?: 0}%",
                wind = windDisplay,
                rainChance = "${forecast?.probabilityOfPrecipitation?.value?.toInt() ?: 0}%",
                feelsLike = feelsLikeDisplay,
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
                isLoading = false,
                isDaytime = forecast?.isDaytime ?: true,
                isDarkTheme = isDarkTheme,
                tempUnit = tempUnit,
                speedUnit = speedUnit,
                dataSource = weatherProvider
            )
        } else if (forecast != null) {
            val tempF = forecast.temperature
            val tempDisplay = if (isMetricTemp) "${((tempF - 32) * 5/9).toInt()}°C" else "${tempF.toInt()}°F"
            val windDisplay = forecast.windSpeed ?: "--"
            
            _uiState.value = _uiState.value.copy(
                cityName = city,
                temperature = tempDisplay,
                condition = forecast.shortForecast,
                humidity = "${forecast.relativeHumidity?.value ?: 0}%",
                wind = windDisplay,
                rainChance = "${forecast.probabilityOfPrecipitation?.value ?: 0}%",
                feelsLike = "--$tempUnit",
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
                isLoading = false,
                isDaytime = forecast.isDaytime,
                isDarkTheme = isDarkTheme,
                tempUnit = tempUnit,
                speedUnit = speedUnit,
                dataSource = weatherProvider
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

    fun refreshWeather() {
        val currentCity = _uiState.value.cityName
        if (currentCity != "Unknown" && currentCity != "Locating...") {
            updateWeather(currentCity)
        } else {
            fetchCurrentLocation()
        }
    }

    private fun updateUiStateFromWeatherApi(data: WeatherApiResponse, lassAqi: LassAqiFeed? = null) {
        val current = data.current
        val forecastDay = data.forecast.forecastDay.firstOrNull()
        val astro = forecastDay?.astro
        
        val isMetricTemp = tempUnit == "°C"
        val isMetricSpeed = speedUnit == "km/h"
        
        val hourlyList = ArrayList<ForecastPeriod>()
        val allHours = data.forecast.forecastDay.flatMap { it.hour }
        val zoneId = try { ZoneId.of(data.location.tzId) } catch (e: Exception) { ZoneId.systemDefault() }
        
        allHours.forEach { hour ->
            // Filter to show only future hours, matching the NWS "Upcoming" behavior
            if (hour.timeEpoch > data.location.localtimeEpoch) {
                // Formatting time with the location's actual zone ID so labels match city local time
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
                        // NEW FIELDS
                        feelsLike = if (isMetricTemp) hour.feelslikeC else hour.feelslikeF,
                        clouds = hour.cloud,
                        uvIndex = hour.uv,
                        windGust = if (isMetricSpeed) "${hour.windKph * 1.2} $speedUnit" else "${hour.windMph * 1.2} $speedUnit", // Gust usually 1.2-1.5x
                        airQualityIndex = 1 // Placeholder for EPA index
                    )
                )
            }
        }
        
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
                detailedForecast = "High near ${if (isMetricTemp) day.day.maxTempC.toInt() else day.day.maxTempF.toInt()}${tempUnit}. Night low around ${if (isMetricTemp) day.day.minTempC.toInt() else day.day.minTempF.toInt()}${tempUnit}.",
                isDaytime = true,
                probabilityOfPrecipitation = ForecastUnitValue(day.day.dailyChanceOfRain.toDouble()),
                relativeHumidity = ForecastUnitValue(0.0),
                // NEW FIELDS
                uvIndex = day.day.uv,
                sunrise = day.astro.sunrise,
                sunset = day.astro.sunset,
                maxTemp = if (isMetricTemp) day.day.maxTempC else day.day.maxTempF,
                minTemp = if (isMetricTemp) day.day.minTempC else day.day.minTempF,
                airQualityIndex = day.day.airQuality?.usEpaIndex ?: 1
            )
        }

        val lassAqiVal = lassAqi?.aqi?.toIntOrNull() 
            ?: lassAqi?.pm25?.let { calculateAqiFromPm25(it) }
            
        val usEpa = lassAqiVal ?: current.airQuality?.usEpaIndex ?: 1
        val aqiStatus = when {
            lassAqiVal != null -> when {
                lassAqiVal <= 50 -> "Good"
                lassAqiVal <= 100 -> "Moderate"
                lassAqiVal <= 150 -> "Unhealthy for Sensitive Groups"
                lassAqiVal <= 200 -> "Unhealthy"
                lassAqiVal <= 300 -> "Very Unhealthy"
                else -> "Hazardous"
            }
            else -> when(usEpa) {
                1 -> "Good"
                2 -> "Moderate"
                3 -> "Unhealthy for Sensitive Groups"
                4 -> "Unhealthy"
                5 -> "Very Unhealthy"
                6 -> "Hazardous"
                else -> "Unknown"
            }
        }
        
        val tempVal = if (isMetricTemp) current.tempC else current.tempF
        val feelsLikeVal = if (isMetricTemp) current.feelslikeC else current.feelslikeF
        val windVal = if (isMetricSpeed) current.windKph else current.windMph
        
        val highTempVal = if (isMetricTemp) forecastDay?.day?.maxTempC else forecastDay?.day?.maxTempF
        val lowTempVal = if (isMetricTemp) forecastDay?.day?.minTempC else forecastDay?.day?.minTempF

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
            currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            sunrise = astro?.sunrise ?: "--:--",
            sunset = astro?.sunset ?: "--:--",
            moonPhase = astro?.moonPhase ?: "Unknown",
            uvIndex = "${current.uv.toInt()}",
            aqi = "$usEpa", 
            aqiStatus = aqiStatus,
            pm25 = "${lassAqi?.pm25?.toInt() ?: current.airQuality?.pm25?.toInt() ?: 0}",
            pm10 = "${current.airQuality?.pm10?.toInt() ?: 0}",
            ozone = "${current.airQuality?.o3?.toInt() ?: 0}", 
            dailyForecasts = dailyList,
            hourlyForecasts = hourlyList.take(24),
            isLoading = false,
            isDarkTheme = isDarkTheme,
            tempUnit = tempUnit,
            speedUnit = speedUnit,
            dataSource = weatherProvider
        )
    }

    private fun calculateAqiFromPm25(pm25: Double): Int {
        return when {
            pm25 <= 12.0 -> ((50.0 / 12.0) * pm25).toInt()
            pm25 <= 35.4 -> ((49.0 / (35.4 - 12.1)) * (pm25 - 12.1) + 51).toInt()
            pm25 <= 55.4 -> ((49.0 / (55.4 - 35.5)) * (pm25 - 35.5) + 101).toInt()
            pm25 <= 150.4 -> ((49.0 / (150.4 - 55.5)) * (pm25 - 55.5) + 151).toInt()
            pm25 <= 250.4 -> ((99.0 / (250.4 - 150.5)) * (pm25 - 150.5) + 201).toInt()
            pm25 <= 350.4 -> ((99.0 / (350.4 - 250.5)) * (pm25 - 250.5) + 301).toInt()
            pm25 <= 500.4 -> ((99.0 / (500.4 - 350.5)) * (pm25 - 350.5) + 401).toInt()
            else -> 500
        }
    }
}


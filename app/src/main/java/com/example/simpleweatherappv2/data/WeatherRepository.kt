package com.example.simpleweatherappv2.data

import android.content.Context
import android.location.Geocoder
import java.util.Locale

@Suppress("DEPRECATION") // For simplicity (newer Geocoder API is more complex)
class WeatherRepository(private val context: Context) {

    private val api = RetrofitInstance.api

    // NEW: Turn "City Name" into (Lat, Lon)
    fun getCoordinates(city: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(city, 1) // Get 1 result

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                Pair(address.latitude, address.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getWeather(lat: Double, lon: Double): ForecastPeriod? {
        try {
            // Step 1: Get the Grid Point
            val pointsResponse = api.getGridPoint(lat, lon)
            val props = pointsResponse.properties

            // Step 2: Get the Forecast using the grid data
            val forecastResponse = api.getForecast(props.gridId, props.gridX, props.gridY)

            // Return the first period (current weather)
            return forecastResponse.properties.periods.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
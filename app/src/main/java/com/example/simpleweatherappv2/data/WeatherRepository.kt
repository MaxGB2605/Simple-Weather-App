package com.example.simpleweatherappv2.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("DEPRECATION")
class WeatherRepository(private val context: Context) {

    private val api = RetrofitInstance.api
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // NEW: Get Current GPS Location
    @SuppressLint("MissingPermission") // We will check permission in the UI before calling this
    suspend fun getCurrentLocation(): Pair<Double, Double>? = suspendCoroutine { continuation ->
        // Check if we actually have permission
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            continuation.resume(null)
            return@suspendCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(Pair(location.latitude, location.longitude))
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
            .addOnCanceledListener {
                continuation.resume(null)
            }
    }

    // Turn "City Name" into (Lat, Lon)
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
            // CHANGE: We now use "getHourlyForecast" to get the current conditions
            val forecastResponse = api.getHourlyForecast(props.gridId, props.gridX, props.gridY)

            // Return the first period (current hour)
            return forecastResponse.properties.periods.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // NEW: Turn (Lat, Lon) into "City Name"
    fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: "Unknown City"
                val state = address.adminArea ?: "" // "NJ", "NY", etc.

                if (state.isNotEmpty()) {
                    "$city, $state" // "Fair Lawn, NJ"
                } else {
                    city
                }
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown Location"
        }
    }

    // NEW: Get precise "Current Observation" from the nearest station
    suspend fun getRealTimeWeather(lat: Double, lon: Double): ObservationProperties? {
        try {
            // 1. Get Grid Points
            val pointsResponse = api.getGridPoint(lat, lon)
            val props = pointsResponse.properties

            // 2. Find the closest Weather Station
            val stationsResponse = api.getStations(props.gridId, props.gridX, props.gridY)
            val firstStation = stationsResponse.features.firstOrNull() ?: return null
            val stationId = firstStation.properties.stationIdentifier

            // 3. Get the Observation from that station
            val observationResponse = api.getObservation(stationId)
            return observationResponse.properties

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // NEW: Get the 7-Day Forecast (List of future days)
    suspend fun getDailyForecasts(lat: Double, lon: Double): List<ForecastPeriod>? {
        try {
            // 1. Get Grid Points
            val pointsResponse = api.getGridPoint(lat, lon)
            val props = pointsResponse.properties

            // 2. Get the DAILY Forecast
            val forecastResponse = api.getForecast(props.gridId, props.gridX, props.gridY)

            // Return the whole list of days
            return forecastResponse.properties.periods
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
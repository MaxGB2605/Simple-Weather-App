package com.example.simpleweatherappv2.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("DEPRECATION")
class WeatherRepository(private val context: Context) {

    private val weatherApi = RetrofitInstance.weatherApi
    private val astronomyApi = RetrofitInstance.astronomyApi
    private val API_KEY = "f7ce63eeaaa248079d7143947250604"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // ==================== LOCATION METHODS ====================

    /**
     * Get current GPS location
     * Returns Pair<Latitude, Longitude> or null if unavailable
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? = suspendCoroutine { continuation ->
        try {
            // Check if we actually have permission
            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasFine && !hasCoarse) {
                continuation.resume(null)
                return@suspendCoroutine
            }

            // Determine priority based on permission
            val priority = if (hasFine) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            // Request a FRESH location fix
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        // Fallback to last known if fresh fix fails
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            continuation.resume(lastLoc?.let { Pair(it.latitude, it.longitude) })
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }
                .addOnFailureListener {
                    // Fallback to last known on error
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc?.let { Pair(it.latitude, it.longitude) })
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume(null)
        }
    }

    /**
     * Convert city name to coordinates using Geocoder
     * Returns Pair<Latitude, Longitude> or null if not found
     */
    fun getCoordinates(city: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(city, 1)

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

    /**
     * Convert coordinates to city name using Geocoder
     * Returns formatted city name or "Unknown Location"
     */
    fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: "Unknown City"
                val state = address.adminArea ?: ""

                if (state.isNotEmpty()) {
                    "$city, $state"
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

    // ==================== WEATHER API METHODS ====================

    /**
     * Get comprehensive weather data from WeatherAPI.com
     * Includes: current weather, forecast, hourly data, AQI, astronomy
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return WeatherApiResponse or null if failed
     */
    suspend fun getWeatherData(lat: Double, lon: Double): WeatherApiResponse? {
        return try {
            weatherApi.getForecast(
                apiKey = API_KEY,
                query = "$lat,$lon",
                days = 7,
                aqi = "yes"  // Include air quality data
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get weather data by city name
     * 
     * @param city City name (e.g., "New York" or "London, UK")
     * @return WeatherApiResponse or null if failed
     */
    suspend fun getWeatherData(city: String): WeatherApiResponse? {
        return try {
            weatherApi.getForecast(
                apiKey = API_KEY,
                query = city,
                days = 7,
                aqi = "yes"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==================== ASTRONOMY API METHODS ====================

    /**
     * Get moon phase image from Astronomy API
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return Image URL or null if failed
     */
    suspend fun getMoonPhaseImage(lat: Double, lon: Double): String? {
        return try {
            val currentDate = java.time.LocalDate.now().toString()
            android.util.Log.d("MoonPhase", "Requesting moon phase for lat=$lat, lon=$lon, date=$currentDate")
            
            val request = AstronomyMoonPhaseRequest(
                style = MoonStyle(),
                observer = Observer(lat, lon, currentDate),
                view = View()
            )
            
            android.util.Log.d("MoonPhase", "Making API call to Astronomy API...")
            val response = astronomyApi.getMoonPhaseImage(request)
            
            android.util.Log.d("MoonPhase", "Success! Image URL: ${response.data.imageUrl}")
            response.data.imageUrl
        } catch (e: Exception) {
            android.util.Log.e("MoonPhase", "Error fetching moon phase: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Get star chart image from Astronomy API
     * 
     * @param lat Latitude
     * @param lon Longitude
     * @return Image URL or null if failed
     */
    suspend fun getStarChartImage(lat: Double, lon: Double): String? {
        return try {
            val currentDate = java.time.LocalDate.now().toString()
            
            // Calculate Zenith coordinates
            // Declination = Latitude
            val dec = lat
            // Right Ascension = Local Sidereal Time
            val ra = calculateLST(lon)
            
            android.util.Log.d("StarChart", "Calculating Zenith for lat=$lat, lon=$lon")
            android.util.Log.d("StarChart", "Zenith RA=$ra, Dec=$dec")
            
            val request = AstronomyStarChartRequest(
                style = "default",  
                observer = Observer(lat, lon, currentDate),
                view = StarChartView(
                    type = "area",
                    parameters = StarChartParameters(
                        position = StarChartPosition(
                            equatorial = EquatorialCoordinates(
                                rightAscension = ra,
                                declination = dec
                            )
                        ),
                        zoom = 3 // Zoom level 3 as per example
                    )
                )
            )
            
            android.util.Log.d("StarChart", "Request JSON: ${com.google.gson.Gson().toJson(request)}")
            android.util.Log.d("StarChart", "Making API call to Astronomy API...")
            val response = astronomyApi.getStarChartImage(request)
            
            android.util.Log.d("StarChart", "Success! Image URL: ${response.data.imageUrl}")
            response.data.imageUrl
        } catch (e: retrofit2.HttpException) {
            android.util.Log.e("StarChart", "HTTP Error ${e.code()}: ${e.message()}", e)
            try {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("StarChart", "Response body: $errorBody")
            } catch (ex: Exception) {
                android.util.Log.e("StarChart", "Could not read error body", ex)
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("StarChart", "Error fetching star chart: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Calculate Local Sidereal Time (LST) in hours
     * Approximates the Right Ascension of the Zenith
     */
    private fun calculateLST(lon: Double): Double {
        val now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
        
        // Calculate Julian Date
        val year = now.year
        val month = now.monthValue
        val day = now.dayOfMonth
        val hour = now.hour + now.minute / 60.0 + now.second / 3600.0
        
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        
        val a = y / 100
        val b = 2 - a + a / 4
        
        val jd = (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5 + hour / 24.0
        
        // GMST calculation (approximate)
        val d = jd - 2451545.0
        
        // GMST in degrees
        var theta = 280.46061837 + 360.98564736629 * d
        
        // Normalize
        theta %= 360.0
        if (theta < 0) theta += 360.0
        
        // Convert to hours
        val gmstHours = theta / 15.0
        
        // LST = GMST + Lon/15
        var lst = gmstHours + lon / 15.0
        
        // Normalize to 0-24
        lst %= 24.0
        if (lst < 0) lst += 24.0
        
        return lst
    }
}
package com.example.simpleweatherappv2.data

class WeatherRepository {
    private val api = RetrofitInstance.api

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
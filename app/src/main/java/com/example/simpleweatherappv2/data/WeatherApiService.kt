package com.example.simpleweatherappv2.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String, // City name, or Lat,Lon
        @Query("days") days: Int = 7,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "no"
    ): WeatherApiResponse
}

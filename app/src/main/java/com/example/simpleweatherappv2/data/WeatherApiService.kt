package com.example.simpleweatherappv2.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int = 3,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "yes"
    ): WeatherApiResponse

    @GET("search.json")
    suspend fun getSearchSuggestions(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): List<SearchSuggestion>
}

package com.example.simpleweatherappv2.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AstronomyApiService {
    @POST("studio/moon-phase")
    suspend fun getMoonPhaseImage(@Body request: AstronomyMoonPhaseRequest): AstronomyResponse

    @POST("studio/star-chart")
    suspend fun getStarChartImage(@Body request: AstronomyStarChartRequest): AstronomyResponse
}

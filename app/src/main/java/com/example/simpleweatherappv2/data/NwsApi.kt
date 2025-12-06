package com.example.simpleweatherappv2.data

import retrofit2.http.GET
import retrofit2.http.Path

interface NwsApi {

    // Step 1: Get grid point from lat/lon
    @GET("points/{latitude},{longitude}")
    suspend fun getGridPoint(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): PointsResponse

    // Step 2: Get forecast from grid coordinates
    @GET("gridpoints/{gridId}/{gridX},{gridY}/forecast")
    suspend fun getForecast(
        @Path("gridId") gridId: String,
        @Path("gridX") gridX: Int,
        @Path("gridY") gridY: Int
    ): ForecastResponse
}
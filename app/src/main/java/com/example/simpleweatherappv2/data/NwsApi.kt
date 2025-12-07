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

    // Step 3: Get HOURLY forecast (more accurate for current conditions)
    @GET("gridpoints/{gridId}/{gridX},{gridY}/forecast/hourly")
    suspend fun getHourlyForecast(
        @Path("gridId") gridId: String,
        @Path("gridX") gridX: Int,
        @Path("gridY") gridY: Int
    ): ForecastResponse

    // Step 4: Get list of weather stations for this grid
    @GET("gridpoints/{gridId}/{gridX},{gridY}/stations")
    suspend fun getStations(
        @Path("gridId") gridId: String,
        @Path("gridX") gridX: Int,
        @Path("gridY") gridY: Int
    ): StationsResponse

    // Step 5: Get the latest observation from a specific station
    @GET("stations/{stationId}/observations/latest")
    suspend fun getObservation(
        @Path("stationId") stationId: String
    ): ObservationResponse
}
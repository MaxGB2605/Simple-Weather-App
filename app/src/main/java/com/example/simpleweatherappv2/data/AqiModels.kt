package com.example.simpleweatherappv2.data

import com.google.gson.annotations.SerializedName

data class LassAqiResponse(
    val feeds: List<LassAqiFeed>,
    @SerializedName("num_of_records") val numOfRecords: Int,
    val source: String,
    val version: String
)

data class LassAqiFeed(
    @SerializedName("SiteName") val siteName: String,
    @SerializedName("s_d0") val pm25: Double,
    @SerializedName("gps_lat") val lat: Double,
    @SerializedName("gps_lon") val lon: Double,
    @SerializedName("timestamp") val timestamp: String,
    
    // Optional/Mapped fields for compatibility if needed, using null defaults
    @SerializedName("County") val county: String? = null,
    @SerializedName("AQI") val aqi: String? = null,
    @SerializedName("status") val status: String? = null
)

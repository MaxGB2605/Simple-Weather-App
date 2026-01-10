package com.example.simpleweatherappv2.data

import retrofit2.http.GET

interface AqiApiService {
    @GET("data/last-all-airbox.json")
    suspend fun getRealtimePm25(): LassAqiResponse
}

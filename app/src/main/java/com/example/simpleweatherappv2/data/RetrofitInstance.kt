package com.example.simpleweatherappv2.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val WEATHER_API_BASE_URL = "https://api.weatherapi.com/v1/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val weatherClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(weatherClient)
            .build()
            .create(WeatherApiService::class.java)
    }

    private val astronomyClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val original = chain.request()
            // TODO: Move these credentials to local.properties for security
            val applicationId = "fa53be43-03aa-49b1-ba88-04d273009580" 
            val applicationSecret = "64f9e98d964883dce2a730abf076144c12dadf06827b243f65712587b372df4d405293a9015a54f22e9c798b1612adabd620b91185bd7a54b9f9cd8b45c8e7603144cf102128ea7597ace059bfb29300fa6eb739d42a70c1dba69a8d3597abc8b06e6506a879308dfcb3cecff15d423c"
            val credentials = android.util.Base64.encodeToString(
                "$applicationId:$applicationSecret".toByteArray(),
                android.util.Base64.NO_WRAP
            )
            
            val request = original.newBuilder()
                .header("Authorization", "Basic $credentials")
                .header("User-Agent", "(SimpleWeatherApp, maximbrazhko@gmail.com)")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .build()

    val astronomyApi: AstronomyApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.astronomyapi.com/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(astronomyClient)
            .build()
            .create(AstronomyApiService::class.java)
    }
}
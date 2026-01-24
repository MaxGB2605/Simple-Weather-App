package com.example.simpleweatherappv2.data

import com.google.gson.annotations.SerializedName

data class AstronomyMoonPhaseRequest(
    val format: String = "png",
    val style: MoonStyle,
    val observer: Observer,
    val view: View
)

data class MoonStyle(
    val moonStyle: String = "sketch",
    val backgroundStyle: String = "solid", 
    val backgroundColor: String = "transparent", // Use transparent to blend with app theme
    val headingColor: String = "white",
    val textColor: String = "white"
)

data class Observer(
    val latitude: Double,
    val longitude: Double,
    val date: String // YYYY-MM-DD
)

data class View(
    val type: String = "portrait-simple",
    val orientation: String = "south-up"
)

data class AstronomyResponse(
    val data: AstronomyData
)

data class AstronomyData(
    val imageUrl: String
)

data class AstronomyStarChartRequest(
    val style: StarChartStyle,
    val observer: Observer,
    val view: StarChartView
)

data class StarChartStyle(
    val starStyle: String = "default",
    val backgroundStyle: String = "black", // stars or black
    val backgroundColor: String = "black",
    val headingColor: String = "white",
    val textColor: String = "white",
    val constellations: ConstellationStyle = ConstellationStyle()
)

data class ConstellationStyle(
    val lines: LineStyle = LineStyle(),
    val art: Boolean = false
)

data class LineStyle(
    val stroke: String = "white",
    val strokeWidth: Int = 1
)

data class StarChartView(
    val type: String = "constellation",
    val parameters: StarChartParameters
)

data class StarChartParameters(
    val constellation: String = "umi" // Ursa Minor, North Star - safe bet for Northern Hemisphere
)

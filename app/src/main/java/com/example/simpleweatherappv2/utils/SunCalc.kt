package com.example.simpleweatherappv2.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

object SunCalc {
    // Zenith for official sunrise/sunset
    private const val ZENITH_OFFICIAL = 90.8333 // 90 degrees 50'

    fun calculateSunriseSunset(
        lat: Double,
        lon: Double,
        date: LocalDate,
        zoneId: ZoneId
    ): Pair<ZonedDateTime?, ZonedDateTime?> {
        val sunriseUtc = calculateTime(lat, lon, date, true)
        val sunsetUtc = calculateTime(lat, lon, date, false)

        val sunrise = sunriseUtc?.let {
            ZonedDateTime.of(date, it, ZoneId.of("UTC")).withZoneSameInstant(zoneId)
        }
        val sunset = sunsetUtc?.let {
            // Sunset might be on the next day in UTC, but we want the local time relative to 'date'
            // The algorithm gives 0-24h UTC.
            ZonedDateTime.of(date, it, ZoneId.of("UTC")).withZoneSameInstant(zoneId)
        }

        return Pair(sunrise, sunset)
    }

    private fun calculateTime(lat: Double, lon: Double, date: LocalDate, isSunrise: Boolean): LocalTime? {
        val n = date.dayOfYear
        val lngHour = lon / 15.0
        
        val tBase = if (isSunrise) n + ((6.0 - lngHour) / 24.0) else n + ((18.0 - lngHour) / 24.0)
        
        val m = (0.9856 * tBase) - 3.289
        val l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        val lNormalized = normalize(l)
        
        var ra = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(lNormalized))))
        ra = normalize(ra)
        
        // Adjust RA to be in the same quadrant as L
        val lQuadrant = (floor(lNormalized / 90.0)) * 90.0
        val raQuadrant = (floor(ra / 90.0)) * 90.0
        ra += (lQuadrant - raQuadrant)
        ra /= 15.0
        
        val sinDec = 0.39782 * sin(Math.toRadians(lNormalized))
        val cosDec = cos(asin(sinDec))
        
        val cosH = (cos(Math.toRadians(ZENITH_OFFICIAL)) - (sinDec * sin(Math.toRadians(lat)))) / (cosDec * cos(Math.toRadians(lat)))
        
        if (cosH > 1) return null // Sun never rises
        if (cosH < -1) return null // Sun never sets
        
        val h = if (isSunrise) {
            360.0 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        }
        
        val hNormalized = h / 15.0
        val t = hNormalized + ra - (0.06571 * tBase) - 6.622
        
        var utcHour = t - lngHour
        // Normalize to 0-24
        while (utcHour < 0) utcHour += 24.0
        while (utcHour >= 24) utcHour -= 24.0
        
        val hour = utcHour.toInt()
        val minuteDec = (utcHour - hour) * 60
        val minute = minuteDec.toInt()
        val second = ((minuteDec - minute) * 60).toInt()
        
        return try {
            LocalTime.of(hour, minute, second)
        } catch (e: Exception) {
            LocalTime.of(0, 0) // Fallback
        }
    }
    
    private fun normalize(angle: Double): Double {
        var a = angle % 360
        if (a < 0) a += 360
        return a
    }
}

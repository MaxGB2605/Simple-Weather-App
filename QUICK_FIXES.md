# Quick Fixes for Immediate Issues

## Bug #1: AQI Always Shows "Good" Status

### Problem
When AQI data is unavailable, the app defaults to showing "1" (Good) instead of "Unavailable".

### Location
`ui/WeatherViewModel.kt`, line 478

### Current Code
```kotlin
val usEpa = lassAqiVal ?: current.airQuality?.usEpaIndex ?: 1  // ❌ Always defaults to 1
```

### Fix
```kotlin
val usEpa = lassAqiVal ?: current.airQuality?.usEpaIndex  // ✅ Returns null when unavailable
```

### Also Update
Line 525 in the same file:
```kotlin
aqi = usEpa?.toString() ?: "--",  // Show "--" when null
```

And lines 283-291 for NWS path:
```kotlin
val aqiStatusValue = when {
    aqiValueInt == null -> "Unavailable"  // ✅ Changed from "Unknown"
    aqiValueInt <= 50 -> "Good"
    // ... rest stays the same
}
```

---

## Bug #2: Hardcoded API Credentials

### Problem
Astronomy API credentials are hardcoded in source code (security risk).

### Location
`data/RetrofitInstance.kt`, lines 59-60

### Current Code
```kotlin
val applicationId = "fa53be43-03aa-49b1-ba88-04d273009580" 
val applicationSecret = "64f9e98d964883dce2a730abf076144c..."
```

### Fix

1. **Move to local.properties**
```properties
# local.properties (this file is gitignored)
astronomy.api.id=fa53be43-03aa-49b1-ba88-04d273009580
astronomy.api.secret=64f9e98d964883dce2a730abf076144c12dadf06827b243f65712587b372df4d405293a9015a54f22e9c798b1612adabd620b91185bd7a54b9f9cd8b45c8e7603144cf102128ea7597ace059bfb29300fa6eb739d42a70c1dba69a8d3597abc8b06e6506a879308dfcb3cecff15d423c
```

2. **Update build.gradle.kts**
```kotlin
android {
    defaultConfig {
        // Read from local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        
        buildConfigField("String", "ASTRONOMY_API_ID", 
            "\"${properties.getProperty("astronomy.api.id")}\"")
        buildConfigField("String", "ASTRONOMY_API_SECRET", 
            "\"${properties.getProperty("astronomy.api.secret")}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

3. **Update RetrofitInstance.kt**
```kotlin
private val astronomyClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .addInterceptor { chain ->
        val original = chain.request()
        val applicationId = BuildConfig.ASTRONOMY_API_ID
        val applicationSecret = BuildConfig.ASTRONOMY_API_SECRET
        val credentials = android.util.Base64.encodeToString(
            "$applicationId:$applicationSecret".toByteArray(),
            android.util.Base64.NO_WRAP
        )
        // ... rest stays the same
    }
    .build()
```

---

## Bug #3: LASS AQI Performance Issue

### Problem
Downloads ALL global air quality stations (thousands) just to find the nearest one.

### Location
`data/WeatherRepository.kt`, lines 226-237

### Current Code
```kotlin
suspend fun getAqiData(lat: Double, lon: Double): LassAqiFeed? {
    return try {
        val response = aqiApi.getRealtimePm25()  // ❌ Downloads everything!
        response.feeds.minByOrNull { feed ->
            calculateDistance(lat, lon, feed.lat, feed.lon)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
```

### Quick Fix (Temporary)
Add a filter to reduce processing:
```kotlin
suspend fun getAqiData(lat: Double, lon: Double): LassAqiFeed? {
    return try {
        val response = aqiApi.getRealtimePm25()
        
        // Pre-filter to stations within ~200km before calculating exact distance
        response.feeds
            .filter { feed ->
                val latDiff = Math.abs(feed.lat - lat)
                val lonDiff = Math.abs(feed.lon - lon)
                latDiff < 2.0 && lonDiff < 2.0  // Rough ~200km box
            }
            .minByOrNull { feed ->
                calculateDistance(lat, lon, feed.lat, feed.lon)
            }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
```

### Better Fix (Recommended)
**Remove LASS API entirely** - WeatherAPI already provides AQI data!

---

## Bug #4: Data Source Confusion

### Problem
App switches between NWS and WeatherAPI, causing inconsistent data.

### Location
`ui/WeatherViewModel.kt`, multiple locations

### Quick Fix
Set default to WeatherAPI only:
```kotlin
private var weatherProvider = "WeatherAPI"  // Remove "NWS" option
```

### Better Fix
Remove the setting entirely and always use WeatherAPI (see full refactoring plan).

---

## Testing These Fixes

### Test AQI Fix
1. Find a location with no AQI data available
2. Verify it shows "Unavailable" or "--" instead of "Good"
3. Find a location with AQI data
4. Verify it shows the correct value

### Test API Credentials
1. Ensure `local.properties` is in `.gitignore`
2. Build the app
3. Verify moon phase and star chart still load
4. Check that credentials aren't in source code

### Test LASS Performance
1. Monitor network traffic
2. Note the response size before and after filter
3. Measure time to fetch AQI data

---

## Apply These Fixes

Run these commands to apply the quick fixes:

```bash
# Create a branch for fixes
git checkout -b fix-immediate-bugs

# After making changes
git add .
git commit -m "Fix AQI default value, secure API credentials, optimize LASS query"
git push origin fix-immediate-bugs
```

---

## Time Estimate

- **Bug #1 (AQI)**: 5 minutes
- **Bug #2 (Credentials)**: 15 minutes
- **Bug #3 (LASS)**: 10 minutes (quick fix) or 30 minutes (remove entirely)
- **Bug #4 (Data Source)**: 5 minutes (quick fix) or 2 hours (full refactor)

**Total for Quick Fixes**: ~35 minutes
**Total for Proper Refactor**: ~3 hours

---

## What's Next?

After these quick fixes, consider the full refactoring plan in `REFACTORING_ANALYSIS.md` to:
- Remove API complexity
- Improve performance
- Simplify codebase
- Eliminate data discrepancies

These quick fixes will make the app work better immediately, but the full refactor will make it maintainable long-term.

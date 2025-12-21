package com.example.simpleweatherappv2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simpleweatherappv2.data.ForecastPeriod
import com.example.simpleweatherappv2.ui.theme.AccentCyan
import com.example.simpleweatherappv2.ui.theme.AccentYellow
import com.example.simpleweatherappv2.ui.theme.GlassCard
import com.example.simpleweatherappv2.ui.theme.SoftWhite
import com.example.simpleweatherappv2.ui.theme.TextSecondary
import com.example.simpleweatherappv2.ui.theme.NightBlue
import com.example.simpleweatherappv2.ui.theme.NightPurple
import com.example.simpleweatherappv2.ui.theme.DayBlue
import com.example.simpleweatherappv2.ui.theme.DayBlueDark
import androidx.compose.ui.text.style.TextAlign
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// --- Top-level Composable Functions ---

@Composable
fun TemperatureRangeBar(
    low: Int,
    high: Int,
    current: Int? = null,
    modifier: Modifier = Modifier
) {
    val totalRange = 100 // Assume 0-100 range for the bar width context or similar
    val barColor = Color.White.copy(alpha = 0.2f)
    val accentColor = Brush.horizontalGradient(listOf(AccentCyan, AccentYellow))

    Box(
        modifier = modifier
            .height(4.dp)
            .fillMaxWidth()
            .background(barColor, RoundedCornerShape(2.dp))
    ) {
        // Highlighting the range (simplified for now)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f) // Just a visual placeholder for the range
                .fillMaxHeight()
                .align(Alignment.Center)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun WeatherStatsGrid(
    humidity: String,
    wind: String,
    rainChance: String,
    precipLabel: String = "Rain",
    precipIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.WaterDrop
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WeatherStatCard(
            icon = Icons.Default.WaterDrop,
            label = "Humidity",
            value = humidity,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        WeatherStatCard(
            icon = Icons.Default.Air,
            label = "Wind",
            value = wind,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        WeatherStatCard(
            icon = precipIcon,
            label = precipLabel,
            value = rainChance,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
fun LocationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onUseGps: () -> Unit,
    onManualEntry: (String) -> Unit,
) {
    if (isVisible) {
        var cityInput by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Choose Location") },
            text = {
                Column {
                    Button(
                        onClick = {
                            onUseGps()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Current Location")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Or enter city name:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        placeholder = { Text("Enter city") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cityInput.isNotBlank()) {
                            onManualEntry(cityInput)
                            onDismiss()
                        }
                    }
                ) {
                    Text("Search")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WeatherStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AccentCyan.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SoftWhite.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SoftWhite.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun WeatherGridItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentCyan.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HourlyForecastSection(
    forecasts: List<ForecastPeriod>,
    onDetailsClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hourly Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
            androidx.compose.material3.TextButton(onClick = onDetailsClick) {
                Text("Details >", color = AccentCyan)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Card wrapping the horizontal scroll
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCard)
        ) {
            LazyRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(forecasts) { period ->
                    HourlyForecastItem(period)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Use Experimental M3 API
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(),
    onNavigateToForecast: () -> Unit = {},
    onNavigateToHourly: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var cityInput by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }
    // Pull to Refresh State
    val pullRefreshState = rememberPullToRefreshState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (uiState.isDarkTheme) {
                        listOf(NightBlue, NightPurple)
                    } else {
                        listOf(DayBlue, DayBlueDark)
                    }
                )
            )
    ) {
        // WRAPPER: Drag to Refresh
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            state = pullRefreshState,
            onRefresh = {
                viewModel.refreshWeather()
                cityInput = "" // Clear input on refresh
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // --- HEADER ---
                WeatherHeader(
                    cityName = uiState.cityName,
                    currentDate = uiState.currentDate,
                    onLocationClick = { showLocationDialog = true },
                    onSettingsClick = onNavigateToSettings
                )


                
                // --- CONTENT ---
                if (uiState.error != null) {
                    // 1. ERROR
                    Spacer(modifier = Modifier.height(32.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Oops!",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else if (uiState.isLoading && uiState.cityName == "Unknown") {
                    // 2. INITIAL LOADING ONLY (Don't hide content if just refreshing)
                    Spacer(modifier = Modifier.height(64.dp))
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(64.dp))
                } else {
                    // 3. WEATHER CONTENT
                    MainWeatherDisplay(
                        temperature = uiState.temperature,
                        condition = uiState.condition,
                        highTemp = uiState.highTemp,
                        lowTemp = uiState.lowTemp,
                        isDaytime = uiState.isDaytime
                    )

                    // Determine Precip Type
                    val isSnow = uiState.condition.contains("Snow", ignoreCase = true) || 
                                 (uiState.temperature.replace("°F", "").toIntOrNull() ?: 100) < 32

                    WeatherStatsGrid(
                        humidity = uiState.humidity,
                        wind = uiState.wind,
                        rainChance = uiState.rainChance,
                        precipLabel = if (isSnow) "Snow" else "Rain",
                        precipIcon = if (isSnow) Icons.Default.AcUnit else Icons.Default.WaterDrop
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    // 4. HOURLY FORECAST (NEW)
                    // 4. HOURLY FORECAST (NEW STYLE)
                    if (uiState.hourlyForecasts.isNotEmpty()) {
                        HourlyForecastSection(
                            forecasts = uiState.hourlyForecasts,
                            onDetailsClick = onNavigateToHourly
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }


// Daily Forecast Preview
                    if (uiState.dailyForecasts.isNotEmpty()) {
                        DailyForecastInline(
                            forecasts = uiState.dailyForecasts,
                            onDetailsClick = onNavigateToForecast
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

// Sun & Moon
                    SunMoonSection(
                        sunrise = uiState.sunrise,
                        sunset = uiState.sunset,
                        uvIndex = uiState.uvIndex,
                        moonPhase = uiState.moonPhase
                    )

                    Spacer(modifier = Modifier.height(16.dp))

// Air Quality
                    AirQualitySection(
                        aqi = uiState.aqi,
                        aqiStatus = uiState.aqiStatus,
                        pm25 = uiState.pm25,
                        pm10 = uiState.pm10,
                        ozone = uiState.ozone
                    )


                }
            }
            // Location Dialog
            LocationDialog(
                isVisible = showLocationDialog,
                onDismiss = { showLocationDialog = false },
                onUseGps = { viewModel.fetchCurrentLocation() },
                onManualEntry = { city -> viewModel.updateWeather(city) }
            )
        }
    }
}

@Composable
fun HourlyForecastItem(period: ForecastPeriod) {
        val timeLabel = try {
            val zdt = ZonedDateTime.parse(period.startTime)
            zdt.format(DateTimeFormatter.ofPattern("h a"))
        } catch (e: Exception) {
            ""
        }

        Card(
            modifier = Modifier.width(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCard)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeLabel,
                    color = SoftWhite,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))

// Icon
                val (icon, iconColor) = when {
                    period.shortForecast.contains(
                        "Sunny",
                        ignoreCase = true
                    ) || period.shortForecast.contains("Clear", ignoreCase = true) ->
                        if (period.isDaytime) Icons.Default.WbSunny to AccentYellow
                        else Icons.Default.Nightlight to Color(0xFFB0C4DE) // Moon icon with light steel blue color

                    period.shortForecast.contains(
                        "Rain",
                        ignoreCase = true
                    ) || period.shortForecast.contains(
                        "Shower",
                        ignoreCase = true
                    ) -> Icons.Default.WaterDrop to AccentCyan

                    period.shortForecast.contains(
                        "Thunder",
                        ignoreCase = true
                    ) || period.shortForecast.contains(
                        "Storm",
                        ignoreCase = true
                    ) -> Icons.Default.Thunderstorm to Color(0xFFFFD700)

                    period.shortForecast.contains(
                        "Snow",
                        ignoreCase = true
                    ) -> Icons.Default.AcUnit to Color.White

                    else -> Icons.Default.Cloud to Color.Gray
                }
                Icon(
                    imageVector = icon,
                    contentDescription = period.shortForecast,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

// Temp
                Text(
                    text = "${period.temperature.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    fun WeatherHeader(
        cityName: String,
        currentDate: String,
        onLocationClick: () -> Unit,
        onSettingsClick: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(48.dp)) // Equalizer spacer for center alignment

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onLocationClick)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = cityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = SoftWhite,
            )
        }

            androidx.compose.material3.IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = SoftWhite,
                )
            }
        }
    }

@Composable
fun MainWeatherDisplay(
    temperature: String,
    condition: String,
    highTemp: String,
    lowTemp: String,
    isDaytime: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Weather Icon
        val (weatherIcon, iconColor) = when {
            condition.contains("Sunny", ignoreCase = true) || 
            condition.contains("Clear", ignoreCase = true) ->
                if (isDaytime) Icons.Default.WbSunny to AccentYellow
                else Icons.Default.Nightlight to Color(0xFFB0C4DE)
            
            condition.contains("Rain", ignoreCase = true) || 
            condition.contains("Shower", ignoreCase = true) ->
                Icons.Default.WaterDrop to AccentCyan
            
            condition.contains("Thunder", ignoreCase = true) || 
            condition.contains("Storm", ignoreCase = true) ->
                Icons.Default.Thunderstorm to Color(0xFFFFD700)
            
            condition.contains("Snow", ignoreCase = true) ->
                Icons.Default.AcUnit to Color.White
            
            else -> Icons.Default.Cloud to Color.Gray
        }
        
        Icon(
            imageVector = weatherIcon,
            contentDescription = condition,
            tint = iconColor,
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Temperature
        Text(
            text = temperature,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Condition (Moved below Temp)
        Text(
            text = condition,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SoftWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // High/Low Temperature
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "H: $highTemp",
                style = MaterialTheme.typography.titleMedium,
                color = SoftWhite
            )
            Text(
                text = "L: $lowTemp",
                style = MaterialTheme.typography.titleMedium,
                color = SoftWhite
            )
        }
    }
}

@Composable
fun DailyForecastInline(
    forecasts: List<ForecastPeriod>,
    onDetailsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
            androidx.compose.material3.TextButton(onClick = onDetailsClick) {
                Text("Details >", color = AccentCyan)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show first 7 days (daytime periods only)
        val daytimePeriods = forecasts.filter { it.isDaytime }.take(7)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                daytimePeriods.forEach { period ->
                    DailyForecastInlineItem(period)
                    if (period != daytimePeriods.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastInlineItem(period: ForecastPeriod) {
    val dayLabel = period.name // "Monday", "Tuesday", etc.
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Name
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = SoftWhite,
            modifier = Modifier.weight(1f)
        )
        
        // Weather Icon
        val (icon, iconColor) = when {
            period.shortForecast.contains("Sunny", ignoreCase = true) || 
            period.shortForecast.contains("Clear", ignoreCase = true) ->
                Icons.Default.WbSunny to AccentYellow
            
            period.shortForecast.contains("Rain", ignoreCase = true) ->
                Icons.Default.WaterDrop to AccentCyan
            
            period.shortForecast.contains("Thunder", ignoreCase = true) ->
                Icons.Default.Thunderstorm to Color(0xFFFFD700)
            
            period.shortForecast.contains("Snow", ignoreCase = true) ->
                Icons.Default.AcUnit to Color.White
            
            else -> Icons.Default.Cloud to Color.Gray
        }
        
        Icon(
            imageVector = icon,
            contentDescription = period.shortForecast,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))

        // Range Bar (Placeholder)
        TemperatureRangeBar(
            low = 0, // Placeholder
            high = 100, // Placeholder
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        )
        
        // Temperature
        Text(
            text = "${period.temperature.toInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SunMoonSection(
    sunrise: String,
    sunset: String,
    uvIndex: String,
    moonPhase: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sun & Moon",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SoftWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sun Card
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Sun",
                        tint = AccentYellow,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sunrise", style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                    Text(sunrise, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sunset", style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                    Text(sunset, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("UV Index", style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                    Text(uvIndex, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            // Moon Card
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = "Moon",
                        tint = Color(0xFFB0C4DE),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Moon Phase", style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                    Text(moonPhase, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun AirQualitySection(
    aqi: String,
    aqiStatus: String,
    pm25: String,
    pm10: String,
    ozone: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Air Quality",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SoftWhite
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // AQI Value and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("AQI", style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                        Text(aqi, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                    Text(aqiStatus, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pollutant Bars
                AirQualityBar("PM2.5", pm25)
                Spacer(modifier = Modifier.height(8.dp))
                AirQualityBar("PM10", pm10)
                Spacer(modifier = Modifier.height(8.dp))
                AirQualityBar("Ozone", ozone)
            }
        }
    }
}

@Composable
fun AirQualityBar(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SoftWhite,
            modifier = Modifier.width(60.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

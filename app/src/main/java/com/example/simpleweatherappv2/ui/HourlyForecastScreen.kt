package com.example.simpleweatherappv2.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Air
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HourlyForecastScreen(
    viewModel: WeatherViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(GlassCard, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SoftWhite
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- LIST ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.hourlyForecasts) { period ->
                    HourlyForecastDetailedItem(period)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastDetailedItem(period: ForecastPeriod) {
    val timeLabel = try {
        val zdt = ZonedDateTime.parse(period.startTime)
        zdt.format(DateTimeFormatter.ofPattern("h a"))
    } catch (e: Exception) {
        ""
    }

    val dateLabel = try {
        val zdt = ZonedDateTime.parse(period.startTime)
        zdt.format(DateTimeFormatter.ofPattern("MMM d"))
    } catch (e: Exception) {
        ""
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassCard),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Time & Date
                Column {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite
                    )
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Middle: Icon & Condition
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                ) {
                    val (icon, iconColor) = when {
                        period.shortForecast.contains("Sunny", ignoreCase = true) ||
                        period.shortForecast.contains("Clear", ignoreCase = true) ->
                            if (period.isDaytime) Icons.Default.WbSunny to AccentYellow
                            else Icons.Default.Nightlight to Color(0xFFB0C4DE)

                        period.shortForecast.contains("Rain", ignoreCase = true) ||
                        period.shortForecast.contains("Shower", ignoreCase = true) ->
                            Icons.Default.WaterDrop to AccentCyan

                        period.shortForecast.contains("Thunder", ignoreCase = true) ||
                        period.shortForecast.contains("Storm", ignoreCase = true) ->
                            Icons.Default.Thunderstorm to Color(0xFFFFD700)

                        period.shortForecast.contains("Snow", ignoreCase = true) ->
                            Icons.Default.AcUnit to Color.White

                        else -> Icons.Default.Cloud to Color.Gray
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = period.shortForecast,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = period.shortForecast,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftWhite,
                        maxLines = 1
                    )
                }

                // Right: Temp & Arrow
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${period.temperature.toInt()}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SoftWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 2-Column Grid for Metrics
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WeatherGridItem(Modifier.weight(1f), Icons.Default.Air, "Wind", period.windSpeed)
                            WeatherGridItem(Modifier.weight(1f), Icons.Default.Thermostat, "Feels Like", "${period.feelsLike?.toInt() ?: "--"}°")
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WeatherGridItem(Modifier.weight(1f), Icons.Default.WaterDrop, "Humidity", "${period.relativeHumidity?.value?.toInt() ?: 0}%")
                            WeatherGridItem(Modifier.weight(1f), Icons.Default.Cloud, "Clouds", "${period.clouds ?: 0}%")
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WeatherGridItem(Modifier.weight(1f), Icons.Default.WbSunny, "UV Index", "${period.uvIndex?.toInt() ?: 0}")
                            WeatherGridItem(Modifier.weight(1f), Icons.Default.Air, "Gust", period.windGust ?: "--")
                        }
                    }
                    
                    if (period.detailedForecast.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = period.detailedForecast,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                        )
                    }
                }
            }
        }
    }
}

// Re-using WeatherDetailItem from WeatherScreen.kt

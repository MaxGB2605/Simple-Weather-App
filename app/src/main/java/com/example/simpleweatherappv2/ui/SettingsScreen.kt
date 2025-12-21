package com.example.simpleweatherappv2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simpleweatherappv2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: WeatherViewModel // passed for future integration
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Derived state from ViewModel
    val isDarkTheme = uiState.isDarkTheme
    val tempUnit = uiState.tempUnit
    val speedUnit = uiState.speedUnit
    val dataSource = uiState.dataSource
    
    // Favorites from ViewModel
    val favorites = uiState.favorites

    // Theme Logic
    val backgroundColorStart = if (isDarkTheme) NightBlue else DayBlue
    val backgroundColorEnd = if (isDarkTheme) NightPurple else DayBlueDark
    val contentColor = if (isDarkTheme) Color.White else TextDark
    val mutedColor = if (isDarkTheme) TextMuted else Color.Gray
    val cardColor = if (isDarkTheme) Color(0xFF2C3E50).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f) // Lighter cards for light mode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(backgroundColorStart, backgroundColorEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- THEME ---
            SettingsSectionCard(containerColor = cardColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Theme", style = MaterialTheme.typography.titleMedium, color = contentColor)
                    }
                    
                    // Toggle
                    Row(
                        modifier = Modifier
                            .background(contentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        SettingsToggleOption("Light", !isDarkTheme, contentColor) { viewModel.toggleTheme(false) }
                        SettingsToggleOption("Dark", isDarkTheme, contentColor) { viewModel.toggleTheme(true) }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // --- UNITS ---
            Text("UNITS", style = MaterialTheme.typography.labelMedium, color = mutedColor)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionCard(containerColor = cardColor) {
                // Temp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temperature", style = MaterialTheme.typography.bodyLarge, color = contentColor)
                    Row(
                        modifier = Modifier
                            .background(contentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        SettingsToggleOption("°F", tempUnit == "°F", contentColor) { viewModel.setTempUnit("°F") }
                        SettingsToggleOption("°C", tempUnit == "°C", contentColor) { viewModel.setTempUnit("°C") }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Wind
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wind Speed", style = MaterialTheme.typography.bodyLarge, color = contentColor)
                    Row(
                        modifier = Modifier
                            .background(contentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        SettingsToggleOption("mph", speedUnit == "mph", contentColor) { viewModel.setSpeedUnit("mph") }
                        SettingsToggleOption("km/h", speedUnit == "km/h", contentColor) { viewModel.setSpeedUnit("km/h") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- DATA SOURCE ---
            Text("DATA SOURCE", style = MaterialTheme.typography.labelMedium, color = mutedColor)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionCard(containerColor = cardColor) {
                Column {
                    Text("Weather API", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = contentColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = dataSource == "WeatherAPI",
                                    onClick = { viewModel.setDataSource("WeatherAPI") },
                                    colors = RadioButtonDefaults.colors(selectedColor = contentColor, unselectedColor = mutedColor)
                                )
                                Text("WeatherAPI.com (Recommended)", color = contentColor)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = dataSource == "NWS",
                                    onClick = { viewModel.setDataSource("NWS") },
                                    colors = RadioButtonDefaults.colors(selectedColor = contentColor, unselectedColor = mutedColor)
                                )
                                Text("National Weather Service", color = contentColor)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- FAVORITES ---
            Text("FAVORITE LOCATIONS", style = MaterialTheme.typography.labelMedium, color = mutedColor)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionCard(containerColor = cardColor) {
                Column {
                    favorites.forEach { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = mutedColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(location, color = contentColor)
                            }
                            IconButton(
                                onClick = { viewModel.removeFavorite(location) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusDanger, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (location != favorites.last()) {
                            Divider(color = contentColor.copy(alpha = 0.1f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* TODO: Add Location Logic */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Location")
                    }
                }
            }
        }
    }
}

// Re-using AccentBlue for the button for consistency if it matches "OpenWeather" style blue
val AccentBlue = Color(0xFF448AFF)

@Composable
fun SettingsSectionCard(
    containerColor: Color = Color(0xFF2C3E50).copy(alpha = 0.6f),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsToggleOption(
    text: String,
    isSelected: Boolean,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (isSelected) AccentBlue else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else textColor.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

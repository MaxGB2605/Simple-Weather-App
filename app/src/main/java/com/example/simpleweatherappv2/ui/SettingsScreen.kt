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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import com.example.simpleweatherappv2.ui.theme.DayBlue
import com.example.simpleweatherappv2.ui.theme.DayBlueDark
import com.example.simpleweatherappv2.ui.theme.NightBlue
import com.example.simpleweatherappv2.ui.theme.NightPurple
import com.example.simpleweatherappv2.ui.theme.StatusDanger
import com.example.simpleweatherappv2.ui.theme.TextDark
import com.example.simpleweatherappv2.ui.theme.TextMuted

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
    
    // Favorites from ViewModel
    val favorites = uiState.favorites

    
    var showAddFavoriteDialog by remember { mutableStateOf(false) }

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
            
            // --- FAVORITES ---
            Text("FAVORITE LOCATIONS", style = MaterialTheme.typography.labelMedium, color = mutedColor)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsSectionCard(containerColor = cardColor) {
                Column {
                    favorites.forEach { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.updateWeather(location)
                                    onBack()
                                }
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
                            HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showAddFavoriteDialog = true },
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

        // Add Favorite Dialog
        if (showAddFavoriteDialog) {
            var locationInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddFavoriteDialog = false },
                title = { Text("Add Favorite Location") },
                text = {
                    Column {
                        Text("Enter the name of the city you'd like to add to your favorites.")
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            placeholder = { Text("e.g. Los Angeles, CA") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (locationInput.isNotBlank()) {
                                viewModel.addFavorite(locationInput)
                                showAddFavoriteDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFavoriteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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

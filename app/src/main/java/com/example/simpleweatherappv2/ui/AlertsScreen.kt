package com.example.simpleweatherappv2.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simpleweatherappv2.data.WeatherAlert
import com.example.simpleweatherappv2.ui.theme.*

@Composable
fun AlertsScreen(
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
                    text = "Weather Alerts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.alerts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active weather alerts",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SoftWhite.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.alerts) { alert ->
                        AlertDetailCard(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertDetailCard(alert: WeatherAlert) {
    var expanded by remember { mutableStateOf(true) } // Default expanded in separate screen
    val cardColor = when (alert.severity?.lowercase()) {
        "extreme", "severe" -> Color(0xFFFF5252).copy(alpha = 0.2f)
        "moderate" -> Color(0xFFFFD740).copy(alpha = 0.2f)
        else -> GlassCard
    }
    val borderColor = when (alert.severity?.lowercase()) {
        "extreme", "severe" -> Color(0xFFFF5252).copy(alpha = 0.5f)
        "moderate" -> Color(0xFFFFD740).copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (alert.severity?.lowercase() == "moderate") Color(0xFFFFD740) else Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.event ?: "Weather Alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Show Less" else "Show More",
                    tint = SoftWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = alert.headline,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftWhite,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!alert.desc.isNullOrBlank()) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = alert.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftWhite,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (!alert.instruction.isNullOrBlank()) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = alert.instruction,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftWhite,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                if (!alert.areas.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Areas Affected",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = alert.areas,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftWhite,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                if (!alert.expires.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Expires: ${alert.expires}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFD740)
                    )
                }
            }
        }
    }
}

package com.example.simpleweatherappv2

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.simpleweatherappv2.ui.ForecastScreen
import com.example.simpleweatherappv2.ui.WeatherScreen
import com.example.simpleweatherappv2.ui.WeatherViewModel
import com.example.simpleweatherappv2.ui.theme.SimpleWeatherAppV2Theme

// Define our screens
enum class AppScreen {
    Today,
    Forecast
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleWeatherAppV2Theme {
                val viewModel: WeatherViewModel = viewModel()
                val navController = rememberNavController()

                // Permission Logic
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        viewModel.fetchCurrentLocation()
                    }
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // NAVIGATION HOST
                    NavHost(
                        navController = navController,
                        startDestination = AppScreen.Today.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Screen 1: Today
                        composable(AppScreen.Today.name) {
                            WeatherScreen(
                                viewModel = viewModel,
                                onNavigateToForecast = { navController.navigate(AppScreen.Forecast.name) }
                            )
                        }

                        // Screen 2: 7-Day Forecast
                        composable(AppScreen.Forecast.name) {
                            ForecastScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
# Weather App Complete Redesign

Comprehensive redesign of SimpleWeatherAppV2 to match provided Figma mockups.

## Design Mockups

````carousel
![Main Screen](C:\Users\Max-WorkPC\.gemini\antigravity\brain\d05b2bca-555b-4f80-8989-e43780df0b12\mockup_main.png)
<!-- slide -->
![24-Hour Forecast](C:\Users\Max-WorkPC\.gemini\antigravity\brain\d05b2bca-555b-4f80-8989-e43780df0b12\mockup_hourly.png)
<!-- slide -->
![7-Day Forecast](C:\Users\Max-WorkPC\.gemini\antigravity\brain\d05b2bca-555b-4f80-8989-e43780df0b12\mockup_7day.png)
<!-- slide -->
![Settings Screen](C:\Users\Max-WorkPC\.gemini\antigravity\brain\d05b2bca-555b-4f80-8989-e43780df0b12\mockup_settings.png)
````

---

## Key Decisions

| Decision | Resolution |
|----------|------------|
| Sun/Moon/AQI Data | Use placeholder values; integrate real APIs later |
| Search Bar | Remove; use location pin icon to show location selection dialog (GPS or manual) |
| Location Management | Settings screen has "Favorite Locations" with add/delete |
| Data Source | Settings allows API selection (OpenWeather, etc.) |
| Units | Settings allows °F/°C and mph/km/h toggle |

---

## Proposed Changes

### Theme & Colors

#### [MODIFY] [Color.kt](file:///e:/SDET%20Automation/Projects/KotlinProjects/SimpleWeatherAppV2/app/src/main/java/com/example/simpleweatherappv2/ui/theme/Color.kt)

Replace purple palette with blue gradient:
```kotlin
val WeatherBlue = Color(0xFF4A90E2)
val WeatherBlueDark = Color(0xFF2A5298)
val GlassCard = Color(0x33FFFFFF)
val GlassCardLight = Color(0x66FFFFFF)
val AccentOrange = Color(0xFFFF6B35)
```

---

### Data Layer

#### [MODIFY] [WeatherUiState.kt](file:///e:/SDET%20Automation/Projects/KotlinProjects/SimpleWeatherAppV2/app/src/main/java/com/example/simpleweatherappv2/ui/WeatherUiState.kt)

Add fields: `highTemp`, `lowTemp`, `currentDate`, `sunrise`, `sunset`, `uvIndex`, `moonPhase`, `aqi`, pollutant values

#### [MODIFY] [WeatherViewModel.kt](file:///e:/SDET%20Automation/Projects/KotlinProjects/SimpleWeatherAppV2/app/src/main/java/com/example/simpleweatherappv2/ui/WeatherViewModel.kt)

- Extract high/low from daily forecast
- Format current date
- Add placeholder values for Sun/Moon/AQI

---

### Screen 1: Main Screen

#### [MODIFY] [WeatherScreen.kt](file:///e:/SDET%20Automation/Projects/KotlinProjects/SimpleWeatherAppV2/app/src/main/java/com/example/simpleweatherappv2/ui/WeatherScreen.kt)

**New Composables:**
- `WeatherHeader` - Location pin (clickable dialog), settings gear, date
- `MainWeatherDisplay` - Large icon, temp, high/low, condition, feels like
- `WeatherStatsRow` - 3 glass cards (Humidity, Wind, Precipitation)
- `HourlyForecastSection` - Horizontal scroll with "More >" link
- `DailyForecastInline` - 7 days with temp bars, "Details >" link
- `SunMoonSection` - Two side-by-side glass cards
- `AirQualitySection` - AQI value, status, pollutant bars
- `LocationDialog` - Choose "Use GPS" or "Enter Location"

---

### Screen 2: 24-Hour Forecast

#### [MODIFY] [ForecastScreen.kt](file:///e:/SDET%20Automation/Projects/KotlinProjects/SimpleWeatherAppV2/app/src/main/java/com/example/simpleweatherappv2/ui/ForecastScreen.kt) → Rename/Split

**New file:** `HourlyForecastScreen.kt`

Each card shows: Time, Date, Condition, Precipitation%, Wind, Humidity, Feels Like, Temperature

---

### Screen 3: 7-Day Forecast

**New file:** `DailyForecastScreen.kt`

Each card shows: Day, Date, Condition, Precipitation%, Humidity, Wind, UV, Sunrise, Sunset, High/Low temps

---

### Screen 4: Settings

**New file:** `SettingsScreen.kt`

**Sections:**
- Appearance: Theme toggle (Light/Dark)
- Units: Temperature (°F/°C), Wind Speed (mph/km/h)
- Data Source: API dropdown (OpenWeather, NWS, etc.)
- Favorite Locations: List with add/delete

**New file:** `SettingsViewModel.kt` - Manages user preferences (SharedPreferences or DataStore)

---

### Navigation

#### [MODIFY] [MainActivity.kt](file:///e:/SDET%20Automation/Projects/KotlinProjects/SimpleWeatherAppV2/app/src/main/java/com/example/simpleweatherappv2/MainActivity.kt)

Update `AppScreen` enum and NavHost to include: `Today`, `HourlyDetail`, `DailyDetail`, `Settings`

---

## File Summary

| File | Action | Purpose |
|------|--------|---------|
| `Color.kt` | MODIFY | New blue color palette |
| `WeatherUiState.kt` | MODIFY | New data fields |
| `WeatherViewModel.kt` | MODIFY | Populate new fields |
| `WeatherScreen.kt` | MODIFY | Complete main screen redesign |
| `HourlyForecastScreen.kt` | NEW | 24-hour detail screen |
| `DailyForecastScreen.kt` | NEW | 7-day detail screen |
| `SettingsScreen.kt` | NEW | Settings with preferences |
| `SettingsViewModel.kt` | NEW | Preferences management |
| `MainActivity.kt` | MODIFY | Navigation updates |

---

## Verification Plan

### Manual Testing

1. Build and run: `.\gradlew installDebug`
2. Visual comparison against mockups for each screen
3. Interaction testing: location dialog, navigation, pull-to-refresh

---

## Implementation Order

1. **Phase 1**: Update Colors → Start with Main Screen
2. **Phase 2**: Update Data Models & ViewModel
3. **Phase 3**: Redesign Main Screen UI
4. **Phase 4**: Create 24-Hour Forecast Screen
5. **Phase 5**: Create 7-Day Forecast Screen
6. **Phase 6**: Create Settings Screen
7. **Phase 7**: Update Navigation
8. **Phase 8**: Testing & Polish

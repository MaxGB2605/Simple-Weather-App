# Full Refactoring Checklist

## Phase 1: Backup & Preparation ✅
- [x] Create analysis documents
- [ ] Create git branch
- [ ] Backup current working state

## Phase 2: Remove NWS API
- [ ] Update RetrofitInstance.kt - Remove NWS client
- [ ] Delete NwsApi.kt
- [ ] Keep ForecastPeriod model (useful), delete rest of NwsModels.kt
- [ ] Update WeatherRepository.kt - Remove NWS methods
- [ ] Update WeatherViewModel.kt - Remove NWS code paths

## Phase 3: Remove LASS AQI API
- [ ] Update RetrofitInstance.kt - Remove LASS client
- [ ] Delete AqiApiService.kt
- [ ] Delete AqiModels.kt
- [ ] Update WeatherRepository.kt - Remove LASS methods
- [ ] Update WeatherViewModel.kt - Remove LASS integration

## Phase 4: Simplify WeatherViewModel
- [ ] Remove weatherProvider setting
- [ ] Remove dual code paths
- [ ] Fix AQI null handling
- [ ] Simplify to single data flow
- [ ] Keep unit conversion logic

## Phase 5: Update UI
- [ ] Remove data source setting from SettingsScreen
- [ ] Update any error messages
- [ ] Verify all features still work

## Phase 6: Clean Up
- [ ] Remove unused imports
- [ ] Update comments
- [ ] Format code
- [ ] Test thoroughly

## Phase 7: Security Fix
- [ ] Move Astronomy API credentials to local.properties
- [ ] Update build.gradle.kts
- [ ] Update RetrofitInstance.kt to use BuildConfig

## APIs After Refactoring
✅ WeatherAPI.com - All weather data + AQI
✅ Astronomy API - Moon phase & star charts

## Expected Results
- Fewer API calls (3 instead of 7-8)
- Faster loading
- No data discrepancies
- Cleaner codebase
- Accurate AQI data

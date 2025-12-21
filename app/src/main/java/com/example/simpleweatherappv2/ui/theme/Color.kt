package com.example.simpleweatherappv2.ui.theme

import androidx.compose.ui.graphics.Color

// --- Theme Backgrounds ---

// Night / Dark Mode Gradient
val NightBlue = Color(0xFF1A1B4B) 
val NightPurple = Color(0xFF2E1C59)

// Day / Light Mode Gradient (Mockup primary colors)
val DayBlue = Color(0xFF4A90E2)        // Lighter blue at top
val DayBlueDark = Color(0xFF2A5298)    // Darker blue at bottom
val DayBlueMid = Color(0xFF3B7BC8)     // Mid-tone for accents

// --- Elements ---

// Glass Card Effects
val GlassCard = Color(0x33FFFFFF)          // 20% White - for cards
val GlassCardLight = Color(0x4DFFFFFF)     // 30% White - lighter cards
val GlassCardDark = Color(0x1AFFFFFF)      // 10% White - subtle cards

// Text Colors
val SoftWhite = Color(0xFFF0F0F5)          // Primary text
val TextSecondary = Color(0xCCFFFFFF)      // 80% White - secondary text
val TextMuted = Color(0x99FFFFFF)          // 60% White - muted text
val TextDark = Color(0xFF2D3436)           // Dark text for light backgrounds (Settings)

// Accent Colors
val AccentOrange = Color(0xFFFF6B35)       // For warnings/high UV
val AccentGreen = Color(0xFF4CAF50)        // For good AQI
val AccentYellow = Color(0xFFFFD700)       // Sun/golden icon
val AccentCyan = Color(0xFF00E5FF)         // Highlights/links

// Status Colors
val StatusGood = Color(0xFF4CAF50)         // Green - Good AQI
val StatusModerate = Color(0xFFFFEB3B)     // Yellow - Moderate
val StatusWarning = Color(0xFFFF9800)      // Orange - Warning
val StatusDanger = Color(0xFFFF5252)       // Red - Danger

// Legacy / Utility colors
val MidnightBlue = NightBlue
val DeepPurple = NightPurple
val NeonCyan = Color(0xFF00E5FF)
val GoldenSun = Color(0xFFFFD700)
val ErrorRed = Color(0xFFFF5252)
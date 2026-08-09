package com.piepoint.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Colors
val OrangeAccent = Color(0xFFFF6B35)
val OrangeLight = Color(0xFFFF8C5E)
val OrangeDark = Color(0xFFD94F1E)
val YellowAccent = Color(0xFFFFC107)

// Neutral Colors
val BackgroundWhite = Color(0xFFFAFAFA)
val SurfaceWhite = Color(0xFFFFFFFF)
val CardBackground = Color(0xFFF5F5F5)

// Glassmorphism Colors
val GlassWhite = Color(0x66FFFFFF)
val GlassWhiteBorder = Color(0x33FFFFFF)
val GlassDark = Color(0x1A000000)

// Gradients
val PremiumGradient = Brush.horizontalGradient(listOf(OrangeAccent, Color(0xFFFF8C5E)))
val SurfaceGradient = Brush.verticalGradient(listOf(SurfaceWhite, Color(0xFFFDFDFD)))
val GlassGradient = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.1f)))

// Text Colors
val TextPrimary = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6B7280)
val TextHint = Color(0xFFADB5BD)

// Semantic Colors
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFE53935)
val WarningAmber = Color(0xFFFFC107)

// Dark Mode Colors
val DarkSurface = Color(0xFF1E1E2E)
val DarkBackground = Color(0xFF121220)
val DarkCard = Color(0xFF2A2A3E)

package seamain.org.typhoonEye.ui.theme

import androidx.compose.ui.graphics.Color

// Brand — deep ocean / storm (seed for tonal palette)
val OceanDeep = Color(0xFF0B1F33)
val OceanMid = Color(0xFF0E3A5C)
val Ocean = Color(0xFF1565A8)
val OceanLight = Color(0xFF4FC3F7)
val SeaFoam = Color(0xFF80DEEA)
val StormNavy = Color(0xFF102A43)

// Surfaces (M3 tonal containers)
val CloudWhite = Color(0xFFF7FAFC)
val Mist = Color(0xFFE8F1F8)
val SurfaceDark = Color(0xFF0F1720)
val CardDark = Color(0xFF162231)
val SurfaceContainerLight = Color(0xFFEEF4F9)
val SurfaceContainerHighLight = Color(0xFFE4EDF5)
val SurfaceContainerHighestLight = Color(0xFFDAE6F0)
val SurfaceContainerDark = Color(0xFF1A2530)
val SurfaceContainerHighDark = Color(0xFF243140)
val SurfaceContainerHighestDark = Color(0xFF2E3D4E)

// Intensity scale (meteorological — semantic, not brand)
val IntensityTd = Color(0xFF64B5F6)      // 热带低压
val IntensityTs = Color(0xFF26A69A)      // 热带风暴
val IntensitySts = Color(0xFFFFB74D)     // 强热带风暴
val IntensityTy = Color(0xFFFF8A65)      // 台风
val IntensitySty = Color(0xFFEF5350)     // 强台风
val IntensitySuper = Color(0xFFB71C1C)   // 超强台风

// Semantic / tertiary (forecast accent — ocean teal, not purple)
val SuccessGreen = Color(0xFF2E7D32)
val WarningAmber = Color(0xFFF9A825)
val ErrorRed = Color(0xFFC62828)
val ForecastTeal = Color(0xFF00838F)
val ForecastTealLight = Color(0xFF4DD0E1)

/** @deprecated Use ForecastTeal; kept for call-site compatibility during migration */
val ForecastPurple = ForecastTeal

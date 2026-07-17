package seamain.org.typhoonEye.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OceanLight,
    onPrimary = OceanDeep,
    primaryContainer = OceanMid,
    onPrimaryContainer = SeaFoam,
    secondary = SeaFoam,
    onSecondary = OceanDeep,
    secondaryContainer = Color(0xFF1A3A4A),
    onSecondaryContainer = SeaFoam,
    tertiary = ForecastPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3A2A55),
    onTertiaryContainer = Color(0xFFE1D4FF),
    background = SurfaceDark,
    onBackground = Color(0xFFE8EEF4),
    surface = CardDark,
    onSurface = Color(0xFFE8EEF4),
    surfaceVariant = Color(0xFF1E2D3D),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF546E7A),
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Ocean,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E8F8),
    onPrimaryContainer = OceanDeep,
    secondary = OceanMid,
    onSecondary = Color.White,
    secondaryContainer = Mist,
    onSecondaryContainer = StormNavy,
    tertiary = ForecastPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE7F6),
    onTertiaryContainer = Color(0xFF311B92),
    background = CloudWhite,
    onBackground = StormNavy,
    surface = Color.White,
    onSurface = StormNavy,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF455A64),
    outline = Color(0xFF90A4AE),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun TyphoonEyeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Brand palette only — skip dynamic system colors for consistent weather look
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

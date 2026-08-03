package seamain.org.typhoonEye.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OceanLight,
    onPrimary = OceanDeep,
    primaryContainer = OceanMid,
    onPrimaryContainer = SeaFoam,
    secondary = SeaFoam,
    onSecondary = OceanDeep,
    secondaryContainer = Color(0xFF1A3A4A),
    onSecondaryContainer = SeaFoam,
    tertiary = ForecastTealLight,
    onTertiary = OceanDeep,
    tertiaryContainer = Color(0xFF00363A),
    onTertiaryContainer = Color(0xFFB2EBF2),
    background = SurfaceDark,
    onBackground = Color(0xFFE8EEF4),
    surface = CardDark,
    onSurface = Color(0xFFE8EEF4),
    surfaceVariant = Color(0xFF1E2D3D),
    onSurfaceVariant = Color(0xFFB0BEC5),
    surfaceContainerLowest = Color(0xFF0A1018),
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = Color(0xFF546E7A),
    outlineVariant = Color(0xFF37474F),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8A1C1C),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE8EEF4),
    inverseOnSurface = OceanDeep,
    inversePrimary = Ocean
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
    tertiary = ForecastTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF00363A),
    background = CloudWhite,
    onBackground = StormNavy,
    surface = Color.White,
    onSurface = StormNavy,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF455A64),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F7FB),
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = Color(0xFF90A4AE),
    outlineVariant = Color(0xFFCFD8DC),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    inverseSurface = StormNavy,
    inverseOnSurface = CloudWhite,
    inversePrimary = OceanLight
)

@Composable
fun TyphoonEyeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You: follow the user's wallpaper palette on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

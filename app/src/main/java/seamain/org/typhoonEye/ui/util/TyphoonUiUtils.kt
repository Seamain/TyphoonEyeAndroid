package seamain.org.typhoonEye.ui.util

import androidx.compose.ui.graphics.Color
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.data.model.TyphoonPoint
import seamain.org.typhoonEye.ui.theme.IntensitySts
import seamain.org.typhoonEye.ui.theme.IntensitySty
import seamain.org.typhoonEye.ui.theme.IntensitySuper
import seamain.org.typhoonEye.ui.theme.IntensityTd
import seamain.org.typhoonEye.ui.theme.IntensityTs
import seamain.org.typhoonEye.ui.theme.IntensityTy

enum class IntensityLevel(val label: String, val shortLabel: String, val rank: Int) {
    TD("热带低压", "TD", 1),
    TS("热带风暴", "TS", 2),
    STS("强热带风暴", "STS", 3),
    TY("台风", "TY", 4),
    STY("强台风", "STY", 5),
    SUPER("超强台风", "SuperTY", 6),
    UNKNOWN("未知", "?", 0)
}

fun resolveIntensity(strong: String, power: String = ""): IntensityLevel {
    val text = "$strong $power".lowercase()
    return when {
        text.contains("超强") || text.contains("superty") -> IntensityLevel.SUPER
        text.contains("强台风") || text.contains("sty") -> IntensityLevel.STY
        text.contains("强热带风暴") || text.contains("sts") -> IntensityLevel.STS
        text.contains("台风") || text == "ty" || text.contains(" ty") -> IntensityLevel.TY
        text.contains("热带风暴") || text.contains("ts") -> IntensityLevel.TS
        text.contains("热带低压") || text.contains("td") -> IntensityLevel.TD
        else -> {
            val p = power.toIntOrNull()
            when {
                p == null -> IntensityLevel.UNKNOWN
                p >= 16 -> IntensityLevel.SUPER
                p >= 14 -> IntensityLevel.STY
                p >= 12 -> IntensityLevel.TY
                p >= 10 -> IntensityLevel.STS
                p >= 8 -> IntensityLevel.TS
                p > 0 -> IntensityLevel.TD
                else -> IntensityLevel.UNKNOWN
            }
        }
    }
}

fun intensityColor(level: IntensityLevel): Color = when (level) {
    IntensityLevel.TD -> IntensityTd
    IntensityLevel.TS -> IntensityTs
    IntensityLevel.STS -> IntensitySts
    IntensityLevel.TY -> IntensityTy
    IntensityLevel.STY -> IntensitySty
    IntensityLevel.SUPER -> IntensitySuper
    IntensityLevel.UNKNOWN -> Color(0xFF78909C)
}

fun Typhoon.currentIntensity(): IntensityLevel {
    val last = points.lastOrNull()
    return resolveIntensity(strong.ifBlank { last?.strong.orEmpty() }, last?.power.orEmpty())
}

fun Typhoon.latestPoint(): TyphoonPoint? = points.lastOrNull()

fun TyphoonPoint.displayIntensity(): String =
    strong.ifBlank { if (power.isNotBlank()) "${power}级" else "—" }

fun TyphoonPoint.moveLabel(): String =
    listOf(moveDirection, moveSpeed.takeIf { it.isNotBlank() }?.let {
        if (it.contains("km") || it.contains("公里")) it else "${it} km/h"
    }).filter { !it.isNullOrBlank() }.joinToString(" · ").ifBlank { "—" }

fun formatCoordinate(lat: Double, lng: Double): String {
    val latDir = if (lat >= 0) "N" else "S"
    val lngDir = if (lng >= 0) "E" else "W"
    return String.format("%.1f°%s, %.1f°%s", kotlin.math.abs(lat), latDir, kotlin.math.abs(lng), lngDir)
}

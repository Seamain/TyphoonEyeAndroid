package seamain.org.typhoonEye.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import seamain.org.typhoonEye.data.model.TyphoonPoint
import seamain.org.typhoonEye.ui.util.intensityColor
import seamain.org.typhoonEye.ui.util.resolveIntensity

@Composable
fun TrackMapCard(
    history: List<TyphoonPoint>,
    forecast: List<TyphoonPoint>,
    modifier: Modifier = Modifier
) {
    val historyColor = MaterialTheme.colorScheme.primary
    val forecastColor = MaterialTheme.colorScheme.tertiary
    val currentColor = MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "路径示意",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "历史实线 · 预报虚线 · 圆点为观测位置",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                if (history.isEmpty() && forecast.isEmpty()) {
                    Text(
                        text = "暂无路径数据",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    TrackCanvas(
                        history = history,
                        forecast = forecast,
                        historyColor = historyColor,
                        forecastColor = forecastColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(color = historyColor, label = "历史路径")
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(color = forecastColor, label = "预报路径")
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(color = currentColor, label = "当前位置")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TrackCanvas(
    history: List<TyphoonPoint>,
    forecast: List<TyphoonPoint>,
    modifier: Modifier = Modifier,
    historyColor: Color = Color(0xFF4FC3F7),
    forecastColor: Color = Color(0xFF00838F)
) {
    val all = history + forecast
    if (all.isEmpty()) return

    Canvas(modifier = modifier) {
        val pad = 12f
        val minLat = all.minOf { it.lat }
        val maxLat = all.maxOf { it.lat }
        val minLng = all.minOf { it.lng }
        val maxLng = all.maxOf { it.lng }
        val latSpan = (maxLat - minLat).coerceAtLeast(0.5)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.5)

        fun project(lat: Double, lng: Double): Offset {
            val x = pad + ((lng - minLng) / lngSpan * (size.width - pad * 2)).toFloat()
            val y = pad + ((maxLat - lat) / latSpan * (size.height - pad * 2)).toFloat()
            return Offset(x, y)
        }

        fun drawPolyline(points: List<TyphoonPoint>, color: Color, dashed: Boolean) {
            if (points.size < 2) return
            val path = Path()
            val first = project(points.first().lat, points.first().lng)
            path.moveTo(first.x, first.y)
            for (i in 1 until points.size) {
                val p = project(points[i].lat, points[i].lng)
                path.lineTo(p.x, p.y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = if (dashed) {
                        PathEffect.dashPathEffect(floatArrayOf(16f, 12f))
                    } else null
                )
            )
        }

        if (history.isNotEmpty() && forecast.isNotEmpty()) {
            val a = project(history.last().lat, history.last().lng)
            val b = project(forecast.first().lat, forecast.first().lng)
            drawLine(
                color = forecastColor.copy(alpha = 0.45f),
                start = a,
                end = b,
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        }

        drawPolyline(history, historyColor, dashed = false)
        drawPolyline(forecast, forecastColor, dashed = true)

        history.forEachIndexed { index, point ->
            val o = project(point.lat, point.lng)
            val level = resolveIntensity(point.strong, point.power)
            val c = intensityColor(level)
            val radius = if (index == history.lastIndex) 10f else 5f
            drawCircle(color = c.copy(alpha = 0.25f), radius = radius + 6f, center = o)
            drawCircle(color = c, radius = radius, center = o)
            if (index == history.lastIndex) {
                drawCircle(color = Color.White, radius = 3.5f, center = o)
            }
        }

        forecast.forEach { point ->
            val o = project(point.lat, point.lng)
            drawCircle(color = forecastColor.copy(alpha = 0.9f), radius = 4.5f, center = o)
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 1.8f, center = o)
        }
    }
}

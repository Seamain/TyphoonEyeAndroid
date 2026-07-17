package seamain.org.typhoonEye.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.ui.components.IntensityBadge
import seamain.org.typhoonEye.ui.components.MetricGrid
import seamain.org.typhoonEye.ui.components.MetricItem
import seamain.org.typhoonEye.ui.components.PointTimelineItem
import seamain.org.typhoonEye.ui.components.StatusChip
import seamain.org.typhoonEye.ui.components.TrackMapCard
import seamain.org.typhoonEye.ui.theme.Ocean
import seamain.org.typhoonEye.ui.theme.OceanDeep
import seamain.org.typhoonEye.ui.theme.OceanMid
import seamain.org.typhoonEye.ui.util.currentIntensity
import seamain.org.typhoonEye.ui.util.formatCoordinate
import seamain.org.typhoonEye.ui.util.latestPoint
import seamain.org.typhoonEye.ui.util.moveLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    typhoon: Typhoon,
    loading: Boolean,
    onBack: () -> Unit,
    onShare: (String) -> Unit,
    shareText: String,
    modifier: Modifier = Modifier
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("概况", "路径图", "历史", "预报")
    val last = typhoon.latestPoint()
    val intensity = typhoon.currentIntensity()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(typhoon.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = typhoon.englishName.ifBlank { typhoon.id },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onShare(shareText) }) {
                        Icon(Icons.Filled.Share, contentDescription = "分享概况")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                DetailHero(typhoon = typhoon, intensityLabel = intensity.label)
            }

            item {
                ScrollableTabRow(
                    selectedTabIndex = tabIndex,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (tabIndex) {
                0 -> {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            if (loading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "正在补全路径详情…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            MetricGrid(
                                items = listOf(
                                    MetricItem(
                                        label = "中心风速",
                                        value = last?.let { "${it.speed} m/s" } ?: "—",
                                        icon = Icons.Filled.Air
                                    ),
                                    MetricItem(
                                        label = "中心气压",
                                        value = last?.let { "${it.pressure} hPa" } ?: "—",
                                        icon = Icons.Filled.Speed
                                    ),
                                    MetricItem(
                                        label = "移动",
                                        value = last?.moveLabel() ?: "—",
                                        icon = Icons.Filled.Explore
                                    ),
                                    MetricItem(
                                        label = "位置",
                                        value = last?.let { formatCoordinate(it.lat, it.lng) } ?: "—",
                                        icon = Icons.Filled.Place
                                    )
                                )
                            )

                            if (typhoon.forecastText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "预报要点",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = typhoon.forecastText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            InfoRow(label = "编号", value = typhoon.id)
                            if (typhoon.startTime.isNotBlank()) {
                                InfoRow(label = "生成时间", value = typhoon.startTime)
                            }
                            if (typhoon.endTime.isNotBlank()) {
                                InfoRow(label = "最新时次", value = typhoon.endTime)
                            }
                            InfoRow(
                                label = "路径点",
                                value = "历史 ${typhoon.points.size} · 预报 ${typhoon.forecastPoints.size}"
                            )
                        }
                    }
                }

                1 -> {
                    item {
                        TrackMapCard(
                            history = typhoon.points,
                            forecast = typhoon.forecastPoints,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                2 -> {
                    item {
                        SectionHeader(
                            title = "历史路径",
                            subtitle = "共 ${typhoon.points.size} 个观测点（由新到旧）",
                            icon = Icons.Filled.Timeline
                        )
                    }
                    val history = typhoon.points.asReversed()
                    if (history.isEmpty()) {
                        item { EmptySection("暂无历史路径数据") }
                    } else {
                        itemsIndexed(
                            items = history,
                            key = { _, p -> "h-${p.time}-${p.lat}-${p.lng}" }
                        ) { index, point ->
                            PointTimelineItem(
                                point = point,
                                isForecast = false,
                                showConnector = index != history.lastIndex,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                3 -> {
                    item {
                        SectionHeader(
                            title = "预报路径",
                            subtitle = "共 ${typhoon.forecastPoints.size} 个预报点",
                            icon = Icons.Filled.Timeline
                        )
                    }
                    if (typhoon.forecastPoints.isEmpty()) {
                        item { EmptySection("暂无预报路径数据") }
                    } else {
                        itemsIndexed(
                            items = typhoon.forecastPoints,
                            key = { _, p -> "f-${p.time}-${p.lat}-${p.lng}" }
                        ) { index, point ->
                            PointTimelineItem(
                                point = point,
                                isForecast = true,
                                showConnector = index != typhoon.forecastPoints.lastIndex,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHero(typhoon: Typhoon, intensityLabel: String) {
    val intensity = typhoon.currentIntensity()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(OceanDeep, OceanMid, Ocean))
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IntensityBadge(level = intensity)
                StatusChip(active = typhoon.status == "active")
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = typhoon.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOfNotNull(
                    typhoon.englishName.takeIf { it.isNotBlank() },
                    intensityLabel.takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            if (typhoon.positionDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = typhoon.positionDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptySection(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

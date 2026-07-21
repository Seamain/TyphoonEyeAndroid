package seamain.org.typhoonEye.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.ui.components.IntensityBadge
import seamain.org.typhoonEye.ui.components.MetricGrid
import seamain.org.typhoonEye.ui.components.MetricItem
import seamain.org.typhoonEye.ui.components.PointTimelineItem
import seamain.org.typhoonEye.ui.components.StatusChip
import seamain.org.typhoonEye.ui.components.TrackMapCard
import seamain.org.typhoonEye.ui.theme.TyphoonEyeTheme
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .semantics { contentDescription = "台风详情 ${typhoon.name}" },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(typhoon.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = typhoon.englishName.ifBlank { typhoon.id },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "返回" }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { onShare(shareText) },
                        modifier = Modifier.semantics { contentDescription = "分享概况" }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
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

            if (loading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "正在补全路径详情" }
                    )
                }
            }

            item {
                SecondaryScrollableTabRow(
                    selectedTabIndex = tabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                AnimatedContent(
                    targetState = tabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "detail-tab"
                ) { index ->
                    when (index) {
                        0 -> OverviewTab(typhoon = typhoon, loading = loading, last = last)
                        1 -> TrackMapCard(
                            history = typhoon.points,
                            forecast = typhoon.forecastPoints,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        2 -> TimelineTab(
                            title = "历史路径",
                            subtitle = "共 ${typhoon.points.size} 个观测点（由新到旧）",
                            points = typhoon.points.asReversed(),
                            isForecast = false,
                            emptyText = "暂无历史路径数据"
                        )
                        else -> TimelineTab(
                            title = "预报路径",
                            subtitle = "共 ${typhoon.forecastPoints.size} 个预报点",
                            points = typhoon.forecastPoints,
                            isForecast = true,
                            emptyText = "暂无预报路径数据"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    typhoon: Typhoon,
    loading: Boolean,
    last: seamain.org.typhoonEye.data.model.TyphoonPoint?
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

@Composable
private fun TimelineTab(
    title: String,
    subtitle: String,
    points: List<seamain.org.typhoonEye.data.model.TyphoonPoint>,
    isForecast: Boolean,
    emptyText: String
) {
    Column {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            icon = Icons.Filled.Timeline
        )
        if (points.isEmpty()) {
            EmptySection(emptyText)
        } else {
            points.forEachIndexed { index, point ->
                PointTimelineItem(
                    point = point,
                    isForecast = isForecast,
                    showConnector = index != points.lastIndex,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailHero(typhoon: Typhoon, intensityLabel: String) {
    val intensity = typhoon.currentIntensity()
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(scheme.primaryContainer, scheme.secondaryContainer)
                )
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
                color = scheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOfNotNull(
                    typhoon.englishName.takeIf { it.isNotBlank() },
                    intensityLabel.takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
            if (typhoon.positionDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = typhoon.positionDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

@Preview(showBackground = true, name = "Detail")
@Composable
private fun DetailScreenPreview() {
    TyphoonEyeTheme {
        DetailScreen(
            typhoon = previewTyphoons.first().copy(
                forecastText = "将以每小时20-25公里的速度向西北方向移动，强度变化不大",
                points = previewTyphoons.first().points,
                forecastPoints = previewTyphoons.first().points
            ),
            loading = false,
            onBack = {},
            onShare = {},
            shareText = "preview"
        )
    }
}

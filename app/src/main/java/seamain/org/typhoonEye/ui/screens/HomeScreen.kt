package seamain.org.typhoonEye.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.ui.DataMode
import seamain.org.typhoonEye.ui.TyphoonUiState
import seamain.org.typhoonEye.ui.components.IntensityBadge
import seamain.org.typhoonEye.ui.components.StatusChip
import seamain.org.typhoonEye.ui.theme.Ocean
import seamain.org.typhoonEye.ui.theme.OceanDeep
import seamain.org.typhoonEye.ui.theme.OceanLight
import seamain.org.typhoonEye.ui.theme.OceanMid
import seamain.org.typhoonEye.ui.util.IntensityLevel
import seamain.org.typhoonEye.ui.util.currentIntensity
import seamain.org.typhoonEye.ui.util.formatCoordinate
import seamain.org.typhoonEye.ui.util.intensityColor
import seamain.org.typhoonEye.ui.util.latestPoint
import seamain.org.typhoonEye.ui.util.moveLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: TyphoonUiState,
    filteredTyphoons: List<Typhoon>,
    isRefreshing: Boolean,
    query: String,
    intensityFilter: IntensityLevel?,
    dataMode: DataMode,
    lastUpdated: String?,
    onQueryChange: (String) -> Unit,
    onFilterChange: (IntensityLevel?) -> Unit,
    onRefresh: () -> Unit,
    onLoadDemo: () -> Unit,
    onTyphoonClick: (Typhoon) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullState = rememberPullToRefreshState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullState
            )
    ) {
        when (uiState) {
            is TyphoonUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "正在获取台风数据…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is TyphoonUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    onRetry = onRefresh,
                    onLoadDemo = onLoadDemo
                )
            }

            is TyphoonUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        HomeHeroHeader(
                            activeCount = uiState.typhoons.count { it.status == "active" },
                            dataMode = dataMode,
                            lastUpdated = lastUpdated,
                            onRefresh = onRefresh,
                            onLoadDemo = onLoadDemo
                        )
                    }
                    item {
                        SearchAndFilters(
                            query = query,
                            intensityFilter = intensityFilter,
                            onQueryChange = onQueryChange,
                            onFilterChange = onFilterChange,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (filteredTyphoons.isEmpty()) {
                        item {
                            EmptyListState(
                                hasAny = uiState.typhoons.isNotEmpty(),
                                onLoadDemo = onLoadDemo,
                                onClearFilters = {
                                    onQueryChange("")
                                    onFilterChange(null)
                                }
                            )
                        }
                    } else {
                        items(items = filteredTyphoons, key = { it.id }) { typhoon ->
                            TyphoonListCard(
                                typhoon = typhoon,
                                onClick = { onTyphoonClick(typhoon) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        PullToRefreshDefaults.Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = isRefreshing,
            state = pullState
        )
    }
}

@Composable
private fun HomeHeroHeader(
    activeCount: Int,
    dataMode: DataMode,
    lastUpdated: String?,
    onRefresh: () -> Unit,
    onLoadDemo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(OceanDeep, OceanMid, Ocean.copy(alpha = 0.92f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Cyclone,
                            contentDescription = null,
                            tint = OceanLight,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "台风眼",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "西北太平洋台风实时监测",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
                Row {
                    IconButton(onClick = onLoadDemo) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = "演示数据",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "刷新",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(title = "活跃台风", value = "$activeCount")
                StatPill(
                    title = "数据模式",
                    value = if (dataMode == DataMode.Live) "实时" else "演示"
                )
                if (lastUpdated != null) {
                    StatPill(title = "更新时间", value = lastUpdated)
                }
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SearchAndFilters(
    query: String,
    intensityFilter: IntensityLevel?,
    onQueryChange: (String) -> Unit,
    onFilterChange: (IntensityLevel?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("搜索台风名称 / 编号") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "清除")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = intensityFilter == null,
                onClick = { onFilterChange(null) },
                label = { Text("全部") }
            )
            IntensityLevel.entries
                .filter { it != IntensityLevel.UNKNOWN }
                .forEach { level ->
                    val selected = intensityFilter == level
                    FilterChip(
                        selected = selected,
                        onClick = { onFilterChange(level) },
                        label = { Text(level.shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = intensityColor(level).copy(alpha = 0.2f),
                            selectedLabelColor = intensityColor(level)
                        )
                    )
                }
        }
    }
}

@Composable
fun TyphoonListCard(
    typhoon: Typhoon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val intensity = typhoon.currentIntensity()
    val last = typhoon.latestPoint()
    val accent = intensityColor(intensity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(148.dp)
                    .background(accent)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = typhoon.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitle = buildList {
                            if (typhoon.englishName.isNotBlank() && typhoon.englishName != typhoon.name) {
                                add(typhoon.englishName)
                            }
                            add(typhoon.id)
                        }.joinToString(" · ")
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IntensityBadge(level = intensity, compact = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusChip(active = typhoon.status == "active")
                    }
                }

                if (typhoon.positionDesc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = typhoon.positionDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (last != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniMetric(
                            icon = Icons.Filled.Air,
                            label = "风速",
                            value = "${last.speed} m/s"
                        )
                        MiniMetric(
                            icon = Icons.Filled.Speed,
                            label = "气压",
                            value = "${last.pressure}"
                        )
                        MiniMetric(
                            icon = Icons.Filled.Navigation,
                            label = "移动",
                            value = last.moveLabel().take(10)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "更新 ${last.time} · ${formatCoordinate(last.lat, last.lng)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMetric(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyListState(
    hasAny: Boolean,
    onLoadDemo: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Cyclone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasAny) "没有匹配的台风" else "当前暂无活跃台风",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hasAny) "试试调整搜索或强度筛选" else "下拉刷新，或加载演示数据预览界面",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (hasAny) {
            OutlinedButton(onClick = onClearFilters) {
                Text("清除筛选")
            }
        } else {
            Button(onClick = onLoadDemo) {
                Icon(Icons.Filled.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("加载演示数据")
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onLoadDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetry) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("重试")
            }
            OutlinedButton(onClick = onLoadDemo) {
                Text("演示数据")
            }
        }
    }
}

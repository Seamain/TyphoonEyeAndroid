package seamain.org.typhoonEye.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.data.model.TyphoonPoint
import seamain.org.typhoonEye.ui.DataMode
import seamain.org.typhoonEye.ui.TyphoonUiState
import seamain.org.typhoonEye.ui.components.IntensityBadge
import seamain.org.typhoonEye.ui.components.StatusChip
import seamain.org.typhoonEye.ui.theme.TyphoonEyeTheme
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullState = rememberPullToRefreshState()
    val activeCount = when (uiState) {
        is TyphoonUiState.Success -> uiState.typhoons.count { it.status == "active" }
        else -> 0
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("台风眼")
                        AnimatedVisibility(visible = scrollBehavior.state.collapsedFraction < 0.5f) {
                            Text(
                                text = "西北太平洋台风实时监测",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = onLoadDemo,
                        modifier = Modifier.semantics { contentDescription = "演示数据" }
                    ) {
                        Icon(Icons.Filled.Science, contentDescription = null)
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.semantics { contentDescription = "刷新" }
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is TyphoonUiState.Loading -> LoadingState()
                is TyphoonUiState.Error -> ErrorState(
                    message = uiState.message,
                    onRetry = onRefresh,
                    onLoadDemo = onLoadDemo
                )
                is TyphoonUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 28.dp)
                    ) {
                        item {
                            StatusSummaryRow(
                                activeCount = activeCount,
                                dataMode = dataMode,
                                lastUpdated = lastUpdated,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
        }
    }
}

@Composable
private fun StatusSummaryRow(
    activeCount: Int,
    dataMode: DataMode,
    lastUpdated: String?,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("活跃 $activeCount") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Cyclone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledLeadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        item {
            SuggestionChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(if (dataMode == DataMode.Live) "实时数据" else "演示数据")
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    disabledContainerColor = if (dataMode == DataMode.Live) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                    disabledLabelColor = if (dataMode == DataMode.Live) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            )
        }
        if (lastUpdated != null) {
            item {
                SuggestionChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("更新 $lastUpdated") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
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
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "搜索台风" },
            singleLine = true,
            placeholder = { Text("搜索台风名称 / 编号") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "清除搜索")
                    }
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = intensityFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("全部") }
                )
            }
            items(
                items = IntensityLevel.entries.filter { it != IntensityLevel.UNKNOWN },
                key = { it.name }
            ) { level ->
                val selected = intensityFilter == level
                val accent = intensityColor(level)
                FilterChip(
                    selected = selected,
                    onClick = { onFilterChange(level) },
                    label = { Text(level.shortLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accent.copy(alpha = 0.18f),
                        selectedLabelColor = accent,
                        selectedLeadingIconColor = accent
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

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "台风 ${typhoon.name}，${intensity.label}"
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(156.dp)
                    .padding(vertical = 12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
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
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = typhoon.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
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
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IntensityBadge(level = intensity, compact = true)
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
                    Spacer(modifier = Modifier.height(14.dp))
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
                    Spacer(modifier = Modifier.height(10.dp))
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
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
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
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "正在加载" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在获取台风数据…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            .padding(40.dp)
            .semantics {
                contentDescription = if (hasAny) "没有匹配的台风" else "当前暂无活跃台风"
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Cyclone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasAny) "没有匹配的台风" else "当前暂无活跃台风",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasAny) "试试调整搜索或强度筛选" else "下拉刷新，或加载演示数据预览界面",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
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
            .padding(32.dp)
            .semantics { contentDescription = "加载失败" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetry) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("重试")
            }
            FilledTonalButton(onClick = onLoadDemo) {
                Text("演示数据")
            }
        }
    }
}

@Preview(showBackground = true, name = "Home · Success")
@Composable
private fun HomeScreenPreview() {
    TyphoonEyeTheme {
        HomeScreen(
            uiState = TyphoonUiState.Success(previewTyphoons),
            filteredTyphoons = previewTyphoons,
            isRefreshing = false,
            query = "",
            intensityFilter = null,
            dataMode = DataMode.Demo,
            lastUpdated = "14:32:01",
            onQueryChange = {},
            onFilterChange = {},
            onRefresh = {},
            onLoadDemo = {},
            onTyphoonClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home · Empty")
@Composable
private fun HomeEmptyPreview() {
    TyphoonEyeTheme {
        HomeScreen(
            uiState = TyphoonUiState.Success(emptyList()),
            filteredTyphoons = emptyList(),
            isRefreshing = false,
            query = "",
            intensityFilter = null,
            dataMode = DataMode.Live,
            lastUpdated = null,
            onQueryChange = {},
            onFilterChange = {},
            onRefresh = {},
            onLoadDemo = {},
            onTyphoonClick = {}
        )
    }
}

internal val previewTyphoons = listOf(
    Typhoon(
        id = "202609",
        name = "巴威",
        englishName = "BAVI",
        status = "active",
        strong = "台风",
        positionDesc = "距离浙闽交界东南方向约890公里",
        points = listOf(
            TyphoonPoint("2026-07-10 14:00", 21.8, 126.9, 960, 40, "13", "台风", "北西", "22")
        )
    ),
    Typhoon(
        id = "202610",
        name = "美莎克",
        englishName = "MEKKHALA",
        status = "active",
        strong = "热带风暴",
        positionDesc = "菲律宾以东洋面",
        points = listOf(
            TyphoonPoint("2026-07-10 14:00", 12.5, 135.2, 998, 18, "8", "热带风暴", "NW", "20")
        )
    )
)

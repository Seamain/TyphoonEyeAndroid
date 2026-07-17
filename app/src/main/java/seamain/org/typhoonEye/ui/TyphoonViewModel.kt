package seamain.org.typhoonEye.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.data.model.TyphoonPoint
import seamain.org.typhoonEye.data.repository.TyphoonRepository
import seamain.org.typhoonEye.ui.util.IntensityLevel
import seamain.org.typhoonEye.ui.util.currentIntensity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class TyphoonUiState {
    data object Loading : TyphoonUiState()
    data class Success(val typhoons: List<Typhoon>) : TyphoonUiState()
    data class Error(val message: String) : TyphoonUiState()
}

enum class DataMode { Live, Demo }

class TyphoonViewModel(private val repository: TyphoonRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TyphoonUiState>(TyphoonUiState.Loading)
    val uiState: StateFlow<TyphoonUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedTyphoon = MutableStateFlow<Typhoon?>(null)
    val selectedTyphoon: StateFlow<Typhoon?> = _selectedTyphoon.asStateFlow()

    private val _detailLoading = MutableStateFlow(false)
    val detailLoading: StateFlow<Boolean> = _detailLoading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _intensityFilter = MutableStateFlow<IntensityLevel?>(null)
    val intensityFilter: StateFlow<IntensityLevel?> = _intensityFilter.asStateFlow()

    private val _dataMode = MutableStateFlow(DataMode.Live)
    val dataMode: StateFlow<DataMode> = _dataMode.asStateFlow()

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated.asStateFlow()

    private val _allTyphoons = MutableStateFlow<List<Typhoon>>(emptyList())

    val filteredTyphoons: StateFlow<List<Typhoon>> = combine(
        _allTyphoons,
        _query,
        _intensityFilter
    ) { list, q, filter ->
        list.filter { typhoon ->
            val matchesQuery = q.isBlank() ||
                typhoon.name.contains(q, ignoreCase = true) ||
                typhoon.englishName.contains(q, ignoreCase = true) ||
                typhoon.id.contains(q, ignoreCase = true)
            val matchesFilter = filter == null || typhoon.currentIntensity() == filter
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setIntensityFilter(level: IntensityLevel?) {
        // null = clear filter (show all); same level again = toggle off
        _intensityFilter.value = when {
            level == null -> null
            _intensityFilter.value == level -> null
            else -> level
        }
    }

    fun refresh(useMock: Boolean = false) {
        viewModelScope.launch {
            if (useMock) {
                val mock = getMockTyphoons()
                _allTyphoons.value = mock
                _uiState.value = TyphoonUiState.Success(mock)
                _dataMode.value = DataMode.Demo
                _lastUpdated.value = nowLabel()
                _selectedTyphoon.value?.let { selected ->
                    _selectedTyphoon.value = mock.find { it.id == selected.id } ?: selected
                }
                return@launch
            }

            val hadData = _uiState.value is TyphoonUiState.Success
            _isRefreshing.value = true
            if (!hadData) {
                _uiState.value = TyphoonUiState.Loading
            }

            repository.getActiveTyphoons()
                .onSuccess { typhoons ->
                    _allTyphoons.value = typhoons
                    _uiState.value = TyphoonUiState.Success(typhoons)
                    _dataMode.value = DataMode.Live
                    _lastUpdated.value = nowLabel()
                    _selectedTyphoon.value?.let { selected ->
                        _selectedTyphoon.value = typhoons.find { it.id == selected.id } ?: selected
                    }
                }
                .onFailure { error ->
                    if (!hadData) {
                        _uiState.value = TyphoonUiState.Error(error.message ?: "未知错误")
                    }
                }
            _isRefreshing.value = false
        }
    }

    fun selectTyphoon(typhoon: Typhoon?) {
        _selectedTyphoon.value = typhoon
        if (typhoon != null && typhoon.points.size <= 1 && _dataMode.value == DataMode.Live) {
            loadDetail(typhoon.id)
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detailLoading.value = true
            repository.getTyphoonDetail(id)
                .onSuccess { detail ->
                    _selectedTyphoon.value = detail
                    _allTyphoons.value = _allTyphoons.value.map {
                        if (it.id == detail.id) detail else it
                    }
                    val state = _uiState.value
                    if (state is TyphoonUiState.Success) {
                        _uiState.value = state.copy(
                            typhoons = state.typhoons.map {
                                if (it.id == detail.id) detail else it
                            }
                        )
                    }
                }
            _detailLoading.value = false
        }
    }

    fun shareSummary(typhoon: Typhoon): String {
        val last = typhoon.points.lastOrNull()
        return buildString {
            appendLine("【台风眼】${typhoon.name}（${typhoon.englishName}）")
            appendLine("编号：${typhoon.id}")
            if (typhoon.strong.isNotBlank()) appendLine("强度：${typhoon.strong}")
            if (typhoon.positionDesc.isNotBlank()) appendLine("位置：${typhoon.positionDesc}")
            if (last != null) {
                appendLine("风速：${last.speed} m/s · 气压：${last.pressure} hPa")
                appendLine("移动：${last.moveDirection} ${last.moveSpeed}".trim())
                appendLine("观测时间：${last.time}")
            }
            if (typhoon.forecastText.isNotBlank()) {
                appendLine("预报：${typhoon.forecastText}")
            }
        }.trim()
    }

    private fun nowLabel(): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    private fun getMockTyphoons(): List<Typhoon> {
        return listOf(
            Typhoon(
                id = "202609",
                name = "巴威",
                englishName = "BAVI",
                status = "active",
                strong = "台风",
                positionDesc = "距离浙闽交界东南方向约890公里",
                forecastText = "“巴威”将以每小时20-25公里的速度向西北方向移动，强度变化不大",
                startTime = "2026-07-02 08:00:00",
                endTime = "2026-07-10 14:00:00",
                points = listOf(
                    TyphoonPoint("2026-07-02 08:00", 11.0, 160.1, 998, 18, "8", "热带风暴", "西北西", "20"),
                    TyphoonPoint("2026-07-04 08:00", 13.3, 153.0, 945, 48, "15", "强台风", "西", "22"),
                    TyphoonPoint("2026-07-07 08:00", 16.2, 139.1, 920, 60, "17", "超强台风", "西北", "24"),
                    TyphoonPoint("2026-07-10 08:00", 20.5, 128.0, 965, 38, "12", "台风", "北西", "20"),
                    TyphoonPoint("2026-07-10 14:00", 21.8, 126.9, 960, 40, "13", "台风", "北西", "22")
                ),
                forecastPoints = listOf(
                    TyphoonPoint("2026-07-11 02:00", 23.0, 125.5, 955, 42, "13", "台风"),
                    TyphoonPoint("2026-07-11 14:00", 24.5, 124.0, 950, 45, "14", "强台风"),
                    TyphoonPoint("2026-07-12 14:00", 27.0, 122.0, 975, 30, "11", "强热带风暴")
                )
            ),
            Typhoon(
                id = "202610",
                name = "美莎克",
                englishName = "MEKKHALA",
                status = "active",
                strong = "热带风暴",
                positionDesc = "菲律宾以东洋面",
                forecastText = "强度维持，总体向西北偏西移动",
                points = listOf(
                    TyphoonPoint("2026-07-09 14:00", 11.2, 137.8, 1000, 16, "8", "热带风暴", "NW", "18"),
                    TyphoonPoint("2026-07-10 14:00", 12.5, 135.2, 998, 18, "8", "热带风暴", "NW", "20")
                ),
                forecastPoints = listOf(
                    TyphoonPoint("2026-07-11 14:00", 13.8, 133.0, 995, 20, "9", "热带风暴")
                )
            ),
            Typhoon(
                id = "202605",
                name = "黑格比",
                englishName = "HAGUPIT",
                status = "dissipated",
                strong = "热带低压",
                points = listOf(
                    TyphoonPoint("2026-06-15 08:00", 18.0, 120.0, 1002, 12, "7", "热带低压", "北", "10")
                )
            )
        )
    }
}

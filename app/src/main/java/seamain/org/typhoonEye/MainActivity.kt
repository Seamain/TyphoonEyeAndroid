package seamain.org.typhoonEye

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import seamain.org.typhoonEye.data.api.JuheTyphoonApi
import seamain.org.typhoonEye.data.api.QWeatherAuthInterceptor
import seamain.org.typhoonEye.data.api.QWeatherTyphoonApi
import seamain.org.typhoonEye.data.repository.TyphoonRepository
import seamain.org.typhoonEye.ui.TyphoonViewModel
import seamain.org.typhoonEye.ui.screens.DetailScreen
import seamain.org.typhoonEye.ui.screens.HomeScreen
import seamain.org.typhoonEye.ui.theme.TyphoonEyeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = createRepository()
        val viewModelFactory = TyphoonViewModelFactory(repository)

        setContent {
            TyphoonEyeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    TyphoonApp(viewModelFactory)
                }
            }
        }
    }

    private fun createRepository(): TyphoonRepository {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        val mediaType = "application/json".toMediaType()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val juheClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val qWeatherAuth = QWeatherAuthInterceptor(
            apiKey = BuildConfig.QWEATHER_API_KEY,
            kid = BuildConfig.QWEATHER_KID,
            projectId = BuildConfig.QWEATHER_PROJECT_ID,
            privateKeyPem = BuildConfig.QWEATHER_PRIVATE_KEY
        )

        val qWeatherClient = OkHttpClient.Builder()
            .addInterceptor(qWeatherAuth)
            .addInterceptor(logging)
            .build()

        val juheRetrofit = Retrofit.Builder()
            .baseUrl("https://apis.juhe.cn/")
            .client(juheClient)
            .addConverterFactory(json.asConverterFactory(mediaType))
            .build()

        val qWeatherBase = BuildConfig.QWEATHER_HOST.ifBlank {
            "https://pu6yvrgfbv.re.qweatherapi.com/"
        }.let { host -> if (host.endsWith("/")) host else "$host/" }

        val qWeatherRetrofit = Retrofit.Builder()
            .baseUrl(qWeatherBase)
            .client(qWeatherClient)
            .addConverterFactory(json.asConverterFactory(mediaType))
            .build()

        return TyphoonRepository(
            juheApi = juheRetrofit.create(JuheTyphoonApi::class.java),
            qWeatherApi = qWeatherRetrofit.create(QWeatherTyphoonApi::class.java),
            juheKey = BuildConfig.JUHE_KEY,
            qWeatherConfigured = qWeatherAuth.hasCredentials
        )
    }
}

private class TyphoonViewModelFactory(
    private val repository: TyphoonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(TyphoonViewModel::class.java)) {
            return TyphoonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun TyphoonApp(factory: ViewModelProvider.Factory) {
    val viewModel: TyphoonViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val filtered by viewModel.filteredTyphoons.collectAsState()
    val selectedTyphoon by viewModel.selectedTyphoon.collectAsState()
    val detailLoading by viewModel.detailLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val query by viewModel.query.collectAsState()
    val intensityFilter by viewModel.intensityFilter.collectAsState()
    val dataMode by viewModel.dataMode.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val context = LocalContext.current

    val current = selectedTyphoon
    if (current != null) {
        DetailScreen(
            typhoon = current,
            loading = detailLoading,
            onBack = { viewModel.selectTyphoon(null) },
            shareText = viewModel.shareSummary(current),
            onShare = { text ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "台风眼 · ${current.name}")
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "分享台风概况"))
            }
        )
    } else {
        HomeScreen(
            uiState = uiState,
            filteredTyphoons = filtered,
            isRefreshing = isRefreshing,
            query = query,
            intensityFilter = intensityFilter,
            dataMode = dataMode,
            lastUpdated = lastUpdated,
            onQueryChange = viewModel::setQuery,
            onFilterChange = viewModel::setIntensityFilter,
            onRefresh = { viewModel.refresh() },
            onLoadDemo = { viewModel.refresh(useMock = true) },
            onTyphoonClick = viewModel::selectTyphoon
        )
    }
}

package seamain.org.typhoonEye.data.repository

import android.util.Log
import seamain.org.typhoonEye.data.api.JuheTyphoonApi
import seamain.org.typhoonEye.data.api.QWeatherAuthInterceptor
import seamain.org.typhoonEye.data.api.QWeatherTyphoonApi
import seamain.org.typhoonEye.data.local.TyphoonLocalDataSource
import seamain.org.typhoonEye.data.model.qWeatherTypeToStrong
import seamain.org.typhoonEye.data.model.toDomain
import seamain.org.typhoonEye.domain.model.Typhoon
import seamain.org.typhoonEye.domain.model.TyphoonFeed
import seamain.org.typhoonEye.domain.repository.TyphoonRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Named

/**
 * Offline-first typhoon repository:
 * 1) Prefer Juhe remote, fallback QWeather
 * 2) On remote success → write Room cache
 * 3) On remote failure → serve Room cache when available
 */
class DefaultTyphoonRepository @Inject constructor(
    private val juheApi: JuheTyphoonApi,
    private val qWeatherApi: QWeatherTyphoonApi,
    @Named("juhe_key") private val juheKey: String,
    private val qWeatherAuth: QWeatherAuthInterceptor,
    private val localDataSource: TyphoonLocalDataSource
) : TyphoonRepository {

    private val qWeatherConfigured: Boolean
        get() = qWeatherAuth.hasCredentials

    private val tag = "TyphoonRepository"

    private class FetchOutcome(
        val typhoons: List<Typhoon>? = null,
        val error: String? = null
    )

    override suspend fun getActiveTyphoons(): Result<TyphoonFeed> {
        val remote = fetchRemoteActive()
        remote.onSuccess { list ->
            runCatching { localDataSource.replaceAll(list) }
                .onFailure { Log.w(tag, "Failed to cache active typhoons", it) }
            return Result.success(TyphoonFeed(typhoons = list, fromCache = false))
        }

        val cached = runCatching { localDataSource.getAll() }.getOrDefault(emptyList())
        if (cached.isNotEmpty()) {
            Log.i(tag, "Serving ${cached.size} typhoon(s) from Room cache")
            return Result.success(
                TyphoonFeed(
                    typhoons = cached,
                    fromCache = true,
                    staleMessage = remote.exceptionOrNull()?.message
                )
            )
        }
        return Result.failure(
            remote.exceptionOrNull()
                ?: Exception("所有数据源均失败。请检查 local.properties（参考 local.properties.example）")
        )
    }

    override suspend fun getTyphoonDetail(id: String): Result<Typhoon> {
        val remote = fetchRemoteDetail(id)
        remote.onSuccess { detail ->
            runCatching { localDataSource.upsert(detail) }
                .onFailure { Log.w(tag, "Failed to cache typhoon detail $id", it) }
            return Result.success(detail)
        }

        val cached = runCatching { localDataSource.getById(id) }.getOrNull()
        if (cached != null) {
            Log.i(tag, "Serving typhoon detail $id from Room cache")
            return Result.success(cached)
        }
        return remote
    }

    private suspend fun fetchRemoteActive(): Result<List<Typhoon>> {
        val errors = ArrayList<String>()

        if (juheKey.isBlank()) {
            errors.add("聚合 JUHE_KEY 未配置")
        } else {
            val juhe = fetchFromJuhe()
            val data = juhe.typhoons
            if (data != null) {
                return Result.success(data)
            }
            juhe.error?.let { errors.add(it) }
        }

        if (!qWeatherConfigured) {
            errors.add("和风凭证未配置（QWEATHER_API_KEY 或 JWT）")
        } else {
            val qWeather = fetchFromQWeather()
            val data = qWeather.typhoons
            if (data != null) {
                return Result.success(data)
            }
            qWeather.error?.let { errors.add(it) }
        }

        val detail = if (errors.isEmpty()) {
            "所有数据源均失败"
        } else {
            errors.joinToString("；")
        }
        return Result.failure(
            Exception("$detail。请检查 local.properties（参考 local.properties.example）")
        )
    }

    private suspend fun fetchRemoteDetail(id: String): Result<Typhoon> {
        if (!id.startsWith("NP_") && juheKey.isNotBlank()) {
            try {
                val response = juheApi.getTyphoonDetail(juheKey, id)
                if (response.errorCode == 0 && response.result?.data != null) {
                    return Result.success(response.result.data.toDomain())
                }
                Log.w(tag, "Juhe detail error: ${response.reason} (${response.errorCode})")
            } catch (e: Exception) {
                Log.e(tag, "Juhe detail request failed", e)
            }
        }

        if (!qWeatherConfigured) {
            return Result.failure(Exception("无法获取台风详情: $id（和风凭证未配置）"))
        }

        // Keep the caller/nav id stable. QWeather storm id is only for the HTTP path;
        // returning NP_* here used to desync Detail route matching (flash → endless loading).
        val stormId = if (id.startsWith("NP_")) id else "NP_${id.takeLast(4)}"
        try {
            val track = qWeatherApi.getStormTrack(stormId)
            if (track.code == "200") {
                val forecast = runCatching { qWeatherApi.getStormForecast(stormId) }.getOrNull()
                val infoNow = track.now
                val typhoon = Typhoon(
                    id = id,
                    name = stormId,
                    englishName = stormId,
                    status = if (track.isActive == "1") "active" else "dissipated",
                    strong = infoNow?.type?.let { qWeatherTypeToStrong(it) }.orEmpty(),
                    points = track.track.map { it.toDomain() }.ifEmpty {
                        listOfNotNull(infoNow?.toDomain())
                    },
                    forecastPoints = forecast?.forecast?.map { it.toDomain() }.orEmpty()
                )
                return Result.success(typhoon)
            }
            Log.e(tag, "QWeather track error code: ${track.code}")
        } catch (e: Exception) {
            Log.e(tag, "QWeather track request failed", e)
            return Result.failure(Exception("无法获取台风详情: $id（${e.message}）"))
        }

        return Result.failure(Exception("无法获取台风详情: $id"))
    }

    private suspend fun fetchFromJuhe(): FetchOutcome {
        return try {
            val listResponse = juheApi.getActiveTyphoons(juheKey)
            when (listResponse.errorCode) {
                0 -> {
                    val active = listResponse.result?.data.orEmpty()
                    if (active.isEmpty()) {
                        Log.d(tag, "Juhe: no active typhoons")
                        return FetchOutcome(typhoons = emptyList())
                    }
                    val typhoons = active.map { info ->
                        try {
                            val detail = juheApi.getTyphoonDetail(juheKey, info.tfid)
                            if (detail.errorCode == 0 && detail.result?.data != null) {
                                detail.result.data.toDomain()
                            } else {
                                info.toDomain()
                            }
                        } catch (e: Exception) {
                            Log.w(tag, "Juhe detail failed for ${info.tfid}, using list snapshot", e)
                            info.toDomain()
                        }
                    }
                    Log.d(tag, "Fetched ${typhoons.size} typhoon(s) from Juhe")
                    FetchOutcome(typhoons = typhoons)
                }
                10001, 10002 -> {
                    val msg = "聚合 KEY 无效（${listResponse.errorCode}: ${listResponse.reason}）"
                    Log.e(tag, msg)
                    FetchOutcome(error = msg)
                }
                10012, 10013, 10022, 10023 -> {
                    val msg = "聚合配额受限（${listResponse.errorCode}）"
                    Log.w(tag, "$msg, fallback to QWeather")
                    FetchOutcome(error = msg)
                }
                else -> {
                    val msg = "聚合错误（${listResponse.errorCode}: ${listResponse.reason}）"
                    Log.e(tag, msg)
                    FetchOutcome(error = msg)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Juhe request failed", e)
            FetchOutcome(error = "聚合请求失败: ${e.message}")
        }
    }

    private suspend fun fetchFromQWeather(): FetchOutcome {
        return try {
            val year = Calendar.getInstance().get(Calendar.YEAR).toString()
            val listResponse = qWeatherApi.getStormList(basin = "NP", year = year)
            if (listResponse.code != "200") {
                val msg = "和风列表错误 code=${listResponse.code}"
                Log.e(tag, msg)
                return FetchOutcome(error = msg)
            }

            val activeStorms = listResponse.storm.filter { it.isActive == "1" }
            if (activeStorms.isEmpty()) {
                Log.d(tag, "QWeather: no active storms in $year")
                return FetchOutcome(typhoons = emptyList())
            }

            val typhoons = activeStorms.map { storm ->
                val track = runCatching { qWeatherApi.getStormTrack(storm.id) }.getOrNull()
                val forecast = runCatching { qWeatherApi.getStormForecast(storm.id) }.getOrNull()
                storm.toDomain(
                    track = track?.track.orEmpty(),
                    now = track?.now,
                    forecast = forecast?.forecast.orEmpty()
                )
            }
            Log.d(tag, "Fetched ${typhoons.size} storm(s) from QWeather")
            FetchOutcome(typhoons = typhoons)
        } catch (e: Exception) {
            Log.e(tag, "QWeather request failed", e)
            FetchOutcome(error = "和风请求失败: ${e.message}")
        }
    }
}

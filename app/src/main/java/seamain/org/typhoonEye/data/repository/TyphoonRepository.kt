package seamain.org.typhoonEye.data.repository

import android.util.Log
import seamain.org.typhoonEye.data.api.JuheTyphoonApi
import seamain.org.typhoonEye.data.api.QWeatherTyphoonApi
import seamain.org.typhoonEye.data.model.Typhoon
import seamain.org.typhoonEye.data.model.qWeatherTypeToStrong
import seamain.org.typhoonEye.data.model.toDomain
import java.util.Calendar

class TyphoonRepository(
    private val juheApi: JuheTyphoonApi,
    private val qWeatherApi: QWeatherTyphoonApi,
    private val juheKey: String
) {
    private val tag = "TyphoonRepository"

    /**
     * 优先聚合数据；配额用尽或失败时回退和风天气。
     */
    suspend fun getActiveTyphoons(): Result<List<Typhoon>> {
        fetchFromJuhe()?.let { return Result.success(it) }
        fetchFromQWeather()?.let { return Result.success(it) }
        return Result.failure(Exception("所有数据源均失败或达到调用限制"))
    }

    /**
     * 按台风 ID 拉取完整路径（聚合优先，失败则和风）。
     * [id] 可为聚合 tfid（如 202609）或和风 stormid（如 NP_2609）。
     */
    suspend fun getTyphoonDetail(id: String): Result<Typhoon> {
        if (!id.startsWith("NP_")) {
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

        val stormId = if (id.startsWith("NP_")) id else "NP_${id.takeLast(4)}"
        try {
            val track = qWeatherApi.getStormTrack(stormId)
            if (track.code == "200") {
                val forecast = runCatching { qWeatherApi.getStormForecast(stormId) }.getOrNull()
                val infoNow = track.now
                val typhoon = Typhoon(
                    id = stormId,
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
        }

        return Result.failure(Exception("无法获取台风详情: $id"))
    }

    private suspend fun fetchFromJuhe(): List<Typhoon>? {
        return try {
            val listResponse = juheApi.getActiveTyphoons(juheKey)
            when (listResponse.errorCode) {
                0 -> {
                    val active = listResponse.result?.data.orEmpty()
                    if (active.isEmpty()) {
                        Log.d(tag, "Juhe: no active typhoons")
                        return emptyList()
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
                    typhoons
                }
                10012, 10013, 10022, 10023 -> {
                    // 请求次数 / 日配额限制
                    Log.w(tag, "Juhe quota exceeded (${listResponse.errorCode}), fallback to QWeather")
                    null
                }
                else -> {
                    Log.e(tag, "Juhe error: ${listResponse.reason} (${listResponse.errorCode})")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Juhe request failed", e)
            null
        }
    }

    private suspend fun fetchFromQWeather(): List<Typhoon>? {
        return try {
            val year = Calendar.getInstance().get(Calendar.YEAR).toString()
            val listResponse = qWeatherApi.getStormList(basin = "NP", year = year)
            if (listResponse.code != "200") {
                Log.e(tag, "QWeather list error code: ${listResponse.code}")
                return null
            }

            val activeStorms = listResponse.storm.filter { it.isActive == "1" }
            if (activeStorms.isEmpty()) {
                Log.d(tag, "QWeather: no active storms in $year")
                return emptyList()
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
            typhoons
        } catch (e: Exception) {
            Log.e(tag, "QWeather request failed", e)
            null
        }
    }
}

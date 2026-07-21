package seamain.org.typhoonEye

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import seamain.org.typhoonEye.data.api.JuheTyphoonApi
import seamain.org.typhoonEye.data.api.QWeatherTyphoonApi
import seamain.org.typhoonEye.data.model.JuheActiveListResponse
import seamain.org.typhoonEye.data.model.JuheActiveListResult
import seamain.org.typhoonEye.data.model.JuheActiveTyphoon
import seamain.org.typhoonEye.data.model.JuheDetailData
import seamain.org.typhoonEye.data.model.JuheDetailResponse
import seamain.org.typhoonEye.data.model.JuheDetailResult
import seamain.org.typhoonEye.data.model.JuheTrackPoint
import seamain.org.typhoonEye.data.model.QWeatherStormInfo
import seamain.org.typhoonEye.data.model.QWeatherStormListResponse
import seamain.org.typhoonEye.data.model.QWeatherStormTrackResponse
import seamain.org.typhoonEye.data.model.QWeatherTrackPoint
import seamain.org.typhoonEye.data.repository.TyphoonRepository

class TyphoonRepositoryTest {

    private lateinit var juheApi: JuheTyphoonApi
    private lateinit var qWeatherApi: QWeatherTyphoonApi
    private lateinit var repository: TyphoonRepository
    private val juheKey = "juhe_key"

    @Before
    fun setup() {
        juheApi = mock()
        qWeatherApi = mock()
        repository = TyphoonRepository(juheApi, qWeatherApi, juheKey)
    }

    @Test
    fun `getActiveTyphoons should return Juhe data when successful`() = runTest {
        whenever(juheApi.getActiveTyphoons(juheKey)).thenReturn(
            JuheActiveListResponse(
                reason = "success",
                errorCode = 0,
                result = JuheActiveListResult(
                    data = listOf(
                        JuheActiveTyphoon(
                            tfid = "202609",
                            name = "巴威",
                            enname = "BAVI",
                            strong = "台风",
                            lat = "21.80",
                            lng = "126.90",
                            speed = "40",
                            pressure = "960",
                            power = "13"
                        )
                    )
                )
            )
        )
        whenever(juheApi.getTyphoonDetail(eq(juheKey), eq("202609"))).thenReturn(
            JuheDetailResponse(
                reason = "success",
                errorCode = 0,
                result = JuheDetailResult(
                    data = JuheDetailData(
                        tfid = "202609",
                        name = "巴威",
                        enname = "BAVI",
                        strong = "台风",
                        points = listOf(
                            JuheTrackPoint(
                                time = "2026-07-10 14:00:00",
                                lat = "21.80",
                                lng = "126.90",
                                speed = "40",
                                pressure = "960",
                                power = "13",
                                strong = "台风"
                            )
                        )
                    )
                )
            )
        )

        val result = repository.getActiveTyphoons()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("巴威", result.getOrNull()?.first()?.name)
        assertEquals(1, result.getOrNull()?.first()?.points?.size)
        verify(juheApi).getActiveTyphoons(juheKey)
        verify(juheApi).getTyphoonDetail(juheKey, "202609")
        verify(qWeatherApi, never()).getStormList(any(), any())
    }

    @Test
    fun `getActiveTyphoons should fallback to QWeather when Juhe limit reached`() = runTest {
        whenever(juheApi.getActiveTyphoons(any())).thenReturn(
            JuheActiveListResponse(reason = "limit reached", errorCode = 10012, result = null)
        )
        whenever(qWeatherApi.getStormList(any(), any())).thenReturn(
            QWeatherStormListResponse(
                code = "200",
                storm = listOf(
                    QWeatherStormInfo(
                        id = "NP_2609",
                        name = "巴威",
                        basin = "NP",
                        year = "2026",
                        isActive = "1"
                    ),
                    QWeatherStormInfo(
                        id = "NP_2601",
                        name = "洛鞍",
                        isActive = "0"
                    )
                )
            )
        )
        whenever(qWeatherApi.getStormTrack("NP_2609")).thenReturn(
            QWeatherStormTrackResponse(
                code = "200",
                isActive = "1",
                track = listOf(
                    QWeatherTrackPoint(
                        time = "2026-07-11T09:00+08:00",
                        lat = "25.2",
                        lon = "124.5",
                        type = "STY",
                        pressure = "950",
                        windSpeed = "42",
                        moveDir = "NW",
                        moveSpeed = "30"
                    )
                )
            )
        )
        whenever(qWeatherApi.getStormForecast("NP_2609")).thenReturn(
            seamain.org.typhoonEye.data.model.QWeatherStormForecastResponse(code = "200", forecast = emptyList())
        )

        val result = repository.getActiveTyphoons()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("巴威", result.getOrNull()?.first()?.name)
        assertEquals("强台风", result.getOrNull()?.first()?.strong)
        verify(juheApi).getActiveTyphoons(juheKey)
        verify(qWeatherApi).getStormList(eq("NP"), any())
        verify(qWeatherApi).getStormTrack("NP_2609")
    }

    @Test
    fun `getActiveTyphoons returns empty list when Juhe has no active typhoons`() = runTest {
        whenever(juheApi.getActiveTyphoons(juheKey)).thenReturn(
            JuheActiveListResponse(
                reason = "success",
                errorCode = 0,
                result = JuheActiveListResult(data = emptyList())
            )
        )

        val result = repository.getActiveTyphoons()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
        verify(qWeatherApi, never()).getStormList(any(), any())
    }

    @Test
    fun `getActiveTyphoons reports missing credentials without crashing`() = runTest {
        val emptyRepo = TyphoonRepository(juheApi, qWeatherApi, juheKey = "", qWeatherConfigured = false)
        val result = emptyRepo.getActiveTyphoons()
        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message.orEmpty()
        assertTrue(message.contains("JUHE_KEY"))
        assertTrue(message.contains("和风"))
        verify(juheApi, never()).getActiveTyphoons(any())
        verify(qWeatherApi, never()).getStormList(any(), any())
    }

    @Test
    fun `getActiveTyphoons falls back when Juhe key invalid 10001`() = runTest {
        whenever(juheApi.getActiveTyphoons(juheKey)).thenReturn(
            JuheActiveListResponse(reason = "错误的请求KEY", errorCode = 10001, result = null)
        )
        whenever(qWeatherApi.getStormList(any(), any())).thenReturn(
            QWeatherStormListResponse(code = "200", storm = emptyList())
        )

        val result = repository.getActiveTyphoons()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
        verify(qWeatherApi).getStormList(eq("NP"), any())
    }
}

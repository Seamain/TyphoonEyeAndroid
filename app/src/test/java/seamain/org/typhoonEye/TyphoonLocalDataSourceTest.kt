package seamain.org.typhoonEye

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import seamain.org.typhoonEye.data.local.TyphoonDao
import seamain.org.typhoonEye.data.local.TyphoonEntity
import seamain.org.typhoonEye.data.local.TyphoonLocalDataSource
import seamain.org.typhoonEye.domain.model.Typhoon
import seamain.org.typhoonEye.domain.model.TyphoonPoint

class TyphoonLocalDataSourceTest {

    private lateinit var dao: TyphoonDao
    private lateinit var dataSource: TyphoonLocalDataSource

    @Before
    fun setup() {
        dao = mock()
        dataSource = TyphoonLocalDataSource(dao)
    }

    @Test
    fun `replaceAll round-trips typhoon points through JSON`() = runTest {
        val typhoon = Typhoon(
            id = "202609",
            name = "巴威",
            englishName = "BAVI",
            status = "active",
            strong = "台风",
            positionDesc = "东海",
            points = listOf(
                TyphoonPoint(
                    time = "2026-07-10 14:00",
                    lat = 21.8,
                    lng = 126.9,
                    pressure = 960,
                    speed = 40,
                    power = "13",
                    strong = "台风",
                    radius7 = "280|250|220|260"
                )
            ),
            forecastPoints = listOf(
                TyphoonPoint("2026-07-11 02:00", 23.0, 125.5, 955, 42, "13", "台风")
            )
        )

        dataSource.replaceAll(listOf(typhoon))

        val captor = argumentCaptor<List<TyphoonEntity>>()
        verify(dao).replaceAll(captor.capture())
        val entity = captor.firstValue.single()
        assertEquals("202609", entity.id)
        assertTrue(entity.pointsJson.contains("21.8") || entity.pointsJson.contains("21.8"))
        assertTrue(entity.forecastPointsJson.contains("23.0") || entity.forecastPointsJson.contains("23"))

        whenever(dao.getAll()).thenReturn(listOf(entity))
        val loaded = dataSource.getAll()
        assertEquals(1, loaded.size)
        assertEquals("巴威", loaded.first().name)
        assertEquals(1, loaded.first().points.size)
        assertEquals(21.8, loaded.first().points.first().lat, 0.0001)
        assertEquals(1, loaded.first().forecastPoints.size)
        assertEquals("280|250|220|260", loaded.first().points.first().radius7)
    }
}

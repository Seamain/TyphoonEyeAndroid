package seamain.org.typhoonEye

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import seamain.org.typhoonEye.domain.util.CoordTransform

class CoordTransformTest {

    @Test
    fun wgs84ToGcj02_offsetsMainlandPoint() {
        // Near Shanghai
        val wgsLng = 121.4737
        val wgsLat = 31.2304
        val gcj = CoordTransform.wgs84ToGcj02(wgsLng, wgsLat)
        assertNotEquals(wgsLng, gcj.lng, 1e-6)
        assertNotEquals(wgsLat, gcj.lat, 1e-6)
        // Offset is typically tens to hundreds of meters → < 0.02°
        assertTrue(kotlin.math.abs(gcj.lng - wgsLng) < 0.02)
        assertTrue(kotlin.math.abs(gcj.lat - wgsLat) < 0.02)
    }

    @Test
    fun wgs84ToGcj02_leavesPacificUnchanged() {
        val lng = 140.0
        val lat = 20.0
        val gcj = CoordTransform.wgs84ToGcj02(lng, lat)
        assertEquals(lng, gcj.lng, 1e-9)
        assertEquals(lat, gcj.lat, 1e-9)
    }
}

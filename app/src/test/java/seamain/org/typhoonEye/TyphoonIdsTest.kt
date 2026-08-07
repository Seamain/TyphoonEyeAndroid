package seamain.org.typhoonEye

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import seamain.org.typhoonEye.domain.util.normalizeTyphoonId
import seamain.org.typhoonEye.domain.util.typhoonIdsMatch

class TyphoonIdsTest {

    @Test
    fun normalize_stripsNpPrefixAndKeepsLast4Digits() {
        assertEquals("2418", normalizeTyphoonId("NP_2418"))
        assertEquals("2418", normalizeTyphoonId("202418"))
        assertEquals("2418", normalizeTyphoonId("2418"))
    }

    @Test
    fun match_juheAndQWeatherIds() {
        assertTrue(typhoonIdsMatch("202418", "NP_2418"))
        assertTrue(typhoonIdsMatch("2418", "NP_2418"))
        assertTrue(typhoonIdsMatch("NP_2418", "NP_2418"))
        assertFalse(typhoonIdsMatch("202418", "NP_2419"))
        assertFalse(typhoonIdsMatch(null, "2418"))
    }
}

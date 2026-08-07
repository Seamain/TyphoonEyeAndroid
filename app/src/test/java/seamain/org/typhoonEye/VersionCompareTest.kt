package seamain.org.typhoonEye

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import seamain.org.typhoonEye.domain.util.compareVersionLabels
import seamain.org.typhoonEye.domain.util.isNewerVersion

class VersionCompareTest {

    @Test
    fun comparesSimpleSemver() {
        assertTrue(isNewerVersion("1.1", "1.0"))
        assertTrue(isNewerVersion("v1.2.0", "1.1.9"))
        assertFalse(isNewerVersion("1.0.0", "1.0"))
        assertEquals(0, compareVersionLabels("v1.0.0", "1.0"))
    }

    @Test
    fun ignoresPrereleaseSuffixForCoreOrder() {
        assertTrue(isNewerVersion("1.1.0-beta", "1.0.9"))
        assertEquals(0, compareVersionLabels("1.0.0-rc1", "1.0.0"))
    }
}

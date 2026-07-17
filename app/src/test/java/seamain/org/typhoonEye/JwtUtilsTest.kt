package seamain.org.typhoonEye

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import seamain.org.typhoonEye.data.util.JwtUtils
import java.util.Base64

class JwtUtilsTest {

    @Test
    fun testGenerateQWeatherJwt() {
        val publicId = "HE12345"
        val projectKey = "secret"
        
        val jwt = JwtUtils.generateQWeatherJwt(publicId, projectKey)
        
        val parts = jwt.split(".")
        assertEquals(3, parts.size)
        
        // Decode header
        val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
        val header = JSONObject(headerJson)
        assertEquals("HS256", header.getString("alg"))
        assertEquals("JWT", header.getString("typ"))
        assertEquals(publicId, header.getString("kid"))
        
        // Decode payload
        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
        val payload = JSONObject(payloadJson)
        assertEquals(publicId, payload.getString("sub"))
        assertEquals("qweather", payload.getString("aud"))
        
        val iat = payload.getLong("iat")
        val exp = payload.getLong("exp")
        assertEquals(600, exp - iat)
        
        // Signature part exists
        assertTrue(parts[2].isNotEmpty())
    }
}

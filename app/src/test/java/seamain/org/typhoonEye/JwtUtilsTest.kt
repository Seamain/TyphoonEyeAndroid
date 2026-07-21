package seamain.org.typhoonEye

import net.i2p.crypto.eddsa.KeyPairGenerator
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import seamain.org.typhoonEye.data.util.JwtUtils
import java.security.SecureRandom
import java.util.Base64

class JwtUtilsTest {

    private fun samplePem(): String {
        val generator = KeyPairGenerator()
        generator.initialize(256, SecureRandom())
        val keyPair = generator.generateKeyPair()
        val pkcs8 = keyPair.private.encoded
        return buildString {
            append("-----BEGIN PRIVATE KEY-----\n")
            append(Base64.getEncoder().encodeToString(pkcs8))
            append("\n-----END PRIVATE KEY-----")
        }
    }

    @Test
    fun generateQWeatherJwt_usesEdDsaHeaderAndValidParts() {
        val kid = "CREDENTIAL_ID"
        val projectId = "PROJECT_ID"
        val jwt = JwtUtils.generateQWeatherJwt(kid, projectId, samplePem())

        val parts = jwt.split(".")
        assertEquals(3, parts.size)

        val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
        val header = JSONObject(headerJson)
        assertEquals("EdDSA", header.getString("alg"))
        assertEquals(kid, header.getString("kid"))

        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
        val payload = JSONObject(payloadJson)
        assertEquals(projectId, payload.getString("sub"))
        assertTrue(payload.getLong("exp") > payload.getLong("iat"))
        assertTrue(parts[2].isNotEmpty())
    }

    @Test
    fun generateQWeatherJwt_rejectsBlankPrivateKey() {
        assertThrows(IllegalArgumentException::class.java) {
            JwtUtils.generateQWeatherJwt("kid", "project", "   ")
        }
    }

    @Test
    fun generateQWeatherJwt_acceptsEscapedNewlines() {
        val pem = samplePem().replace("\n", "\\n")
        val jwt = JwtUtils.generateQWeatherJwt("kid", "sub", pem)
        assertEquals(3, jwt.split(".").size)
    }
}

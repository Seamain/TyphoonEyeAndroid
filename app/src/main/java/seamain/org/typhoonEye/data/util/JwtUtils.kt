package seamain.org.typhoonEye.data.util

import org.json.JSONObject
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object JwtUtils {

    fun generateQWeatherJwt(publicId: String, projectKey: String): String {
        val iat = System.currentTimeMillis() / 1000
        val exp = iat + 600 // 10 minutes expiry

        val header = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
            put("kid", publicId)
        }

        val payload = JSONObject().apply {
            put("sub", publicId)
            put("iat", iat)
            put("exp", exp)
            put("aud", "qweather")
        }

        val headerBase64 = base64UrlEncode(header.toString().toByteArray())
        val payloadBase64 = base64UrlEncode(payload.toString().toByteArray())

        val signatureBase = "$headerBase64.$payloadBase64"
        val signature = hmacSha256(signatureBase, projectKey)
        val signatureBase64 = base64UrlEncode(signature)

        return "$signatureBase.$signatureBase64"
    }

    private fun base64UrlEncode(input: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input)
    }

    private fun hmacSha256(input: String, key: String): ByteArray {
        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        hmac.init(secretKey)
        return hmac.doFinal(input.toByteArray())
    }
}

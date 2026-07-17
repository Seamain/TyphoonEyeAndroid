package seamain.org.typhoonEye.data.api

import okhttp3.Interceptor
import okhttp3.Response
import seamain.org.typhoonEye.data.util.JwtUtils

/**
 * Injects QWeather JWT as `Authorization: Bearer <token>`.
 * Matches Postman collection auth (Bearer {{JWT_key}}).
 */
class QWeatherAuthInterceptor(
    private val publicId: String,
    private val projectKey: String
) : Interceptor {

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var tokenExpiresAtMs: Long = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = currentToken()
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }

    private fun currentToken(): String {
        val now = System.currentTimeMillis()
        // Refresh 60s before expiry (JWT lifetime is 600s)
        val existing = cachedToken
        if (existing != null && now < tokenExpiresAtMs - 60_000) {
            return existing
        }
        val jwt = JwtUtils.generateQWeatherJwt(publicId, projectKey)
        cachedToken = jwt
        tokenExpiresAtMs = now + 600_000
        return jwt
    }
}

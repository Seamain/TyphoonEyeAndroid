package seamain.org.typhoonEye.domain.repository

import seamain.org.typhoonEye.domain.model.Typhoon
import seamain.org.typhoonEye.domain.model.TyphoonFeed

/**
 * Single source of truth for typhoon list / detail.
 * Implementations may combine remote APIs with a local Room cache.
 */
interface TyphoonRepository {
    suspend fun getActiveTyphoons(): Result<TyphoonFeed>
    suspend fun getTyphoonDetail(id: String): Result<Typhoon>
}

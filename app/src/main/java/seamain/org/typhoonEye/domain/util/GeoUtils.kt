package seamain.org.typhoonEye.domain.util

import seamain.org.typhoonEye.domain.model.Typhoon
import seamain.org.typhoonEye.domain.model.UserLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Great-circle distance in kilometers (WGS84 sphere). */
fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

/** Distance from [user] to the typhoon's latest observed point, or null if unknown. */
fun Typhoon.distanceKmFrom(user: UserLocation?): Double? {
    if (user == null || !user.isValid) return null
    val last = points.lastOrNull() ?: return null
    if (last.lat == 0.0 && last.lng == 0.0) return null
    return haversineKm(user.latitude, user.longitude, last.lat, last.lng)
}

fun Double.roundKm(): Int = coerceAtLeast(0.0).roundToInt()

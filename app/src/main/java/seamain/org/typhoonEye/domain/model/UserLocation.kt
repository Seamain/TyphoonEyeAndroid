package seamain.org.typhoonEye.domain.model

/**
 * Cached / last-known device position used for local weather alerts.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    /** Epoch millis when this fix was obtained. */
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
    val label: String = ""
) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)
}

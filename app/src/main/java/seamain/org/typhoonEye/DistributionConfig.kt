package seamain.org.typhoonEye

import android.content.Context

/**
 * Distribution switches resolved from resources so [main] always compiles.
 * F-Droid overrides [R.bool.enable_in_app_updates] via `src/fdroid/res`.
 */
object DistributionConfig {

    @Volatile
    private var inAppUpdatesCached: Boolean? = null

    fun enableInAppUpdates(context: Context): Boolean {
        inAppUpdatesCached?.let { return it }
        val value = context.applicationContext.resources.getBoolean(R.bool.enable_in_app_updates)
        inAppUpdatesCached = value
        return value
    }
}

package seamain.org.typhoonEye.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import seamain.org.typhoonEye.domain.model.UserLocation

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    System,
    Light,
    Dark;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.find { it.name == value } ?: System
    }
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val appLanguage: AppLanguage = AppLanguage.ZhHans,
    val liveActivityEnabled: Boolean = true,
    val emergencyAlertsEnabled: Boolean = true,
    /** Prefer device GPS for official warning lookups. */
    val locationAlertsEnabled: Boolean = true,
    val dynamicColorEnabled: Boolean = true,
    /** Last successful fix used by background Worker when fresh GPS is unavailable. */
    val cachedUserLocation: UserLocation? = null
)

class UserPreferencesRepository(private val context: Context) {

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            themeMode = ThemeMode.fromStorage(prefs[Keys.THEME_MODE]),
            appLanguage = prefs[Keys.APP_LANGUAGE]?.let(AppLanguage::fromStorage)
                ?: AppLanguage.current(),
            liveActivityEnabled = prefs[Keys.LIVE_ACTIVITY] ?: true,
            emergencyAlertsEnabled = prefs[Keys.EMERGENCY_ALERTS] ?: true,
            locationAlertsEnabled = prefs[Keys.LOCATION_ALERTS] ?: true,
            dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: true,
            cachedUserLocation = prefs.toCachedLocation()
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.APP_LANGUAGE] = language.name }
        // Must run on main thread — may recreate Activities.
        withContext(Dispatchers.Main.immediate) {
            AppLanguage.apply(language)
        }
    }

    suspend fun setLiveActivityEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LIVE_ACTIVITY] = enabled }
    }

    suspend fun setEmergencyAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EMERGENCY_ALERTS] = enabled }
    }

    suspend fun setLocationAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOCATION_ALERTS] = enabled }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun cacheUserLocation(location: UserLocation) {
        if (!location.isValid) return
        context.dataStore.edit { prefs ->
            prefs[Keys.CACHED_LAT] = location.latitude
            prefs[Keys.CACHED_LNG] = location.longitude
            prefs[Keys.CACHED_LOC_AT] = location.updatedAtEpochMs
            prefs[Keys.CACHED_LOC_LABEL] = location.label
        }
    }

    suspend fun getCachedUserLocation(): UserLocation? =
        context.dataStore.data.map { it.toCachedLocation() }.first()

    suspend fun getNotifiedAlertIds(): Set<String> =
        context.dataStore.data.map { it[Keys.NOTIFIED_ALERTS] ?: emptySet() }.first()

    suspend fun setNotifiedAlertIds(ids: Set<String>) {
        context.dataStore.edit { it[Keys.NOTIFIED_ALERTS] = ids }
    }

    private fun Preferences.toCachedLocation(): UserLocation? {
        val lat = this[Keys.CACHED_LAT] ?: return null
        val lng = this[Keys.CACHED_LNG] ?: return null
        val loc = UserLocation(
            latitude = lat,
            longitude = lng,
            updatedAtEpochMs = this[Keys.CACHED_LOC_AT] ?: 0L,
            label = this[Keys.CACHED_LOC_LABEL].orEmpty()
        )
        return loc.takeIf { it.isValid }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val LIVE_ACTIVITY = booleanPreferencesKey("live_activity_enabled")
        val EMERGENCY_ALERTS = booleanPreferencesKey("emergency_alerts_enabled")
        val LOCATION_ALERTS = booleanPreferencesKey("location_alerts_enabled")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val NOTIFIED_ALERTS = stringSetPreferencesKey("notified_alert_ids")
        val CACHED_LAT = doublePreferencesKey("cached_user_lat")
        val CACHED_LNG = doublePreferencesKey("cached_user_lng")
        val CACHED_LOC_AT = longPreferencesKey("cached_user_loc_at")
        val CACHED_LOC_LABEL = stringPreferencesKey("cached_user_loc_label")
    }
}

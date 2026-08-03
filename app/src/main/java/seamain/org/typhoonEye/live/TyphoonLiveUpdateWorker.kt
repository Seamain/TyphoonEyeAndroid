package seamain.org.typhoonEye.live

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import seamain.org.typhoonEye.data.location.LocationProvider
import seamain.org.typhoonEye.data.preferences.UserPreferencesRepository
import seamain.org.typhoonEye.domain.repository.TyphoonRepository
import seamain.org.typhoonEye.domain.repository.WarningRepository
import java.util.concurrent.TimeUnit

/**
 * Periodically refreshes active typhoons, Live notification, and emergency alerts.
 * Uses cached / last-known user location when location-based alerts are enabled.
 */
@HiltWorker
class TyphoonLiveUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val preferences: UserPreferencesRepository,
    private val typhoonRepository: TyphoonRepository,
    private val warningRepository: WarningRepository,
    private val locationProvider: LocationProvider,
    private val liveNotifier: TyphoonLiveNotifier,
    private val alertNotifier: TyphoonAlertNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = preferences.settings.first()

            if (!settings.liveActivityEnabled && !settings.emergencyAlertsEnabled) {
                liveNotifier.cancel()
                alertNotifier.publish(emptyList(), enabled = false)
                return Result.success()
            }

            typhoonRepository.getActiveTyphoons()
                .onSuccess { feed ->
                    val active = feed.typhoons.filter { it.status == "active" }
                    if (settings.emergencyAlertsEnabled) {
                        val userLocation = if (settings.locationAlertsEnabled) {
                            // Prefer a fresh fix when OS still grants it in background;
                            // otherwise reuse last foreground cache.
                            val fresh = if (locationProvider.hasLocationPermission()) {
                                locationProvider.getLocation(timeoutMs = 8_000L)
                            } else {
                                null
                            }
                            fresh?.also { preferences.cacheUserLocation(it) }
                                ?: settings.cachedUserLocation
                                ?: preferences.getCachedUserLocation()
                        } else {
                            null
                        }
                        warningRepository.fetchTyphoonAlerts(active, userLocation)
                            .onSuccess { alerts ->
                                alertNotifier.publish(alerts, enabled = true)
                            }
                            .onFailure { err ->
                                Log.w(TAG, "Alert refresh failed: ${err.message}")
                            }
                    } else {
                        alertNotifier.publish(emptyList(), enabled = false)
                    }
                    // Always refresh Live after alerts so it isn't swallowed by Alerting grouping.
                    if (settings.liveActivityEnabled) {
                        liveNotifier.update(active, enabled = true)
                    } else {
                        liveNotifier.cancel()
                    }
                }
                .onFailure { err ->
                    Log.w(TAG, "Background refresh failed: ${err.message}")
                }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker error", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "TyphoonLiveWorker"
        const val UNIQUE_WORK = "typhoon_live_update"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TyphoonLiveUpdateWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK)
        }
    }
}

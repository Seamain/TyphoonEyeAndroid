package seamain.org.typhoonEye.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import seamain.org.typhoonEye.domain.model.UserLocation
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Thin wrapper around Fused Location. Never throws — returns null when
 * permission missing / Play Services unavailable / timeout.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Prefer a fresh fix; fall back to last-known. Applies a hard timeout so
     * callers (UI / Worker) never hang.
     */
    suspend fun getLocation(timeoutMs: Long = 12_000L): UserLocation? {
        if (!hasLocationPermission()) return null
        return withTimeoutOrNull(timeoutMs) {
            currentLocation() ?: lastKnownLocation()
        }?.also { loc ->
            Log.d(TAG, "Got location ${loc.latitude},${loc.longitude}")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): UserLocation? {
        if (!hasLocationPermission()) return null
        return runCatching {
            val cts = CancellationTokenSource()
            val priority = if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            } else {
                Priority.PRIORITY_LOW_POWER
            }
            val loc = client.getCurrentLocation(priority, cts.token).awaitTask()
                ?: return null
            UserLocation(
                latitude = loc.latitude,
                longitude = loc.longitude,
                updatedAtEpochMs = loc.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            ).withOptionalLabel()
        }.onFailure { Log.w(TAG, "currentLocation failed: ${it.message}") }
            .getOrNull()
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastKnownLocation(): UserLocation? {
        if (!hasLocationPermission()) return null
        return runCatching {
            val loc = client.lastLocation.awaitTask() ?: return null
            UserLocation(
                latitude = loc.latitude,
                longitude = loc.longitude,
                updatedAtEpochMs = loc.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            ).withOptionalLabel()
        }.onFailure { Log.w(TAG, "lastKnown failed: ${it.message}") }
            .getOrNull()
    }

    private suspend fun UserLocation.withOptionalLabel(): UserLocation {
        if (!Geocoder.isPresent()) return this
        val label = withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val list = if (Build.VERSION.SDK_INT >= 33) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            cont.resume(addresses)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                }
                val a = list?.firstOrNull() ?: return@runCatching ""
                listOfNotNull(
                    a.locality ?: a.subAdminArea,
                    a.adminArea
                ).distinct().joinToString(" · ")
            }.getOrDefault("")
        }
        return if (label.isBlank()) this else copy(label = label)
    }

    companion object {
        private const val TAG = "LocationProvider"
    }
}

private suspend fun <T> Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { value -> cont.resume(value) }
        addOnFailureListener { e -> cont.resumeWithException(e) }
        addOnCanceledListener { cont.cancel() }
    }

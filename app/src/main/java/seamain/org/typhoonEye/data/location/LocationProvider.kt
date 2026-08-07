package seamain.org.typhoonEye.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
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

/**
 * Pure Android OS LocationManager implementation without any Google Play Services (GMS) dependencies,
 * compliant with F-Droid inclusion policies.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
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
        val lm = locationManager ?: return null
        if (!hasLocationPermission()) return null

        return runCatching {
            val loc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getCurrentLocationApi30(lm) ?: requestSingleUpdateLegacy(lm)
            } else {
                requestSingleUpdateLegacy(lm)
            } ?: return null

            UserLocation(
                latitude = loc.latitude,
                longitude = loc.longitude,
                updatedAtEpochMs = loc.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            ).withOptionalLabel()
        }.onFailure { Log.w(TAG, "currentLocation failed: ${it.message}") }
            .getOrNull()
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationApi30(lm: LocationManager): Location? {
        val provider = getBestProvider(lm) ?: return null
        val cancellationSignal = CancellationSignal()
        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { cancellationSignal.cancel() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                lm.getCurrentLocation(
                    provider,
                    cancellationSignal,
                    context.mainExecutor
                ) { location ->
                    if (cont.isActive) {
                        cont.resume(location)
                    }
                }
            } else {
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdateLegacy(lm: LocationManager): Location? {
        val provider = getBestProvider(lm) ?: return null
        return suspendCancellableCoroutine { cont ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    if (cont.isActive) {
                        cont.resume(location)
                    }
                }
                @Deprecated("Deprecated in API 29")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    lm.removeUpdates(this)
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
            }

            cont.invokeOnCancellation {
                lm.removeUpdates(listener)
            }

            try {
                lm.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                lm.removeUpdates(listener)
                if (cont.isActive) {
                    cont.resume(null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastKnownLocation(): UserLocation? {
        val lm = locationManager ?: return null
        if (!hasLocationPermission()) return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (lm.isProviderEnabled(provider)) {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.time > bestLocation.time)) {
                        bestLocation = loc
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: IllegalArgumentException) {
            }
        }

        val loc = bestLocation ?: return null
        return UserLocation(
            latitude = loc.latitude,
            longitude = loc.longitude,
            updatedAtEpochMs = loc.time.takeIf { it > 0L } ?: System.currentTimeMillis()
        ).withOptionalLabel()
    }

    private fun getBestProvider(lm: LocationManager): String? {
        return when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
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


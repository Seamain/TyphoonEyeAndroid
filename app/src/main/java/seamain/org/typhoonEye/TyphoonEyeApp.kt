package seamain.org.typhoonEye

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import seamain.org.typhoonEye.data.preferences.AppLanguage
import seamain.org.typhoonEye.data.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltAndroidApp
class TyphoonEyeApp : Application(), Configuration.Provider {

    @Inject lateinit var preferences: UserPreferencesRepository
    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // AppCompat restores locales via localeConfig / auto_store_locales.
        // Re-sync DataStore → AppCompat when they diverge (incl. System = empty list).
        applicationScope.launch {
            runCatching {
                val stored = preferences.settings.first().appLanguage
                val appLocales = AppCompatDelegate.getApplicationLocales()
                val followingSystem = appLocales.isEmpty
                val needsApply = when (stored) {
                    AppLanguage.System -> !followingSystem
                    else -> {
                        val current = AppLanguage.fromLocaleTags(appLocales.toLanguageTags())
                        current != stored
                    }
                }
                if (needsApply) {
                    AppLanguage.apply(stored)
                }
            }
        }
    }
}

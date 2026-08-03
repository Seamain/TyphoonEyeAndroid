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
        // AppCompat already restores locales via localeConfig / auto_store_locales.
        // Only re-sync DataStore → AppCompat when they diverge — never block startup.
        applicationScope.launch {
            runCatching {
                val stored = preferences.settings.first().appLanguage
                val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                if (currentTags.isBlank() || AppLanguage.fromLocaleTags(currentTags) != stored) {
                    AppLanguage.apply(stored)
                }
            }
        }
    }
}

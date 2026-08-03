package seamain.org.typhoonEye.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import seamain.org.typhoonEye.data.preferences.UserPreferencesRepository
import seamain.org.typhoonEye.live.TyphoonAlertNotifier
import seamain.org.typhoonEye.live.TyphoonLiveNotifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository = UserPreferencesRepository(context)

    @Provides
    @Singleton
    fun provideTyphoonLiveNotifier(
        @ApplicationContext context: Context
    ): TyphoonLiveNotifier = TyphoonLiveNotifier(context)

    @Provides
    @Singleton
    fun provideTyphoonAlertNotifier(
        @ApplicationContext context: Context,
        preferences: UserPreferencesRepository
    ): TyphoonAlertNotifier = TyphoonAlertNotifier(context, preferences)
}

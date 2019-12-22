package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.broadcast.Broadcasters
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(app: ShuffleApplication): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    fun provideBroadcasters(): Broadcasters = Broadcasters()
}
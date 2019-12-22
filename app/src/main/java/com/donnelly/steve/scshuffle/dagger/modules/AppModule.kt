package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.broadcast.Broadcasters
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val application: ShuffleApplication) {

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun provideBroadcasters(): Broadcasters = Broadcasters()
}
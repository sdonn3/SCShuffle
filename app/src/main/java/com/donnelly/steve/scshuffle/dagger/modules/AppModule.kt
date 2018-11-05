package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import com.donnelly.steve.scshuffle.application.RxBus
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val application: Application) {
    private val rxBus = RxBus()

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun provideRxBus(): RxBus {
        return rxBus
    }
}
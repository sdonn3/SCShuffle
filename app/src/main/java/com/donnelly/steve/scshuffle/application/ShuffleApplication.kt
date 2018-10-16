package com.donnelly.steve.scshuffle.application

import android.app.Application
import com.donnelly.steve.scshuffle.dagger.components.DaggerNetComponent
import com.donnelly.steve.scshuffle.dagger.components.NetComponent
import com.donnelly.steve.scshuffle.dagger.modules.AppModule
import com.donnelly.steve.scshuffle.dagger.modules.NetModule

class ShuffleApplication : Application() {
    companion object {
        private const val BASE_URL = "https://api.soundcloud.com"
    }

    lateinit var netComponent: NetComponent

    override fun onCreate() {
        super.onCreate()

        netComponent = DaggerNetComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule(BASE_URL))
                .build()
    }
}
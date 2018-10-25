package com.donnelly.steve.scshuffle.application

import android.app.Application
import com.donnelly.steve.scshuffle.dagger.components.DaggerNetComponent
import com.donnelly.steve.scshuffle.dagger.components.DaggerPlayerComponent
import com.donnelly.steve.scshuffle.dagger.components.NetComponent
import com.donnelly.steve.scshuffle.dagger.components.PlayerComponent
import com.donnelly.steve.scshuffle.dagger.modules.AppModule
import com.donnelly.steve.scshuffle.dagger.modules.DatabaseModule
import com.donnelly.steve.scshuffle.dagger.modules.NetModule

class ShuffleApplication : Application() {
    companion object {
        private const val BASE_URL = "https://api.soundcloud.com"
        private const val BASE_URL_V2 = "https://api-v2.soundcloud.com"
    }

    lateinit var netComponent: NetComponent
    lateinit var playerComponent: PlayerComponent

    override fun onCreate() {
        super.onCreate()

        netComponent = DaggerNetComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule(BASE_URL, BASE_URL_V2))
                .build()

        playerComponent = DaggerPlayerComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule(BASE_URL, BASE_URL_V2))
                .databaseModule(DatabaseModule(this))
                .build()
    }
}
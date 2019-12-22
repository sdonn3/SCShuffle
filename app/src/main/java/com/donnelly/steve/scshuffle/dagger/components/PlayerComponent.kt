package com.donnelly.steve.scshuffle.dagger.components

import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.dagger.modules.AppModule
import com.donnelly.steve.scshuffle.dagger.modules.DatabaseModule
import com.donnelly.steve.scshuffle.dagger.modules.NetModule
import com.donnelly.steve.scshuffle.dagger.modules.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DatabaseModule::class, NetModule::class, ViewModelModule::class])
interface PlayerComponent : AndroidInjector<ShuffleApplication> {
    @Component.Factory
    interface Factory {
        fun create(appModule: AppModule, databaseModule: DatabaseModule, @BindsInstance shuffleApplication: ShuffleApplication): PlayerComponent
    }
}
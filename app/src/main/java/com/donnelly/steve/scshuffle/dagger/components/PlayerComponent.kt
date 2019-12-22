package com.donnelly.steve.scshuffle.dagger.components

import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.dagger.modules.*
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            AppModule::class,
            DatabaseModule::class,
            NetModule::class,
            InjectorsModule::class,
            ViewModelModule::class
        ]
)
interface PlayerComponent : AndroidInjector<ShuffleApplication> {
    @Component.Factory
    interface Factory : AndroidInjector.Factory<ShuffleApplication>
}
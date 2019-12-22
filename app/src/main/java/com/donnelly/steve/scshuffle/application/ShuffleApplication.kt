package com.donnelly.steve.scshuffle.application

import com.donnelly.steve.scshuffle.dagger.components.PlayerComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ShuffleApplication : DaggerApplication() {
    private val playerComponent: PlayerComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerPlayerComponent.factory().create(this) as PlayerComponent
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = playerComponent
}
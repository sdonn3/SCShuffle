package com.donnelly.steve.scshuffle.dagger.components

import com.donnelly.steve.scshuffle.dagger.modules.AppModule
import com.donnelly.steve.scshuffle.dagger.modules.DatabaseModule
import com.donnelly.steve.scshuffle.dagger.modules.NetModule
import com.donnelly.steve.scshuffle.database.ShuffleDatabase
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.features.player.PlayerActivity
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules=[AppModule::class, DatabaseModule::class, NetModule::class])
interface PlayerComponent{
    fun inject(activity: PlayerActivity)
    fun inject(viewModel: PlayerViewModel)

    fun trackDao() : TrackDao
    fun shuffleDatabase() : ShuffleDatabase
    fun scService() : SCService
    fun scServiceV2() : SCServiceV2
}
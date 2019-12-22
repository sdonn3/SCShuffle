package com.donnelly.steve.scshuffle.dagger.modules

import com.donnelly.steve.scshuffle.features.login.LoginActivity
import com.donnelly.steve.scshuffle.features.player.PlayerActivity
import com.donnelly.steve.scshuffle.features.player.fragment.LibraryFragment
import com.donnelly.steve.scshuffle.features.player.fragment.PlaylistFragment
import com.donnelly.steve.scshuffle.features.player.service.AudioService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface InjectorsModule {
    @ContributesAndroidInjector
    fun playerActivity(): PlayerActivity

    @ContributesAndroidInjector
    fun loginActivity(): LoginActivity

    @ContributesAndroidInjector
    fun libraryFragment(): LibraryFragment

    @ContributesAndroidInjector
    fun playlistFragment(): PlaylistFragment

    @ContributesAndroidInjector
    fun audioService(): AudioService
}
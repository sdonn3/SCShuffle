package com.donnelly.steve.scshuffle.dagger.modules

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.features.player.playlist.Playlist
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
    fun providePlaylist(trackDao: TrackDao): Playlist = Playlist(trackDao)
}
package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import androidx.room.Room
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.ShuffleDatabase
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(val application: ShuffleApplication) {

    @Singleton
    @Provides
    fun providesDatabase(): ShuffleDatabase {
        return Room.databaseBuilder(
                application,
                ShuffleDatabase::class.java,
                "shuffleDB"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun providesTrackDao(shuffleDatabase: ShuffleDatabase): TrackDao = shuffleDatabase.trackDao()
}
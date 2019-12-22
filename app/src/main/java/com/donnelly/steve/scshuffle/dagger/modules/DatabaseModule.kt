package com.donnelly.steve.scshuffle.dagger.modules

import androidx.room.Room
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.ShuffleDatabase
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun providesDatabase(application: ShuffleApplication): ShuffleDatabase {
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
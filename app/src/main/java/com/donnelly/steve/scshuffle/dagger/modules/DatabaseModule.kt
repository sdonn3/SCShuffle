package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import androidx.room.Room
import com.donnelly.steve.scshuffle.database.ShuffleDatabase
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(application: Application) {
    private val shuffleDatabase: ShuffleDatabase = Room.databaseBuilder(
            application, ShuffleDatabase::class.java, "shuffleDB"
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun providesDatabase(): ShuffleDatabase {
        return shuffleDatabase
    }

    @Singleton
    @Provides
    fun providesDao(): TrackDao {
        return shuffleDatabase.trackDao()
    }
}
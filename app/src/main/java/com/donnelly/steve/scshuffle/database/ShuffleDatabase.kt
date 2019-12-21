package com.donnelly.steve.scshuffle.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.models.Track

@Database(entities = [Track::class], version=1)
abstract class ShuffleDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
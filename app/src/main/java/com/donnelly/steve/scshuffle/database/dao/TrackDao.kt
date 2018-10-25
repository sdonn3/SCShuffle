package com.donnelly.steve.scshuffle.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.donnelly.steve.scshuffle.network.models.Track
import io.reactivex.Flowable

@Dao
interface TrackDao {
    @Query("SELECT * FROM trackData")
    fun getAllTracks(): Flowable<List<Track>>

    @Query("SELECT * FROM trackData WHERE scShuffleId = :scID")
    fun loadSingle(scID: Int): Flowable<Track>

    @Insert(onConflict = REPLACE)
    fun insert(track: Track)

    @Query("DELETE from trackData")
    fun clearAllTracks()

    @Query("SELECT count(*) FROM trackData")
    fun getCount(): Flowable<Int>
}
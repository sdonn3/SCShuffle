package com.donnelly.steve.scshuffle.database.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.donnelly.steve.scshuffle.network.models.Track

@Dao
interface TrackDao {
    @Query("SELECT * FROM trackData WHERE title LIKE '%' || :input || '%' ORDER BY title ASC")
    fun getSearchedTracksPaged(input: String): DataSource.Factory<Int, Track>

    @Query("SELECT * FROM trackData ORDER BY title ASC")
    fun getAllTracksPaged(): DataSource.Factory<Int, Track>

    @Query("SELECT * FROM trackData WHERE scShuffleId = :scID")
    fun loadSingle(scID: Int): Track

    @Query("SELECT * FROM trackData ORDER BY RANDOM() Limit 1")
    fun returnRandomTrack(): Track

    @Insert(onConflict = REPLACE)
    fun insert(track: Track)

    @Query("DELETE from trackData")
    fun clearAllTracks()

    @Query("SELECT count(*) FROM trackData")
    fun getCount(): Int
}
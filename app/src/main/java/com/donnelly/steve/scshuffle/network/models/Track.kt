package com.donnelly.steve.scshuffle.network.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "trackData")
data class Track(
        @PrimaryKey(autoGenerate = true) var scShuffleId: Int?,
        @ColumnInfo var artworkUrl: String?,
        @ColumnInfo var duration: Int?,
        @ColumnInfo var fullDuration:Int?,
        @ColumnInfo var genre: String?,
        @ColumnInfo var id: Int?,
        @ColumnInfo var streamable: Boolean?,
        @ColumnInfo var tagList: String?,
        @ColumnInfo var title: String?,
        @ColumnInfo var userId: String?,
        @ColumnInfo var waveformUrl: String?,
        @Ignore var media: TranscodingResponse?,
        @Ignore var user: UserV2?,
        @ColumnInfo var streamUrl: String?
) {
    constructor() : this (null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null )
}
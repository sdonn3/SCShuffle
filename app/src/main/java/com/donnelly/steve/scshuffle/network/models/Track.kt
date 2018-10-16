package com.donnelly.steve.scshuffle.network.models

data class Track(
        val artworkUrl: String?,
        val duration: Int?,
        val fullDuration:Int?,
        val genre: String?,
        val id: Int?,
        val streamable: Boolean?,
        val tagList: String?,
        val title: String?,
        val userId: String?,
        val waveformUrl: String?,
        val media: TranscodingResponse?,
        val user: UserV2?
)
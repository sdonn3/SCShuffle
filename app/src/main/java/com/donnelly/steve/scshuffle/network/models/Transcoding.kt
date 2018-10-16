package com.donnelly.steve.scshuffle.network.models

data class Transcoding(
        val url: String?,
        val preset: String?,
        val duration: Int?,
        val format: Format?,
        val quality: String?
)
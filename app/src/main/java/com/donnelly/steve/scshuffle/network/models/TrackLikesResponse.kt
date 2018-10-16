package com.donnelly.steve.scshuffle.network.models

data class TrackLikesResponse(
        val collection: List<CollectionResponse>?,
        val next_href: String?
)
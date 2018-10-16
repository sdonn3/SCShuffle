package com.donnelly.steve.scshuffle.network.models

data class User(
        val id: Int?,
        val permalink: String?,
        val uri: String?,
        val permalinkUrl: String?,
        val avatarUrl: String?,
        val country: String?,
        val fullName: String?,
        val city: String?,
        val description: String?,
        val publicFavoritesCount: Int?,
        val plan:String?
)
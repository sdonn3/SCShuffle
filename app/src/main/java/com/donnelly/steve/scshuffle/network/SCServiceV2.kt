package com.donnelly.steve.scshuffle.network

import com.donnelly.steve.scshuffle.network.models.StreamUrlResponse
import com.donnelly.steve.scshuffle.network.models.TrackLikesResponse
import com.donnelly.steve.scshuffle.network.models.WaveFormResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

private const val SOUNDCLOUD_CLIENT_ID = "IhtlaRd6b0rFJltJuuJANoRF5c2CQB9a"

interface SCServiceV2 {
    @GET("/users/{userId}/track_likes?client_id=$SOUNDCLOUD_CLIENT_ID")
    suspend fun getLikes(@Path("userId") userId: Int, @Query("limit") limit: Int, @Query("offset") offset: Long?): TrackLikesResponse

    @GET("")
    suspend fun getStreamUrl(@Url urlString: String, @Query("client_id") clientId: String = SOUNDCLOUD_CLIENT_ID): StreamUrlResponse

    @GET("")
    suspend fun getWaveform(@Url urlString: String): WaveFormResponse
}
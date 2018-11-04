package com.donnelly.steve.scshuffle.network

import com.donnelly.steve.scshuffle.network.models.StreamUrlResponse
import com.donnelly.steve.scshuffle.network.models.TrackLikesResponse
import com.donnelly.steve.scshuffle.network.models.WaveFormResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface SCServiceV2{
    companion object {
        const val SOUNDCLOUD_CLIENT_ID = "IhtlaRd6b0rFJltJuuJANoRF5c2CQB9a"
    }

    @GET("/users/{userId}/track_likes?client_id=$SOUNDCLOUD_CLIENT_ID")
    fun getLikes(@Path("userId") userId: Int, @Query("limit") limit: Int, @Query("offset") offset: Long?): Observable<TrackLikesResponse>

    @GET("")
    fun getStreamUrl(@Url urlString: String, @Query("client_id") clientId : String): Observable<StreamUrlResponse>

    @GET("")
    fun getWaveform(@Url urlString: String): Observable<WaveFormResponse>
}
package com.donnelly.steve.scshuffle.network

import com.donnelly.steve.scshuffle.network.models.TokenResponse
import com.donnelly.steve.scshuffle.network.models.User
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val SOUNDCLOUD_CLIENT_ID = "IhtlaRd6b0rFJltJuuJANoRF5c2CQB9a"
private const val REDIRECT_URI = "scshuffle://redirect"
private const val GRANT_TYPE_AUTH_CODE = "authorization_code"
private const val SOUNDCLOUD_CLIENT_SECRET = "lSeWtXC7UFZ2oH0Yyqov6oBEn3qLxkCX"

interface SCService{
    @POST("/oauth2/token?client_id=$SOUNDCLOUD_CLIENT_ID&client_secret=$SOUNDCLOUD_CLIENT_SECRET&redirect_uri=$REDIRECT_URI&grant_type=$GRANT_TYPE_AUTH_CODE")
    suspend fun token(@Query("code") authCode: String): TokenResponse

    @GET("/me")
    suspend fun me(@Query("oauth_token")token: String): User
}
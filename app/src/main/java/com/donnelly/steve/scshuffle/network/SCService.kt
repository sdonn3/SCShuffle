package com.donnelly.steve.scshuffle.network

import com.donnelly.steve.scshuffle.network.models.TokenResponse
import com.donnelly.steve.scshuffle.network.models.User
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SCService{
    companion object {
        const val SOUNDCLOUD_CLIENT_ID = "IhtlaRd6b0rFJltJuuJANoRF5c2CQB9a"
        const val SOUNDCLOUD_CLIENT_SECRET = "lSeWtXC7UFZ2oH0Yyqov6oBEn3qLxkCX"
        const val GRANT_TYPE_AUTH_CODE = "authorization_code"
        const val REFRESH_TOKEN = "refresh_token"
        const val REDIRECT_URI = "scshuffle://redirect"
    }

    @POST("/oauth2/token?client_id=$SOUNDCLOUD_CLIENT_ID&client_secret=$SOUNDCLOUD_CLIENT_SECRET&redirect_uri=$REDIRECT_URI&grant_type=$GRANT_TYPE_AUTH_CODE")
    fun token(@Query("code") authCode: String): Single<TokenResponse>

    @GET("/me")
    fun me(@Query("oauth_token")token: String): Observable<User>
}
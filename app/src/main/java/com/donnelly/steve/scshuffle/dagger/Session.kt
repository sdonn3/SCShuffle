package com.donnelly.steve.scshuffle.dagger

import android.content.SharedPreferences

class Session (
        private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val AUTH_CODE = "AUTH_CODE"
        private const val OAUTH_TOKEN = "OAUTH_TOKEN"
    }

    var authCode: String?
    var authToken: String?

    init{
        authCode = sharedPreferences.getString(AUTH_CODE, null)
        authToken = sharedPreferences.getString(OAUTH_TOKEN, null)
    }

    fun putAuthCode(code: String) {
        authCode = code
        sharedPreferences.edit().putString(AUTH_CODE, code).apply()
    }

    fun putAuthToken(token: String) {
        authToken = token
        sharedPreferences.edit().putString(OAUTH_TOKEN, token).apply()
    }
}
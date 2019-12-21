package com.donnelly.steve.scshuffle.dagger

import android.content.SharedPreferences

private const val AUTH_CODE = "AUTH_CODE"
private const val OAUTH_TOKEN = "OAUTH_TOKEN"

class Session (
        private val sharedPreferences: SharedPreferences
) {
    var authCode: String?
        get() = sharedPreferences.getString(AUTH_CODE, null)
        set(value) = sharedPreferences.edit().putString(AUTH_CODE, value).apply()

    var authToken: String?
        get() = sharedPreferences.getString(OAUTH_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(OAUTH_TOKEN, value).apply()

}
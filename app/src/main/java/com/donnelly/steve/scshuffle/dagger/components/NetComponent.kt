package com.donnelly.steve.scshuffle.dagger.components

import com.donnelly.steve.scshuffle.features.login.LoginActivity
import com.donnelly.steve.scshuffle.dagger.modules.AppModule
import com.donnelly.steve.scshuffle.dagger.modules.NetModule
import com.donnelly.steve.scshuffle.network.SCService
import com.google.gson.Gson
import dagger.Component
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Singleton
@Component(modules=[AppModule::class, NetModule::class])
interface NetComponent {
    fun inject(activity: LoginActivity)

    fun retrofit() : Retrofit
    fun okHttpClient() : OkHttpClient
    fun scService() : SCService
    fun gson() : Gson
}
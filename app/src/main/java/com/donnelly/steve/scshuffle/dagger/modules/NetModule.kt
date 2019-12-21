package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetModule(val baseUrl: String, val baseUrlV2: String) {
    companion object {
        private const val BYTES_PER_KILOBYTE = 1024
        private const val KILOBYTES_PER_MEGABYTE = 1024
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    fun provideOkHttpCache(application: Application): Cache {
        val cacheSize = 10L * BYTES_PER_KILOBYTE * KILOBYTES_PER_MEGABYTE
        return Cache(application.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val clientBuilder = OkHttpClient.Builder().addInterceptor(interceptor)
        clientBuilder.cache(cache)
        return clientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build()
    }

    @Provides
    @Singleton
    fun provideSCService(retrofit: Retrofit): SCService {
        return retrofit.create(SCService::class.java)
    }

    @Provides
    @Singleton
    fun provideSCServiceV2(gson: Gson, okHttpClient: OkHttpClient): SCServiceV2 {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrlV2)
                .client(okHttpClient)
                .build()
        return retrofit.create(SCServiceV2::class.java)
    }

    @Provides
    @Singleton
    fun provideSession(sharedPreferences: SharedPreferences): Session {
        return Session(sharedPreferences)
    }
}
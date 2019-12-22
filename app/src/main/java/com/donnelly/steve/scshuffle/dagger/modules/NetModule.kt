package com.donnelly.steve.scshuffle.dagger.modules

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.broadcast.Broadcasters
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
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val BYTES_PER_KILOBYTE = 1024
private const val KILOBYTES_PER_MEGABYTE = 1024

@Module
class NetModule {
    @Provides
    @Named("baseUrl")
    fun provideBaseUrl() = "https://api.soundcloud.com"

    @Provides
    @Named("baseUrl2")
    fun provideBaseUrlV2() = "https://api-v2.soundcloud.com"

    @Provides
    @Singleton
    fun provideOkHttpCache(application: ShuffleApplication): Cache {
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
    fun provideRetrofit(
            @Named("baseUrl") baseUrl: String,
            gson: Gson,
            okHttpClient: OkHttpClient
    ): Retrofit {
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
    fun provideSCServiceV2(
            @Named("baseUrl2") baseUrl2:
            String, gson: Gson,
            okHttpClient:
            OkHttpClient
    ): SCServiceV2 {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl2)
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
package com.fakhrirasyids.leech.data.remote.network

import com.fakhrirasyids.leech.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal object LeechApiConfig {
    @Volatile
    private var INSTANCE: LeechApiService? = null

    @JvmSynthetic
    fun getApiService(): LeechApiService = INSTANCE ?: synchronized(this) {
        INSTANCE ?: Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(
                OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build()
            )
            .build()
            .create(LeechApiService::class.java)
            .also { INSTANCE = it }
    }

    private const val DEFAULT_READ_TIMEOUT = 10000L
    private const val DEFAULT_CONNECT_TIMEOUT = 10000L
}

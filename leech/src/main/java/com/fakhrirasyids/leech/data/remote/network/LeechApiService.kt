package com.fakhrirasyids.leech.data.remote.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url

internal interface LeechApiService {
    @Streaming
    @GET
    suspend fun getDownload(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = mapOf()
    ): ResponseBody
}

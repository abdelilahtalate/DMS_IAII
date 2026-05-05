package com.example.dms_app.core.network
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.dms_app.core.config.AppConfig

object ApiClient {

    private val BASE_URL = AppConfig.BASE_URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Skip logging the body for the stream endpoint, otherwise OkHttp 
    // will try to buffer the infinite stream into memory and block forever.
    private val customLoggingInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        if (request.url.encodedPath.contains("preview")) {
            chain.proceed(request)
        } else {
            logging.intercept(chain)
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(customLoggingInterceptor)
        .readTimeout(0, java.util.concurrent.TimeUnit.SECONDS) // Infinite timeout for stream
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

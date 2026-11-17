package com.example.studentmvvm.network

import com.example.studentmvvm.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()
    val apiService: StudentApiService by lazy {
        Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient).addConverterFactory(GsonConverterFactory.create())
            .build().create(StudentApiService::class.java)
    }
}

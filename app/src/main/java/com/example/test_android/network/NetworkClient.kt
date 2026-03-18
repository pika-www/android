package com.example.test_android.network

import com.example.test_android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    // 定义一个内部常量，确保以 / 结尾
    private const val COMMON_PATH = "cephalon/user-center/v1/"
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + COMMON_PATH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 暴露给外部调用的 ApiService
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
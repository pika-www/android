package com.example.test_android.network

import android.content.Context
import com.example.test_android.BuildConfig
import com.example.test_android.network.interceptor.TokenInterceptor
import com.example.test_android.network.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private var okHttpClient: OkHttpClient? = null

    // 在 Application 的 onCreate 中调用 NetworkClient.init(this)
    fun init(ctx: Context) {

        // 关键点：强制使用 ApplicationContext，防止 Activity 内存泄露
        val appContext = ctx.applicationContext

        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor(appContext))
                .addInterceptor(AuthInterceptor(appContext))
                .build()
        }
    }

    private val retrofit by lazy {
        val client = okHttpClient ?: throw IllegalStateException("请先调用 NetworkClient.init(context)")
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "cephalon/user-center/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatService: ChatService by lazy {
        retrofit.create(ChatService::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
package com.example.test_android.network.interceptor

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import com.example.test_android.network.TokenManager

class TokenInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val token = TokenManager(context).getToken()

        val newRequest = request.newBuilder().apply {

            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }

        }.build()

        return chain.proceed(newRequest)
    }
}
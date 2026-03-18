package com.example.test_android.network

import com.example.test_android.model.LoginData
import com.example.test_android.model.LoginRequest
import com.example.test_android.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse<LoginData>>
}

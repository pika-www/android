package com.example.test_android.model

import com.google.gson.annotations.SerializedName

data class CallbackResponse<T>(
    val code: Int,
    val msg: String,
    val data: T
)


data class LoginData(
    val token: String,
    val id: String?,
    @SerializedName("nick_name")
    val nickName: String?,
    val phone: String?,
    @SerializedName("user_status")
    val userStatus: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("jpg_url")
    val avatarUrl: String?
)

data class LoginRequest(
    @SerializedName("app_type")
    val appType: String,
    val phone: String,
    val pwd: String,
    val way: String
)
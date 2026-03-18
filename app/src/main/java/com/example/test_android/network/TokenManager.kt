package com.example.test_android.network

import android.content.Context
import android.content.SharedPreferences
import com.example.test_android.model.LoginData
import com.google.gson.Gson

/**
 * 用户数据管理工具类（持久化存储整个用户信息）
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val gson = Gson() // ✨ 用于对象与 JSON 字符串的转换

    /**
     * ✨ 核心修改：存储整个 LoginData 对象
     */
    fun saveUserData(userData: LoginData) {
        // 将对象转为 JSON 字符串
        val userJson = gson.toJson(userData)
        prefs.edit().putString("user_data_json", userJson).apply()

        // 为了方便旧代码调用，我们依然保留一个单独的 Token 字段（可选）
        prefs.edit().putString("auth_token", userData.token).apply()
    }

    /**
     * ✨ 核心修改：获取整个 LoginData 对象
     */
    fun getUserData(): LoginData? {
        val userJson = prefs.getString("user_data_json", null)
        return if (userJson != null) {
            // 将 JSON 字符串转回 LoginData 对象
            gson.fromJson(userJson, LoginData::class.java)
        } else {
            null
        }
    }

    /**
     * 获取 Token (保持兼容性)
     */
    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    /**
     * 清除所有用户数据（退出登录时调用）
     */
    fun clearAll() {
        prefs.edit().clear().apply() // ✨ 一次性清空所有数据
    }
}

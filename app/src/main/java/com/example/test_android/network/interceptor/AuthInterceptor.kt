package com.example.test_android.network.interceptor

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.test_android.network.TokenManager
import com.example.test_android.ui.MainActivity
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

class AuthInterceptor(private val context: Context) : Interceptor {

    companion object {
        private val isRedirecting = AtomicBoolean(false)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // 1. 先检查 HTTP 状态码（处理原有的 4xx 逻辑）
        if (!response.isSuccessful) {
            if (response.code in 400..499) {
                handleUnauthorized()
            }
            return response
        }

        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE) // Buffer 整个响应体
        val buffer = source?.buffer

        // 克隆一份 body 字符串，防止原数据流关闭导致报错
        val responseString = buffer?.clone()?.readString(Charsets.UTF_8)

        if (!responseString.isNullOrEmpty()) {
            // 检查字符串中是否包含业务错误码
            // 这里的判断可以根据你的 JSON 结构更精确，比如使用 JSONObject 解析
            if (responseString.contains("\"code\":40000") || responseString.contains("token 失效")) {
                handleUnauthorized()
                showToast("登录已失效，请重新登录")
            }
        }

        return response
    }

    /**
     * 将你提供的 switch 逻辑转换为 Kotlin 的 when 表达式
     */
    private fun getErrorMessage(status: Int, url: String): String {
        return when (status) {
            302 -> "接口重定向了！"
            400 -> "参数不正确！"
            401 -> "您未登录，或者登录已经超时，请先登录！"
            403 -> "您没有权限操作！"
            404 -> "请求地址出错: $url"
            408 -> "请求超时！"
            409 -> "系统已存在相同数据！"
            500 -> "服务繁忙！"
            501 -> "服务未实现！"
            502 -> "网关错误！"
            503 -> "服务不可用！"
            504 -> "服务暂时无法访问，请稍后再试！"
            505 -> "HTTP版本不受支持！"
            else -> "异常问题 (错误码: $status)，请稍后再试！"
        }
    }

    /**
     * 处理 401 未授权跳转
     */
    private fun handleUnauthorized() {
        // 1. 清除 Token
        TokenManager(context).clearAll()

        // 2. 检查是否正在跳转中，防止重复开启 Activity
        if (isRedirecting.compareAndSet(false, true)) {
            val intent = Intent(context, MainActivity::class.java).apply {
                // 必须加这两个 Flag，否则非 Activity 环境启动会报错，且无法清空旧页面
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)

            // 3. 延迟重置状态（给系统一点时间完成 Activity 的启动）
            // 或者：根本不需要在这里 reset，直到用户重新登录成功后再由外部逻辑重置
            Handler(Looper.getMainLooper()).postDelayed({
                isRedirecting.set(false)
            }, 2000)
        }
    }

    /**
     * 拦截器在子线程运行，通过 Handler 切换到主线程显示 Toast
     */
    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
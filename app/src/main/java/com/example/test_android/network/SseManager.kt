package com.example.test_android.network

import android.util.Log
import com.example.test_android.BuildConfig
import com.example.test_android.model.ChatItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SseManager(
    private val tokenManager: TokenManager,
    private val onMessageReceived: (String) -> Unit
) {

    private var eventSource: EventSource? = null

    /**
     * 启动 SSE 连接
     * @param url 请求地址
     * @param model 模型名称 (如 "DeepSeek-R1")
     * @param chatItems 完整的上下文对话列表
     */
    fun startSse(url: String, model: String, chatItems: List<ChatItem>) {
        // 1. 停止之前的连接（防止打字机冲突）
        stopSse()

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES) // SSE 必须长连接
            .build()

        val domain = "${BuildConfig.BASE_URL}cephalon/user-center/v1/"
        val fullUrl = domain + url

        // 2. 构建符合 OpenAI/DeepSeek 标准的 JSON 请求体
        val root = JSONObject()
        root.put("model", model)
        root.put("stream", true)

        val msgArray = JSONArray()
        chatItems.forEach { item ->
            val msgObj = JSONObject()
            // 💡 关键：根据 ChatItem 类型映射后端需要的 role
            // 通常用户是 "user"，AI 是 "assistant"
            val role = if (item.type == ChatItem.TYPE_USER) "user" else "assistant"
            msgObj.put("role", role)
            msgObj.put("content", item.content)
            msgArray.put(msgObj)
        }
        root.put("messages", msgArray)

        // 3. 准备 RequestBody
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = root.toString().toRequestBody(mediaType)

        // 4. 获取最新的 Token
        val token = tokenManager.getToken() ?: ""

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .header("Content-Type", "application/json") // 明确指定 Content-Type
            .build()

        // 5. 定义监听器
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d("SSE", "连接已成功开启")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                // 核心：收到数据片段，通过回调传给 Activity
                onMessageReceived(data)
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("SSE", "服务器已关闭连接")
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("SSE", "连接失败或出错: ${t?.message}")
                // 如果需要，可以在这里触发一个重试或错误提示的回调
            }
        }

        // 6. 启动
        val factory = EventSources.createFactory(client)
        eventSource = factory.newEventSource(request, listener)
    }

    /**
     * 停止连接
     */
    fun stopSse() {
        eventSource?.cancel()
        eventSource = null
    }
}
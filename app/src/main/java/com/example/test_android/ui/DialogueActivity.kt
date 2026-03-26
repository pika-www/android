package com.example.test_android.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_android.R
import com.example.test_android.model.CallbackResponse
import com.example.test_android.model.ChatDetailData
import com.example.test_android.model.ChatItem
import com.example.test_android.network.NetworkClient
import com.example.test_android.network.SseManager
import com.example.test_android.network.TokenManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DialogueActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sseManager: SseManager
    private lateinit var tokenManager: TokenManager

    // 性能优化：缓冲区与节流控制
    private val sseBuffer = StringBuilder()
    private var lastUpdateTime = 0L
    private val updateINTERVAL = 100L // 每100ms合并一次碎片，防止刷新过快

    private val chatItems = mutableListOf<ChatItem>()
    private lateinit var adapter: DialogueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialogue)

        val chatName = intent.getStringExtra("chat_name")
        recyclerView = findViewById(R.id.rvChat)

        // ✨ 初始化：传入 list 引用
        adapter = DialogueAdapter(chatItems)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            // 建议开启，优化滚动体验
            stackFromEnd = false
        }

        tokenManager = TokenManager(this)

        // 初始化 SSE，回调处理
        sseManager = SseManager(tokenManager) { data ->
            handleSseMessage(data)
        }

        val btnSend = findViewById<Button>(R.id.btnSend)
        val etInput = findViewById<EditText>(R.id.etInput)

        supportActionBar?.title = chatName

        // 1. 加载历史数据
        loadHistoryData()

        // 2. 发送逻辑
        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotBlank()) {
                // UI 插入用户消息
                insertNewChatItem(ChatItem(text, ChatItem.TYPE_USER))
                etInput.setText("")

                // 准备 SSE
                val sseUrl = "model/chat/completions"
                val modelName = chatName ?: "DeepSeek-R1"

                // 发起前置清空
                sseBuffer.setLength(0)
                lastUpdateTime = 0L

                sseManager.startSse(sseUrl, modelName, chatItems)
            }
        }
    }

    /**
     * 加载历史记录：全量刷新
     */
    private fun loadHistoryData() {
        NetworkClient.chatService.getChatContent("69c25da75dedc2787a97f0c7")
            .enqueue(object : Callback<CallbackResponse<ChatDetailData>> {
                override fun onResponse(call: Call<CallbackResponse<ChatDetailData>>, response: Response<CallbackResponse<ChatDetailData>>) {
                    if (response.isSuccessful) {
                        val chatDetail = response.body()?.data
                        chatItems.clear()
                        chatDetail?.chatMessageBlocks?.forEach { block ->
                            block.chatMessages.forEach { msg ->
                                if (msg.userContent.isNotBlank()) chatItems.add(ChatItem(msg.userContent, ChatItem.TYPE_USER))
                                if (msg.systemContent.isNotBlank()) chatItems.add(ChatItem(msg.systemContent, ChatItem.TYPE_SYSTEM))
                            }
                        }
                        // 历史记录加载是全量的，可以使用这个，或者更高级的 DiffUtil
                        adapter.notifyDataSetChanged()
                        scrollToBottom()
                    }
                }
                override fun onFailure(call: Call<CallbackResponse<ChatDetailData>>, t: Throwable) {
                    Log.e("API_FAILURE", t.message ?: "error")
                }
            })
    }

    private fun handleSseMessage(data: String) {
        val raw = data.trim()

        // 处理结束
        if (raw == "[DONE]") {
            sseManager.stopSse()
            flushBuffer() // 最后一吐
            return
        }

        if (raw == "[FAILED]") {
            sseManager.stopSse()
            updateErrorMessage("请求超时")
            return
        }

        try {
            val json = JSONObject(raw)
            // 处理特殊 code
            val code = json.optInt("code", 0)
            if (code != 0 && code != 200) {
                val errorMsg = when(code) {
                    30002 -> "余额不足，请充值"
                    50000 -> "系统繁忙，请稍后再试"
                    else -> "接口异常($code)"
                }
                updateErrorMessage(errorMsg)
                sseManager.stopSse()
                return
            }

            // 解析 Content 碎片
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val content = choices.getJSONObject(0).optJSONObject("delta")?.optString("content") ?: ""

                if (content.isNotEmpty()) {
                    sseBuffer.append(content)
                    val now = System.currentTimeMillis()
                    // ⏳ 节流控制：每 100ms 刷新一次 UI
                    if (now - lastUpdateTime > updateINTERVAL) {
                        flushBuffer()
                        lastUpdateTime = now
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SSE", "JSON Error: $e")
        }
    }

    /**
     * ✨ 核心：将缓冲区内容同步到 UI
     */
    private fun flushBuffer() {
        val text = sseBuffer.toString()
        if (text.isEmpty()) return
        sseBuffer.setLength(0) // 清空

        runOnUiThread {
            appendSystemMessage(text)
        }
    }

    /**
     * ✨ 场景 1：插入全新的消息行 (notifyItemInserted)
     */
    private fun insertNewChatItem(item: ChatItem) {
        chatItems.add(item)
        adapter.notifyItemInserted(chatItems.size - 1)
        scrollToBottom()
    }

    /**
     * ✨ 场景 2：更新最后一条系统消息 (notifyItemChanged)
     */
    private fun appendSystemMessage(newChunk: String) {
        if (chatItems.isNotEmpty() && chatItems.last().type == ChatItem.TYPE_SYSTEM) {
            val lastIndex = chatItems.size - 1
            val currentContent = chatItems[lastIndex].content
            chatItems[lastIndex] = chatItems[lastIndex].copy(content = currentContent + newChunk)

            // 💡 仅刷新这一行，效率极高
            adapter.notifyItemChanged(lastIndex)
        } else {
            // 如果最后一条不是系统消息，则新建一行
            insertNewChatItem(ChatItem(newChunk, ChatItem.TYPE_SYSTEM))
        }
        scrollToBottom()
    }

    private fun updateErrorMessage(msg: String) {
        runOnUiThread {
            insertNewChatItem(ChatItem(msg, ChatItem.TYPE_SYSTEM))
        }
    }

    private fun scrollToBottom() {
        if (chatItems.isNotEmpty()) {
            recyclerView.post {
                recyclerView.scrollToPosition(chatItems.size - 1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sseManager.stopSse()
    }
}
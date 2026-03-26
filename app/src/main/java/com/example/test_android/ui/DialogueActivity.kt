package com.example.test_android.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.test_android.R
import com.example.test_android.model.CallbackResponse
import com.example.test_android.model.ChatDetailData
import com.example.test_android.model.ModelResp
import com.example.test_android.network.NetworkClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DialogueActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialogue)

        val chatId = intent.getStringExtra("chat_id")
        val chatName = intent.getStringExtra("chat_name")
        val btnSend = findViewById<Button>(R.id.btnSend)
        val etInput = findViewById<EditText>(R.id.etInput)

        android.util.Log.d("Dialogue", "id=$chatId name=$chatName")

        // 可以设置标题
        supportActionBar?.title = chatName

        // 强行断言 chatId 不可能为 null
        NetworkClient.chatService.getChatSessions(chatId!!).enqueue(object : Callback<CallbackResponse<List<ModelResp>>> {

            override fun onResponse(
                call: Call<CallbackResponse<List<ModelResp>>>,
                response: Response<CallbackResponse<List<ModelResp>>>
            ) {
                android.util.Log.d("API", "onResponse 触发")

                if (response.isSuccessful) {
                    val body = response.body()
                    val listItem = body?.data

                    android.util.Log.d("session","对话记录：${listItem}")
                }
            }

            override fun onFailure(
                call: Call<CallbackResponse<List<ModelResp>>>,
                t: Throwable
            ) {
                android.util.Log.e("API", "onFailure: ${t.message}")
            }
        })

        // 请求对话的聊天内容
        NetworkClient.chatService.getChatContent("69c25da75dedc2787a97f0c7")
            .enqueue(object : Callback<CallbackResponse<ChatDetailData>> {

                override fun onResponse(
                    call: Call<CallbackResponse<ChatDetailData>>,
                    response: Response<CallbackResponse<ChatDetailData>>
                ) {
                    if (response.isSuccessful) {
                        val chatDetail = response.body()?.data

                        android.util.Log.d("chatMessage","用户对话信息：${chatDetail}")

                        // 遍历获取所有消息
//                        chatDetail?.chat_message_blocks?.forEach { block ->
//                            block.chat_messages.forEach { msg ->
//                                android.util.Log.d("API_DATA", "用户说: ${msg.user_content}")
//                                android.util.Log.d("API_DATA", "系统回: ${msg.system_content}")
//                            }
//                        }
                    } else {
                        android.util.Log.e("API_ERROR", "错误码: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<CallbackResponse<ChatDetailData>>, t: Throwable) {
                    android.util.Log.e("API_FAILURE", "网络崩溃: ${t.message}")
                }
            })


        btnSend.setOnClickListener {
            val text = etInput.text.toString()
            android.util.Log.d("InputMsg","输入的信息：${text}")
            etInput.setText("")
        }
    }
}
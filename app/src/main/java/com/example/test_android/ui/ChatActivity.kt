package com.example.test_android.ui

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_android.R
import com.example.test_android.model.CallbackResponse
import com.example.test_android.model.ChatResponse
import com.example.test_android.network.NetworkClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatActivity : AppCompatActivity() {
    // 1. 在这里声明变量
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        requestData()
    }


    private fun requestData() {
        android.util.Log.d("API", "开始请求")

        NetworkClient.chatService.getChat(1,100, null).enqueue(object : Callback<CallbackResponse<ChatResponse>> {

            override fun onResponse(
                call: Call<CallbackResponse<ChatResponse>>,
                response: Response<CallbackResponse<ChatResponse>>
            ) {
                android.util.Log.d("API", "onResponse 触发")

                if (response.isSuccessful) {
                    val body = response.body()
                    val listItem = body?.data?.list


                    if(listItem != null){
                        runOnUiThread {
                            val adapter = ChatAdapter(listItem)
                            recyclerView.adapter = adapter

                            // 请求成功现在进行跳转
                            adapter.onItemClick = { item ->

                                val intent = Intent(this@ChatActivity, DialogueActivity::class.java)

                                intent.putExtra("chat_id", item.id)
                                intent.putExtra("chat_name", item.name)

                                startActivity(intent)
                            }
                        }
                    }




                } else {
                    // 处理 404, 500 等服务器错误
                    android.util.Log.e("API", "请求失败，错误码: ${response.code()}")
                }
            }

            override fun onFailure(
                call: Call<CallbackResponse<ChatResponse>>,
                t: Throwable
            ) {
                android.util.Log.e("API", "onFailure: ${t.message}")
            }
        })
    }
}
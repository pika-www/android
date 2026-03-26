package com.example.test_android.network

import com.example.test_android.model.ChatResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.test_android.model.CallbackResponse
import com.example.test_android.model.ModelResp
import com.example.test_android.model.ChatDetailData
import retrofit2.http.Path

interface ChatService {

    @GET("model")
    fun getChat(
        @Query("page_index") pageIndex: Int,
        @Query("page_size") pageSize: Int,
        @Query("model_id") modelId: String? = null
    ): Call<CallbackResponse<ChatResponse>>


    @GET("model/chat/sessions")
    fun getChatSessions(
        @Query("model_id") modelId: String
    ):Call<CallbackResponse<List<ModelResp>>>


    @GET("model/chat/session/{session_id}")
    fun getChatContent(
        @Path("session_id") sessionId: String
    ): Call<CallbackResponse<ChatDetailData>>
}
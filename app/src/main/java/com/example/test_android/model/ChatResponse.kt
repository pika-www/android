package com.example.test_android.model
import com.google.gson.annotations.SerializedName

//传参类型
data class ChatRequest(
    @SerializedName("page_index")
    val pageIndex: Int,
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("model_id")
    val modelId: String
)



/**
 * RespModelList
 */
data class ChatResponse (
    val list: List<ModelResp>? = null,

    @SerializedName("page_index")
    val pageIndex: Long? = null,

    @SerializedName("page_size")
    val pageSize: Long? = null,

    val total: Long? = null
)

data class ModelResp (
    val author: String? = null,

    @SerializedName("author_en")
    val authorEn: String? = null,

    @SerializedName("author_picture_url")
    val authorPictureURL: String? = null,

    val description: String? = null,

    @SerializedName("description_en")
    val descriptionEn: String? = null,

    val id: String? = null,

    @SerializedName("is_official")
    val isOfficial: Boolean? = null,

    @SerializedName("is_star")
    val isStar: Boolean? = null,

    @SerializedName("model_type")
    val modelType: String? = null,

    @SerializedName("model_url")
    val modelURL: String? = null,

    val name: String? = null,

    @SerializedName("picture_url")
    val pictureURL: String? = null,

    @SerializedName("star_count")
    val starCount: Long? = null,

    @SerializedName("user_count")
    val userCount: Long? = null
)



// 2. data 对应的对象
data class ChatDetailData(
    @SerializedName("session_name")
    val sessionName: String,

    @SerializedName("chat_message_blocks")
    val chatMessageBlocks: List<ChatBlock>
)

// 3. 对应 chat_message_blocks 数组里的项
data class ChatBlock(
    @SerializedName("block_id")
    val blockId: String,

    @SerializedName("chat_messages")
    val chatMessages: List<ChatMessage>
)

// 4. 对应真正的消息内容
data class ChatMessage(
    @SerializedName("user_content")
    val userContent: String,

    @SerializedName("system_content")
    val systemContent: String
)
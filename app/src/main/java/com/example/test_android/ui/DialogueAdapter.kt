package com.example.test_android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_android.R
import com.example.test_android.model.ChatItem
import io.noties.markwon.Markwon

class DialogueAdapter(private val dataList: List<ChatItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 声明 Markwon
    private lateinit var markwon: Markwon

    // --- 在这里定义 ViewHolder ---
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMsg: TextView = view.findViewById(R.id.tvMsgUser) // 确保 ID 正确
    }

    class SystemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMsg: TextView = view.findViewById(R.id.tvMsgSystem) // 确保 ID 正确
    }

    override fun getItemViewType(position: Int): Int = dataList[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (!::markwon.isInitialized) {
            markwon = Markwon.create(parent.context)
        }

        return when (viewType) {
            ChatItem.TYPE_USER -> UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false))
            ChatItem.TYPE_SYSTEM -> SystemViewHolder(inflater.inflate(R.layout.item_chat_system, parent, false))
            else -> throw IllegalArgumentException("未知类型")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        val formattedContent = item.content.replace("\\n", "\n")

        when (holder) {
            is UserViewHolder -> {
                // 现在 holder 被识别为 UserViewHolder，它拥有 tvMsg 属性了！
                holder.tvMsg.text = formattedContent.trim()
            }
            is SystemViewHolder -> {
                // 现在 holder 被识别为 SystemViewHolder，它也拥有 tvMsg 属性了！
                markwon.setMarkdown(holder.tvMsg, formattedContent.trim())
                holder.tvMsg.setTextIsSelectable(true)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
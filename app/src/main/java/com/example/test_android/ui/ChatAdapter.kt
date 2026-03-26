package com.example.test_android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_android.R
import com.example.test_android.model.ModelResp

class ChatAdapter(private val dataList: List<ModelResp>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // 1. 定义点击回调，传入 ModelResp 对象
    var onItemClick: ((ModelResp) -> Unit)? = null

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPicture: ImageView = view.findViewById(R.id.ivPicture)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvOfficial: TextView = view.findViewById(R.id.tvOfficial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = dataList[position]
        val context = holder.itemView.context

        // 2. 绑定点击事件
        holder.itemView.setOnClickListener {
            android.util.Log.d("ChatInfo","打印成功，${item}")
            onItemClick?.invoke(item)

        }

        // 文本渲染
        holder.tvName.text = context.getString(R.string.label_model_name, item.name)
        holder.tvAuthor.text = context.getString(R.string.label_author, item.author)
        holder.tvDescription.text = if (item.description.isNullOrEmpty()) {
            context.getString(R.string.label_no_description)
        } else item.description

        // 是否为官方镜像
        holder.tvOfficial.visibility = if (item.isOfficial == true) View.VISIBLE else View.GONE

        // 图片加载逻辑
        val finalImageUrl = if (!item.pictureURL.isNullOrEmpty()) {
            "https://myelin.cloud/minio/api/v1/buckets/static-host/objects/download?preview=true&prefix=${item.pictureURL}&version_id=null"
        } else {
            R.drawable.ic_launcher_background
        }

        Glide.with(context)
            .load(finalImageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.ivPicture)
    }

    override fun getItemCount(): Int = dataList.size
}
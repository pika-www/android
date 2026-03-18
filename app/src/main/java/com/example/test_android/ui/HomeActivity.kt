package com.example.test_android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.util.Log // ✨ 添加这一行，导入安卓日志工具
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test_android.R
import com.example.test_android.network.TokenManager
import coil.load

class HomeActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 2. 初始化 TokenManager
        tokenManager = TokenManager(this)

        // 找到布局中的渲染信息
        val btnLogout = findViewById<Button>(R.id.btn_logout)
        val btnGetInfo = findViewById<Button>(R.id.btn_get_info)
        val userName = findViewById<TextView>(R.id.user_name)
        val avatarImg = findViewById<ImageView>(R.id.iv_user_avatar)
        userName.text = "你好,${tokenManager.getUserData()?.nickName}"
        val imageUrl = "https://myelin.cloud/minio/api/v1/buckets/cephalon-frontend-test/objects/download?preview=true&prefix=${tokenManager.getUserData()?.avatarUrl}&version_id=null"

        avatarImg.load(imageUrl){
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_foreground)
        }

        // 2. 设置点击事件
        btnLogout.setOnClickListener {
            tokenManager.clearAll()
            // ✨ 跳转回登录页 (MainActivity)
            val intent = Intent(this, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            // 销毁当前主页
            finish()
        }





        btnGetInfo.setOnClickListener {
            // ✨ 核心：从 Storage 中获取存入的 Token
            val savedData = tokenManager.getUserData()

            if (savedData?.token != null) {
                // 打印日志查看
                Log.i("detail", "成功获取到 Token: ${savedData.token}")
                Log.i("userDetail", "已成功获取用户信息: $savedData ")
                // 弹出提示显示 Token（前几位）
                Toast.makeText(this, "当前 Token: ${savedData.token.take(10)}...", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("detail", "未找到存入的 Token！")
                Toast.makeText(this, "未登录或 Token 已失效", Toast.LENGTH_SHORT).show()
            }
        }



    }
}

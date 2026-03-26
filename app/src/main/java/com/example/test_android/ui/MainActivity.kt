package com.example.test_android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test_android.R
import com.example.test_android.model.LoginData
import com.example.test_android.model.LoginRequest
import com.example.test_android.model.CallbackResponse
//import com.example.test_android.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.test_android.network.TokenManager

import android.text.TextWatcher
import android.text.Editable
import com.example.test_android.network.NetworkClient
import com.google.android.material.textfield.TextInputLayout

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager

    // 示例：让启动页停留，直到数据加载完成
    private fun keepSplashScreenForData(splashScreen: SplashScreen) {
        var isReady = false
        // 模拟 2 秒的后台加载任务
        Handler(Looper.getMainLooper()).postDelayed({
            isReady = true
        }, 2000)

        // 核心逻辑：直到 isReady 为 true，启动页才会消失
        splashScreen.setKeepOnScreenCondition { !isReady }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. 初始化启动屏，这一步会处理主题的切换
        // 注意：这行代码必须在 setContentView 之前调用！！！
        val splashScreen = installSplashScreen()

        NetworkClient.init(this) // 👈 必须调用
        // 开启全屏沉浸式模式
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(this)
        keepSplashScreenForData(splashScreen)

        val savedToken  = tokenManager.getUserData()?.token
        // 检测是否有 token
        if(!savedToken.isNullOrEmpty()){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            // 销毁登录页
            finish()
            return
        }

      // 没有 token 继续执行
        setContentView(R.layout.activity_main)

        // 2. 处理状态栏和导航栏的边距适配
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. 找到布局中的控件
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val tilUsername = findViewById<TextInputLayout>(R.id.til_username)
        val etPassword = findViewById<EditText>(R.id.et_password)

        // 监听输入的用户名
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                val isError = input.isNotEmpty() && input.length != 11

                // 1. 让输入框背景变红 (触发 selector)
                etUsername.isActivated = isError

                // 2. 控制提示文字的显隐
                if (isError) {
                    tilUsername.error = "请输入正确的 11 位手机号"
                } else {
                    tilUsername.error = null
                    // 彻底清除错误状态，防止留白
                    tilUsername.isErrorEnabled = false
                    tilUsername.isErrorEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })



        // 5. 设置登录按钮点击事件
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // ✨ 构造 JSON 请求对象
                val loginRequest = LoginRequest(
                    appType = "platform",
                    phone = username,
                    pwd = password,
                    way = "phone_pwd"
                )

                // ✨ 发起异步网络请求
                NetworkClient.apiService.login(loginRequest).enqueue(object : Callback<CallbackResponse<LoginData>> {
                    override fun onResponse(call: Call<CallbackResponse<LoginData>>, response: Response<CallbackResponse<LoginData>>) {
                        // 在 Logcat 中打印原始结果，方便调试
                        Log.d("LoginResult", "HTTP 状态码: ${response.code()}")
                        Log.d("LoginResult", "服务器返回内容: ${response.body()}")

                        if (response.isSuccessful) {
                            val loginData = response.body()
                            if (loginData?.code == 20000) {
                              // val saveToken = loginData.data.token

                                tokenManager.saveUserData(loginData.data)

                                // ✨ 2. 存入“仓库” (内存管理)
                                // 注意：在 Activity 间共享 ViewModel 需要使用单例或 Application 级别，
                                // 简单起见，您可以先将数据通过 Intent 传给 HomeActivity
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.putExtra("user_data", loginData.msg) // 举例传个消息

                                Toast.makeText(this@MainActivity, "登录成功", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            } else {
                                // 业务逻辑错误（如：非法登录、密码错误）
                                Toast.makeText(this@MainActivity, "登录失败：${loginData?.msg}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 请求失败（如：404, 500 错误）
                            val errorMsg = response.errorBody()?.string()
                            Log.e("LoginResult", "请求失败详情: $errorMsg")
                            Toast.makeText(this@MainActivity, "服务器异常，请稍后再试", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CallbackResponse<LoginData>>, t: Throwable) {
                        // 网络连接失败（如：断网、超时）
                        Log.e("LoginResult", "网络连接失败: ${t.message}")
                        Toast.makeText(this@MainActivity, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

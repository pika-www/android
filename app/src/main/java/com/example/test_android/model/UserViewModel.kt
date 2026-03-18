package com.example.test_android.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    // 使用 LiveData 存储用户信息，界面可以“订阅”它的变化
    val userData = MutableLiveData<LoginResponse<LoginData>>()

    // 更新数据的方法
    fun setUserInfo(data: LoginResponse<LoginData>) {
        userData.value = data
    }
}

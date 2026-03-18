plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.test_android"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.test_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // 这里的 getByName("debug") 没问题
        getByName("debug") {
            buildConfigField("String", "BASE_URL", "\"https://test.unicorn.org.cn/\"")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        // 把两个 release 合并成一个
        getByName("release") {
            isDebuggable = false
            buildConfigField("String", "BASE_URL", "\"https://prod.unicorn.org.cn/\"")
            isMinifyEnabled = true

            // 💡 加上这一行：让 release 模式也使用 debug 的签名配置
            //  signingConfig = signingConfigs.getByName("debug")

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("io.coil-kt:coil:2.6.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 请求 app 接口

//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")


}
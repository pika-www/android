# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# 1. 保护模型类：防止字段名变成 a, b, c
# 请将 com.example.test_android 替换为你实际的包名
-keep class com.example.test_android.model.** { *; }

# 2. 专门保护 LoginResponse，防止 R8 认为它是抽象类或移除构造函数
-keep public class com.example.test_android.model.LoginResponse { *; }

# 3. 保护 Retrofit 和 Gson 的运行环境
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn retrofit2.**

# 4. 保护 OkHttp (防止 Network Inspector 报错)
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
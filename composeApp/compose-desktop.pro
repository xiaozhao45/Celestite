# 保持 Compose 内部使用的类不被混淆
-keepclassmembers class org.jetbrains.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# 保持你的主类不被混淆
-keep class com.xiaozhao45.celestite.MainKt { *; }

# 保持 Voyager 导航库（如果有反射调用）
-keep class cafe.adriel.voyager.** { *; }

# 保持 Settings 库
-keep class com.russhwolf.settings.** { *; }

# 忽略混淆时的警告
-dontwarn **
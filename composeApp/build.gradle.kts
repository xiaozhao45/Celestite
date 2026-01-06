import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose 基础库
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)

                // UI 主题
                implementation("com.materialkolor:material-kolor:4.0.5")

                // AndroidX Lifecycle
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                // Markdown 渲染
                implementation("com.mikepenz:multiplatform-markdown-renderer:0.38.1")
                implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.38.1")

                // Settings 本地存储
                implementation("com.russhwolf:multiplatform-settings:1.3.0")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.3.0")

                // Voyager 导航
                implementation("cafe.adriel.voyager:voyager-navigator:1.0.1")
                implementation("cafe.adriel.voyager:voyager-transitions:1.0.1")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.1")

                // 本地模块
                implementation(projects.shared)
                implementation(project(":xuan-utils-pro"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
            }
        }
    }
}

android {
    namespace = "com.xiaozhao45.celestite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.xiaozhao45.celestite"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        // 开启核心库脱糖 (API Desugaring)
        isCoreLibraryDesugaringEnabled = true

        // 统一使用 Java 11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// 必须在根级别添加脱糖依赖库
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}

compose.desktop {
    application {
        mainClass = "com.xiaozhao45.celestite.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.xiaozhao45.celestite"
            packageVersion = "1.0.0"
        }
    }
}
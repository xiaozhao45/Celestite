package com.xiaozhao45.celestite

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
package com.xiaozhao45.celestite

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import xuan.core.qimen.zhuan.QiMenZhiRun
import xuan.core.qimen.zhuan.settings.QiMenZhuanPanJiChuSetting
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.sin
import kotlin.math.PI

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Celestite",
        state = WindowState(size = DpSize(1000.dp, 600.dp)) // 设置初始大小为 1000x600
    ) {
        App()
    }
}




/**
 * JVM (Desktop) 平台的音频播放实现
 * @param frequency 频率 (Hz)
 * @param durationSeconds 总持续时间 (秒)，包含淡出时间
 */
actual fun playTone(frequency: Double, durationSeconds: Double) {
    Thread {
        try {
            val sampleRate = 44100f
            val totalSamples = (durationSeconds * sampleRate).toInt()

            // 淡出时间设置为最后2秒
            val fadeOutDuration = 2.0
            val fadeOutSamples = (fadeOutDuration * sampleRate).toInt()

            // 计算淡出开始的位置
            val fadeStartSamples = totalSamples - fadeOutSamples

            // 确保淡出时间不超过总时间
            val actualFadeStart = fadeStartSamples.coerceAtLeast(0)
            val actualFadeSamples = minOf(fadeOutSamples, totalSamples)

            val buffer = ByteArray(totalSamples)

            // 生成带淡出效果的正弦波
            for (i in 0 until totalSamples) {
                val angle = 2.0 * PI * i / (sampleRate / frequency)
                var value = (sin(angle) * 127).toInt()

                // 应用淡出效果（最后2秒）
                if (i >= actualFadeStart) {
                    val fadeProgress = (i - actualFadeStart).toDouble() / actualFadeSamples
                    val fadeMultiplier = 1.0 - fadeProgress // 从1线性衰减到0
                    value = (value * fadeMultiplier).toInt()
                }

                buffer[i] = value.toByte()
            }

            val audioFormat = AudioFormat(sampleRate, 8, 1, true, true)
            val lineInfo = javax.sound.sampled.DataLine.Info(SourceDataLine::class.java, audioFormat)

            if (!AudioSystem.isLineSupported(lineInfo)) {
                println("Audio line not supported")
                return@Thread
            }

            val line = AudioSystem.getLine(lineInfo) as SourceDataLine
            line.open(audioFormat, totalSamples)
            line.start()
            line.write(buffer, 0, totalSamples)
            line.drain()
            line.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
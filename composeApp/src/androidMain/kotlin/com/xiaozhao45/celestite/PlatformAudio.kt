package com.xiaozhao45.celestite

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/**
 * Android 平台的音频播放实现
 * @param frequency 频率 (Hz)
 * @param durationSeconds 总持续时间 (秒)，包含淡出时间
 */
actual fun playTone(frequency: Double, durationSeconds: Double) {
    Thread {
        try {
            val sampleRate = 44100
            val totalSamples = (durationSeconds * sampleRate).toInt()
            val sample = DoubleArray(totalSamples)

            // 淡出时间设置为最后2秒
            val fadeOutDuration = 2.0
            val fadeOutSamples = (fadeOutDuration * sampleRate).toInt()

            // 计算淡出开始的位置
            val fadeStartSamples = totalSamples - fadeOutSamples

            // 确保淡出时间不超过总时间
            val actualFadeStart = fadeStartSamples.coerceAtLeast(0)
            val actualFadeSamples = minOf(fadeOutSamples, totalSamples)

            // 生成带淡出效果的正弦波
            for (i in 0 until totalSamples) {
                // 生成正弦波
                val angle = 2.0 * Math.PI * i.toDouble() / (sampleRate / frequency)
                var amplitude = Math.sin(angle)

                // 应用淡出效果（最后2秒）
                if (i >= actualFadeStart) {
                    val fadeProgress = (i - actualFadeStart).toDouble() / actualFadeSamples
                    val fadeMultiplier = 1.0 - fadeProgress // 从1线性衰减到0
                    amplitude *= fadeMultiplier
                }

                sample[i] = amplitude
            }

            // 转换为 16-bit PCM
            val generatedSnd = ByteArray(2 * totalSamples)
            var idx = 0
            for (dVal in sample) {
                // 振幅缩放，防止爆音（考虑淡出因子）
                val valShort = (dVal * 32767).toInt().toShort()
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = (valShort.toInt() and 0xff00 ushr 8).toByte()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()

            // 延时释放资源，等待音频播放完成
            Thread.sleep((durationSeconds * 1000).toLong() + 200)
            audioTrack.release()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
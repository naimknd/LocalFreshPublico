package com.example.localfresh.utils

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Locale

class CartTimer(
    private val onTick: (minutes: Int, seconds: Int, remainingTime: Long) -> Unit,
    private val onFinish: () -> Unit
) {
    private var timerHandler: Handler? = null
    private lateinit var timerRunnable: Runnable
    private var expirationTimeMillis: Long = 0
    private var isRunning = false

    fun start(expirationTimeString: String, expiresInMinutes: Int) {
        stop()
        expirationTimeMillis = parseExpirationTime(expirationTimeString, expiresInMinutes)
        timerHandler = Handler(Looper.getMainLooper())
        startTimer()
    }

    fun resume() {
        if (isRunning && ::timerRunnable.isInitialized) {
            timerHandler?.post(timerRunnable)
        }
    }

    fun stop() {
        timerHandler?.removeCallbacks(timerRunnable)
        timerHandler = null
        isRunning = false
    }

    private fun startTimer() {
        isRunning = true
        timerRunnable = object : Runnable {
            override fun run() {
                val remainingTime = expirationTimeMillis - System.currentTimeMillis()

                if (remainingTime <= 0) {
                    onFinish()
                    stop()
                    return
                }

                val minutes = (remainingTime / 60000).toInt()
                val seconds = ((remainingTime % 60000) / 1000).toInt()
                onTick(minutes, seconds, remainingTime)

                if (isRunning) {
                    timerHandler?.postDelayed(this, 1000)
                }
            }
        }
        timerHandler?.post(timerRunnable)
    }

    private fun parseExpirationTime(timeString: String, fallbackMinutes: Int): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse(timeString)?.time
                ?: System.currentTimeMillis() + (fallbackMinutes * 60 * 1000)
        } catch (e: Exception) {
            System.currentTimeMillis() + (fallbackMinutes * 60 * 1000)
        }
    }
}
package com.alad.audiobooster

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.audiofx.LoudnessEnhancer
import android.os.Binder
import android.os.Build
import android.os.IBinder

class BoosterService : Service() {

    private val binder = LocalBinder()
    private var enhancer: LoudnessEnhancer? = null

    inner class LocalBinder : Binder() {
        fun getService(): BoosterService = this@BoosterService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        enhancer = try {
            LoudnessEnhancer(0).apply { enabled = true }
        } catch (e: Exception) {
            null
        }
    }

    /** gainMillibels: 0..3000 (0..30 dB) */
    fun setGain(gainMillibels: Int) {
        try {
            enhancer?.setTargetGain(gainMillibels)
        } catch (e: Exception) {
            // some devices reject session 0 effects; ignore safely
        }
    }

    fun isEnhancerReady(): Boolean = enhancer != null

    override fun onDestroy() {
        enhancer?.release()
        enhancer = null
        super.onDestroy()
    }

    private fun startForegroundWithNotification() {
        val channelId = "audio_booster_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Audio Booster", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("ChatGPT Audio Booster is active")
            .setContentText("Boosting system audio output")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }
}

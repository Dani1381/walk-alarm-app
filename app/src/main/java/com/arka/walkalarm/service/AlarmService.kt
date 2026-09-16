package com.arka.walkalarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.arka.walkalarm.ui.AlarmMissionActivity

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var originalVolume: Int = 100
    private var isFocusModeActive = false

    companion object {
        const val CHANNEL_ID = "WalkAlarmChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_LOWER_VOLUME = "com.arka.walkalarm.ACTION_LOWER_VOLUME"
        const val ACTION_RESTORE_VOLUME = "com.arka.walkalarm.ACTION_RESTORE_VOLUME"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "WalkAlarm:AlarmServiceWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L /* 10 minutes */)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_LOWER_VOLUME) {
            lowerVolumeForFocus()
            return START_STICKY
        } else if (action == ACTION_RESTORE_VOLUME) {
            restoreMaxVolume()
            return START_STICKY
        }

        val requiredSteps = intent?.getIntExtra("REQUIRED_STEPS", 30) ?: 30
        val missionType = intent?.getStringExtra("MISSION_TYPE") ?: "WALK"

        val fullScreenIntent = Intent(this, AlarmMissionActivity::class.java).apply {
            putExtra("REQUIRED_STEPS", requiredSteps)
            putExtra("MISSION_TYPE", missionType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 WalkAlarm is Ringing!")
            .setContentText("Complete your mission to dismiss the alarm!")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        startAlarmSound()
        startVibration()

        // Launch Mission screen immediately
        startActivity(fullScreenIntent)

        return START_STICKY
    }

    private fun startAlarmSound() {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            originalVolume = maxVol
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun lowerVolumeForFocus() {
        if (isFocusModeActive) return
        isFocusModeActive = true
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val focusVol = (maxVol * 0.35f).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, focusVol, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restoreMaxVolume() {
        if (!isFocusModeActive) return
        isFocusModeActive = false
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 800, 400, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreMaxVolume()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WalkAlarm Alert Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm Mission Notification"
                setBypassDnd(true)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

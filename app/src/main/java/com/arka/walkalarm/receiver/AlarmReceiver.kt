package com.arka.walkalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.arka.walkalarm.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val requiredSteps = intent?.getIntExtra("REQUIRED_STEPS", 30) ?: 30

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("REQUIRED_STEPS", requiredSteps)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

package com.arka.walkalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arka.walkalarm.service.AlarmService

class WakeUpCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
        val isConfirmedAwake = prefs.getBoolean("WAKEUP_CONFIRMED", false)

        if (!isConfirmedAwake) {
            // User did not respond to Wake-Up Check in time! Ring alarm again!
            val alarmIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("REQUIRED_STEPS", 20)
                putExtra("MISSION_TYPE", "WAKEUP_CHECK_FAILED")
            }
            context.startForegroundService(alarmIntent)
        }
    }
}

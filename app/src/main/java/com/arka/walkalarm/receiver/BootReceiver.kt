package com.arka.walkalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arka.walkalarm.alarm.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore configured alarms from SharedPreferences
            val prefs = context.getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("ALARM_ENABLED", false)
            if (isEnabled) {
                val hour = prefs.getInt("ALARM_HOUR", 7)
                val minute = prefs.getInt("ALARM_MINUTE", 0)
                val steps = prefs.getInt("REQUIRED_STEPS", 30)
                AlarmScheduler(context).scheduleAlarm(hour, minute, steps)
            }
        }
    }
}

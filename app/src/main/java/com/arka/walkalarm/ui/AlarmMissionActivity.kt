package com.arka.walkalarm.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.arka.walkalarm.R
import com.arka.walkalarm.sensor.StepDetectorManager
import com.arka.walkalarm.service.AlarmService

class AlarmMissionActivity : AppCompatActivity() {

    private var requiredSteps = 30
    private var currentSteps = 0
    private lateinit var stepDetector: StepDetectorManager

    private lateinit var tvRemainingSteps: TextView
    private lateinit var progressBarSteps: ProgressBar
    private lateinit var tvSensorInfo: TextView
    private lateinit var btnDismiss: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm_mission)

        requiredSteps = intent.getIntExtra("REQUIRED_STEPS", 30)

        tvRemainingSteps = findViewById(R.id.tvRemainingSteps)
        progressBarSteps = findViewById(R.id.progressBarSteps)
        tvSensorInfo = findViewById(R.id.tvSensorInfo)
        btnDismiss = findViewById(R.id.btnDismiss)

        progressBarSteps.max = requiredSteps
        updateStepUI()

        // Init Step Detector
        stepDetector = StepDetectorManager(this) {
            runOnUiThread {
                currentSteps++
                updateStepUI()
                if (currentSteps >= requiredSteps) {
                    completeMission()
                }
            }
        }

        btnDismiss.setOnClickListener {
            if (currentSteps >= requiredSteps) {
                stopAlarmAndExit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepDetector.startListening()
    }

    override fun onPause() {
        super.onPause()
        if (currentSteps < requiredSteps) {
            // Keep listening even if user attempts to minimize
            stepDetector.startListening()
        } else {
            stepDetector.stopListening()
        }
    }

    private fun updateStepUI() {
        val remaining = (requiredSteps - currentSteps).coerceAtLeast(0)
        tvRemainingSteps.text = remaining.toString()
        progressBarSteps.progress = currentSteps.coerceAtMost(requiredSteps)

        if (remaining > 0) {
            tvSensorInfo.text = "🚶 Keep moving! $currentSteps / $requiredSteps completed"
        }
    }

    private fun completeMission() {
        stepDetector.stopListening()
        tvRemainingSteps.text = "0"
        tvSensorInfo.text = "🎉 Mission Accomplished! You are awake!"
        btnDismiss.isEnabled = true
        btnDismiss.text = "✅ TURN OFF ALARM"
        btnDismiss.setBackgroundColor(getColor(R.color.accent_green))
    }

    private fun stopAlarmAndExit() {
        // Stop background alarm audio & vibration service
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
        finish()
    }

    // Block back button until mission is completed
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentSteps >= requiredSteps) {
            super.onBackPressed()
        }
    }
}

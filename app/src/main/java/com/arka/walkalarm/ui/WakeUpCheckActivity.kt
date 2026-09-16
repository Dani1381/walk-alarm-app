package com.arka.walkalarm.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.arka.walkalarm.R

class WakeUpCheckActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnConfirmAwake: Button
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        setContentView(R.layout.activity_wakeup_check)

        tvTimer = findViewById(R.id.tvWakeUpTimer)
        btnConfirmAwake = findViewById(R.id.btnConfirmAwake)

        // 3-minute window to confirm awake
        timer = object : CountDownTimer(180000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                val min = sec / 60
                val remSec = sec % 60
                tvTimer.text = String.format("%02d:%02d", min, remSec)
            }

            override fun onFinish() {
                // Time expired! Trigger fail check
                tvTimer.text = "00:00"
                finish()
            }
        }.start()

        btnConfirmAwake.setOnClickListener {
            // Confirm awake in SharedPreferences
            val prefs = getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("WAKEUP_CONFIRMED", true).apply()
            timer?.cancel()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}

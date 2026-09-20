package com.arka.walkalarm.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arka.walkalarm.R
import com.arka.walkalarm.sensor.StepDetectorManager
import com.arka.walkalarm.service.AlarmService
import com.arka.walkalarm.ThemeManager
import kotlin.random.Random

class AlarmMissionActivity : AppCompatActivity() {

    // Walk Mission State
    private var requiredSteps = 20
    private var currentSteps = 0
    private var isWalkCompleted = false
    private lateinit var stepDetector: StepDetectorManager

    // Puzzle Mission State
    private var requiredPuzzles = 2
    private var solvedPuzzles = 0
    private var currentCorrectAnswer = 0
    private var isPuzzleCompleted = false

    // Focus Mode (Calm Sound)
    private var focusTimer: CountDownTimer? = null
    private var isFocusActive = false

    // UI Elements
    private lateinit var tvRemainingSteps: TextView
    private lateinit var progressBarSteps: ProgressBar
    private lateinit var tvStepStatusBadge: TextView
    private lateinit var tvSensorInfo: TextView
    private lateinit var btnDismiss: Button
    private lateinit var btnCalmSound: Button

    private lateinit var tvPuzzleEquation: TextView
    private lateinit var tvPuzzleStatusBadge: TextView
    private lateinit var btnChoice1: Button
    private lateinit var btnChoice2: Button
    private lateinit var btnChoice3: Button
    private lateinit var btnChoice4: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aggressive Full-Screen Wake-up & Lock Screen Bypass
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_alarm_mission)

        applyActiveTheme()

        requiredSteps = intent.getIntExtra("REQUIRED_STEPS", 20)

        initViews()
        setupWalkMission()
        setupPuzzleMission()
        setupFocusMode()

        // Show motivating wake-up pop-up
        showWakeUpPopup()
    }

    private fun applyActiveTheme() {
        val currentTheme = ThemeManager.getCurrentTheme(this)
        findViewById<android.view.View>(android.R.id.content)?.setBackgroundColor(currentTheme.bgMainColor)
    }

    private fun initViews() {
        tvRemainingSteps = findViewById(R.id.tvRemainingSteps)
        progressBarSteps = findViewById(R.id.progressBarSteps)
        tvStepStatusBadge = findViewById(R.id.tvStepStatusBadge)
        tvSensorInfo = findViewById(R.id.tvSensorInfo)
        btnDismiss = findViewById(R.id.btnDismiss)
        btnCalmSound = findViewById(R.id.btnCalmSound)

        tvPuzzleEquation = findViewById(R.id.tvPuzzleEquation)
        tvPuzzleStatusBadge = findViewById(R.id.tvPuzzleStatusBadge)
        btnChoice1 = findViewById(R.id.btnChoice1)
        btnChoice2 = findViewById(R.id.btnChoice2)
        btnChoice3 = findViewById(R.id.btnChoice3)
        btnChoice4 = findViewById(R.id.btnChoice4)

        progressBarSteps.max = requiredSteps
        updateStepUI()

        btnDismiss.setOnClickListener {
            if (isWalkCompleted && isPuzzleCompleted) {
                stopService(Intent(this, AlarmService::class.java))
                finish()
            }
        }
    }

    private fun setupWalkMission() {
        stepDetector = StepDetectorManager(this) {
            currentSteps++
            updateStepUI()

            if (currentSteps >= requiredSteps && !isWalkCompleted) {
                isWalkCompleted = true
                onWalkCompleted()
            }
        }
        stepDetector.startListening()

        tvSensorInfo.text = "سنسور گام‌شمار فعال • شتاب‌سنج و گام‌شمار هوشمند"
    }

    private fun updateStepUI() {
        val remaining = (requiredSteps - currentSteps).coerceAtLeast(0)
        tvRemainingSteps.text = remaining.toString()
        progressBarSteps.progress = currentSteps

        if (remaining == 0) {
            tvStepStatusBadge.text = "تکمیل شد ✓"
            tvStepStatusBadge.setTextColor(getColor(R.color.neon_emerald))
        } else {
            tvStepStatusBadge.text = "$currentSteps / $requiredSteps گام"
        }
    }

    private fun onWalkCompleted() {
        stepDetector.stopListening()
        checkAllMissionsDone()
    }

    private fun setupPuzzleMission() {
        generateNewPuzzle()

        val buttons = listOf(btnChoice1, btnChoice2, btnChoice3, btnChoice4)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                val chosen = btn.text.toString().toIntOrNull()
                if (chosen == currentCorrectAnswer) {
                    solvedPuzzles++
                    if (solvedPuzzles >= requiredPuzzles) {
                        isPuzzleCompleted = true
                        tvPuzzleStatusBadge.text = "معماها حل شد ✓"
                        tvPuzzleStatusBadge.setTextColor(getColor(R.color.neon_emerald))
                        tvPuzzleEquation.text = "آفرین! ذهن شما کاملاً بیدار شد 🧠"
                        buttons.forEach { b -> b.isEnabled = false }
                        checkAllMissionsDone()
                    } else {
                        generateNewPuzzle()
                    }
                } else {
                    btn.setBackgroundColor(getColor(R.color.neon_rose))
                    btn.postDelayed({
                        btn.setBackgroundColor(getColor(R.color.surface_card))
                    }, 500)
                }
            }
        }
    }

    private fun generateNewPuzzle() {
        val a = Random.nextInt(12, 45)
        val b = Random.nextInt(7, 30)
        val isAddition = Random.nextBoolean()

        val question: String
        if (isAddition) {
            currentCorrectAnswer = a + b
            question = "$a + $b = ?"
        } else {
            val high = maxOf(a, b)
            val low = minOf(a, b)
            currentCorrectAnswer = high - low
            question = "$high - $low = ?"
        }

        tvPuzzleEquation.text = question
        tvPuzzleStatusBadge.text = "معمای ${solvedPuzzles + 1} از $requiredPuzzles"

        val choices = mutableListOf(currentCorrectAnswer)
        while (choices.size < 4) {
            val offset = Random.nextInt(-9, 10)
            val fake = currentCorrectAnswer + offset
            if (fake != currentCorrectAnswer && fake >= 0 && !choices.contains(fake)) {
                choices.add(fake)
            }
        }
        choices.shuffle()

        btnChoice1.text = choices[0].toString()
        btnChoice2.text = choices[1].toString()
        btnChoice3.text = choices[2].toString()
        btnChoice4.text = choices[3].toString()
    }

    private fun setupFocusMode() {
        btnCalmSound.setOnClickListener {
            if (!isFocusActive) {
                activateFocusMode()
            }
        }
    }

    private fun activateFocusMode() {
        isFocusActive = true
        btnCalmSound.isEnabled = false
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_LOWER_VOLUME
        }
        startService(intent)

        focusTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                btnCalmSound.text = "سکوت موقت: ${sec}s"
            }

            override fun onFinish() {
                isFocusActive = false
                btnCalmSound.isEnabled = true
                btnCalmSound.text = "آرامش صدا (۳۰ ثانیه)"
                val restoreIntent = Intent(this@AlarmMissionActivity, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_RESTORE_VOLUME
                }
                startService(restoreIntent)
            }
        }.start()
    }

    private fun checkAllMissionsDone() {
        if (isWalkCompleted && isPuzzleCompleted) {
            btnDismiss.isEnabled = true
            btnDismiss.text = "خاموش کردن زنگ (مأموریت‌ها با موفقیت انجام شد ✓)"
            btnDismiss.setBackgroundColor(getColor(R.color.neon_emerald))
        }
    }

    private fun showWakeUpPopup() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("☀️ بیدار شو قهرمان!")
            .setMessage("برای خاموش کردن زنگ، باید $requiredSteps گام راه بروی و معماهای ریاضی را حل کنی. تسلیم نشو!")
            .setPositiveButton("شروع مأموریت") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create()

        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stepDetector.stopListening()
        focusTimer?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent dismissing alarm via Back button
    }
}

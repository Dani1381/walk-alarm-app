package com.arka.walkalarm.ui

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

        // Lock screen bypass & wake screen
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

        requiredSteps = intent.getIntExtra("REQUIRED_STEPS", 20)

        initViews()
        setupWalkMission()
        setupPuzzleMission()
        setupFocusMode()

        // Show motivating wake-up pop-up
        showWakeUpPopup()
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
                stopAlarmAndExit()
            }
        }
    }

    private fun setupWalkMission() {
        stepDetector = StepDetectorManager(this) {
            runOnUiThread {
                if (!isWalkCompleted) {
                    currentSteps++
                    updateStepUI()
                    if (currentSteps >= requiredSteps) {
                        isWalkCompleted = true
                        tvStepStatusBadge.text = "✅ Done"
                        tvStepStatusBadge.setTextColor(getColor(R.color.accent_emerald))
                        checkAllMissionsDone()
                    }
                }
            }
        }
    }

    private fun setupPuzzleMission() {
        val choiceButtons = listOf(btnChoice1, btnChoice2, btnChoice3, btnChoice4)
        for (btn in choiceButtons) {
            btn.setOnClickListener {
                val selected = btn.text.toString().toIntOrNull()
                if (selected == currentCorrectAnswer) {
                    solvedPuzzles++
                    // Auto-lower volume temporarily while user is actively solving
                    activateFocusMode(15)
                    if (solvedPuzzles >= requiredPuzzles) {
                        isPuzzleCompleted = true
                        tvPuzzleEquation.text = "🎉 Solved!"
                        tvPuzzleStatusBadge.text = "✅ Brain is active!"
                        tvPuzzleStatusBadge.setTextColor(getColor(R.color.accent_emerald))
                        for (b in choiceButtons) b.isEnabled = false
                        checkAllMissionsDone()
                    } else {
                        tvPuzzleStatusBadge.text = "$solvedPuzzles / $requiredPuzzles solved! Next one:"
                        generateNewPuzzle()
                    }
                } else {
                    // Wrong answer penalty: shake or prompt
                    tvPuzzleStatusBadge.text = "❌ Wrong! Try again!"
                    tvPuzzleStatusBadge.setTextColor(getColor(R.color.accent_rose))
                    // Restore loud alarm immediately on mistake
                    restoreLoudSound()
                }
            }
        }
        generateNewPuzzle()
    }

    private fun generateNewPuzzle() {
        val type = Random.nextInt(2) // 0: addition/subtraction, 1: multiplication
        val num1: Int
        val num2: Int
        val equationStr: String

        if (type == 0) {
            num1 = Random.nextInt(18, 59)
            num2 = Random.nextInt(15, 49)
            currentCorrectAnswer = num1 + num2
            equationStr = "$num1 + $num2 = ?"
        } else {
            num1 = Random.nextInt(6, 12)
            num2 = Random.nextInt(4, 9)
            currentCorrectAnswer = num1 * num2
            equationStr = "$num1 × $num2 = ?"
        }

        tvPuzzleEquation.text = equationStr

        // Generate 3 unique wrong answers
        val wrongAnswers = mutableSetOf<Int>()
        while (wrongAnswers.size < 3) {
            val delta = Random.nextInt(-10, 11)
            val fake = currentCorrectAnswer + delta
            if (fake != currentCorrectAnswer && fake > 0) {
                wrongAnswers.add(fake)
            }
        }

        val allChoices = (wrongAnswers.toList() + currentCorrectAnswer).shuffled()
        val buttons = listOf(btnChoice1, btnChoice2, btnChoice3, btnChoice4)
        for (i in 0..3) {
            buttons[i].text = allChoices[i].toString()
        }
    }

    private fun setupFocusMode() {
        btnCalmSound.setOnClickListener {
            activateFocusMode(25) // 25 seconds calm period
        }
    }

    private fun activateFocusMode(seconds: Int) {
        focusTimer?.cancel()
        isFocusActive = true
        btnCalmSound.isEnabled = false

        // Send Intent to Service to lower volume
        val lowerIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_LOWER_VOLUME
        }
        startService(lowerIntent)

        focusTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secLeft = millisUntilFinished / 1000
                btnCalmSound.text = "🔉 Calm: ${secLeft}s left to solve"
            }

            override fun onFinish() {
                restoreLoudSound()
            }
        }.start()
    }

    private fun restoreLoudSound() {
        focusTimer?.cancel()
        isFocusActive = false
        btnCalmSound.isEnabled = true
        btnCalmSound.text = "🔉 Calm Sound (Focus Mode)"

        val restoreIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_RESTORE_VOLUME
        }
        startService(restoreIntent)
    }

    private fun showWakeUpPopup() {
        try {
            AlertDialog.Builder(this)
                .setTitle("☀️ Rise and Shine, Dani!")
                .setMessage("To turn off this alarm:\n\n1️⃣ Walk $requiredSteps real steps\n2️⃣ Solve $requiredPuzzles quick math puzzles\n\nSound can be calmed with the 'Calm Sound' button while you solve!")
                .setPositiveButton("Let's Go! 💪", null)
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStepUI() {
        val remaining = (requiredSteps - currentSteps).coerceAtLeast(0)
        tvRemainingSteps.text = remaining.toString()
        progressBarSteps.progress = currentSteps.coerceAtMost(requiredSteps)
        tvSensorInfo.text = "🚶 Keep moving! $currentSteps / $requiredSteps steps taken"
    }

    private fun checkAllMissionsDone() {
        if (isWalkCompleted && isPuzzleCompleted) {
            stepDetector.stopListening()
            focusTimer?.cancel()
            restoreLoudSound()

            btnDismiss.isEnabled = true
            btnDismiss.text = "✅ TURN OFF ALARM"
            btnDismiss.setBackgroundColor(getColor(R.color.accent_emerald))
            btnDismiss.setTextColor(getColor(R.color.text_main))

            // Completion pop-up dialog
            AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("All missions completed! You are fully awake and ready for the day.")
                .setPositiveButton("Dismiss Alarm 🚀") { _, _ ->
                    stopAlarmAndExit()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun stopAlarmAndExit() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!isWalkCompleted) {
            stepDetector.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isWalkCompleted) {
            stepDetector.startListening()
        } else {
            stepDetector.stopListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        focusTimer?.cancel()
        stepDetector.stopListening()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isWalkCompleted && isPuzzleCompleted) {
            super.onBackPressed()
        }
    }
}

package com.arka.walkalarm

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.arka.walkalarm.alarm.AlarmScheduler
import com.arka.walkalarm.presets.TemplateManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var scheduler: AlarmScheduler
    private var selectedHour = 7
    private var selectedMinute = 0

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            Toast.makeText(this, "Permissions needed for step missions and alarm execution!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduler = AlarmScheduler(this)
        checkAndRequestPermissions()

        val tvCurrentAlarm = findViewById<TextView>(R.id.tvCurrentAlarm)
        val btnSetTime = findViewById<MaterialButton>(R.id.btnSetTime)
        val btnTestNow = findViewById<MaterialButton>(R.id.btnTestNow)

        // Load saved state
        val prefs = getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
        selectedHour = prefs.getInt("ALARM_HOUR", 7)
        selectedMinute = prefs.getInt("ALARM_MINUTE", 0)

        tvCurrentAlarm.text = String.format("%02d:%02d", selectedHour, selectedMinute)

        setupTemplateCards()

        btnSetTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                tvCurrentAlarm.text = String.format("%02d:%02d", selectedHour, selectedMinute)
                
                val currentTemplate = TemplateManager.getSelectedTemplate(this)
                prefs.edit().apply {
                    putBoolean("ALARM_ENABLED", true)
                    putInt("ALARM_HOUR", selectedHour)
                    putInt("ALARM_MINUTE", selectedMinute)
                    putInt("REQUIRED_STEPS", currentTemplate.requiredSteps)
                    putInt("REQUIRED_PUZZLES", currentTemplate.requiredPuzzles)
                    apply()
                }

                scheduler.scheduleAlarm(selectedHour, selectedMinute, currentTemplate.requiredSteps)
                Toast.makeText(this, "Alarm set for %02d:%02d".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
            }, selectedHour, selectedMinute, true).show()
        }

        btnTestNow.setOnClickListener {
            val currentTemplate = TemplateManager.getSelectedTemplate(this)
            scheduler.scheduleQuickTest(5, currentTemplate.requiredSteps)
            Toast.makeText(this, "Alarm will trigger in 5 seconds! Lock phone to test!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupTemplateCards() {
        val cardBalanced = findViewById<MaterialCardView>(R.id.cardTemplateBalanced)
        val cardExtreme = findViewById<MaterialCardView>(R.id.cardTemplateExtreme)
        val cardWalker = findViewById<MaterialCardView>(R.id.cardTemplateWalker)
        val cardBrain = findViewById<MaterialCardView>(R.id.cardTemplateBrain)

        val rbBalanced = findViewById<RadioButton>(R.id.rbBalanced)
        val rbExtreme = findViewById<RadioButton>(R.id.rbExtreme)
        val rbWalker = findViewById<RadioButton>(R.id.rbWalker)
        val rbBrain = findViewById<RadioButton>(R.id.rbBrain)

        val selected = TemplateManager.getSelectedTemplate(this).id

        fun updateSelection(id: String) {
            TemplateManager.setSelectedTemplate(this, id)
            val currentTemplate = TemplateManager.getSelectedTemplate(this)
            
            // Save to prefs
            getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE).edit().apply {
                putInt("REQUIRED_STEPS", currentTemplate.requiredSteps)
                putInt("REQUIRED_PUZZLES", currentTemplate.requiredPuzzles)
                apply()
            }

            rbBalanced.isChecked = (id == "balanced")
            rbExtreme.isChecked = (id == "heavy_sleeper")
            rbWalker.isChecked = (id == "pure_walker")
            rbBrain.isChecked = (id == "brain_workout")

            val activeBorder = getColor(R.color.accent_emerald)
            val normalBorder = getColor(R.color.surface_card_border)

            cardBalanced.strokeColor = if (id == "balanced") activeBorder else normalBorder
            cardExtreme.strokeColor = if (id == "heavy_sleeper") getColor(R.color.accent_rose) else normalBorder
            cardWalker.strokeColor = if (id == "pure_walker") getColor(R.color.primary_accent) else normalBorder
            cardBrain.strokeColor = if (id == "brain_workout") getColor(R.color.accent_violet) else normalBorder

            Toast.makeText(this, "Selected: ${currentTemplate.name}", Toast.LENGTH_SHORT).show()
        }

        updateSelection(selected)

        cardBalanced.setOnClickListener { updateSelection("balanced") }
        cardExtreme.setOnClickListener { updateSelection("heavy_sleeper") }
        cardWalker.setOnClickListener { updateSelection("pure_walker") }
        cardBrain.setOnClickListener { updateSelection("brain_workout") }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}

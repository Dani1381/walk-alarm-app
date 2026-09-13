package com.arka.walkalarm

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arka.walkalarm.alarm.AlarmScheduler
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var scheduler: AlarmScheduler
    private var selectedHour = 7
    private var selectedMinute = 0
    private var requiredSteps = 30

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            Toast.makeText(this, "Permissions needed for Step detection and Alarm!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduler = AlarmScheduler(this)
        checkAndRequestPermissions()

        val tvCurrentAlarm = findViewById<TextView>(R.id.tvCurrentAlarm)
        val btnSetTime = findViewById<Button>(R.id.btnSetTime)
        val btnTestNow = findViewById<Button>(R.id.btnTestNow)
        val rgSteps = findViewById<RadioGroup>(R.id.rgSteps)

        // Load saved state
        val prefs = getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
        selectedHour = prefs.getInt("ALARM_HOUR", 7)
        selectedMinute = prefs.getInt("ALARM_MINUTE", 0)
        requiredSteps = prefs.getInt("REQUIRED_STEPS", 30)

        tvCurrentAlarm.text = String.format("Scheduled: %02d:%02d", selectedHour, selectedMinute)

        when (requiredSteps) {
            15 -> findViewById<RadioButton>(R.id.rb15).isChecked = true
            50 -> findViewById<RadioButton>(R.id.rb50).isChecked = true
            else -> findViewById<RadioButton>(R.id.rb30).isChecked = true
        }

        rgSteps.setOnCheckedChangeListener { _, checkedId ->
            requiredSteps = when (checkedId) {
                R.id.rb15 -> 15
                R.id.rb50 -> 50
                else -> 30
            }
            savePreferences()
            scheduler.scheduleAlarm(selectedHour, selectedMinute, requiredSteps)
            Toast.makeText(this, "Alarm set to $requiredSteps steps", Toast.LENGTH_SHORT).show()
        }

        btnSetTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                tvCurrentAlarm.text = String.format("Scheduled: %02d:%02d", selectedHour, selectedMinute)
                savePreferences()
                scheduler.scheduleAlarm(selectedHour, selectedMinute, requiredSteps)
                Toast.makeText(this, "Alarm set for %02d:%02d".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
            }, selectedHour, selectedMinute, true).show()
        }

        btnTestNow.setOnClickListener {
            scheduler.scheduleQuickTest(5, requiredSteps)
            Toast.makeText(this, "Alarm will trigger in 5 seconds! Lock your phone to test!", Toast.LENGTH_LONG).show()
        }
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("ALARM_ENABLED", true)
            putInt("ALARM_HOUR", selectedHour)
            putInt("ALARM_MINUTE", selectedMinute)
            putInt("REQUIRED_STEPS", requiredSteps)
            apply()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

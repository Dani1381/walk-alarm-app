package com.arka.walkalarm

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arka.walkalarm.alarm.AlarmScheduler
import com.arka.walkalarm.presets.TemplateManager
import com.arka.walkalarm.service.AlarmService
import com.arka.walkalarm.AppTheme
import com.arka.walkalarm.ThemeManager
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
            Toast.makeText(this, "مجوزهای لازم جهت اجرای مأموریت‌ها و سنسورها الزامی است!", Toast.LENGTH_LONG).show()
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

        // Volume Controls
        val sbVolume = findViewById<SeekBar>(R.id.sbVolume)
        val tvVolumePercent = findViewById<TextView>(R.id.tvVolumePercent)

        // Load saved state
        val prefs = getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE)
        selectedHour = prefs.getInt("ALARM_HOUR", 7)
        selectedMinute = prefs.getInt("ALARM_MINUTE", 0)
        val savedVolume = prefs.getInt(AlarmService.KEY_ALARM_VOLUME, 100)

        tvCurrentAlarm.text = String.format("%02d:%02d", selectedHour, selectedMinute)

        // Volume UI setup
        sbVolume.progress = savedVolume
        tvVolumePercent.text = "$savedVolume%"

        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val vol = progress.coerceIn(10, 100)
                tvVolumePercent.text = "$vol%"
                prefs.edit().putInt(AlarmService.KEY_ALARM_VOLUME, vol).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupTemplateCards()
        setupThemeCards()

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
                Toast.makeText(this, "زنگ برای ساعت %02d:%02d تنظیم شد".format(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show()
            }, selectedHour, selectedMinute, true).show()
        }

        btnTestNow.setOnClickListener {
            val currentTemplate = TemplateManager.getSelectedTemplate(this)
            scheduler.scheduleQuickTest(5, currentTemplate.requiredSteps)
            Toast.makeText(this, "زنگ تا ۵ ثانیه دیگر به صدا درمی‌آید! گوشی را قفل کنید تا تست شود!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupThemeCards() {
        val cardCyber = findViewById<MaterialCardView>(R.id.cardThemeCyber)
        val cardPurple = findViewById<MaterialCardView>(R.id.cardThemePurple)
        val cardEmerald = findViewById<MaterialCardView>(R.id.cardThemeEmerald)
        val cardAmber = findViewById<MaterialCardView>(R.id.cardThemeAmber)

        val rbCyber = findViewById<RadioButton>(R.id.rbThemeCyber)
        val rbPurple = findViewById<RadioButton>(R.id.rbThemePurple)
        val rbEmerald = findViewById<RadioButton>(R.id.rbThemeEmerald)
        val rbAmber = findViewById<RadioButton>(R.id.rbThemeAmber)

        val activeTheme = ThemeManager.getCurrentTheme(this)

        fun highlightTheme(theme: AppTheme) {
            rbCyber.isChecked = (theme == AppTheme.CYBER_OLED)
            rbPurple.isChecked = (theme == AppTheme.MIDNIGHT_PURPLE)
            rbEmerald.isChecked = (theme == AppTheme.EMERALD_NIGHT)
            rbAmber.isChecked = (theme == AppTheme.SUNSET_AMBER)

            ThemeManager.setTheme(this, theme)
            findViewById<android.view.View>(R.id.mainRootLayout)?.setBackgroundColor(theme.bgMainColor)
        }

        highlightTheme(activeTheme)

        cardCyber.setOnClickListener { highlightTheme(AppTheme.CYBER_OLED) }
        cardPurple.setOnClickListener { highlightTheme(AppTheme.MIDNIGHT_PURPLE) }
        cardEmerald.setOnClickListener { highlightTheme(AppTheme.EMERALD_NIGHT) }
        cardAmber.setOnClickListener { highlightTheme(AppTheme.SUNSET_AMBER) }

        rbCyber.setOnClickListener { highlightTheme(AppTheme.CYBER_OLED) }
        rbPurple.setOnClickListener { highlightTheme(AppTheme.MIDNIGHT_PURPLE) }
        rbEmerald.setOnClickListener { highlightTheme(AppTheme.EMERALD_NIGHT) }
        rbAmber.setOnClickListener { highlightTheme(AppTheme.SUNSET_AMBER) }
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

            getSharedPreferences("WalkAlarmPrefs", Context.MODE_PRIVATE).edit().apply {
                putInt("REQUIRED_STEPS", currentTemplate.requiredSteps)
                putInt("REQUIRED_PUZZLES", currentTemplate.requiredPuzzles)
                apply()
            }

            rbBalanced.isChecked = (id == "balanced")
            rbExtreme.isChecked = (id == "extreme")
            rbWalker.isChecked = (id == "walker")
            rbBrain.isChecked = (id == "brain")
        }

        updateSelection(selected)

        cardBalanced.setOnClickListener { updateSelection("balanced") }
        cardExtreme.setOnClickListener { updateSelection("extreme") }
        cardWalker.setOnClickListener { updateSelection("walker") }
        cardBrain.setOnClickListener { updateSelection("brain") }

        rbBalanced.setOnClickListener { updateSelection("balanced") }
        rbExtreme.setOnClickListener { updateSelection("extreme") }
        rbWalker.setOnClickListener { updateSelection("walker") }
        rbBrain.setOnClickListener { updateSelection("brain") }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.WAKE_LOCK
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}

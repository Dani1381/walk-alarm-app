package com.arka.walkalarm.presets

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * AlarmTemplate: Customizable alarm signature (mission type, challenge levels, timeout,
 * volume rules, and a human-friendly template label).
 *
 * Persisted to SharedPrefs as JSON under the same name; can be refreshed after a reboot
 * via AlarmPreBootReceiver.
 */
data class AlarmTemplate(
    val id: String,
    val name: String,
    val useStepMission: Boolean,
    val usePuzzleMission: Boolean,
    val stepTarget: Int,
    val puzzleTarget: Int,
    val challengeLevel: String,
    val wakeUpCheckEnabled: Boolean,
    val wakeUpTimeoutMinutes: Int,
    val soundTemplate: String,
    val volumeRules: String,
    val description: String
) {
    val totalMissions: Int
        get() = if (useStepMission && usePuzzleMission) 2 else if (useStepMission) 1 else if (usePuzzleMission) 1 else 0

    companion object {
        private const val PREFS_NAME = "AlarmTemplatesCache"
        private val SENSOR_OFF = "sensor_off"
        private val VOLUME_CAUTIOUS = "volume_cautious"
        private val VOLUME偏高 = "volume_max"
        private val VOLUME老 = "volume_silent"

        // Access to hard‑coded sound names (to compare what user selects inside app)
        fun getHardcodedSoundOptions(): List<String> = listOf("Piano Win32", "Gentle Loop", "Phone Vibes", "Classic Alarm")

        fun toSharedPrefs(context: Context, presets: List<AlarmTemplate>) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("templates_json", JSONArray(presets.map { it.toJsonString() }).toString()).apply()
        }

        fun fromSharedPrefs(context: Context): List<AlarmTemplate> {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("templates_json", "[]")
            if (jsonStr != null) {
                return JSONArray(jsonStr).toList()
            }
            return getHardcodedPresets()
        }

        fun getHardcodedPresets(): List<AlarmTemplate> = listOf(
            AlarmTemplate(
                id = "calm_gentle",
                name = "30: Gentle Wake",
                useStepMission = false,
                usePuzzleMission = false,
                stepTarget = 20,
                puzzleTarget = 0,
                challengeLevel = "Easy",
                wakeUpCheckEnabled = false,
                wakeUpTimeoutMinutes = 3,
                soundTemplate = "Piano Win32",
                volumeRules = VOLUME_CAUTIOUS,
                description = "Very calming sound, no serious challenges, short verification."
            ),
            AlarmTemplate(
                id = "brain_workout",
                name = "45: Brain Workout",
                useStepMission = true,
                usePuzzleMission = true,
                stepTarget = 15,
                puzzleTarget = 3,
                challengeLevel = "Hard",
                wakeUpCheckEnabled = false,
                wakeUpTimeoutMinutes = 5,
                soundTemplate = "Gentle Loop",
                volumeRules = VOLUME_CAUTIOUS,
                description = "6 challenges: 15 steps + 3 puzzles. Pay with focused noise (lowered while solving)."
            ),
            AlarmTemplate(
                id = "techie_gentle",
                name = "60: Techy Gentle",
                useStepMission = true,
                usePuzzleMission = true,
                stepTarget = 25,
                puzzleTarget = 4,
                challengeLevel = "Medium",
                wakeUpCheckEnabled = true,
                wakeUpTimeoutMinutes = 5,
                soundTemplate = "Piano Win32",
                volumeRules = VOLUME_CAUTIOUS,
                description = "9 challenges: 25 steps and 4 puzzles, then 5‑min verify window, still calm."
            ),
            AlarmTemplate(
                id = "guilty_party_energized",
                name = "30: Guilty Party Energized",
                useStepMission = true,
                usePuzzleMission = false,
                stepTarget = 30,
                puzzleTarget = 0,
                challengeLevel = "Medium",
                wakeUpCheckEnabled = false,
                wakeUpTimeoutMinutes = 2,
                soundTemplate = "Phone Vibes",
                volumeRules = VOLUME偏高,
                description = "Playful prompt: oops, 30 steps needed. Energetic vibing sound, quick verify."
            ),
            AlarmTemplate(
                id = "elderly_poly_hum",
                name = "20: Elderly Poly%",
                useStepMission = false,
                usePuzzleMission = true,
                stepTarget = 0,
                puzzleTarget = 2,
                challengeLevel = "Simple",
                wakeUpCheckEnabled = false,
                wakeUpTimeoutMinutes = 3,
                soundTemplate = "Classic Alarm",
                volumeRules = VOLUME_CAUTIOUS,
                description = "Simple: 2 puzzles max (a,b+5, ×4). Good for non-tech users. No steps."
            ),
            AlarmTemplate(
                id = "classic_neo_calm",
                name = "20: Classic Neo Calm",
                useStepMission = true,
                usePuzzleMission = false,
                stepTarget = 20,
                puzzleTarget = 0,
                challengeLevel = "Easy",
                wakeUpCheckEnabled = false,
                wakeUpTimeoutMinutes = 4,
                soundTemplate = "Piano Win32",
                volumeRules = VOLUME_CAUTIOUS,
                description = "20 measured steps feel satisfying. No math, calm classical vibe."
            ),
            AlarmTemplate(
                id = "retro_funk_energetic",
                name = "45: Retro Funk Energized",
                useStepMission = true,
                usePuzzleMission = true,
                stepTarget = 35,
                puzzleTarget = 5,
                challengeLevel = "Hard",
                wakeUpCheckEnabled = true,
                wakeUpTimeoutMinutes = 3,
                soundTemplate = "Phone Vibes",
                volumeRules = VOLUME最高,
                description = "Big mix: 35 steps + 5 puzzles (15+35, 4×12, 20-3, 5×7=50, 18+34=52), retro + loud + short fuse."
            ),
            AlarmTemplate(
                id = "random_riot_folk",
                name = "20: Random Riot Folk",
                useStepMission = false,
                usePuzzleMission = true,
                stepTarget = 0,
                puzzleTarget = 3,
                challengeLevel = "Medium",
                wakeUpCheckEnabled = false,
                wakeUpTimeoutMinutes = 5,
                soundTemplate = "Classic Alarm"
)
        )
    }

    fun toJsonString(): String {
        val obj = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("useStepMission", useStepMission)
            put("usePuzzleMission", usePuzzleMission)
            put("stepTarget", stepTarget)
            put("puzzleTarget", puzzleTarget)
            put("challengeLevel", challengeLevel)
            put("wakeUpCheckEnabled", wakeUpCheckEnabled)
            put("wakeUpTimeoutMinutes", wakeUpTimeoutMinutes)
            put("soundTemplate", soundTemplate)
            put("volumeRules", volumeRules)
            put("description", description)
        }
        return obj.toString()
    }

    companion object {
        private val PREFS_TRIGGERS = "AlarmTriggerSettings"
        private const val KEY_SELECTED_TEMPLATE_ID = "selected_template_id"

        private fun getTriggerPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_TRIGGERS, Context.MODE_PRIVATE)
        }

        var currentlySelectedTemplateId: String?
            get() = getTriggerPrefs(applicationContext).getString(KEY_SELECTED_TEMPLATE_ID, null)
            set(value) {
                getTriggerPrefs(applicationContext).edit().putString(KEY_SELECTED_TEMPLATE_ID, value).apply()
            }
    }
}
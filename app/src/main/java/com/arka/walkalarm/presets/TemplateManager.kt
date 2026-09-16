package com.arka.walkalarm.presets

import android.content.Context

data class MissionTemplate(
    val id: String,
    val name: String,
    val description: String,
    val requiredSteps: Int,
    val requiredPuzzles: Int,
    val puzzleDifficulty: String, // "EASY", "MEDIUM", "HARD"
    val wakeUpCheckEnabled: Boolean
)

object TemplateManager {

    private const val PREFS_NAME = "WalkAlarmTemplates"
    private const val KEY_SELECTED_TEMPLATE = "selected_template_id"

    val DEFAULT_TEMPLATES = listOf(
        MissionTemplate(
            id = "balanced",
            name = "⚡ Balanced Wake",
            description = "15 Steps + 2 Math Puzzles (Best for everyday)",
            requiredSteps = 15,
            requiredPuzzles = 2,
            puzzleDifficulty = "MEDIUM",
            wakeUpCheckEnabled = true
        ),
        MissionTemplate(
            id = "heavy_sleeper",
            name = "🔥 Extreme (Heavy Sleeper)",
            description = "35 Steps + 4 Hard Puzzles + Alertness Check",
            requiredSteps = 35,
            requiredPuzzles = 4,
            puzzleDifficulty = "HARD",
            wakeUpCheckEnabled = true
        ),
        MissionTemplate(
            id = "math_genius",
            name = "🧠 Brain Workout",
            description = "0 Steps + 5 Math Puzzles (Solve in bed)",
            requiredSteps = 0,
            requiredPuzzles = 5,
            puzzleDifficulty = "MEDIUM",
            wakeUpCheckEnabled = false
        ),
        MissionTemplate(
            id = "pure_walk",
            name = "🚶 Pure Walker",
            description = "30 Steps only, No math puzzles",
            requiredSteps = 30,
            requiredPuzzles = 0,
            puzzleDifficulty = "EASY",
            wakeUpCheckEnabled = false
        ),
        MissionTemplate(
            id = "gentle",
            name = "🌸 Gentle Rise",
            description = "10 Steps + 1 Easy Math Puzzle",
            requiredSteps = 10,
            requiredPuzzles = 1,
            puzzleDifficulty = "EASY",
            wakeUpCheckEnabled = false
        )
    )

    fun getSelectedTemplate(context: Context): MissionTemplate {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_SELECTED_TEMPLATE, "balanced")
        return DEFAULT_TEMPLATES.find { it.id == id } ?: DEFAULT_TEMPLATES[0]
    }

    fun setSelectedTemplate(context: Context, templateId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_TEMPLATE, templateId).apply()
    }
}

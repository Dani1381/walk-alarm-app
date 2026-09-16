package com.arka.walkalarm.presets

import android.content.Context

data class AlarmTemplate(
    val id: String,
    val name: String,
    val description: String,
    val requiredSteps: Int,
    val requiredPuzzles: Int,
    val wakeUpCheckEnabled: Boolean
)

object TemplateManager {

    private const val PREFS_NAME = "WalkAlarmTemplates"
    private const val KEY_SELECTED_TEMPLATE = "selected_template_id"

    val DEFAULT_TEMPLATES = listOf(
        AlarmTemplate(
            id = "balanced",
            name = "⚡ Balanced Wake (Default)",
            description = "15 Steps + 2 Math Puzzles + Alertness Check",
            requiredSteps = 15,
            requiredPuzzles = 2,
            wakeUpCheckEnabled = true
        ),
        AlarmTemplate(
            id = "heavy_sleeper",
            name = "🔥 Extreme (Heavy Sleeper)",
            description = "35 Steps + 4 Puzzles + Alertness Check",
            requiredSteps = 35,
            requiredPuzzles = 4,
            wakeUpCheckEnabled = true
        ),
        AlarmTemplate(
            id = "brain_workout",
            name = "🧠 Brain Workout (No Walking)",
            description = "0 Steps + 5 Math Puzzles in Bed",
            requiredSteps = 0,
            requiredPuzzles = 5,
            wakeUpCheckEnabled = false
        ),
        AlarmTemplate(
            id = "pure_walker",
            name = "🚶 Pure Walker (No Math)",
            description = "30 Steps only",
            requiredSteps = 30,
            requiredPuzzles = 0,
            wakeUpCheckEnabled = false
        ),
        AlarmTemplate(
            id = "gentle",
            name = "🌸 Gentle Rise",
            description = "10 Steps + 1 Easy Puzzle",
            requiredSteps = 10,
            requiredPuzzles = 1,
            wakeUpCheckEnabled = false
        )
    )

    fun getSelectedTemplate(context: Context): AlarmTemplate {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_SELECTED_TEMPLATE, "balanced")
        return DEFAULT_TEMPLATES.find { it.id == id } ?: DEFAULT_TEMPLATES[0]
    }

    fun setSelectedTemplate(context: Context, templateId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_TEMPLATE, templateId).apply()
    }
}

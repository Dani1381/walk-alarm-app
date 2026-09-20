package com.arka.walkalarm.theme

import android.content.Context
import android.graphics.Color
import com.arka.walkalarm.R

enum class AppTheme(
    val id: String,
    val title: String,
    val bgMainColor: Int,
    val surfaceColor: Int,
    val accentColor: Int,
    val textColor: Int,
    val badgeBgColor: Int
) {
    CYBER_OLED(
        "cyber_oled",
        "Cyber Dark (پیش‌فرض)",
        Color.parseColor("#080C14"),
        Color.parseColor("#131D31"),
        Color.parseColor("#06B6D4"),
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#1E293B")
    ),
    MIDNIGHT_PURPLE(
        "midnight_purple",
        "بنفش نئونی (Neon Violet)",
        Color.parseColor("#0D0B18"),
        Color.parseColor("#1E1835"),
        Color.parseColor("#A855F7"),
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#2A2049")
    ),
    EMERALD_NIGHT(
        "emerald_night",
        "زمردی سایبری (Cyber Emerald)",
        Color.parseColor("#051614"),
        Color.parseColor("#0A2B27"),
        Color.parseColor("#10B981"),
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#12423C")
    ),
    SUNSET_AMBER(
        "sunset_amber",
        "غروب کهربایی (Sunset Amber)",
        Color.parseColor("#180D06"),
        Color.parseColor("#2C180C"),
        Color.parseColor("#F59E0B"),
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#442512")
    );

    companion object {
        private const val PREFS_NAME = "WalkAlarmThemePrefs"
        private const val KEY_THEME = "APP_THEME_ID"

        fun getCurrentTheme(context: Context): AppTheme {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val themeId = prefs.getString(KEY_THEME, CYBER_OLED.id)
            return values().find { it.id == themeId } ?: CYBER_OLED
        }

        fun setTheme(context: Context, theme: AppTheme) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, theme.id)
                .apply()
        }
    }
}

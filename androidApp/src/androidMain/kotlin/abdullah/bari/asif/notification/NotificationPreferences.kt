package abdullah.bari.asif.notification

import android.content.Context
import android.content.SharedPreferences

object NotificationPreferences {
    private const val PREFS_NAME = "newscrawler_notification_prefs"
    private const val KEY_FILTERS = "notification_filters"
    private const val KEY_ENABLED = "notifications_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getNotificationFilters(context: Context): List<String> {
        val raw = getPrefs(context).getString(KEY_FILTERS, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun setNotificationFilters(context: Context, filters: List<String>) {
        val joined = filters.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(";")
        getPrefs(context).edit().putString(KEY_FILTERS, joined).apply()
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}

package abdullah.bari.asif.db

import kotlinx.coroutines.flow.MutableSharedFlow

class SettingsDaoImpl(
    private val driver: DatabaseDriver,
    private val changeNotificationFlow: MutableSharedFlow<Unit>
) : SettingsDao {

    override suspend fun getSetting(key: String, defaultValue: String): String {
        val sql = "SELECT value FROM settings WHERE key = ?"
        val results = driver.query(sql, listOf(key)) { row ->
            row.getString("value")
        }
        return results.firstOrNull() ?: defaultValue
    }

    override suspend fun saveSetting(key: String, value: String) {
        val sql = "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)"
        driver.execute(sql, listOf(key, value))
        changeNotificationFlow.emit(Unit)
    }
}

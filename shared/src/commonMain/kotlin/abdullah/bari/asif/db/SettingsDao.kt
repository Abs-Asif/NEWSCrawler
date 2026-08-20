package abdullah.bari.asif.db

interface SettingsDao {
    suspend fun getSetting(key: String, defaultValue: String): String
    suspend fun saveSetting(key: String, value: String)
}

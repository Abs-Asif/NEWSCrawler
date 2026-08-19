package abdullah.bari.asif.db

import abdullah.bari.asif.model.InstalledSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class SourceDaoImpl(
    private val driver: DatabaseDriver,
    private val changeNotificationFlow: MutableSharedFlow<Unit>
) : SourceDao {

    override fun getInstalledSources(): Flow<List<InstalledSource>> = flow {
        emit(queryInstalledSources())
        changeNotificationFlow.collect {
            emit(queryInstalledSources())
        }
    }

    private fun queryInstalledSources(): List<InstalledSource> {
        val sql = "SELECT source_id, is_enabled, last_synced_at FROM installed_sources"
        return driver.query(sql) { row ->
            InstalledSource(
                sourceId = row.getString("source_id"),
                isEnabled = row.getInt("is_enabled") != 0,
                lastSyncedAt = row.getLong("last_synced_at")
            )
        }
    }

    override suspend fun isSourceInstalled(sourceId: String): Boolean {
        val sql = "SELECT count(*) AS cnt FROM installed_sources WHERE source_id = ?"
        val counts = driver.query(sql, listOf(sourceId)) { row -> row.getInt("cnt") }
        return counts.firstOrNull()?.let { it > 0 } ?: false
    }

    override suspend fun getInstalledSource(sourceId: String): InstalledSource? {
        val sql = "SELECT source_id, is_enabled, last_synced_at FROM installed_sources WHERE source_id = ?"
        return driver.query(sql, listOf(sourceId)) { row ->
            InstalledSource(
                sourceId = row.getString("source_id"),
                isEnabled = row.getInt("is_enabled") != 0,
                lastSyncedAt = row.getLong("last_synced_at")
            )
        }.firstOrNull()
    }

    override suspend fun setSourceInstalled(sourceId: String, isEnabled: Boolean, lastSyncedAt: Long) {
        val sql = """
            INSERT OR REPLACE INTO installed_sources (source_id, is_enabled, last_synced_at)
            VALUES (?, ?, ?)
        """.trimIndent()
        driver.execute(sql, listOf(sourceId, if (isEnabled) 1 else 0, lastSyncedAt))
        changeNotificationFlow.emit(Unit)
    }

    override suspend fun updateLastSynced(sourceId: String, lastSyncedAt: Long) {
        val sql = "UPDATE installed_sources SET last_synced_at = ? WHERE source_id = ?"
        driver.execute(sql, listOf(lastSyncedAt, sourceId))
        changeNotificationFlow.emit(Unit)
    }

    override suspend fun removeInstalledSource(sourceId: String) {
        val sql = "DELETE FROM installed_sources WHERE source_id = ?"
        driver.execute(sql, listOf(sourceId))
        changeNotificationFlow.emit(Unit)
    }
}

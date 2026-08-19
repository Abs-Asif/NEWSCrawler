package abdullah.bari.asif.db

import abdullah.bari.asif.model.InstalledSource
import kotlinx.coroutines.flow.Flow

interface SourceDao {
    fun getInstalledSources(): Flow<List<InstalledSource>>
    suspend fun isSourceInstalled(sourceId: String): Boolean
    suspend fun setSourceInstalled(sourceId: String, isEnabled: Boolean = true, lastSyncedAt: Long = 0L)
    suspend fun updateLastSynced(sourceId: String, lastSyncedAt: Long)
    suspend fun removeInstalledSource(sourceId: String)
    suspend fun getInstalledSource(sourceId: String): InstalledSource?
}

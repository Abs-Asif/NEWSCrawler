package abdullah.bari.asif.model

import kotlinx.serialization.Serializable

@Serializable
data class InstalledSource(
    val sourceId: String,
    val isEnabled: Boolean = true,
    val lastSyncedAt: Long = 0L
)

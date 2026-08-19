package abdullah.bari.asif.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsSource(
    val id: String,
    val name: String,
    val baseUrl: String,
    val imageUrl: String,
    val updatedAt: String,
    val fetchUrl: String,
    val logic: SourceLogic,
    val isInstalled: Boolean = false
)

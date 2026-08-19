package abdullah.bari.asif.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsArticle(
    val id: String,
    val sourceId: String,
    val sourceName: String,
    val sourceLogoUrl: String,
    val title: String,
    val articleUrl: String,
    val imageUrl: String? = null,
    val publishedAt: String,
    val fetchedAt: Long
)

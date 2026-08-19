package abdullah.bari.asif.model

import kotlinx.serialization.Serializable

@Serializable
data class SourceLogic(
    val type: SourceType,
    val dateFormatPattern: String? = null,
    val selectors: ElementSelectors
)

enum class SourceType { RSS, SITEMAP_XML, CUSTOM_HTML }

@Serializable
data class ElementSelectors(
    val containerSelector: String? = null,
    val itemSelector: String? = null,
    val titleSelector: String? = null,
    val linkSelector: String? = null,
    val imageSelector: String? = null,
    val dateSelector: String? = null,
    val urlSelector: String? = null,
    val locSelector: String? = null,
    val lastmodSelector: String? = null
)

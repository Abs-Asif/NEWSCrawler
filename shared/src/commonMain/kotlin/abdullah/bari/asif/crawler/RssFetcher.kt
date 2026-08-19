package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock

class RssFetcher(
    private val httpClient: HttpClient = HttpClient()
) : NewsFetcher {

    override suspend fun fetchArticles(source: NewsSource): List<NewsArticle> {
        return try {
            val responseText = httpClient.get(source.fetchUrl).bodyAsText()
            parseRssXml(responseText, source)
        } catch (e: Exception) {
            emptyList()
        }
    }

    internal fun parseRssXml(xmlContent: String, source: NewsSource): List<NewsArticle> {
        val doc = parseXmlSafe(xmlContent) ?: return emptyList()
        val fetchedAt = Clock.System.now().toEpochMilliseconds()

        var itemElements = doc.findAllSafe("item")
        if (itemElements.isEmpty()) {
            itemElements = doc.findAllSafe("entry")
        }

        val articles = mutableListOf<NewsArticle>()
        for (element in itemElements) {
            val titleEl = element.findAllSafe("title").firstOrNull()
            val rawTitle = titleEl?.text?.cleanCdataAndEntities() ?: ""
            if (rawTitle.isBlank()) continue

            var rawLink = ""
            val linkEl = element.findAllSafe("link").firstOrNull()
            if (linkEl != null) {
                rawLink = linkEl.getAttr("href") ?: linkEl.text.trim()
            }

            if (rawLink.isBlank()) {
                val outerHtml = element.outerHtml
                val linkMatch = Regex("<link[^>]*>([^<]+)</link>", RegexOption.IGNORE_CASE).find(outerHtml)
                    ?: Regex("<link[^>]+href=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(outerHtml)
                if (linkMatch != null) {
                    rawLink = linkMatch.groupValues[1].trim()
                }
            }

            if (rawLink.isBlank()) {
                rawLink = element.findAllSafe("guid").firstOrNull()?.text?.trim() ?: ""
            }
            if (rawLink.isBlank()) continue

            val fullArticleUrl = resolveUrl(source.baseUrl, rawLink)

            var pubDate = element.findAllSafe("pubDate").firstOrNull()?.text?.trim()
                .takeUnless { it.isNullOrBlank() }
                ?: element.findAllSafe("pubdate").firstOrNull()?.text?.trim()
                .takeUnless { it.isNullOrBlank() }
                ?: element.findAllSafe("dc\\:date").firstOrNull()?.text?.trim()
                .takeUnless { it.isNullOrBlank() }
                ?: element.findAllSafe("published").firstOrNull()?.text?.trim()
                .takeUnless { it.isNullOrBlank() }
                ?: element.findAllSafe("updated").firstOrNull()?.text?.trim()
                .takeUnless { it.isNullOrBlank() }

            if (pubDate.isNullOrBlank()) {
                val outerHtml = element.outerHtml
                val pubMatch = Regex("<pubDate[^>]*>([^<]+)</pubDate>", RegexOption.IGNORE_CASE).find(outerHtml)
                    ?: Regex("<dc:date[^>]*>([^<]+)</dc:date>", RegexOption.IGNORE_CASE).find(outerHtml)
                    ?: Regex("<published[^>]*>([^<]+)</published>", RegexOption.IGNORE_CASE).find(outerHtml)
                    ?: Regex("<updated[^>]*>([^<]+)</updated>", RegexOption.IGNORE_CASE).find(outerHtml)
                if (pubMatch != null) {
                    pubDate = pubMatch.groupValues[1].trim()
                }
            }

            if (pubDate.isNullOrBlank()) {
                pubDate = Clock.System.now().toString()
            }

            var imageUrl: String? = null
            val mediaContent = element.findAllSafe("media\\:content")
                .ifEmpty { element.findAllSafe("media\\:thumbnail") }
                .ifEmpty { element.findAllSafe("enclosure") }
                .firstOrNull { it.getAttr("url") != null }

            if (mediaContent != null) {
                imageUrl = mediaContent.getAttr("url")
            }

            if (imageUrl.isNullOrBlank()) {
                val outerHtml = element.outerHtml
                val mediaMatch = Regex("(?:media:content|media:thumbnail|enclosure)[^>]+url=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(outerHtml)
                if (mediaMatch != null) {
                    imageUrl = mediaMatch.groupValues[1].trim()
                }
            }

            if (imageUrl.isNullOrBlank()) {
                val contentElement = element.findAllSafe("description")
                    .ifEmpty { element.findAllSafe("content") }
                    .firstOrNull()
                val descriptionText = contentElement?.text ?: ""
                val imgMatch = Regex("<img[^>]+src=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(descriptionText)
                if (imgMatch != null) {
                    imageUrl = imgMatch.groupValues[1]
                }
            }

            val fullImageUrl = imageUrl?.let { resolveUrl(source.baseUrl, it) }

            articles.add(
                NewsArticle(
                    id = generateArticleId(fullArticleUrl),
                    sourceId = source.id,
                    sourceName = source.name,
                    sourceLogoUrl = source.imageUrl,
                    title = rawTitle,
                    articleUrl = fullArticleUrl,
                    imageUrl = fullImageUrl,
                    publishedAt = pubDate,
                    fetchedAt = fetchedAt
                )
            )
        }
        return articles
    }
}

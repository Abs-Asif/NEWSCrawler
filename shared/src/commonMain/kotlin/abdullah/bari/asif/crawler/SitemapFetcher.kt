package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SitemapFetcher(
    private val httpClient: HttpClient = HttpClient(),
    private val maxArticlesToFetch: Int = 15
) : NewsFetcher {

    override suspend fun fetchArticles(source: NewsSource): List<NewsArticle> {
        return try {
            val resolvedUrl = resolveFetchUrl(source.fetchUrl)
            val xmlText = httpClient.get(resolvedUrl).bodyAsText()
            parseSitemapXml(xmlText, source)
        } catch (e: Exception) {
            emptyList()
        }
    }

    internal fun resolveFetchUrl(fetchUrl: String): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val yearStr = now.year.toString().padStart(4, '0')
        val monthStr = now.monthNumber.toString().padStart(2, '0')
        val dayStr = now.dayOfMonth.toString().padStart(2, '0')

        return fetchUrl
            .replace("{year}", yearStr)
            .replace("{month}", monthStr)
            .replace("{day}", dayStr)
    }

    internal suspend fun parseSitemapXml(xmlContent: String, source: NewsSource): List<NewsArticle> {
        val doc = parseXmlSafe(xmlContent) ?: return emptyList()
        val fetchedAt = Clock.System.now().toEpochMilliseconds()

        val urlElements = doc.findAllSafe("url")
        val articles = mutableListOf<NewsArticle>()

        val targetElements = urlElements.take(maxArticlesToFetch)

        for (urlEl in targetElements) {
            val loc = urlEl.findAllSafe("loc").firstOrNull()?.text?.trim() ?: continue
            if (loc.isBlank()) continue

            val fullArticleUrl = resolveUrl(source.baseUrl, loc)
            val lastmod = urlEl.findAllSafe("lastmod").firstOrNull()?.text?.trim()
                ?.ifBlank { null }
                ?: Clock.System.now().toString()

            var title: String? = null
            var imageUrl: String? = null

            try {
                val pageHtml = httpClient.get(fullArticleUrl).bodyAsText()
                val pageDoc = parseHtmlSafe(pageHtml)
                if (pageDoc != null) {
                    title = pageDoc.findAllSafe("meta[property='og:title']").firstOrNull()?.getAttr("content")
                        ?.cleanCdataAndEntities()
                        ?.ifBlank { null }
                        ?: pageDoc.findAllSafe("meta[name='twitter:title']").firstOrNull()?.getAttr("content")
                            ?.cleanCdataAndEntities()
                            ?.ifBlank { null }
                        ?: pageDoc.findAllSafe("title").firstOrNull()?.text
                            ?.cleanCdataAndEntities()
                            ?.ifBlank { null }

                    imageUrl = pageDoc.findAllSafe("meta[property='og:image']").firstOrNull()?.getAttr("content")
                        ?.ifBlank { null }
                        ?: pageDoc.findAllSafe("meta[name='twitter:image']").firstOrNull()?.getAttr("content")
                            ?.ifBlank { null }
                }
            } catch (e: Exception) {
                // Ignore single article network fetch error, fallback below
            }

            if (title.isNullOrBlank() || title == "404" || title.contains("Page Not Found", ignoreCase = true)) {
                val slug = fullArticleUrl.substringAfterLast('/').substringBefore('?').substringBefore('#')
                title = slug.replace('-', ' ').replace('_', ' ').replace(Regex("\\.html?$"), "")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }

            if (title.isBlank()) continue

            val fullImageUrl = imageUrl?.let { resolveUrl(source.baseUrl, it) }

            articles.add(
                NewsArticle(
                    id = generateArticleId(fullArticleUrl),
                    sourceId = source.id,
                    sourceName = source.name,
                    sourceLogoUrl = source.imageUrl,
                    title = title,
                    articleUrl = fullArticleUrl,
                    imageUrl = fullImageUrl,
                    publishedAt = lastmod,
                    fetchedAt = fetchedAt
                )
            )
        }

        return articles
    }
}

package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import it.skrape.selects.DocElement
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SitemapFetcher(
    private val httpClient: HttpClient = HttpClient(),
    private val maxArticlesToFetch: Int = 50
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

        // Handle sitemapindex by fetching child sitemaps if present
        val sitemapElements = doc.findAllSafe("sitemap")
        if (sitemapElements.isNotEmpty()) {
            val childArticles = mutableListOf<NewsArticle>()
            val childLocs = sitemapElements.mapNotNull {
                it.findAllSafe("loc").firstOrNull()?.text?.trim()
            }.filter { it.isNotBlank() }

            // Prioritize post/news/article sitemaps
            val preferredLocs = childLocs.filter { loc ->
                loc.contains("post", ignoreCase = true) ||
                        loc.contains("news", ignoreCase = true) ||
                        loc.contains("article", ignoreCase = true) ||
                        loc.contains("daily", ignoreCase = true)
            }.ifEmpty { childLocs }

            for (childLoc in preferredLocs.take(2)) {
                try {
                    val childXml = httpClient.get(resolveUrl(source.baseUrl, childLoc)).bodyAsText()
                    val childDoc = parseXmlSafe(childXml) ?: continue
                    val subArticles = parseUrlElements(childDoc.findAllSafe("url"), source, fetchedAt)
                    childArticles.addAll(subArticles)
                    if (childArticles.size >= maxArticlesToFetch) break
                } catch (e: Exception) {
                    // Ignore child sitemap fetch failure
                }
            }
            if (childArticles.isNotEmpty()) {
                return childArticles.take(maxArticlesToFetch)
            }
        }

        val urlElements = doc.findAllSafe("url")
        return parseUrlElements(urlElements, source, fetchedAt)
    }

    private fun parseUrlElements(
        urlElements: List<DocElement>,
        source: NewsSource,
        fetchedAt: Long
    ): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()

        for (urlEl in urlElements) {
            if (articles.size >= maxArticlesToFetch) break

            val loc = urlEl.findAllSafe("loc").firstOrNull()?.text?.trim() ?: continue
            if (loc.isBlank()) continue

            val fullArticleUrl = resolveUrl(source.baseUrl, loc)

            // Extract Google News title or standard title tag in XML
            val xmlTitle = urlEl.findAllSafe("news\\:title").firstOrNull()?.text?.cleanCdataAndEntities()
                ?.ifBlank { null }
                ?: urlEl.findAllSafe("title").firstOrNull()?.text?.cleanCdataAndEntities()
                    ?.ifBlank { null }

            // Extract publication date in XML
            val pubDate = urlEl.findAllSafe("news\\:publication_date").firstOrNull()?.text?.trim()
                ?.ifBlank { null }
                ?: urlEl.findAllSafe("lastmod").firstOrNull()?.text?.trim()
                    ?.ifBlank { null }
                ?: Clock.System.now().toString()

            // Extract image in XML
            val imageUrl = urlEl.findAllSafe("image\\:loc").firstOrNull()?.text?.trim()
                ?.ifBlank { null }
                ?: urlEl.findAllSafe("image\\:image").firstOrNull()?.findAllSafe("image\\:loc")?.firstOrNull()?.text?.trim()
                    ?.ifBlank { null }

            var title: String? = xmlTitle

            if (title.isNullOrBlank() || title == "404" || title.contains("Page Not Found", ignoreCase = true)) {
                val slug = fullArticleUrl.substringAfterLast('/').substringBefore('?').substringBefore('#')
                val slugClean = slug.replace(Regex("-\\d+$"), "")
                    .replace('-', ' ')
                    .replace('_', ' ')
                    .replace(Regex("\\.html?$"), "")
                    .trim()
                if (slugClean.length > 3) {
                    title = slugClean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }

            if (title.isNullOrBlank()) continue

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
                    publishedAt = pubDate,
                    fetchedAt = fetchedAt
                )
            )
        }

        return articles
    }
}

package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock

class CustomHtmlFetcher(
    private val httpClient: HttpClient = HttpClient()
) : NewsFetcher {

    override suspend fun fetchArticles(source: NewsSource): List<NewsArticle> {
        return try {
            val htmlText = httpClient.get(source.fetchUrl).bodyAsText()
            parseCustomHtml(htmlText, source)
        } catch (e: Exception) {
            emptyList()
        }
    }

    internal fun parseCustomHtml(htmlContent: String, source: NewsSource): List<NewsArticle> {
        val doc = parseHtmlSafe(htmlContent) ?: return emptyList()
        val fetchedAt = Clock.System.now().toEpochMilliseconds()
        val selectors = source.logic.selectors

        val containerSelector = selectors.containerSelector
        val titleSelector = selectors.titleSelector
        val linkSelector = selectors.linkSelector
        val imageSelector = selectors.imageSelector
        val dateSelector = selectors.dateSelector

        val containers = if (!containerSelector.isNullOrBlank()) {
            doc.findAllSafe(containerSelector)
        } else {
            doc.findAllSafe("article")
                .ifEmpty { doc.findAllSafe(".post") }
                .ifEmpty { doc.findAllSafe(".news-item") }
                .ifEmpty { doc.findAllSafe("li") }
        }

        val articles = mutableListOf<NewsArticle>()

        for (container in containers) {
            val titleText = if (!titleSelector.isNullOrBlank()) {
                container.findAllSafe(titleSelector).firstOrNull()?.text?.cleanCdataAndEntities()
            } else {
                container.findAllSafe("h1")
                    .ifEmpty { container.findAllSafe("h2") }
                    .ifEmpty { container.findAllSafe("h3") }
                    .ifEmpty { container.findAllSafe("h4") }
                    .ifEmpty { container.findAllSafe("a") }
                    .firstOrNull()?.text?.cleanCdataAndEntities()
            } ?: continue

            if (titleText.isBlank()) continue

            var rawLink: String? = null
            if (!linkSelector.isNullOrBlank()) {
                val el = container.findAllSafe(linkSelector).firstOrNull()
                rawLink = el?.getAttr("href")?.ifBlank { el.text }
            }
            if (rawLink.isNullOrBlank()) {
                rawLink = container.findAllSafe("a").firstOrNull { it.getAttr("href") != null }?.getAttr("href")
            }
            if (rawLink.isNullOrBlank()) continue

            val fullArticleUrl = resolveUrl(source.baseUrl, rawLink)

            var rawImage: String? = null
            if (!imageSelector.isNullOrBlank()) {
                val imgEl = container.findAllSafe(imageSelector).firstOrNull()
                rawImage = imgEl?.getAttr("src") ?: imgEl?.getAttr("data-src")
            }
            if (rawImage.isNullOrBlank()) {
                val imgEl = container.findAllSafe("img").firstOrNull { it.getAttr("src") != null }
                rawImage = imgEl?.getAttr("src") ?: imgEl?.getAttr("data-src")
            }

            val fullImageUrl = rawImage?.takeIf { it.isNotBlank() }?.let { resolveUrl(source.baseUrl, it) }

            var pubDate: String? = null
            if (!dateSelector.isNullOrBlank()) {
                pubDate = container.findAllSafe(dateSelector).firstOrNull()?.text?.trim()
            }
            if (pubDate.isNullOrBlank()) {
                pubDate = container.findAllSafe("time")
                    .ifEmpty { container.findAllSafe(".date") }
                    .ifEmpty { container.findAllSafe(".pubdate") }
                    .firstOrNull()?.text?.trim()
            }
            if (pubDate.isNullOrBlank()) {
                pubDate = Clock.System.now().toString()
            }

            articles.add(
                NewsArticle(
                    id = generateArticleId(fullArticleUrl),
                    sourceId = source.id,
                    sourceName = source.name,
                    sourceLogoUrl = source.imageUrl,
                    title = titleText,
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

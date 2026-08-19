package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.model.SourceType
import io.ktor.client.HttpClient

class UniversalFetcher(
    private val httpClient: HttpClient = HttpClient(),
    rssFetcher: NewsFetcher? = null,
    sitemapFetcher: NewsFetcher? = null,
    customHtmlFetcher: NewsFetcher? = null
) : NewsFetcher {

    private val rss = rssFetcher ?: RssFetcher(httpClient)
    private val sitemap = sitemapFetcher ?: SitemapFetcher(httpClient)
    private val customHtml = customHtmlFetcher ?: CustomHtmlFetcher(httpClient)

    override suspend fun fetchArticles(source: NewsSource): List<NewsArticle> {
        return when (source.logic.type) {
            SourceType.RSS -> rss.fetchArticles(source)
            SourceType.SITEMAP_XML -> sitemap.fetchArticles(source)
            SourceType.CUSTOM_HTML -> customHtml.fetchArticles(source)
        }
    }
}

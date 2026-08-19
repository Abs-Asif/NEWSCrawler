package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.ElementSelectors
import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.model.SourceLogic
import abdullah.bari.asif.model.SourceType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class UniversalFetcherTest {

    private class MockFetcher(private val response: List<NewsArticle>) : NewsFetcher {
        var calledCount = 0
            private set

        override suspend fun fetchArticles(source: NewsSource): List<NewsArticle> {
            calledCount++
            return response
        }
    }

    private val sampleArticle = NewsArticle(
        id = "123",
        sourceId = "src1",
        sourceName = "Source 1",
        sourceLogoUrl = "logo.png",
        title = "Title",
        articleUrl = "https://example.com/1",
        imageUrl = null,
        publishedAt = "2025-02-18",
        fetchedAt = 1000000000L
    )

    @Test
    fun testUniversalFetcherRoutesBySourceType() = runBlocking {
        val mockRss = MockFetcher(listOf(sampleArticle.copy(id = "rss_art")))
        val mockSitemap = MockFetcher(listOf(sampleArticle.copy(id = "sitemap_art")))
        val mockCustomHtml = MockFetcher(listOf(sampleArticle.copy(id = "custom_art")))

        val universalFetcher = UniversalFetcher(
            rssFetcher = mockRss,
            sitemapFetcher = mockSitemap,
            customHtmlFetcher = mockCustomHtml
        )

        val rssSource = createSource(SourceType.RSS)
        val sitemapSource = createSource(SourceType.SITEMAP_XML)
        val customSource = createSource(SourceType.CUSTOM_HTML)

        val rssResults = universalFetcher.fetchArticles(rssSource)
        assertEquals(1, rssResults.size)
        assertEquals("rss_art", rssResults[0].id)
        assertEquals(1, mockRss.calledCount)

        val sitemapResults = universalFetcher.fetchArticles(sitemapSource)
        assertEquals(1, sitemapResults.size)
        assertEquals("sitemap_art", sitemapResults[0].id)
        assertEquals(1, mockSitemap.calledCount)

        val customResults = universalFetcher.fetchArticles(customSource)
        assertEquals(1, customResults.size)
        assertEquals("custom_art", customResults[0].id)
        assertEquals(1, mockCustomHtml.calledCount)
    }

    private fun createSource(type: SourceType) = NewsSource(
        id = "test_source_${type.name}",
        name = "Test Source ${type.name}",
        baseUrl = "https://example.com",
        imageUrl = "https://example.com/logo.png",
        updatedAt = "2025.02.18.12.00",
        fetchUrl = "https://example.com/feed",
        logic = SourceLogic(
            type = type,
            selectors = ElementSelectors()
        )
    )
}

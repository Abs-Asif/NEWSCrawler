package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.ElementSelectors
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.model.SourceLogic
import abdullah.bari.asif.model.SourceType
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SitemapFetcherTest {

    private val fetcher = SitemapFetcher(maxArticlesToFetch = 10)

    private val sitemapSource = NewsSource(
        id = "who_sitemap",
        name = "World Health Organization News",
        baseUrl = "https://www.who.int",
        imageUrl = "https://www.who.int/favicon.ico",
        updatedAt = "2025.02.18.12.00",
        fetchUrl = "https://www.who.int/sitemap-{year}-{month}.xml",
        logic = SourceLogic(
            type = SourceType.SITEMAP_XML,
            dateFormatPattern = "yyyy-MM",
            selectors = ElementSelectors(
                urlSelector = "url",
                locSelector = "loc",
                lastmodSelector = "lastmod"
            )
        )
    )

    @Test
    fun testResolveFetchUrlPlaceholders() {
        val resolved = fetcher.resolveFetchUrl("https://www.who.int/sitemap-{year}-{month}-{day}.xml")
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val expectedYear = now.year.toString().padStart(4, '0')
        val expectedMonth = now.monthNumber.toString().padStart(2, '0')
        val expectedDay = now.dayOfMonth.toString().padStart(2, '0')

        assertTrue(resolved.contains(expectedYear))
        assertTrue(resolved.contains(expectedMonth))
        assertTrue(resolved.contains(expectedDay))
        assertEquals("https://www.who.int/sitemap-$expectedYear-$expectedMonth-$expectedDay.xml", resolved)
    }

    @Test
    fun testParseSitemapXml() = runBlocking {
        val xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                <url>
                    <loc>https://www.who.int/news/item/18-02-2025-global-health-update</loc>
                    <lastmod>2025-02-18T10:00:00Z</lastmod>
                </url>
                <url>
                    <loc>/news/item/17-02-2025-pandemic-prevention-report</loc>
                    <lastmod>2025-02-17T15:30:00Z</lastmod>
                </url>
            </urlset>
        """.trimIndent()

        val articles = fetcher.parseSitemapXml(xmlContent, sitemapSource)

        assertEquals(2, articles.size)

        val first = articles[0]
        assertEquals("https://www.who.int/news/item/18-02-2025-global-health-update", first.articleUrl)
        assertEquals("2025-02-18T10:00:00Z", first.publishedAt)
        assertEquals("18 02 2025 global health update", first.title)
        assertNotNull(first.id)

        val second = articles[1]
        assertEquals("https://www.who.int/news/item/17-02-2025-pandemic-prevention-report", second.articleUrl)
        assertEquals("2025-02-17T15:30:00Z", second.publishedAt)
    }
}

package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.ElementSelectors
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.model.SourceLogic
import abdullah.bari.asif.model.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RssFetcherTest {

    private val fetcher = RssFetcher()

    private val rssSource = NewsSource(
        id = "med_news_today_rss",
        name = "Medical News Today",
        baseUrl = "https://www.medicalnewstoday.com",
        imageUrl = "https://www.medicalnewstoday.com/favicon.ico",
        updatedAt = "2025.02.18.12.00",
        fetchUrl = "https://www.medicalnewstoday.com/rss/featurednews.xml",
        logic = SourceLogic(
            type = SourceType.RSS,
            selectors = ElementSelectors(
                itemSelector = "item",
                titleSelector = "title",
                linkSelector = "link",
                imageSelector = "media:content",
                dateSelector = "pubDate"
            )
        )
    )

    @Test
    fun testParseStandardRssXml() {
        val xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/">
                <channel>
                    <title>Medical News Today</title>
                    <link>https://www.medicalnewstoday.com</link>
                    <item>
                        <title><![CDATA[New Breakthrough in Cancer Research &amp; Treatment]]></title>
                        <link>https://www.medicalnewstoday.com/articles/cancer-research-breakthrough</link>
                        <pubDate>Tue, 18 Feb 2025 10:00:00 GMT</pubDate>
                        <media:content url="https://www.medicalnewstoday.com/images/cancer.jpg" medium="image" />
                        <description>Scientists have discovered a new pathway for target therapy.</description>
                    </item>
                    <item>
                        <title>Heart Health: Simple Habits for Better Cardiovascular System</title>
                        <link>/articles/heart-health-tips</link>
                        <pubDate>Mon, 17 Feb 2025 08:30:00 GMT</pubDate>
                        <enclosure url="https://www.medicalnewstoday.com/images/heart.jpg" type="image/jpeg"/>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val articles = fetcher.parseRssXml(xmlContent, rssSource)

        assertEquals(2, articles.size)

        val first = articles[0]
        assertEquals("New Breakthrough in Cancer Research & Treatment", first.title)
        assertEquals("https://www.medicalnewstoday.com/articles/cancer-research-breakthrough", first.articleUrl)
        assertEquals("https://www.medicalnewstoday.com/images/cancer.jpg", first.imageUrl)
        assertEquals("Tue, 18 Feb 2025 10:00:00 GMT", first.publishedAt)
        assertEquals("med_news_today_rss", first.sourceId)
        assertNotNull(first.id)
        assertTrue(first.id.isNotBlank())

        val second = articles[1]
        assertEquals("Heart Health: Simple Habits for Better Cardiovascular System", second.title)
        assertEquals("https://www.medicalnewstoday.com/articles/heart-health-tips", second.articleUrl)
        assertEquals("https://www.medicalnewstoday.com/images/heart.jpg", second.imageUrl)
    }

    @Test
    fun testParseAtomXmlFeed() {
        val xmlContent = """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
                <title>Medical Journal</title>
                <entry>
                    <title>Study reveals benefits of Mediterranean Diet</title>
                    <link rel="alternate" href="https://www.medicalnewstoday.com/articles/diet-study"/>
                    <updated>2025-02-18T12:00:00Z</updated>
                    <content type="html">&lt;img src="https://www.medicalnewstoday.com/images/diet.jpg"/&gt;&lt;p&gt;Content here&lt;/p&gt;</content>
                </entry>
            </feed>
        """.trimIndent()

        val articles = fetcher.parseRssXml(xmlContent, rssSource)

        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals("Study reveals benefits of Mediterranean Diet", article.title)
        assertEquals("https://www.medicalnewstoday.com/articles/diet-study", article.articleUrl)
        assertEquals("https://www.medicalnewstoday.com/images/diet.jpg", article.imageUrl)
        assertEquals("2025-02-18T12:00:00Z", article.publishedAt)
    }

    @Test
    fun testMalformedXmlReturnsEmptyListGracefully() {
        val articles = fetcher.parseRssXml("Invalid XML string", rssSource)
        assertTrue(articles.isEmpty())
    }
}

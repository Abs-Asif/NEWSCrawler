package abdullah.bari.asif

import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.model.SourceType
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NewsSourceSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testSourcesJsonParsing() {
        val jsonFile = if (File("sources/sources.json").exists()) {
            File("sources/sources.json")
        } else {
            File("../sources/sources.json")
        }
        assertTrue(jsonFile.exists(), "sources/sources.json file must exist at ${jsonFile.absolutePath}")

        val jsonContent = jsonFile.readText()
        val sources = json.decodeFromString<List<NewsSource>>(jsonContent)

        assertEquals(3, sources.size, "Should parse 3 news sources")

        val rssSource = sources.find { it.logic.type == SourceType.RSS }
        assertNotNull(rssSource, "RSS source should be present")
        assertEquals("med_news_today_rss", rssSource.id)
        assertEquals("item", rssSource.logic.selectors.itemSelector)

        val sitemapSource = sources.find { it.logic.type == SourceType.SITEMAP_XML }
        assertNotNull(sitemapSource, "Sitemap source should be present")
        assertEquals("who_sitemap", sitemapSource.id)
        assertEquals("url", sitemapSource.logic.selectors.urlSelector)

        val customHtmlSource = sources.find { it.logic.type == SourceType.CUSTOM_HTML }
        assertNotNull(customHtmlSource, "Custom HTML source should be present")
        assertEquals("nih_news_custom", customHtmlSource.id)
        assertEquals("li.news-item", customHtmlSource.logic.selectors.containerSelector)
    }
}

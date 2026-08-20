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

        assertEquals(6, sources.size, "Should parse 6 news sources")

        val sitemapSource = sources.find { it.logic.type == SourceType.SITEMAP_XML }
        assertNotNull(sitemapSource, "SITEMAP_XML source should be present")
        assertEquals("the_hindu_sitemap", sitemapSource.id)
        assertEquals("url", sitemapSource.logic.selectors.urlSelector)
    }
}

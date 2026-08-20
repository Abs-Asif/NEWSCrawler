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

        assertEquals(7, sources.size, "Should parse 7 news sources")

        val rssSource = sources.find { it.logic.type == SourceType.RSS }
        assertNotNull(rssSource, "RSS source should be present")
        assertEquals("ndtv_news_rss", rssSource.id)
        assertEquals("item", rssSource.logic.selectors.itemSelector)
    }
}

package abdullah.bari.asif

import abdullah.bari.asif.db.AppDatabase
import abdullah.bari.asif.db.JdbcDatabaseDriver
import abdullah.bari.asif.repository.NewsSourceRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewsSourceRepositoryTest {

    private lateinit var database: AppDatabase
    private val repository = NewsSourceRepository()

    @BeforeTest
    fun setup() {
        val driver = JdbcDatabaseDriver(":memory:")
        database = AppDatabase(driver)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun testParseSourcesJson() {
        val sources = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON)
        assertEquals(7, sources.size)
        assertTrue(sources.any { it.id == "ndtv_news_rss" })
        assertTrue(sources.any { it.id == "toi_news_rss" })
        assertTrue(sources.any { it.id == "the_hindu_rss" })
    }

    @Test
    fun testGetEnabledSourcesWhenDbEmptyAutoInitializes() = runBlocking {
        val enabledSources = repository.getEnabledSources(database)
        assertEquals(7, enabledSources.size)
        assertTrue(enabledSources.all { it.isInstalled })
    }

    @Test
    fun testGetEnabledSourcesRespectsDisabledSources() = runBlocking {
        // Initialize DB with all installed, but disable one
        database.sourceDao.setSourceInstalled("ndtv_news_rss", isEnabled = true)
        database.sourceDao.setSourceInstalled("toi_news_rss", isEnabled = false)
        database.sourceDao.setSourceInstalled("the_hindu_rss", isEnabled = true)

        val enabledSources = repository.getEnabledSources(database)
        assertEquals(2, enabledSources.size)
        assertFalse(enabledSources.any { it.id == "toi_news_rss" })
        assertTrue(enabledSources.any { it.id == "ndtv_news_rss" })
        assertTrue(enabledSources.any { it.id == "the_hindu_rss" })
    }
}

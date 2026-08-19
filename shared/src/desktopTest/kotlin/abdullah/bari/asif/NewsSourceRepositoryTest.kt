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
        assertEquals(3, sources.size)
        assertTrue(sources.any { it.id == "med_news_today_rss" })
        assertTrue(sources.any { it.id == "who_sitemap" })
        assertTrue(sources.any { it.id == "nih_news_custom" })
    }

    @Test
    fun testGetEnabledSourcesWhenDbEmptyAutoInitializes() = runBlocking {
        val enabledSources = repository.getEnabledSources(database)
        assertEquals(3, enabledSources.size)
        assertTrue(enabledSources.all { it.isInstalled })
    }

    @Test
    fun testGetEnabledSourcesRespectsDisabledSources() = runBlocking {
        // Initialize DB with all installed, but disable one
        database.sourceDao.setSourceInstalled("med_news_today_rss", isEnabled = true)
        database.sourceDao.setSourceInstalled("who_sitemap", isEnabled = false)
        database.sourceDao.setSourceInstalled("nih_news_custom", isEnabled = true)

        val enabledSources = repository.getEnabledSources(database)
        assertEquals(2, enabledSources.size)
        assertFalse(enabledSources.any { it.id == "who_sitemap" })
        assertTrue(enabledSources.any { it.id == "med_news_today_rss" })
        assertTrue(enabledSources.any { it.id == "nih_news_custom" })
    }
}

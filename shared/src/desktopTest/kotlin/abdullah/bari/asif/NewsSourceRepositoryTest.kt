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
        assertEquals(6, sources.size)
        assertTrue(sources.any { it.id == "the_hindu_sitemap" })
        assertTrue(sources.any { it.id == "greater_kashmir_sitemap" })
        assertTrue(sources.any { it.id == "siasat_daily_sitemap" })
    }

    @Test
    fun testGetEnabledSourcesWhenDbEmptyAutoInitializes() = runBlocking {
        val enabledSources = repository.getEnabledSources(database)
        assertEquals(6, enabledSources.size)
        assertTrue(enabledSources.all { it.isInstalled })
    }

    @Test
    fun testGetEnabledSourcesRespectsDisabledSources() = runBlocking {
        // Initialize DB with all installed, but disable one
        database.sourceDao.setSourceInstalled("the_hindu_sitemap", isEnabled = true)
        database.sourceDao.setSourceInstalled("greater_kashmir_sitemap", isEnabled = false)
        database.sourceDao.setSourceInstalled("siasat_daily_sitemap", isEnabled = true)

        val enabledSources = repository.getEnabledSources(database)
        assertEquals(2, enabledSources.size)
        assertFalse(enabledSources.any { it.id == "greater_kashmir_sitemap" })
        assertTrue(enabledSources.any { it.id == "the_hindu_sitemap" })
        assertTrue(enabledSources.any { it.id == "siasat_daily_sitemap" })
    }
}

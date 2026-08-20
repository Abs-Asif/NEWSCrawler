package abdullah.bari.asif

import abdullah.bari.asif.crawler.UniversalFetcher
import abdullah.bari.asif.db.AppDatabase
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.repository.NewsSourceRepository
import abdullah.bari.asif.worker.NewsCrawlerWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationStartupTest {

    private lateinit var database: AppDatabase
    private val repository = NewsSourceRepository()

    @Before
    fun setup() {
        val driver = TestJdbcDatabaseDriver(":memory:")
        database = AppDatabase(driver)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppDatabaseTablesCreatedOnStartup() {
        // Verify database tables are properly initialized
        assertNotNull(database.articleDao)
        assertNotNull(database.sourceDao)
        assertNotNull(database.settingsDao)
    }

    @Test
    fun testCrawlerWorkerSyncExecutionOnStartup() = runBlocking {
        val fetcher = UniversalFetcher()
        val syncResult = NewsCrawlerWorker.performSync(
            db = database,
            fetcher = fetcher,
            sourceRepository = repository
        )

        // Verify crawler worker sync executes safely without throwing exceptions
        assertTrue(syncResult.syncedSourceCount >= 0)
    }
}

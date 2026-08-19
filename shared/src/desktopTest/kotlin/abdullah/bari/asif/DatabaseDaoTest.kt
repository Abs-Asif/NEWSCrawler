package abdullah.bari.asif

import abdullah.bari.asif.db.AppDatabase
import abdullah.bari.asif.db.JdbcDatabaseDriver
import abdullah.bari.asif.model.NewsArticle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DatabaseDaoTest {

    private lateinit var database: AppDatabase

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
    fun testSourceDaoOperations() = runBlocking {
        val sourceDao = database.sourceDao

        // Initially no sources installed
        assertFalse(sourceDao.isSourceInstalled("source_1"))
        val initialSources = sourceDao.getInstalledSources().first()
        assertTrue(initialSources.isEmpty())

        // Install source 1
        sourceDao.setSourceInstalled("source_1", isEnabled = true, lastSyncedAt = 1000L)

        assertTrue(sourceDao.isSourceInstalled("source_1"))
        val installedSource = sourceDao.getInstalledSource("source_1")
        assertNotNull(installedSource)
        assertEquals("source_1", installedSource.sourceId)
        assertTrue(installedSource.isEnabled)
        assertEquals(1000L, installedSource.lastSyncedAt)

        // Update sync timestamp
        sourceDao.updateLastSynced("source_1", 2000L)
        val updatedSource = sourceDao.getInstalledSource("source_1")
        assertEquals(2000L, updatedSource?.lastSyncedAt)

        // Remove source
        sourceDao.removeInstalledSource("source_1")
        assertFalse(sourceDao.isSourceInstalled("source_1"))
        assertNull(sourceDao.getInstalledSource("source_1"))
    }

    @Test
    fun testArticleDaoOperations() = runBlocking {
        val articleDao = database.articleDao

        val articles = listOf(
            NewsArticle(
                id = "article_1_hash",
                sourceId = "source_1",
                sourceName = "Source One",
                sourceLogoUrl = "https://example.com/logo.png",
                title = "First News Article",
                articleUrl = "https://example.com/article1",
                imageUrl = "https://example.com/image1.jpg",
                publishedAt = "2025-02-18T12:00:00Z",
                fetchedAt = 1000000L
            ),
            NewsArticle(
                id = "article_2_hash",
                sourceId = "source_1",
                sourceName = "Source One",
                sourceLogoUrl = "https://example.com/logo.png",
                title = "Second News Article",
                articleUrl = "https://example.com/article2",
                imageUrl = null,
                publishedAt = "2025-02-18T13:00:00Z",
                fetchedAt = 2000000L
            ),
            NewsArticle(
                id = "article_3_hash",
                sourceId = "source_2",
                sourceName = "Source Two",
                sourceLogoUrl = "https://example.com/logo2.png",
                title = "Third News Article",
                articleUrl = "https://example.com/article3",
                imageUrl = "https://example.com/image3.jpg",
                publishedAt = "2025-02-18T14:00:00Z",
                fetchedAt = 3000000L
            )
        )

        // Insert articles
        articleDao.insertArticles(articles)

        val allArticles = articleDao.getAllArticles().first()
        assertEquals(3, allArticles.size)
        // Ordered by fetched_at DESC
        assertEquals("article_3_hash", allArticles[0].id)
        assertEquals("article_2_hash", allArticles[1].id)
        assertEquals("article_1_hash", allArticles[2].id)

        // Query by source
        val source1Articles = articleDao.getArticlesBySource("source_1").first()
        assertEquals(2, source1Articles.size)

        // Delete by source
        articleDao.deleteArticlesBySource("source_1")
        val remainingArticles = articleDao.getAllArticles().first()
        assertEquals(1, remainingArticles.size)
        assertEquals("source_2", remainingArticles[0].sourceId)

        // Clear all
        articleDao.clearAll()
        val emptyArticles = articleDao.getAllArticles().first()
        assertTrue(emptyArticles.isEmpty())
    }

    @Test
    fun testArticleDeduplication() = runBlocking {
        val articleDao = database.articleDao

        val articleOriginal = NewsArticle(
            id = "duplicate_hash",
            sourceId = "source_1",
            sourceName = "Source One",
            sourceLogoUrl = "https://example.com/logo.png",
            title = "Original Title",
            articleUrl = "https://example.com/article",
            imageUrl = null,
            publishedAt = "2025-02-18T12:00:00Z",
            fetchedAt = 1000L
        )

        val articleUpdated = NewsArticle(
            id = "duplicate_hash",
            sourceId = "source_1",
            sourceName = "Source One",
            sourceLogoUrl = "https://example.com/logo.png",
            title = "Updated Title",
            articleUrl = "https://example.com/article",
            imageUrl = "https://example.com/image.jpg",
            publishedAt = "2025-02-18T12:00:00Z",
            fetchedAt = 2000L
        )

        articleDao.insertArticles(listOf(articleOriginal))
        assertEquals(1, articleDao.getAllArticles().first().size)
        assertEquals("Original Title", articleDao.getAllArticles().first()[0].title)

        // Re-inserting with same primary key `id` replaces old row
        articleDao.insertArticles(listOf(articleUpdated))
        val articlesAfterUpdate = articleDao.getAllArticles().first()
        assertEquals(1, articlesAfterUpdate.size)
        assertEquals("Updated Title", articlesAfterUpdate[0].title)
        assertEquals("https://example.com/image.jpg", articlesAfterUpdate[0].imageUrl)
    }
}

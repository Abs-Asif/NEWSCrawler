package abdullah.bari.asif

import abdullah.bari.asif.crawler.NewsFetcher
import abdullah.bari.asif.db.AppDatabase
import abdullah.bari.asif.db.DatabaseDriver
import abdullah.bari.asif.db.DbRow
import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.repository.NewsSourceRepository
import abdullah.bari.asif.worker.NewsCrawlerWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestJdbcDatabaseDriver(dbPath: String = ":memory:") : DatabaseDriver {
    private val connection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    override fun execute(sql: String, args: List<Any?>) {
        connection.prepareStatement(sql).use { stmt ->
            bindArgs(stmt, args)
            stmt.executeUpdate()
        }
    }

    override fun <T> query(sql: String, args: List<Any?>, mapper: (DbRow) -> T): List<T> {
        val result = mutableListOf<T>()
        connection.prepareStatement(sql).use { stmt ->
            bindArgs(stmt, args)
            stmt.executeQuery().use { rs ->
                val row = TestJdbcDbRow(rs)
                while (rs.next()) {
                    result.add(mapper(row))
                }
            }
        }
        return result
    }

    private fun bindArgs(stmt: PreparedStatement, args: List<Any?>) {
        args.forEachIndexed { index, arg ->
            val paramIndex = index + 1
            when (arg) {
                null -> stmt.setNull(paramIndex, java.sql.Types.NULL)
                is String -> stmt.setString(paramIndex, arg)
                is Long -> stmt.setLong(paramIndex, arg)
                is Int -> stmt.setInt(paramIndex, arg)
                is Double -> stmt.setDouble(paramIndex, arg)
                is Boolean -> stmt.setInt(paramIndex, if (arg) 1 else 0)
                else -> stmt.setString(paramIndex, arg.toString())
            }
        }
    }

    override fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}

private class TestJdbcDbRow(private val rs: ResultSet) : DbRow {
    override fun getString(columnName: String): String = rs.getString(columnName) ?: ""
    override fun getStringOrNull(columnName: String): String? = rs.getString(columnName)
    override fun getLong(columnName: String): Long = rs.getLong(columnName)
    override fun getLongOrNull(columnName: String): Long? {
        val value = rs.getLong(columnName)
        return if (rs.wasNull()) null else value
    }
    override fun getInt(columnName: String): Int = rs.getInt(columnName)
}

class NewsCrawlerWorkerTest {

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
    fun testPerformSyncFetchesAndDeduplicatesArticles() = runBlocking {
        val source1 = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON).first()

        // Pre-insert an existing article into DB
        val existingArticle = NewsArticle(
            id = "hash_100",
            sourceId = source1.id,
            sourceName = source1.name,
            sourceLogoUrl = source1.imageUrl,
            title = "Existing Article Title",
            articleUrl = "https://example.com/100",
            imageUrl = null,
            publishedAt = "2025-02-18",
            fetchedAt = 1000L
        )
        database.articleDao.insertArticles(listOf(existingArticle))

        // Fake fetcher returns 1 existing article and 1 new article
        val fakeFetcher = object : NewsFetcher {
            override suspend fun fetchArticles(source: NewsSource): List<NewsArticle> {
                return listOf(
                    existingArticle,
                    NewsArticle(
                        id = "hash_200",
                        sourceId = source.id,
                        sourceName = source.name,
                        sourceLogoUrl = source.imageUrl,
                        title = "New Cancer Discovery Headline",
                        articleUrl = "https://example.com/200",
                        imageUrl = "https://example.com/200.jpg",
                        publishedAt = "2025-02-18",
                        fetchedAt = 2000L
                    )
                )
            }
        }

        var dispatchedNotificationsCount = 0
        var notifiedArticles: List<NewsArticle> = emptyList()

        val syncResult = NewsCrawlerWorker.performSync(
            db = database,
            fetcher = fakeFetcher,
            sourceRepository = repository,
            onNewArticlesDispatched = { newArticles ->
                dispatchedNotificationsCount++
                notifiedArticles = newArticles
            }
        )

        assertEquals(6, syncResult.fetchedTotal) // 3 sources * 2 articles = 6
        // Deduplication: hash_100 is duplicate per source, hash_200 is new per source
        assertTrue(syncResult.newArticlesCount > 0)

        val dbArticles = database.articleDao.getAllArticles().first()
        assertTrue(dbArticles.any { it.id == "hash_200" })
        assertEquals(1, dispatchedNotificationsCount)
        assertEquals(syncResult.newArticlesCount, notifiedArticles.size)

        // Verify last_synced_at was updated in DB
        val installedSource = database.sourceDao.getInstalledSource(source1.id)
        assertTrue(installedSource != null && installedSource.lastSyncedAt > 0L)
    }
}

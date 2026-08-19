package abdullah.bari.asif.db

import kotlinx.coroutines.flow.MutableSharedFlow

class AppDatabase(val driver: DatabaseDriver) {
    private val articleChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
    private val sourceChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    val articleDao: ArticleDao = ArticleDaoImpl(driver, articleChanges)
    val sourceDao: SourceDao = SourceDaoImpl(driver, sourceChanges)

    init {
        createTables()
    }

    private fun createTables() {
        driver.execute(
            """
            CREATE TABLE IF NOT EXISTS articles (
                id TEXT PRIMARY KEY,
                source_id TEXT NOT NULL,
                source_name TEXT NOT NULL,
                source_logo_url TEXT NOT NULL,
                title TEXT NOT NULL,
                article_url TEXT NOT NULL,
                image_url TEXT,
                published_at TEXT NOT NULL,
                fetched_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        driver.execute(
            """
            CREATE TABLE IF NOT EXISTS installed_sources (
                source_id TEXT PRIMARY KEY,
                is_enabled INTEGER NOT NULL,
                last_synced_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    fun close() {
        driver.close()
    }
}

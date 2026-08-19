package abdullah.bari.asif.db

import abdullah.bari.asif.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class ArticleDaoImpl(
    private val driver: DatabaseDriver,
    private val changeNotificationFlow: MutableSharedFlow<Unit>
) : ArticleDao {

    override fun getAllArticles(): Flow<List<NewsArticle>> = flow {
        emit(queryAllArticles())
        changeNotificationFlow.collect {
            emit(queryAllArticles())
        }
    }

    override fun getArticlesBySource(sourceId: String): Flow<List<NewsArticle>> = flow {
        emit(queryArticlesBySource(sourceId))
        changeNotificationFlow.collect {
            emit(queryArticlesBySource(sourceId))
        }
    }

    private fun queryAllArticles(): List<NewsArticle> {
        val sql = "SELECT id, source_id, source_name, source_logo_url, title, article_url, image_url, published_at, fetched_at FROM articles ORDER BY fetched_at DESC"
        return driver.query(sql) { row ->
            NewsArticle(
                id = row.getString("id"),
                sourceId = row.getString("source_id"),
                sourceName = row.getString("source_name"),
                sourceLogoUrl = row.getString("source_logo_url"),
                title = row.getString("title"),
                articleUrl = row.getString("article_url"),
                imageUrl = row.getStringOrNull("image_url"),
                publishedAt = row.getString("published_at"),
                fetchedAt = row.getLong("fetched_at")
            )
        }
    }

    private fun queryArticlesBySource(sourceId: String): List<NewsArticle> {
        val sql = "SELECT id, source_id, source_name, source_logo_url, title, article_url, image_url, published_at, fetched_at FROM articles WHERE source_id = ? ORDER BY fetched_at DESC"
        return driver.query(sql, listOf(sourceId)) { row ->
            NewsArticle(
                id = row.getString("id"),
                sourceId = row.getString("source_id"),
                sourceName = row.getString("source_name"),
                sourceLogoUrl = row.getString("source_logo_url"),
                title = row.getString("title"),
                articleUrl = row.getString("article_url"),
                imageUrl = row.getStringOrNull("image_url"),
                publishedAt = row.getString("published_at"),
                fetchedAt = row.getLong("fetched_at")
            )
        }
    }

    override suspend fun insertArticles(articles: List<NewsArticle>) {
        if (articles.isEmpty()) return
        val sql = """
            INSERT OR REPLACE INTO articles
            (id, source_id, source_name, source_logo_url, title, article_url, image_url, published_at, fetched_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        for (article in articles) {
            driver.execute(
                sql,
                listOf(
                    article.id,
                    article.sourceId,
                    article.sourceName,
                    article.sourceLogoUrl,
                    article.title,
                    article.articleUrl,
                    article.imageUrl,
                    article.publishedAt,
                    article.fetchedAt
                )
            )
        }
        changeNotificationFlow.emit(Unit)
    }

    override suspend fun deleteArticlesBySource(sourceId: String) {
        val sql = "DELETE FROM articles WHERE source_id = ?"
        driver.execute(sql, listOf(sourceId))
        changeNotificationFlow.emit(Unit)
    }

    override suspend fun clearAll() {
        driver.execute("DELETE FROM articles")
        changeNotificationFlow.emit(Unit)
    }
}

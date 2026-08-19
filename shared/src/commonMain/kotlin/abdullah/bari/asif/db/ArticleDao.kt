package abdullah.bari.asif.db

import abdullah.bari.asif.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface ArticleDao {
    fun getAllArticles(): Flow<List<NewsArticle>>
    fun getArticlesBySource(sourceId: String): Flow<List<NewsArticle>>
    suspend fun insertArticles(articles: List<NewsArticle>)
    suspend fun deleteArticlesBySource(sourceId: String)
    suspend fun clearAll()
}

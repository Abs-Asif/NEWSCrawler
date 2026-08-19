package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource

interface NewsFetcher {
    suspend fun fetchArticles(source: NewsSource): List<NewsArticle>
}

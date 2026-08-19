package abdullah.bari.asif.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import abdullah.bari.asif.crawler.NewsFetcher
import abdullah.bari.asif.crawler.UniversalFetcher
import abdullah.bari.asif.db.AndroidDatabaseDriver
import abdullah.bari.asif.db.AppDatabase
import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.notification.NotificationDispatcher
import abdullah.bari.asif.repository.NewsSourceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class NewsCrawlerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase(AndroidDatabaseDriver(applicationContext))
            val fetcher = UniversalFetcher()
            val repository = NewsSourceRepository()

            performSync(
                db = db,
                fetcher = fetcher,
                sourceRepository = repository,
                onNewArticlesDispatched = { articles ->
                    NotificationDispatcher.dispatchNewArticleNotifications(
                        applicationContext,
                        articles
                    )
                }
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        data class SyncResult(
            val fetchedTotal: Int,
            val newArticlesCount: Int,
            val syncedSourceCount: Int
        )

        suspend fun performSync(
            db: AppDatabase,
            fetcher: NewsFetcher,
            sourceRepository: NewsSourceRepository,
            onNewArticlesDispatched: (List<NewsArticle>) -> Unit = {}
        ): SyncResult = coroutineScope {
            val enabledSources = sourceRepository.getEnabledSources(db)
            if (enabledSources.isEmpty()) {
                return@coroutineScope SyncResult(0, 0, 0)
            }

            val fetchedArticles = enabledSources.map { source ->
                async {
                    try {
                        fetcher.fetchArticles(source)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()

            val existingArticles = db.articleDao.getAllArticles().first()
            val existingIds = existingArticles.map { it.id }.toSet()

            // Filter out duplicate articles matching URL hash (id)
            val newArticles = fetchedArticles.filter { it.id !in existingIds }

            if (newArticles.isNotEmpty()) {
                db.articleDao.insertArticles(newArticles)
            }

            val now = System.currentTimeMillis()
            for (source in enabledSources) {
                db.sourceDao.updateLastSynced(source.id, now)
            }

            if (newArticles.isNotEmpty()) {
                onNewArticlesDispatched(newArticles)
            }

            SyncResult(
                fetchedTotal = fetchedArticles.size,
                newArticlesCount = newArticles.size,
                syncedSourceCount = enabledSources.size
            )
        }
    }
}

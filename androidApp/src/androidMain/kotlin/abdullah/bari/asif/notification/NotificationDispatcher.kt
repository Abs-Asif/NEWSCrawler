package abdullah.bari.asif.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import abdullah.bari.asif.MainActivity
import abdullah.bari.asif.android.R
import abdullah.bari.asif.filter.WordFilterEngine
import abdullah.bari.asif.model.NewsArticle

object NotificationDispatcher {
    const val CHANNEL_ID = "news_crawler_channel"
    const val CHANNEL_NAME = "News Alerts"
    const val CHANNEL_DESC = "Notifications for new matching news headlines"

    const val EXTRA_ARTICLE_ID = "extra_article_id"
    const val EXTRA_ARTICLE_TITLE = "extra_article_title"
    const val EXTRA_ARTICLE_URL = "extra_article_url"
    const val EXTRA_SOURCE_NAME = "extra_source_name"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Filters newly scraped articles against active notification word filters
     * and posts a system notification for each matching article.
     *
     * @return Number of notifications posted.
     */
    fun dispatchNewArticleNotifications(context: Context, articles: List<NewsArticle>): Int {
        if (articles.isEmpty()) return 0
        if (!NotificationPreferences.isNotificationsEnabled(context)) return 0

        val filters = NotificationPreferences.getNotificationFilters(context)
        val matchingArticles = articles.filter { WordFilterEngine.matches(it.title, filters) }

        if (matchingArticles.isEmpty()) return 0

        createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        var count = 0

        for (article in matchingArticles) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Cannot post notifications without runtime permission
                    break
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ARTICLE_ID, article.id)
                putExtra(EXTRA_ARTICLE_TITLE, article.title)
                putExtra(EXTRA_ARTICLE_URL, article.articleUrl)
                putExtra(EXTRA_SOURCE_NAME, article.sourceName)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                article.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_newspaper)
                .setContentTitle(article.sourceName)
                .setContentText(article.title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(article.title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                notificationManager.notify(article.id.hashCode(), notification)
                count++
            } catch (e: SecurityException) {
                // Ignore if missing permission at runtime
                break
            }
        }

        return count
    }
}

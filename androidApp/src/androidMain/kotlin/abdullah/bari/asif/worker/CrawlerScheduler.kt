package abdullah.bari.asif.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CrawlerScheduler {
    const val WORK_NAME = "NewsCrawlerBackgroundSyncWork"
    const val DEFAULT_INTERVAL_MINUTES = 60L

    fun buildPeriodicWorkRequest(
        intervalMinutes: Long = DEFAULT_INTERVAL_MINUTES,
        requiresWifiOnly: Boolean = false
    ) = PeriodicWorkRequestBuilder<NewsCrawlerWorker>(
        maxOf(intervalMinutes, 15L),
        TimeUnit.MINUTES
    ).setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(if (requiresWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
    ).build()

    fun schedulePeriodicSync(
        context: Context,
        intervalMinutes: Long = DEFAULT_INTERVAL_MINUTES,
        requiresWifiOnly: Boolean = false
    ) {
        val workRequest = buildPeriodicWorkRequest(intervalMinutes, requiresWifiOnly)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun triggerOneTimeSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<NewsCrawlerWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}

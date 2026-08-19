package abdullah.bari.asif

import android.app.Application
import abdullah.bari.asif.notification.NotificationDispatcher
import abdullah.bari.asif.worker.CrawlerScheduler

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationDispatcher.createNotificationChannel(this)
        CrawlerScheduler.schedulePeriodicSync(this)
    }
}

package abdullah.bari.asif

import android.app.Application
import abdullah.bari.asif.notification.NotificationDispatcher
import abdullah.bari.asif.ui.utils.PlatformShare
import abdullah.bari.asif.worker.CrawlerScheduler

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformShare.init(this)
        NotificationDispatcher.createNotificationChannel(this)
        CrawlerScheduler.schedulePeriodicSync(this)
    }
}

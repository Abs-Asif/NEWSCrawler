package abdullah.bari.asif

import android.app.Application
import abdullah.bari.asif.notification.NotificationDispatcher
import abdullah.bari.asif.ui.utils.PlatformShare
import abdullah.bari.asif.worker.CrawlerScheduler

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            PlatformShare.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            NotificationDispatcher.createNotificationChannel(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            CrawlerScheduler.schedulePeriodicSync(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

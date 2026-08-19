package abdullah.bari.asif.db

import android.content.Context

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): DatabaseDriver {
        return AndroidDatabaseDriver(context, "newscrawler.db")
    }
}

package abdullah.bari.asif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import abdullah.bari.asif.db.AndroidDatabaseDriver
import abdullah.bari.asif.db.AppDatabase

class MainActivity : ComponentActivity() {
    private var database: AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase(AndroidDatabaseDriver(applicationContext))
        database = db

        setContent {
            App(
                database = db,
                onExitApp = { finish() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        database?.close()
    }
}

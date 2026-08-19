package abdullah.bari.asif.ui.utils

import android.content.Context
import android.content.Intent

actual object PlatformShare {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun shareText(text: String, title: String?) {
        val ctx = appContext ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (title != null) {
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntent = Intent.createChooser(shareIntent, title ?: "Share Article").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(chooserIntent)
    }
}

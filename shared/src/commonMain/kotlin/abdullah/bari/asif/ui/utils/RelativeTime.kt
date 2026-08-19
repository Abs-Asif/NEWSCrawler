package abdullah.bari.asif.ui.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object RelativeTime {
    fun format(publishedAt: String, fetchedAt: Long = 0L): String {
        if (publishedAt.isBlank() && fetchedAt <= 0L) return ""

        val publishedInstant = try {
            if (publishedAt.isNotBlank()) {
                Instant.parse(publishedAt)
            } else null
        } catch (e: Exception) {
            null
        }

        val millis = when {
            publishedInstant != null -> publishedInstant.toEpochMilliseconds()
            fetchedAt > 0L -> fetchedAt
            else -> return publishedAt
        }

        val nowMillis = Clock.System.now().toEpochMilliseconds()
        val diffMillis = nowMillis - millis

        if (diffMillis < 0) {
            return if (publishedAt.isNotBlank()) publishedAt else "Recently"
        }

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> if (publishedAt.isNotBlank()) publishedAt else "${days / 7}w ago"
        }
    }
}

package abdullah.bari.asif

import androidx.work.NetworkType
import abdullah.bari.asif.worker.CrawlerScheduler
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CrawlerSchedulerTest {

    @Test
    fun testBuildPeriodicWorkRequestDefaultConstraints() {
        val request = CrawlerScheduler.buildPeriodicWorkRequest(
            intervalMinutes = 60L,
            requiresWifiOnly = false
        )

        assertNotNull(request)
        assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun testBuildPeriodicWorkRequestWifiConstraints() {
        val request = CrawlerScheduler.buildPeriodicWorkRequest(
            intervalMinutes = 30L,
            requiresWifiOnly = true
        )

        assertNotNull(request)
        assertEquals(NetworkType.UNMETERED, request.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun testBuildPeriodicWorkRequestEnforcesMinimumInterval() {
        val request = CrawlerScheduler.buildPeriodicWorkRequest(
            intervalMinutes = 5L, // WorkManager min is 15 minutes
            requiresWifiOnly = false
        )

        assertNotNull(request)
        assertEquals(15 * 60 * 1000L, request.workSpec.intervalDuration)
    }
}

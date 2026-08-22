package abdullah.bari.asif.ui

import abdullah.bari.asif.repository.NewsSourceRepository
import abdullah.bari.asif.ui.FetchInterval
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreAndSettingsTest {

    private val repository = NewsSourceRepository()
    private val sources = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON)

    @Test
    fun testSearchFilteringInStore() {
        val query = "The Hindu"
        val filtered = sources.filter {
            it.name.contains(query, ignoreCase = true) || it.baseUrl.contains(query, ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("the_hindu_sitemap", filtered.first().id)
    }

    @Test
    fun testSearchFilteringByUrl() {
        val query = "thehindu.com"
        val filtered = sources.filter {
            it.name.contains(query, ignoreCase = true) || it.baseUrl.contains(query, ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("the_hindu_sitemap", filtered.first().id)
    }

    @Test
    fun testToggleInstallationState() {
        val originalSource = sources.first()
        assertFalse(originalSource.isInstalled)

        val installedSource = originalSource.copy(isInstalled = true)
        assertTrue(installedSource.isInstalled)
    }

    @Test
    fun testFetchIntervalMinutes() {
        assertEquals(15L, FetchInterval.MIN_15.minutes)
        assertEquals(30L, FetchInterval.MIN_30.minutes)
        assertEquals(60L, FetchInterval.HOUR_1.minutes)
        assertEquals(360L, FetchInterval.HOUR_6.minutes)
        assertEquals(-1L, FetchInterval.MANUAL.minutes)
    }
}

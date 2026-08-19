package abdullah.bari.asif.filter

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordFilterEngineTest {

    @Test
    fun testEmptyFiltersMatchAllTitles() {
        assertTrue(WordFilterEngine.matches("Any headline title", emptyList()))
        assertTrue(WordFilterEngine.matches("Any headline title", listOf("   ", "")))
    }

    @Test
    fun testPositiveFiltersInclusion() {
        val title = "New breakthrough in cancer research published"
        assertTrue(WordFilterEngine.matches(title, listOf("cancer")))
        assertTrue(WordFilterEngine.matches(title, listOf("CANCER")))
        assertTrue(WordFilterEngine.matches(title, listOf("breakthrough")))
        assertFalse(WordFilterEngine.matches(title, listOf("diabetes")))
    }

    @Test
    fun testNegativeFiltersExclusion() {
        val title = "Police update on local incident"
        assertFalse(WordFilterEngine.matches(title, listOf("!police")))
        assertFalse(WordFilterEngine.matches(title, listOf("-police")))
        assertFalse(WordFilterEngine.matches(title, listOf("!POLICE")))

        val safeTitle = "Medical community celebrates new discovery"
        assertTrue(WordFilterEngine.matches(safeTitle, listOf("!police")))
        assertTrue(WordFilterEngine.matches(safeTitle, listOf("-police")))
    }

    @Test
    fun testCompoundPositiveAndNegativeFilters() {
        val matchingTitle = "5 people killed in tragic highway accident"
        val excludedTitle = "5 people killed in police chase"

        val filtersExclamation = listOf("killed", "!police")
        val filtersMinus = listOf("killed", "-police")

        assertTrue(WordFilterEngine.matches(matchingTitle, filtersExclamation))
        assertTrue(WordFilterEngine.matches(matchingTitle, filtersMinus))

        assertFalse(WordFilterEngine.matches(excludedTitle, filtersExclamation))
        assertFalse(WordFilterEngine.matches(excludedTitle, filtersMinus))
    }
}

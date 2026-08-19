package abdullah.bari.asif.filter

object WordFilterEngine {
    /**
     * Evaluates whether an article title matches the given list of filter rules.
     *
     * Rules:
     * - Terms starting with '!' or '-' are negative filters. The title MUST NOT contain the keyword (case-insensitive).
     * - Terms not starting with '!' or '-' are positive filters. If positive filters are present,
     *   the title MUST contain at least one positive keyword (case-insensitive).
     * - If no filters are provided, returns true.
     */
    fun matches(title: String, filters: List<String>): Boolean {
        val trimmedFilters = filters.map { it.trim() }.filter { it.isNotEmpty() }
        if (trimmedFilters.isEmpty()) return true

        val lowerTitle = title.lowercase()

        val negativeKeywords = trimmedFilters
            .filter { it.startsWith("!") || it.startsWith("-") }
            .map { it.substring(1).trim().lowercase() }
            .filter { it.isNotEmpty() }

        val positiveKeywords = trimmedFilters
            .filter { !it.startsWith("!") && !it.startsWith("-") }
            .map { it.lowercase() }

        // Reject if title contains any negative keyword
        for (neg in negativeKeywords) {
            if (lowerTitle.contains(neg)) {
                return false
            }
        }

        // If positive keywords exist, title must contain at least one
        if (positiveKeywords.isNotEmpty()) {
            val matchesPositive = positiveKeywords.any { lowerTitle.contains(it) }
            if (!matchesPositive) {
                return false
            }
        }

        return true
    }
}

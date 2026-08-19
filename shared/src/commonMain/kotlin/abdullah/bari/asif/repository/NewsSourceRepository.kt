package abdullah.bari.asif.repository

import abdullah.bari.asif.db.AppDatabase
import abdullah.bari.asif.model.NewsSource
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class NewsSourceRepository(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    fun parseSourcesJson(jsonContent: String): List<NewsSource> {
        return json.decodeFromString(jsonContent)
    }

    suspend fun getEnabledSources(
        db: AppDatabase,
        sourcesJson: String = DEFAULT_SOURCES_JSON
    ): List<NewsSource> {
        val availableSources = parseSourcesJson(sourcesJson)
        val installedSources = db.sourceDao.getInstalledSources().first()

        if (installedSources.isEmpty()) {
            // Auto-initialize default sources as installed & enabled if DB is empty
            for (source in availableSources) {
                db.sourceDao.setSourceInstalled(source.id, isEnabled = true)
            }
            return availableSources.map { it.copy(isInstalled = true) }
        }

        val enabledIds = installedSources
            .filter { it.isEnabled }
            .map { it.sourceId }
            .toSet()

        val installedIds = installedSources
            .map { it.sourceId }
            .toSet()

        return availableSources
            .filter { it.id in enabledIds }
            .map { it.copy(isInstalled = it.id in installedIds) }
    }

    companion object {
        val DEFAULT_SOURCES_JSON = """
            [
              {
                "id": "med_news_today_rss",
                "name": "Medical News Today",
                "baseUrl": "https://www.medicalnewstoday.com",
                "imageUrl": "https://www.medicalnewstoday.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://www.medicalnewstoday.com/rss/featurednews.xml",
                "logic": {
                  "type": "RSS",
                  "selectors": {
                    "itemSelector": "item",
                    "titleSelector": "title",
                    "linkSelector": "link",
                    "imageSelector": "media:content",
                    "dateSelector": "pubDate"
                  }
                }
              },
              {
                "id": "who_sitemap",
                "name": "World Health Organization News",
                "baseUrl": "https://www.who.int",
                "imageUrl": "https://www.who.int/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://www.who.int/sitemap-{year}-{month}.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "dateFormatPattern": "yyyy-MM",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              },
              {
                "id": "nih_news_custom",
                "name": "NIH Research Matters",
                "baseUrl": "https://www.nih.gov",
                "imageUrl": "https://www.nih.gov/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://www.nih.gov/news-events/news-releases",
                "logic": {
                  "type": "CUSTOM_HTML",
                  "selectors": {
                    "containerSelector": "li.news-item",
                    "titleSelector": "h3.title a",
                    "linkSelector": "h3.title a[href]",
                    "imageSelector": "img.thumbnail[src]",
                    "dateSelector": "span.date"
                  }
                }
              }
            ]
        """.trimIndent()
    }
}

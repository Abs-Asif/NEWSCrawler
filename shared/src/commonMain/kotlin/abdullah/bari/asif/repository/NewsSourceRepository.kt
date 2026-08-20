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
                "id": "ndtv_news_rss",
                "name": "NDTV News",
                "baseUrl": "https://www.ndtv.com",
                "imageUrl": "https://www.ndtv.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://feeds.feedburner.com/ndtvnews-top-stories",
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
                "id": "toi_news_rss",
                "name": "Times of India",
                "baseUrl": "https://timesofindia.indiatimes.com",
                "imageUrl": "https://timesofindia.indiatimes.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
                "logic": {
                  "type": "RSS",
                  "selectors": {
                    "itemSelector": "item",
                    "titleSelector": "title",
                    "linkSelector": "link",
                    "imageSelector": "enclosure",
                    "dateSelector": "pubDate"
                  }
                }
              },
              {
                "id": "india_today_rss",
                "name": "India Today",
                "baseUrl": "https://www.indiatoday.in",
                "imageUrl": "https://www.indiatoday.in/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://www.indiatoday.in/rss/1206584",
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
                "id": "the_hindu_rss",
                "name": "The Hindu",
                "baseUrl": "https://www.thehindu.com",
                "imageUrl": "https://www.thehindu.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://www.thehindu.com/news/national/feeder/default.rss",
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
                "id": "economic_times_rss",
                "name": "Economic Times",
                "baseUrl": "https://economictimes.indiatimes.com",
                "imageUrl": "https://economictimes.indiatimes.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://economictimes.indiatimes.com/rssfeedstopstories.cms",
                "logic": {
                  "type": "RSS",
                  "selectors": {
                    "itemSelector": "item",
                    "titleSelector": "title",
                    "linkSelector": "link",
                    "imageSelector": "enclosure",
                    "dateSelector": "pubDate"
                  }
                }
              },
              {
                "id": "zee_news_rss",
                "name": "Zee News",
                "baseUrl": "https://zeenews.india.com",
                "imageUrl": "https://zeenews.india.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://zeenews.india.com/rss/india-national-news.xml",
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
                "id": "indian_express_rss",
                "name": "Indian Express",
                "baseUrl": "https://indianexpress.com",
                "imageUrl": "https://indianexpress.com/favicon.ico",
                "updatedAt": "2025.02.18.12.00",
                "fetchUrl": "https://indianexpress.com/feed/",
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
              }
            ]
        """.trimIndent()
    }
}

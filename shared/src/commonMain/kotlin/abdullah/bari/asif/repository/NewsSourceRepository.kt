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
                "id": "the_hindu_sitemap",
                "name": "The Hindu",
                "baseUrl": "https://www.thehindu.com",
                "imageUrl": "https://www.thehindu.com/favicon.ico",
                "updatedAt": "2025.02.20.12.00",
                "fetchUrl": "https://www.thehindu.com/sitemap/googlenews/all/all.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              },
              {
                "id": "greater_kashmir_sitemap",
                "name": "Greater Kashmir",
                "baseUrl": "https://www.greaterkashmir.com",
                "imageUrl": "https://www.greaterkashmir.com/favicon.ico",
                "updatedAt": "2025.02.20.12.00",
                "fetchUrl": "https://www.greaterkashmir.com/sitemap.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              },
              {
                "id": "siasat_daily_sitemap",
                "name": "The Siasat Daily",
                "baseUrl": "https://www.siasat.com",
                "imageUrl": "https://www.siasat.com/favicon.ico",
                "updatedAt": "2025.02.20.12.00",
                "fetchUrl": "https://www.siasat.com/post-sitemap.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              },
              {
                "id": "dt_next_sitemap",
                "name": "DT Next",
                "baseUrl": "https://www.dtnext.in",
                "imageUrl": "https://www.dtnext.in/favicon.ico",
                "updatedAt": "2025.02.20.12.00",
                "fetchUrl": "https://www.dtnext.in/sitemap.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              },
              {
                "id": "telangana_today_sitemap",
                "name": "Telangana Today",
                "baseUrl": "https://telanganatoday.com",
                "imageUrl": "https://telanganatoday.com/favicon.ico",
                "updatedAt": "2025.02.20.12.00",
                "fetchUrl": "https://telanganatoday.com/sitemap.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              },
              {
                "id": "business_standard_sitemap",
                "name": "Business Standard",
                "baseUrl": "https://www.businessstandard.com",
                "imageUrl": "https://www.businessstandard.com/favicon.ico",
                "updatedAt": "2025.02.20.12.00",
                "fetchUrl": "https://www.businessstandard.com/sitemap.xml",
                "logic": {
                  "type": "SITEMAP_XML",
                  "selectors": {
                    "urlSelector": "url",
                    "locSelector": "loc",
                    "lastmodSelector": "lastmod"
                  }
                }
              }
            ]
        """.trimIndent()
    }
}

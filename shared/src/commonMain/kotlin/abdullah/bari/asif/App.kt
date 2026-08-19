package abdullah.bari.asif

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.repository.NewsSourceRepository
import abdullah.bari.asif.ui.FetchInterval
import abdullah.bari.asif.ui.HomeScreen
import abdullah.bari.asif.ui.SettingsScreen
import abdullah.bari.asif.ui.StoreScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class Screen { HOME, STORE, SETTINGS }

@Composable
fun App(
    articles: List<NewsArticle> = defaultSampleArticles,
    sources: List<NewsSource> = defaultSampleSources,
    onToggleInstallSource: ((NewsSource, Boolean) -> Unit)? = null,
    onClearCache: (() -> Unit)? = null
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var sourceList by remember(sources) { mutableStateOf(sources) }
    var articleList by remember(articles) { mutableStateOf(articles) }

    var selectedInterval by remember { mutableStateOf(FetchInterval.HOUR_1) }
    var syncWifiOnly by remember { mutableStateOf(false) }
    var syncWhenCharging by remember { mutableStateOf(false) }

    MaterialTheme {
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    articles = articleList,
                    onStoreClick = { currentScreen = Screen.STORE },
                    onSettingsClick = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.STORE -> {
                StoreScreen(
                    sources = sourceList,
                    onToggleInstall = { source, install ->
                        sourceList = sourceList.map {
                            if (it.id == source.id) it.copy(isInstalled = install) else it
                        }
                        onToggleInstallSource?.invoke(source, install)
                    },
                    onBackClick = { currentScreen = Screen.HOME }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    currentInterval = selectedInterval,
                    syncWifiOnly = syncWifiOnly,
                    syncWhenCharging = syncWhenCharging,
                    onIntervalChange = { selectedInterval = it },
                    onWifiOnlyChange = { syncWifiOnly = it },
                    onSyncWhenChargingChange = { syncWhenCharging = it },
                    onClearCacheClick = {
                        articleList = emptyList()
                        onClearCache?.invoke()
                    },
                    onBackClick = { currentScreen = Screen.HOME }
                )
            }
        }
    }
}

private val repository = NewsSourceRepository()
private val defaultSampleSources = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON)
    .map { it.copy(isInstalled = true) }

private val defaultSampleArticles = listOf(
    NewsArticle(
        id = "sample_1",
        sourceId = "med_news_today_rss",
        sourceName = "Medical News Today",
        sourceLogoUrl = "https://www.medicalnewstoday.com/favicon.ico",
        title = "New breakthrough in cancer research published in medical journal",
        articleUrl = "https://www.medicalnewstoday.com/articles/breakthrough-cancer-research",
        imageUrl = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d",
        publishedAt = "2025-02-18T12:00:00Z",
        fetchedAt = 1739880000000L
    ),
    NewsArticle(
        id = "sample_2",
        sourceId = "who_sitemap",
        sourceName = "World Health Organization",
        sourceLogoUrl = "https://www.who.int/favicon.ico",
        title = "Global health update on seasonal flu and prevention strategies",
        articleUrl = "https://www.who.int/news/item/seasonal-flu-prevention",
        imageUrl = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982",
        publishedAt = "2025-02-18T10:30:00Z",
        fetchedAt = 1739874600000L
    ),
    NewsArticle(
        id = "sample_3",
        sourceId = "nih_news_custom",
        sourceName = "NIH Research Matters",
        sourceLogoUrl = "https://www.nih.gov/favicon.ico",
        title = "5 people injured in highway traffic incident; emergency teams respond",
        articleUrl = "https://www.nih.gov/news-events/highway-incident-report",
        imageUrl = null,
        publishedAt = "2025-02-18T08:15:00Z",
        fetchedAt = 1739866500000L
    )
)

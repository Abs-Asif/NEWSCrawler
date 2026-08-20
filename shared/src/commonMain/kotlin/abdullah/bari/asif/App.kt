package abdullah.bari.asif

import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.repository.NewsSourceRepository
import abdullah.bari.asif.ui.FetchInterval
import abdullah.bari.asif.ui.HomeScreen
import abdullah.bari.asif.ui.SettingsScreen
import abdullah.bari.asif.ui.StoreScreen
import abdullah.bari.asif.ui.utils.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onClearCache: (() -> Unit)? = null,
    onExitApp: (() -> Unit)? = null
) {
    var screenStack by remember { mutableStateOf(listOf(Screen.HOME)) }
    val currentScreen = screenStack.last()

    var sourceList by remember(sources) { mutableStateOf(sources) }
    var articleList by remember(articles) { mutableStateOf(articles) }

    var selectedInterval by remember { mutableStateOf(FetchInterval.HOUR_1) }
    var customMinutesValue by remember { mutableStateOf<Long?>(10L) }
    var syncWifiOnly by remember { mutableStateOf(false) }
    var syncWhenCharging by remember { mutableStateOf(false) }
    var showImages by remember { mutableStateOf(true) }

    var showExitDialog by remember { mutableStateOf(false) }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenStack = screenStack + screen
        }
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else {
            showExitDialog = true
        }
    }

    // Intercept system back gestures
    BackHandler(enabled = true) {
        navigateBack()
    }

    MaterialTheme {
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    articles = articleList,
                    showImages = showImages,
                    onStoreClick = { navigateTo(Screen.STORE) },
                    onSettingsClick = { navigateTo(Screen.SETTINGS) }
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
                    onBackClick = { navigateBack() }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    currentInterval = selectedInterval,
                    syncWifiOnly = syncWifiOnly,
                    syncWhenCharging = syncWhenCharging,
                    showImages = showImages,
                    customMinutes = customMinutesValue,
                    onIntervalChange = { selectedInterval = it },
                    onCustomMinutesChange = { customMinutesValue = it },
                    onWifiOnlyChange = { syncWifiOnly = it },
                    onSyncWhenChargingChange = { syncWhenCharging = it },
                    onShowImagesChange = { showImages = it },
                    onClearCacheClick = {
                        articleList = emptyList()
                        onClearCache?.invoke()
                    },
                    onBackClick = { navigateBack() }
                )
            }
        }

        // Exit App Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit NEWScrawler?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            onExitApp?.invoke()
                        }
                    ) {
                        Text("Exit", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private val repository = NewsSourceRepository()
private val defaultSampleSources = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON)
    .map { it.copy(isInstalled = true) }

private val defaultSampleArticles = listOf(
    NewsArticle(
        id = "sample_1",
        sourceId = "ndtv_news_rss",
        sourceName = "NDTV News",
        sourceLogoUrl = "https://www.ndtv.com/favicon.ico",
        title = "US Body Seeks Sanctions On RSS Ahead Of Mohan Bhagwat's New York Visit",
        articleUrl = "https://www.ndtv.com/india-news/us-commission-on-international-religious-freedom-uscirf-seeks-sanctions-on-rss-11934850",
        imageUrl = "https://c.ndtvimg.com/2026-08/u9v3fvbc_mohan-bhagwat_625x300_15_August_26.png",
        publishedAt = "2026-08-20T14:33:03+05:30",
        fetchedAt = 1739880000000L
    ),
    NewsArticle(
        id = "sample_2",
        sourceId = "toi_news_rss",
        sourceName = "Times of India",
        sourceLogoUrl = "https://timesofindia.indiatimes.com/favicon.ico",
        title = "I-T uncovers Rs 1.29 lakh cr overseas remittances trail; money sent to China, UAE & more",
        articleUrl = "https://timesofindia.indiatimes.com/business/india-business/overseas-remittance-in-focus-i-t-uncovers-rs-1-29-lakh-crore-trail/articleshow/133365723.cms",
        imageUrl = "https://static.toiimg.com/photo/msid-133366334,imgsize-614155.cms",
        publishedAt = "2026-08-20T11:32:12+05:30",
        fetchedAt = 1739874600000L
    ),
    NewsArticle(
        id = "sample_3",
        sourceId = "the_hindu_rss",
        sourceName = "The Hindu",
        sourceLogoUrl = "https://www.thehindu.com/favicon.ico",
        title = "UNSC membership must not be used to 'legitimise' terrorists: India",
        articleUrl = "https://www.thehindu.com/news/national/unsc-membership-must-not-be-used-to-legitimise-terrorists-india/article71367818.ece",
        imageUrl = "https://th-i.thgim.com/public/incoming/u8vs81/article71367825.ece/alternates/LANDSCAPE_1200/PTI04_29_2025_000017B.jpg",
        publishedAt = "2026-08-20T10:30:51+05:30",
        fetchedAt = 1739866500000L
    )
)

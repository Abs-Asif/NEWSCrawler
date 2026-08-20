package abdullah.bari.asif

import abdullah.bari.asif.crawler.NewsFetcher
import abdullah.bari.asif.crawler.UniversalFetcher
import abdullah.bari.asif.db.AppDatabase
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

enum class Screen { HOME, STORE, SETTINGS }

@Composable
fun App(
    database: AppDatabase? = null,
    articles: List<NewsArticle> = defaultSampleArticles,
    sources: List<NewsSource> = defaultSampleSources,
    onToggleInstallSource: ((NewsSource, Boolean) -> Unit)? = null,
    onClearCache: (() -> Unit)? = null,
    onExitApp: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { NewsSourceRepository() }
    val fetcher: NewsFetcher = remember { UniversalFetcher() }

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

    // If database is available, observe DB articles, sources, and settings
    if (database != null) {
        // 1. Observe Cached Articles
        LaunchedEffect(database) {
            database.articleDao.getAllArticles().collect { dbArticles ->
                if (dbArticles.isNotEmpty()) {
                    articleList = dbArticles
                }
            }
        }

        // 2. Load & Observe Installed Sources
        LaunchedEffect(database) {
            val enabled = repository.getEnabledSources(database)
            val allParsed = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON)
            val installedIds = enabled.map { it.id }.toSet()
            sourceList = allParsed.map { src ->
                src.copy(isInstalled = src.id in installedIds)
            }
        }

        // 3. Load Saved Settings (Persisted Settings Layer)
        LaunchedEffect(database) {
            val savedIntervalName = database.settingsDao.getSetting("fetch_interval", FetchInterval.HOUR_1.name)
            selectedInterval = try {
                FetchInterval.valueOf(savedIntervalName)
            } catch (e: Exception) {
                FetchInterval.HOUR_1
            }

            val savedCustomMins = database.settingsDao.getSetting("custom_minutes", "10")
            customMinutesValue = savedCustomMins.toLongOrNull() ?: 10L

            syncWifiOnly = database.settingsDao.getSetting("sync_wifi_only", "false").toBoolean()
            syncWhenCharging = database.settingsDao.getSetting("sync_when_charging", "false").toBoolean()
            showImages = database.settingsDao.getSetting("show_images", "true").toBoolean()
        }

        // 4. Initial Launch Fetcher Execution
        LaunchedEffect(database) {
            syncArticlesFromNetwork(database, fetcher, repository) { freshArticles ->
                if (freshArticles.isNotEmpty()) {
                    articleList = freshArticles
                }
            }
        }
    }

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
                    onRefresh = {
                        if (database != null) {
                            coroutineScope.launch {
                                syncArticlesFromNetwork(database, fetcher, repository) { freshArticles ->
                                    if (freshArticles.isNotEmpty()) {
                                        articleList = freshArticles
                                    }
                                }
                            }
                        }
                    },
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
                        if (database != null) {
                            coroutineScope.launch {
                                if (install) {
                                    database.sourceDao.setSourceInstalled(source.id, isEnabled = true)
                                } else {
                                    database.sourceDao.removeInstalledSource(source.id)
                                    database.articleDao.deleteArticlesBySource(source.id)
                                }
                                syncArticlesFromNetwork(database, fetcher, repository) { freshArticles ->
                                    articleList = freshArticles
                                }
                            }
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
                    onIntervalChange = { interval ->
                        selectedInterval = interval
                        if (database != null) {
                            coroutineScope.launch {
                                database.settingsDao.saveSetting("fetch_interval", interval.name)
                            }
                        }
                    },
                    onCustomMinutesChange = { mins ->
                        customMinutesValue = mins
                        if (database != null) {
                            coroutineScope.launch {
                                database.settingsDao.saveSetting("custom_minutes", mins.toString())
                            }
                        }
                    },
                    onWifiOnlyChange = { wifiOnly ->
                        syncWifiOnly = wifiOnly
                        if (database != null) {
                            coroutineScope.launch {
                                database.settingsDao.saveSetting("sync_wifi_only", wifiOnly.toString())
                            }
                        }
                    },
                    onSyncWhenChargingChange = { charging ->
                        syncWhenCharging = charging
                        if (database != null) {
                            coroutineScope.launch {
                                database.settingsDao.saveSetting("sync_when_charging", charging.toString())
                            }
                        }
                    },
                    onShowImagesChange = { visible ->
                        showImages = visible
                        if (database != null) {
                            coroutineScope.launch {
                                database.settingsDao.saveSetting("show_images", visible.toString())
                            }
                        }
                    },
                    onClearCacheClick = {
                        articleList = emptyList()
                        if (database != null) {
                            coroutineScope.launch {
                                database.articleDao.clearAll()
                            }
                        }
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

private suspend fun syncArticlesFromNetwork(
    database: AppDatabase,
    fetcher: NewsFetcher,
    repository: NewsSourceRepository,
    onArticlesUpdated: (List<NewsArticle>) -> Unit
) {
    withContext(Dispatchers.Default) {
        try {
            val enabledSources = repository.getEnabledSources(database)
            if (enabledSources.isEmpty()) return@withContext

            val fetched = enabledSources.map { source ->
                async {
                    try {
                        fetcher.fetchArticles(source)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()

            if (fetched.isNotEmpty()) {
                database.articleDao.insertArticles(fetched)
            }

            val now = Clock.System.now().toEpochMilliseconds()
            for (source in enabledSources) {
                database.sourceDao.updateLastSynced(source.id, now)
            }

            val allDbArticles = database.articleDao.getAllArticles().let { flow ->
                var list = emptyList<NewsArticle>()
                val job = launch {
                    flow.collect { list = it }
                }
                job.cancel()
                list
            }
            if (allDbArticles.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    onArticlesUpdated(allDbArticles)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private val repository = NewsSourceRepository()
private val defaultSampleSources = repository.parseSourcesJson(NewsSourceRepository.DEFAULT_SOURCES_JSON)
    .map { it.copy(isInstalled = true) }

private val defaultSampleArticles = listOf(
    NewsArticle(
        id = "sample_1",
        sourceId = "the_hindu_sitemap",
        sourceName = "The Hindu",
        sourceLogoUrl = "https://www.thehindu.com/favicon.ico",
        title = "Rupee settles on flat note, higher by 3 paise at 95.70 against U.S. dollar",
        articleUrl = "https://www.thehindu.com/business/markets/rupee-settles-on-flat-note-higher-by-3-paise-at-9570-against-us-dollar/article71368736.ece",
        imageUrl = "https://th-i.thgim.com/public/incoming/78omhv/article71368751.ece/alternates/FREE_1200/2026-08-18T033847Z_943858627_RC2HDJA2XNJY_RTRMADP_3_INDIA-MARKETS-RUPEE.JPG",
        publishedAt = "2026-08-20T16:24:41+05:30",
        fetchedAt = 1739880000000L
    ),
    NewsArticle(
        id = "sample_2",
        sourceId = "greater_kashmir_sitemap",
        sourceName = "Greater Kashmir",
        sourceLogoUrl = "https://www.greaterkashmir.com/favicon.ico",
        title = "UGC equity regulations 2026 under reconsideration: Centre to Supreme Court",
        articleUrl = "https://www.greaterkashmir.com/national/ugc-equity-regulations-2026",
        imageUrl = "https://th-i.thgim.com/public/incoming/ha0sex/article71368741.ece/alternates/FREE_1200/20260213488L.jpg",
        publishedAt = "2026-08-20T16:24:36+05:30",
        fetchedAt = 1739874600000L
    ),
    NewsArticle(
        id = "sample_3",
        sourceId = "siasat_daily_sitemap",
        sourceName = "The Siasat Daily",
        sourceLogoUrl = "https://www.siasat.com/favicon.ico",
        title = "Telangana floods claimed over 40 lives cabinet approves relief package",
        articleUrl = "https://www.siasat.com/telangana-floods-claimed-over-40-lives-cabinet-approves-relief-package-2656209",
        imageUrl = "https://cdn.siasat.com/wp-content/uploads/2023/07/Free-Medical-Camp-44.jpg",
        publishedAt = "2026-08-20T15:10:00+05:30",
        fetchedAt = 1739866500000L
    )
)

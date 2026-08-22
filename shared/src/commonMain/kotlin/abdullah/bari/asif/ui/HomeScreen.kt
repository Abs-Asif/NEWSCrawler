package abdullah.bari.asif.ui

import abdullah.bari.asif.filter.WordFilterEngine
import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.ui.components.NewsArticleCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class FilterItem(
    val name: String,
    val term: String
)

val NewsLogoIcon: ImageVector
    get() {
        if (_newsLogoIcon != null) return _newsLogoIcon!!
        _newsLogoIcon = ImageVector.Builder(
            name = "NewsLogoIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Unspecified)
        ) {
            moveTo(19f, 3f)
            lineTo(5f, 3f)
            lineTo(3f, 5f)
            lineTo(3f, 19f)
            lineTo(5f, 21f)
            lineTo(19f, 21f)
            lineTo(21f, 19f)
            lineTo(21f, 5f)
            close()
            moveTo(12f, 8f)
            lineTo(7f, 8f)
            lineTo(7f, 6f)
            lineTo(12f, 6f)
            close()
            moveTo(17f, 16f)
            lineTo(7f, 16f)
            lineTo(7f, 14f)
            lineTo(17f, 14f)
            close()
            moveTo(17f, 12f)
            lineTo(7f, 12f)
            lineTo(7f, 10f)
            lineTo(17f, 10f)
            close()
        }.build()
        return _newsLogoIcon!!
    }
private var _newsLogoIcon: ImageVector? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    articles: List<NewsArticle>,
    modifier: Modifier = Modifier,
    initialFilters: List<FilterItem> = emptyList(),
    showImages: Boolean = true,
    onRefresh: (() -> Unit)? = null,
    onStoreClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterList by remember { mutableStateOf(initialFilters) }
    var selectedFilter by remember { mutableStateOf<FilterItem?>(null) } // null = "All"
    var showAddFilterDialog by remember { mutableStateOf(false) }
    var filterNameInput by remember { mutableStateOf("") }
    var filterTermInput by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

    // Pull To Refresh State
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            isRefreshing = true
            onRefresh?.invoke()
            delay(600) // Brief delay for instant refresh UX
            isRefreshing = false
            pullToRefreshState.endRefresh()
        }
    }

    // Filter articles based on search query and WordFilterEngine
    val filteredArticles = remember(articles, searchQuery, selectedFilter) {
        articles.filter { article ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                article.title.contains(searchQuery.trim(), ignoreCase = true)
            }
            val matchesEngine = if (selectedFilter == null) {
                true
            } else {
                WordFilterEngine.matches(article.title, listOf(selectedFilter!!.term))
            }
            matchesSearch && matchesEngine
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = NewsLogoIcon,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        val titleText = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("NEWS")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("crawler")
                            }
                        }
                        Text(text = titleText)
                    }
                },
                actions = {
                    if (onStoreClick != null) {
                        IconButton(onClick = onStoreClick) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Store"
                            )
                        }
                    }
                    if (onSettingsClick != null) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Top Circular Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search news titles...") },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // 2. Filter Bar with "All" chip and Edge-Pinned Edit Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Default "All" chip
                        item {
                            FilterChip(
                                selected = (selectedFilter == null),
                                onClick = { selectedFilter = null },
                                label = { Text("All", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        // Filter Chips
                        items(filterList, key = { it.name + "_" + it.term }) { filter ->
                            val isSelected = (selectedFilter == filter)
                            val isNegative = WordFilterEngine.isNegativeFilter(filter.term)

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFilter = if (isSelected) null else filter
                                },
                                label = {
                                    Text(
                                        text = filter.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            filterList = filterList.filter { it != filter }
                                            if (selectedFilter == filter) {
                                                selectedFilter = null
                                            }
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove filter",
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isNegative) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    selectedLabelColor = if (isNegative) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            )
                        }
                    }

                    // Fixed Edit/Add Filter Icon pinned at the right edge
                    IconButton(
                        onClick = { showAddFilterDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Filters",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 3. Article Feed List
                if (filteredArticles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (articles.isEmpty()) {
                                "No articles loaded yet."
                            } else {
                                "No articles match your search or filter rules."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredArticles, key = { it.id }) { article ->
                            NewsArticleCard(
                                article = article,
                                showImage = showImages,
                                onClick = { selectedArticle = article }
                            )
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Add/Edit Filter Dialog with Name & Keyword Fields
    if (showAddFilterDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFilterDialog = false
                filterNameInput = ""
                filterTermInput = ""
            },
            title = { Text("Add Custom Filter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Set a friendly display name and rule. Prefix term with '!' or '-' for negative filters.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = filterNameInput,
                        onValueChange = { filterNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Display Name (e.g. Sports)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = filterTermInput,
                        onValueChange = { filterTermInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Filter Term (e.g. sports or !crime)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val term = filterTermInput.trim()
                        val name = filterNameInput.trim().ifEmpty { term }
                        if (term.isNotEmpty()) {
                            val newItem = FilterItem(name = name, term = term)
                            if (!filterList.contains(newItem)) {
                                filterList = filterList + newItem
                            }
                        }
                        showAddFilterDialog = false
                        filterNameInput = ""
                        filterTermInput = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddFilterDialog = false
                        filterNameInput = ""
                        filterTermInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Article Detail Bottom Sheet
    selectedArticle?.let { article ->
        ArticleBottomSheet(
            article = article,
            onDismissRequest = { selectedArticle = null }
        )
    }
}

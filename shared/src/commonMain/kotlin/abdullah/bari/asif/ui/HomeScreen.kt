package abdullah.bari.asif.ui

import abdullah.bari.asif.filter.WordFilterEngine
import abdullah.bari.asif.model.NewsArticle
import abdullah.bari.asif.ui.components.NewsArticleCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    articles: List<NewsArticle>,
    modifier: Modifier = Modifier,
    initialFilters: List<String> = emptyList(),
    onStoreClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilters by remember { mutableStateOf(initialFilters.toMutableList()) }
    var showAddFilterDialog by remember { mutableStateOf(false) }
    var filterInputText by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }

    // Filter articles based on search query and WordFilterEngine
    val filteredArticles = remember(articles, searchQuery, activeFilters) {
        articles.filter { article ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                article.title.contains(searchQuery.trim(), ignoreCase = true)
            }
            val matchesEngine = WordFilterEngine.matches(article.title, activeFilters)
            matchesSearch && matchesEngine
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NEWScrawler",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (onStoreClick != null) {
                        IconButton(onClick = onStoreClick) {
                            Text("Store")
                        }
                    }
                    if (onSettingsClick != null) {
                        IconButton(onClick = onSettingsClick) {
                            Text("Settings")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search news titles...") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text("✕")
                        }
                    }
                }
            )

            // 2. Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active filter chips
                items(activeFilters, key = { it }) { filter ->
                    val isNegative = WordFilterEngine.isNegativeFilter(filter)
                    val displayTerm = WordFilterEngine.cleanTerm(filter)

                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = {
                            Text(
                                text = if (isNegative) "! $displayTerm" else displayTerm,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    activeFilters = activeFilters.toMutableList().apply { remove(filter) }
                                },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Text("✕", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isNegative) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            selectedLabelColor = if (isNegative) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    )
                }

                // "Add Filter" Button
                item {
                    AssistChip(
                        onClick = { showAddFilterDialog = true },
                        label = { Text("+ Add Filter") }
                    )
                }
            }

            // 3. Article Feed
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
                            onClick = { selectedArticle = article }
                        )
                    }
                }
            }
        }
    }

    // Add Filter Dialog
    if (showAddFilterDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFilterDialog = false
                filterInputText = ""
            },
            title = { Text("Add Word Filter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a keyword. Prefix with '!' or '-' for negative filters (e.g., '!police' or '-sports').",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = filterInputText,
                        onValueChange = { filterInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. killed or !police") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = filterInputText.trim()
                        if (trimmed.isNotEmpty() && !activeFilters.contains(trimmed)) {
                            activeFilters = activeFilters.toMutableList().apply { add(trimmed) }
                        }
                        showAddFilterDialog = false
                        filterInputText = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddFilterDialog = false
                        filterInputText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Swipe-Up Article Detail Bottom Sheet
    selectedArticle?.let { article ->
        ArticleBottomSheet(
            article = article,
            onDismissRequest = { selectedArticle = null }
        )
    }
}

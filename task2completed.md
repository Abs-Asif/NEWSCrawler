# Task 2: Core Data Models, Extension Source Definitions & Local Storage

## Objective
Define the domain data models for news articles and source extensions, establish the root `/sources/sources.json` schema file, and set up a local persistence layer (SQLite/Room) to store cached articles and user-enabled source extensions.

---

## Detailed Step-by-Step Instructions

### Step 2.1: Domain Models (`abdullah.bari.asif.model`)
Create model data classes serialized via `kotlinx.serialization`:

1. `NewsSource`:
   ```kotlin
   @Serializable
   data class NewsSource(
       val id: String,
       val name: String,
       val baseUrl: String,
       val imageUrl: String,
       val updatedAt: String,
       val fetchUrl: String,
       val logic: SourceLogic,
       val isInstalled: Boolean = false
   )
   ```

2. `SourceLogic`:
   ```kotlin
   @Serializable
   data class SourceLogic(
       val type: SourceType, // RSS, SITEMAP_XML, CUSTOM_HTML
       val dateFormatPattern: String? = null,
       val selectors: ElementSelectors
   )

   enum class SourceType { RSS, SITEMAP_XML, CUSTOM_HTML }

   @Serializable
   data class ElementSelectors(
       val containerSelector: String? = null,
       val itemSelector: String? = null,
       val titleSelector: String? = null,
       val linkSelector: String? = null,
       val imageSelector: String? = null,
       val dateSelector: String? = null,
       val urlSelector: String? = null,
       val locSelector: String? = null,
       val lastmodSelector: String? = null
   )
   ```

3. `NewsArticle`:
   ```kotlin
   @Serializable
   data class NewsArticle(
       val id: String, // Hash of article link URL
       val sourceId: String,
       val sourceName: String,
       val sourceLogoUrl: String,
       val title: String,
       val articleUrl: String,
       val imageUrl: String? = null,
       val publishedAt: String,
       val fetchedAt: Long
   )
   ```

### Step 2.2: Universal Source JSON Configuration File
Create directory `/sources/` in the project root and create `/sources/sources.json`.
Populate it with validated JSON according to the schema in `project-GOAL.md`.

### Step 2.3: Local Persistence Layer
Implement local persistence using Room/SQLDelight or SQLite abstraction:
- Table `articles`: Stores cached news articles (`id` PRIMARY KEY, `source_id`, `title`, `article_url`, `image_url`, `published_at`, `fetched_at`).
- Table `installed_sources`: Stores user-installed sources (`source_id` PRIMARY KEY, `is_enabled`, `last_synced_at`).
- Provide DAO interface (`ArticleDao` and `SourceDao`) for CRUD operations and reactive flow queries (`Flow<List<NewsArticle>>`).

---

## Verification & Acceptance Criteria
1. JSON parser successfully parses `/sources/sources.json` into `List<NewsSource>`.
2. SQLite/Room database creates tables and supports saving and querying articles.

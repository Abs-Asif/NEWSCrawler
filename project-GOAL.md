# NEWScrawler - Project Goal & Architectural Specification

## 1. Executive Summary & Vision

**NEWScrawler** is a Kotlin Multiplatform (KMP) news aggregation application designed to periodically fetch news headlines, titles, and metadata from an expandable library of global news sources. Because news portals use diverse content distribution technologies—ranging from standardized RSS feeds and dynamic XML sitemaps to unformatted HTML archives—NEWScrawler provides a **Universal Source Engine** driven by a declarative JSON specification (`/sources/sources.json`).

The application features a modern UI with an in-app **Extension Store**, allowing users to enable or disable news sources like browser extensions. Advanced word-based filtering allows users to filter news feeds using positive inclusion and negative exclusion rules.

---

## 2. General Project Specifications

| Property | Value |
| :--- | :--- |
| **Application Name** | NEWScrawler |
| **Package Name** | `abdullah.bari.asif` |
| **Target Platforms** | Android (Primary target now), Windows Desktop (JVM - Future target architecture) |
| **Language & Frameworks** | Kotlin Multiplatform, Compose Multiplatform, Coroutines |
| **HTTP Engine** | Ktor Client (`ktor-client-core`, `ktor-client-okhttp`, `ktor-client-content-negotiation`) |
| **Scraping Engine** | Skrape.it (`it.skrap:skrapeit`) |
| **Local Database** | Room / SQLDelight (SQLite-backed article and source persistence) |
| **Background Scheduler** | Android WorkManager (Cron-like periodic background synchronization) |
| **Version Scheme** | Creation Timestamp (`YYYY.MM.DD.hh.mm`) generated dynamically at build time |

---

## 3. UI/UX Structure & Screen Layouts

### 3.1. App Bar & Navigation
- **Header Title**: "NEWScrawler"
- **Action Icons**:
  - **Store Button** (Shopping/Extension store icon): Navigates to the Sources Store screen.
  - **Settings Button** (Gear icon): Opens background sync and filter settings.

### 3.2. Home Feed Screen
1. **Search Bar**:
   - Real-time text search filtering article titles.
2. **Word Filter Chips (Active Filters Row)**:
   - Horizontal scrollable row of active filter chips.
   - Supports **Positive Filters** (e.g., `killed`) and **Negative Filters** (e.g., `!police` or `-police`).
   - "Add Filter" button to open a filter dialog.
3. **Article List (Feed)**:
   - Vertical list of news article cards.
   - Card contents:
     - Article Headline / Title
     - News Source Name & Favicon/Logo
     - Publication timestamp or fetched date
     - Optional thumbnail image preview
4. **Article Detail Swipe-Up Bottom Sheet**:
   - Triggered upon clicking any article item.
   - Elements:
     - Drag handle / Close button
     - Full Article Title
     - High-resolution Lead/Open Graph Image (`og:image`)
     - News Source Name & Timestamp
     - **Share Button**: Invokes platform native share sheet with article URL.
     - **Open in Browser Button**: Opens source URL in default external web browser.

### 3.3. Extension Store Screen
1. **Store Search Bar**: Search available sources by name, URL, or category.
2. **Source Tiles / Cards Grid**:
   - Source Logo/Image
   - Source Name (e.g., *Medical News Today*)
   - Base URL (e.g., `https://www.medicalnewstoday.com`)
   - Source Logic Badge (`RSS`, `SITEMAP`, `CUSTOM_HTML`)
   - **Add / Remove Action Button**: Toggle switch or button allowing users to install or uninstall sources into their personal feed.

### 3.4. Settings Screen
- **Background Sync Schedule**: Options for 15 min, 30 min, 1 hour, 6 hours, or Disabled.
- **Network Constraints**: Sync on Wi-Fi only, sync only when charging.
- **Cache Management**: Clear offline article cache, reset installed extensions.

---

## 4. App Logic & Architecture

### 4.1. Universal Source System (`/sources/sources.json`)
Sources are defined in `/sources/sources.json` within the root repository. The JSON structure defines the parsing strategy for each news site:

```json
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
```

### 4.2. Scraping & Fetching Logic Types
1. **RSS Feed (`RSS`)**:
   - Parses standard RSS/Atom XML structures using Ktor and XML parsing.
2. **XML Sitemap (`SITEMAP_XML`)**:
   - Evaluates dynamic date placeholders in `fetchUrl` (e.g., `{year}`, `{month}`, `{day}`).
   - Fetches the XML sitemap, extracts article links, and fetches Open Graph (`og:image`, `og:title`) metadata using Skrape.it.
3. **Custom HTML Webpage (`CUSTOM_HTML`)**:
   - Uses Skrape.it CSS DOM selectors (`containerSelector`, `titleSelector`, `linkSelector`, `imageSelector`) to extract article titles and links directly from HTML archives or front pages.

### 4.3. Filtering Logic Engine
Word filters evaluate article titles based on user-defined positive and negative terms:
- **Positive Term (`word`)**: Title **MUST** contain `word` (case-insensitive).
- **Negative Term (`!word` or `-word`)**: Title **MUST NOT** contain `word`.
- **Compound Filters**: Filter rules can combine terms. For example, `killed !police` matches titles containing "killed" unless "police" is also present.

### 4.4. Background Cron Job Engine
- Uses Android `WorkManager` with `PeriodicWorkRequestBuilder` (default every 1 hour).
- Reads installed/active extensions from local storage.
- Fetches articles concurrently using Ktor.
- Deduplicates articles by URL hash and stores them in local SQLite DB.
- Triggers local system notifications when new articles matching positive user filters are detected.

---

## 5. Build System & CI/CD Pipeline

- **Version System**: Dynamic version name formatted as `YYYY.MM.DD.hh.mm` derived during Gradle build configuration execution.
- **Keystore Credentials**: Prefilled release keystore (`release.keystore`) included in repository or generated via GitHub Actions secrets.
- **GitHub Actions Workflow** (`.github/workflows/release.yml`):
  - Triggers on push to `main` branch or release tag.
  - Sets up JDK 17 & Android SDK.
  - Runs `./gradlew assembleRelease`.
  - Creates a GitHub Release and attaches `app-release.apk`.

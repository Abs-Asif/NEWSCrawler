# Task 3: Content Fetching Engines & Medical News Sources

## Objective
Implement fetching engines using Ktor Client and Skrape.it to handle three distinct news source strategies (RSS Feed, XML Sitemap with dynamic dynamic date formatting, and Custom HTML Scraping). Add 3 prominent medical news portal source definitions to `/sources/sources.json`.

---

## Detailed Step-by-Step Instructions

### Step 3.1: Core Fetcher Interface
Define universal fetcher contract in `abdullah.bari.asif.crawler`:
```kotlin
interface NewsFetcher {
    suspend fun fetchArticles(source: NewsSource): List<NewsArticle>
}
```

### Step 3.2: RSS Feed Fetcher (`RssFetcher.kt`)
- Fetch XML feed using Ktor Client.
- Parse standard RSS elements (`<item>`, `<title>`, `<link>`, `<pubDate>`) or Atom elements (`<entry>`, `<title>`, `<link href="...">`).
- Extract image thumbnails from `<media:content url="...">` or `<enclosure url="...">`.

### Step 3.3: Dynamic XML Sitemap Fetcher (`SitemapFetcher.kt`)
- Dynamically resolve date patterns in `fetchUrl`:
  - Replace `{year}` with current 4-digit year (e.g. `2025`).
  - Replace `{month}` with 2-digit month (e.g. `02`).
  - Replace `{day}` with 2-digit day.
- Fetch XML sitemap index or sitemap file (`<url><loc>...</loc><lastmod>...</lastmod></url>`).
- Filter links representing news articles.
- Extract title and Open Graph image (`og:image`) using Skrape.it HTML head parser.

### Step 3.4: Custom HTML Webpage Scraper (`CustomHtmlFetcher.kt`)
- Use Skrape.it HTTP engine or HTML document parser.
- Apply CSS selectors specified in `source.logic.selectors`:
  - `containerSelector`: Targets parent article blocks.
  - `titleSelector`: Extracts text headline.
  - `linkSelector`: Extracts target URL.
  - `imageSelector`: Extracts thumbnail image `src`.
  - `dateSelector`: Extracts article publication string.

### Step 3.5: Add 3 Prominent Medical News Sources
Populate `/sources/sources.json` with 3 prominent medical sources testing all 3 strategies:

1. **Medical News Today (RSS Feed)**:
   - Base URL: `https://www.medicalnewstoday.com`
   - Fetch URL: `https://www.medicalnewstoday.com/rss/featurednews.xml`
   - Logic Type: `RSS`

2. **World Health Organization Newsroom (XML Sitemap)**:
   - Base URL: `https://www.who.int`
   - Fetch URL: `https://www.who.int/sitemap-{year}-{month}.xml`
   - Logic Type: `SITEMAP_XML`

3. **NIH Research Matters (Custom Webpage)**:
   - Base URL: `https://www.nih.gov`
   - Fetch URL: `https://www.nih.gov/news-events/news-releases`
   - Logic Type: `CUSTOM_HTML`

---

## Verification & Acceptance Criteria
1. Unit tests pass verifying article extraction for all 3 source strategies.
2. Fetchers extract headlines, URLs, and images properly without throwing network or DOM selector exceptions.

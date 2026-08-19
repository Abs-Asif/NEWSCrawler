# Task 4: Background Cron Job Engine & Notification System

## Objective
Implement a background cron/periodic worker using Android WorkManager to fetch news articles periodically from enabled user extensions, update the local database, and emit system notifications when new matching articles arrive.

---

## Detailed Step-by-Step Instructions

### Step 4.1: Background Sync Worker (`NewsCrawlerWorker.kt`)
Create `NewsCrawlerWorker` extending Android's `CoroutineWorker`:
- Retrieve all installed/enabled `NewsSource` entries from local database.
- Execute article fetch requests across enabled sources in parallel using `coroutineScope` and Ktor.
- Filter out duplicate articles already present in the database (matching URL hash).
- Save newly discovered articles to SQLite/Room database.

### Step 4.2: WorkManager Scheduler Configuration
Create `CrawlerScheduler.kt`:
- Configure `PeriodicWorkRequestBuilder<NewsCrawlerWorker>` with default interval (e.g. 1 hour).
- Set network constraints: `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()`.
- Schedule background execution on application initialization in `AndroidApplication.kt`.

### Step 4.3: Local System Notifications
Implement notification dispatcher:
- Check matching user filter keywords against newly scraped article titles.
- If a new article matches active user filters, post a system notification with title, source name, and pending intent targeting article detail view.

---

## Verification & Acceptance Criteria
1. WorkManager triggers background sync task without memory leaks or crashes.
2. New articles are added to local storage seamlessly upon worker completion.
3. System notification appears when a new matching headline is crawled.

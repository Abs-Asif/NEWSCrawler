# Task 6: Extension Store UI & App Settings

## Objective
Implement the Extension Store interface allowing users to search available news sources from `/sources/sources.json` and toggle them on/off like browser extensions. Build the app settings screen to configure background synchronization preferences.

---

## Detailed Step-by-Step Instructions

### Step 6.1: Extension Store Screen (`StoreScreen.kt`)
1. **Search Bar**: Search input field to filter available extensions by source name or URL.
2. **Source Tiles / Extension Grid**:
   - Grid or vertical list displaying `SourceTile`:
     - Source Logo (`AsyncImage`)
     - Source Title (e.g. *Medical News Today*)
     - Base URL (e.g. `https://www.medicalnewstoday.com`)
     - Strategy Badge (`RSS`, `SITEMAP`, `CUSTOM`)
     - **Add / Remove Button**: Toggles installation state (`isInstalled`).
       - If not installed: "Add Extension" (Button filled).
       - If installed: "Remove Extension" (Outlined / Destructive button style).

### Step 6.2: App Settings Screen (`SettingsScreen.kt`)
- Background fetch interval selector (15 minutes, 30 minutes, 1 hour, 6 hours, Manual).
- Network constraint toggles ("Sync on Wi-Fi only", "Sync when charging").
- Cache management button: "Clear cached articles".

---

## Verification & Acceptance Criteria
1. Toggling "Add Extension" installs the source and includes its fetched articles in the main feed.
2. Store search bar properly filters sources in real time.

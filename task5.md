# Task 5: Home Screen UI, Advanced Word Filter Engine & Swipe-Up Sheet

## Objective
Build the Compose Multiplatform Home Feed screen featuring title search, dynamic positive/negative word filter chips, article card feed layout, and a swipe-up modal bottom sheet for article detail viewing and sharing.

---

## Detailed Step-by-Step Instructions

### Step 5.1: Word Filter Logic Engine (`WordFilterEngine.kt`)
Implement filtering logic for compound terms:
- Input: List of filter strings (e.g., `["killed", "!police"]` or `["killed", "-police"]`).
- Rule evaluation logic:
  - Positive rules (e.g., `killed`): Article title **MUST** contain "killed" (case-insensitive).
  - Negative rules (e.g., `!police` or `-police`): Article title **MUST NOT** contain "police" (case-insensitive).
- Test class `WordFilterEngineTest.kt` verifying positive and negative filtering behavior.

### Step 5.2: Filter Chips & Search Bar UI
In `HomeScreen.kt`:
1. Top Search Bar: `OutlinedTextField` filtering news title string.
2. Filter Chips Row:
   - Scrollable `LazyRow` displaying active filter chips.
   - Distinct visual representation for positive filters (e.g., Primary container color) vs negative filters (e.g., Error/Secondary container color).
   - "Add Filter" button displaying a dialog to enter word filters (supports `!` or `-` prefix for negative filter terms).

### Step 5.3: Article Feed & Card Layout
- `LazyColumn` rendering `NewsArticleCard`:
  - News source icon logo (`AsyncImage`) and source title.
  - News article title text (styled header typography).
  - Relative publication time string.
  - Optional article thumbnail image preview.

### Step 5.4: Article Detail Swipe-Up Bottom Sheet (`ArticleBottomSheet.kt`)
- `ModalBottomSheet` displaying:
  - High-res lead image / `og:image`.
  - Headline title.
  - Source logo, name, and publication date.
  - **Share Action Button**: Invokes Android Share Intent with article link.
  - **Browser Action Button**: Launches system web browser targeting article URL.

---

## Verification & Acceptance Criteria
1. Unit tests pass for word filtering logic (positive inclusion and negative exclusion).
2. Tapping an article opens the swipe-up bottom sheet with title and image preview.
3. Filter chips correctly refine displayed articles in real time.

# Task 1: Project Initialization & KMP Architecture Setup

## Objective
Initialize the Kotlin Multiplatform (KMP) project structure configured for Android (primary target) while keeping the target architecture modular for future Windows Desktop (JVM) support. Configure Gradle dependencies, package naming, time-based versioning, and minimal launcher assets.

---

## Detailed Step-by-Step Instructions

### Step 1.1: Multiplatform Repository Structure Setup
Create the standard KMP project directory hierarchy:
- `androidApp/`: Android specific entry point, Application class, and AndroidManifest.
- `shared/`: Common multiplatform module containing logic and Compose UI.
  - `shared/src/commonMain/kotlin/abdullah/bari/asif/`: Shared domain, UI, and data code.
  - `shared/src/androidMain/kotlin/abdullah/bari/asif/`: Android specific platform implementations.
  - `shared/src/desktopMain/kotlin/abdullah/bari/asif/`: Future Windows Desktop JVM stubs.

### Step 1.2: Root & Shared `build.gradle.kts` Configuration
1. Set package namespace: `abdullah.bari.asif`.
2. Configure **Dynamic Versioning**:
   - Implement a Kotlin function in `build.gradle.kts` to set `versionName` dynamically based on creation timestamp format `YYYY.MM.DD.hh.mm` (e.g., using `java.time.LocalDateTime.now().format(...)`).
3. Add Required KMP Dependencies:
   - **Compose Multiplatform UI**: `org.jetbrains.compose`
   - **Ktor Client Core & Engine**:
     - `io.ktor:ktor-client-core:2.3.8`
     - `io.ktor:ktor-client-okhttp:2.3.8` (Android)
     - `io.ktor:ktor-client-content-negotiation:2.3.8`
     - `io.ktor:ktor-serialization-kotlinx-json:2.3.8`
   - **Skrape.it Scraping Library**:
     - `it.skrap:skrapeit:1.2.2`
   - **KotlinX Serialization & Coroutines**:
     - `org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2`
     - `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0`
     - `org.jetbrains.kotlinx:kotlinx-datetime:0.5.0`
   - **AndroidX & WorkManager (Android Source Set)**:
     - `androidx.work:work-runtime-ktx:2.9.0`
     - `androidx.activity:activity-compose:1.8.2`
     - `io.coil-kt:coil-compose:2.5.0` (Image loading with fallback og:image support)

### Step 1.3: Minimal Newspaper Launcher Icon
Create a minimal vector drawable (`ic_newspaper.xml`) in `androidApp/src/main/res/drawable/`:
- Vector paths depicting a classic folded newspaper with simple headline bars.
- Configure `mipmap/ic_launcher` to reference `ic_newspaper`.

### Step 1.4: Android Manifest & Permissions Configuration
In `androidApp/src/main/AndroidManifest.xml`:
- Package: `abdullah.bari.asif`
- Declare permissions:
  - `<uses-permission android:name="android.permission.INTERNET" />`
  - `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`
  - `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`

---

## Verification & Acceptance Criteria
1. Project builds cleanly using `./gradlew assembleDebug`.
2. `versionName` generated matches `YYYY.MM.DD.hh.mm` pattern.
3. Ktor, Skrape.it, and Compose Multiplatform dependencies resolve without version conflicts.

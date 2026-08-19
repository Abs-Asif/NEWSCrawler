# Task 7: Keystore Credentials, Release Build Configuration & GitHub Actions CI/CD Workflow

## Objective
Create prefilled keystore credentials to sign release Android builds and set up a GitHub Actions workflow that builds `release.apk` automatically and publishes it to GitHub Releases.

---

## Detailed Step-by-Step Instructions

### Step 7.1: Prefilled Release Keystore Setup
1. Generate prefilled demo release keystore using Keytool:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias newcastle_key -keyalg RSA -keysize 2048 -validity 10000 -storepass newscrawler123 -keypass newscrawler123 -dname "CN=NEWScrawler, OU=Dev, O=NEWScrawler, L=City, S=State, C=US"
   ```
2. Place `release.keystore` in the root or `androidApp/` folder.
3. Store keystore parameters in `keystore.properties` or environment variables:
   - `KEYSTORE_PASSWORD=newscrawler123`
   - `KEY_ALIAS=newcastle_key`
   - `KEY_PASSWORD=newscrawler123`

### Step 7.2: Gradle Release Signing Configuration
In `androidApp/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "newscrawler123"
            keyAlias = System.getenv("KEY_ALIAS") ?: "newcastle_key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "newscrawler123"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### Step 7.3: GitHub Actions Release Workflow (`.github/workflows/release.yml`)
Create `.github/workflows/release.yml`:
```yaml
name: Build & Publish Release APK

on:
  push:
    branches:
      - main
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'
          cache: gradle

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Make Gradle Executable
        run: chmod +x gradlew

      - name: Build Release APK
        run: ./gradlew assembleRelease

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        if: startsWith(github.ref, 'refs/tags/')
        with:
          files: androidApp/build/outputs/apk/release/androidApp-release.apk
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## Verification & Acceptance Criteria
1. Command `./gradlew assembleRelease` builds a signed release APK successfully locally.
2. GitHub Action workflow runs cleanly on pushing to `main` or release tags, creating a release containing `androidApp-release.apk`.

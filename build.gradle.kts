plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.compose) apply false
}

fun generateDynamicVersionName(): String {
    return java.time.LocalDateTime.now().format(
        java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm")
    )
}

extra["dynamicVersionName"] = generateDynamicVersionName()

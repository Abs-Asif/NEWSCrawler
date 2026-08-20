# Keep attributes for reflection and serialization
-keepattributes *Annotation*,ElementValuePairs,Signature,InnerClasses,EnclosingMethod

# Keep all App Packages
-keep class abdullah.bari.asif.** { *; }
-keepclassmembers class abdullah.bari.asif.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** $serializer;
}
-keep class * implements kotlinx.serialization.KSerializer
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** INSTANCE;
}
-keepclassmembers enum * {
    *** valueOf(java.lang.String);
    ***[] values();
}

# Keep WorkManager Workers & Initializers
-keep class androidx.work.** { *; }
-keepclassmembers class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Jetpack Compose & Compose Multiplatform
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Ktor & OkHttp
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-keep class io.ktor.client.engine.** { *; }
-keepclassmembers class io.ktor.client.engine.** { *; }
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }

# Keep SQLite & DB drivers
-keep class android.database.sqlite.** { *; }
-keep class androidx.sqlite.** { *; }
-keep class org.sqlite.** { *; }

# Keep Skrapeit, HtmlUnit, Jsoup, Jackson
-keep class it.skrape.** { *; }
-keepclassmembers class it.skrape.** { *; }
-keep class org.htmlunit.** { *; }
-keepclassmembers class org.htmlunit.** { *; }
-keep class com.gargoylesoftware.** { *; }
-keepclassmembers class com.gargoylesoftware.** { *; }
-keep class net.sourceforge.htmlunit.** { *; }
-keepclassmembers class net.sourceforge.htmlunit.** { *; }
-keep class org.jsoup.** { *; }
-keepclassmembers class org.jsoup.** { *; }

# Don't warn on missing optional/runtime references from third-party libraries (Skrapeit, HtmlUnit, Logback, OkHttp, etc.)
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn groovy.lang.**
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn java.lang.management.**
-dontwarn java.sql.**
-dontwarn javax.imageio.**
-dontwarn javax.lang.model.**
-dontwarn javax.management.**
-dontwarn javax.naming.**
-dontwarn javax.servlet.**
-dontwarn javax.swing.**
-dontwarn net.sourceforge.htmlunit.**
-dontwarn org.apache.bsf.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.html.**
-dontwarn org.apache.xalan.**
-dontwarn org.apache.xerces.**
-dontwarn org.apache.xml.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.codehaus.janino.**
-dontwarn org.ietf.jgss.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
-dontwarn sun.reflect.**
-dontwarn com.gargoylesoftware.**
-dontwarn org.htmlunit.**
-dontwarn it.skrape.**
-dontwarn okhttp3.**
-dontwarn okio.**

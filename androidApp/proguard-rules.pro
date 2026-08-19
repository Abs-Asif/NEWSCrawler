# Keep attributes for reflection and serialization
-keepattributes *Annotation*,ElementValuePairs,Signature,InnerClasses,EnclosingMethod

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
-keep class * implements kotlinx.serialization.KSerializer {
    *;
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *;
}

# Keep Ktor
-keep class io.ktor.** { *; }

# Keep SQLite & DB drivers
-keep class androidx.sqlite.** { *; }
-keep class org.sqlite.** { *; }

# Keep all App Packages
-keep class abdullah.bari.asif.model.** { *; }
-keepclassmembers class abdullah.bari.asif.model.** { *; }

-keep class abdullah.bari.asif.db.** { *; }
-keepclassmembers class abdullah.bari.asif.db.** { *; }

-keep class abdullah.bari.asif.crawler.** { *; }
-keepclassmembers class abdullah.bari.asif.crawler.** { *; }

-keep class abdullah.bari.asif.repository.** { *; }
-keepclassmembers class abdullah.bari.asif.repository.** { *; }

-keep class abdullah.bari.asif.worker.** { *; }
-keepclassmembers class abdullah.bari.asif.worker.** { *; }

-keep class abdullah.bari.asif.notification.** { *; }
-keepclassmembers class abdullah.bari.asif.notification.** { *; }

-keep class abdullah.bari.asif.ui.** { *; }
-keepclassmembers class abdullah.bari.asif.ui.** { *; }

-keep class abdullah.bari.asif.filter.** { *; }
-keepclassmembers class abdullah.bari.asif.filter.** { *; }

# Don't warn on missing optional/runtime references from third-party libraries (Skrapeit, HtmlUnit, Logback, etc.)
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
-dontwarn org.apache.bsf.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.codehaus.janino.**
-dontwarn org.ietf.jgss.**
-dontwarn sun.reflect.**

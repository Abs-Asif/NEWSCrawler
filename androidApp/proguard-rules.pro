# Keep Ktor classes and serialization
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep SQLite & SQLDelight / DB drivers
-keep class androidx.sqlite.** { *; }
-keep class org.sqlite.** { *; }

# Keep App Data Models
-keep class abdullah.bari.asif.db.** { *; }
-keep class abdullah.bari.asif.crawler.** { *; }

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

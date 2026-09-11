# ProGuard & R8 Configuration for High Obfuscation & Optimization

# Optimize and obfuscate aggressively
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# Preserve line numbers for stack traces while hiding source file paths
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Moshi & Retrofit
-keepclassmembers class com.example.data.model.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-dontwarn kotlinx.coroutines.**

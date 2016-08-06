# Atarashii
-keepclassmembers class * implements java.io.Serializable { *; }
-keepclassmembers class net.somethingdreadful.MAL.forum.ForumInterface { *; }

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Butterknife
-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Picasso
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

# MobiHelp
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
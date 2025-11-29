# === CRITICAL: Keep Everything for Networking ===
-keeppackagenames io.ktor.**
-keeppackagenames okhttp3.**
-keeppackagenames okio.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keepclassmembers class kotlin.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# === Ktor - Keep EVERYTHING ===
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-keepnames class io.ktor.** { *; }
-dontwarn io.ktor.**

# Ktor Debug
-dontwarn java.lang.management.**

# === OkHttp - Keep EVERYTHING ===
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-keepnames class okhttp3.** { *; }
-dontwarn okhttp3.**

# === Okio - Keep EVERYTHING ===
-keep class okio.** { *; }
-keepclassmembers class okio.** { *; }
-keepnames class okio.** { *; }
-dontwarn okio.**

# === Network Stack - Keep EVERYTHING ===
-keep class java.net.** { *; }
-keep class javax.net.** { *; }
-keep class sun.security.ssl.** { *; }
-keepclassmembers class java.net.** { *; }
-keepclassmembers class javax.net.** { *; }

# DNS Resolution
-keep class java.net.InetAddress { *; }
-keep class java.net.Inet4Address { *; }
-keep class java.net.Inet6Address { *; }
-keep class java.net.InetSocketAddress { *; }

# SSL/TLS
-keep class javax.net.ssl.** { *; }
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# === Kotlinx Serialization ===
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class zed.rainxch.githubstore.**$$serializer { *; }
-keep @kotlinx.serialization.Serializable class zed.rainxch.githubstore.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class zed.rainxch.githubstore.** {
    *** Companion;
}

# Keep your models
-keep class zed.rainxch.githubstore.core.domain.model.** { *; }

# === AndroidX Security ===
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**

# BuildConfig
-keep class zed.rainxch.githubstore.BuildConfig { *; }

# General
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# === START: Auth Fix ===
-dontoptimize
-keepattributes *Annotation*,Signature,Exception,InnerClasses,EnclosingMethod

# Keep serialization infrastructure
-keep class kotlinx.serialization.** { *; }
-keep class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor plugins
-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.serialization.** { *; }

# Keep your entire core package (narrow this down later)
-keep class zed.rainxch.githubstore.core.** { *; }
-keepclassmembers class zed.rainxch.githubstore.core.** { *; }
# === END: Auth Fix ===

-keep class zed.rainxch.githubstore.core.data.remote.dto.** { *; }
-keep class zed.rainxch.githubstore.core.domain.model.auth.** { *; }

# If your models are in different packages, list them:
-keep class zed.rainxch.githubstore.**.*DeviceStart* { *; }
-keep class zed.rainxch.githubstore.**.*DeviceToken* { *; }
-keep class zed.rainxch.githubstore.**.*AuthConfig* { *; }

# Keep the companion objects explicitly
-keepclassmembers class zed.rainxch.githubstore.**.DeviceStart {
    public static ** Companion;
}
-keepclassmembers class zed.rainxch.githubstore.**.DeviceTokenSuccess {
    public static ** Companion;
}
-keepclassmembers class zed.rainxch.githubstore.**.DeviceTokenError {
    public static ** Companion;
}
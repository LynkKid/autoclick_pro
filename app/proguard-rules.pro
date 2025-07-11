# Add project specific ProGuard rules here.

#----------------------------------------
# Preserve useful debugging info (optional)
# Uncomment below if bạn muốn debug crash chính xác hơn:
#-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

#----------------------------------------
# Keep billing classes
-keep class com.android.billingclient.** { *; }
-keep class com.anjlab.android.iab.v3.** { *; }

#----------------------------------------
# Keep accessibility service
-keep class com.auto.click.AutoClickService { *; }

#----------------------------------------
# Keep model classes (AppInfo, OptionConfig,...)
-keep class com.auto.click.model.** { *; }

#----------------------------------------
# Keep InAppMNG
-keep class com.auto.click.InAppMNG { *; }

#----------------------------------------
# Keep WebView JS interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#----------------------------------------
# Keep XPopup library
-keep class com.lxj.xpopup.** { *; }

#----------------------------------------
# Keep utility classes
-keep class com.auto.click.appcomponents.utility.** { *; }

#----------------------------------------
# Keep Gson usage (critical for fixing Missing type parameter)
-keepattributes Signature
-keepattributes *Annotation*

# Keep TypeToken (this is KEY for generic deserialization)
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep all Gson-related classes
-keep class com.google.gson.** { *; }

# Keep any custom adapters
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

#----------------------------------------
# Optional: keep classes annotated with @Keep (if used)
-keep class * {
    @androidx.annotation.Keep *;
}

#----------------------------------------
# Keep Retrofit (if used)
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

#----------------------------------------
# Optional: suppress warnings from internal sun.misc APIs
-dontwarn sun.misc.**

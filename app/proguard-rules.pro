# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep billing classes
-keep class com.android.billingclient.** { *; }
-keep class com.anjlab.android.iab.v3.** { *; }

# Keep accessibility service
-keep class com.auto.click.AutoClickService { *; }

# Keep model classes
-keep class com.auto.click.model.** { *; }

# Keep InAppMNG
-keep class com.auto.click.InAppMNG { *; }

# Keep WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
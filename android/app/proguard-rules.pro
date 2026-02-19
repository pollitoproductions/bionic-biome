# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Keep WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, ...);
}

-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, ...);
}

# Keep JavaScript interfaces exposed to WebView
-keepclassmembers class com.pollitoproductions.bionicbiome.MainActivity$VibrationInterface {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.pollitoproductions.bionicbiome.MainActivity$AndroidInterface {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep AdMob — public API
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep AdMob internal classes (rendering, touch handling, click tracking)
# R8 can strip these since they're loaded via reflection / dynamic dispatch
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.internal.ads.**

# Keep Google Ads base classes
-keep class com.google.ads.** { *; }
-dontwarn com.google.ads.**

# Keep IMA / mediation adapters that may be bundled
-keep class com.google.android.gms.ads.mediation.** { *; }
-keep class com.google.android.gms.ads.rewarded.** { *; }
-keep class com.google.android.gms.ads.interstitial.** { *; }

# Keep classes used by the ad SDK for view inflation and click handling
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class * extends android.app.Activity
-keepclassmembers class * extends android.view.View {
    public boolean onTouchEvent(android.view.MotionEvent);
    public boolean dispatchTouchEvent(android.view.MotionEvent);
}

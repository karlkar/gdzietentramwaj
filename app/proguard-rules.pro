# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }

-keepnames @com.google.android.gms.common.annotation.KeepName class *

# Called by introspection
-keep class com.google.android.gms.maps.**

-keep class * extends java.util.ListResourceBundle {
    protected java.lang.Object[][] getContents();
}

# This keeps the class name as well as the creator field, because the
# "safe parcelable" can require them during unmarshalling.
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepnames class okhttp3.Request** { *; }
-keepnames class okhttp3.Response** { *; }
-keepnames interface okhttp3.** { *; }
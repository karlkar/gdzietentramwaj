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

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn okio.**
-dontwarn java.lang.invoke.*
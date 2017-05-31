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
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn okio.**
-dontwarn java.lang.invoke.*

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*

-dontwarn sun.misc.Unsafe

-keepnames class com.google.ads.** # Don't proguard AdMob classes
-keepnames class com.google.android.gms.ads.** # Don't proguard AdMob classes

-keep public class com.google.android.gms.ads.** {
public *;
}

-keep public class com.google.ads.** {
public *;
}

-keepnames class com.google.maps.android.ui.**

-keep public class com.google.maps.android.ui.** {
public *;
}

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

#-keep class com.kksionek.gdzietentramwaj.**
#
#-keep public class com.kksionek.gdzietentramwaj.** {
#public *;
#}

-keep class com.kksionek.gdzietentramwaj.ViewModel.**
-keep public class com.kksionek.gdzietentramwaj.ViewModel.** {
public *;
}
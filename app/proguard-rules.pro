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
-dontwarn javax.annotation.**

-dontwarn java.lang.invoke.*

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*

-dontwarn sun.misc.Unsafe

## Android architecture components: Lifecycle
# LifecycleObserver's empty constructor is considered to be unused by proguard
-keep class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
    <init>(...);
}

-keep class android.arch.lifecycle.GenericLifecycleObserver {*;}
-keep class android.arch.lifecycle.ProcessLifecycleOwner* {*;}
-keep class android.arch.lifecycle.Lifecycle* {*;}
-keep class android.arch.lifecycle.Lifecycling {*;}
-keep class android.arch.lifecycle.LifecycleDispatcher* { *; }
-keep class android.arch.lifecycle.LifecycleOwner {*;}
-keep class android.arch.lifecycle.LifecycleRegistry {*;}
-keep class android.arch.lifecycle.ReportFragment* {*;}
-keep class android.arch.lifecycle.ViewModel* {*;}
-keep class android.arch.lifecycle.HolderFragment* {*;}
-keep class android.arch.lifecycle.Observer {*;}
-keep class android.arch.lifecycle.EmptyActivityLifecycleCallbacks { *; }
-keep class android.arch.lifecycle.state.SavedStateProvider {*;}
-keep class android.arch.lifecycle.state.StateMap {*;}
-keep class android.arch.lifecycle.AndroidViewModel {*;}
-keep class android.arch.lifecycle.MutableLiveData {*;}
-keep class android.arch.lifecycle.LiveData* {*;}
-keep class android.arch.lifecycle.OnLifecycleEvent {*;}
-keep class android.arch.lifecycle.ReflectiveGenericLifecycleObserver* {*;}
-keep class android.arch.lifecycle.ComputableLiveData* {*;}
-keep class android.arch.core.executor.AppToolkitTaskExecutor {*;}
-keep class android.arch.core.executor.TaskExecutor {*;}
-keep class android.arch.core.executor.DefaultTaskExecutor {*;}
-keep class android.arch.core.internal.** {*;}
-keep class android.support.v4.app.Fragment {*;}

-keep class android.arch.persistence.room.DatabaseConfiguration {*;}
-keep class android.arch.persistence.room.EntityInsertionAdapter {*;}
-keep class android.arch.persistence.room.InvalidationTracker* {*;}
-keep class android.arch.persistence.room.Room {*;}
-keep class android.arch.persistence.room.RoomDatabase* {*;}
-keep class android.arch.persistence.room.RoomOpenHelper* {*;}
-keep class android.arch.persistence.room.RoomSQLiteQuery {*;}
-keep class android.arch.persistence.room.SharedSQLiteStatement {*;}
-keep class android.arch.persistence.db.framework.FrameworkSQLiteOpenHelper* {*;}
-keep class android.arch.persistence.db.framework.FrameworkSQLiteDatabase* {*;}
-keep class android.arch.persistence.db.framework.FrameworkSQLiteProgram {*;}
-keep class android.arch.persistence.db.framework.FrameworkSQLiteStatement {*;}
-keep class android.arch.persistence.db.SupportSQLiteOpenHelper* {*;}
-keep class android.arch.persistence.db.SupportSQLiteQuery {*;}
-keep class android.arch.persistence.db.SupportSQLiteProgram {*;}
-keep class android.arch.persistence.db.SupportSQLiteDatabase {*;}
-keep class android.arch.persistence.db.SupportSQLiteStatement {*;}
-keep class android.arch.persistence.db.SimpleSQLiteQuery {*;}
-keep class android.arch.persistence.room.RoomMasterTable {*;}
-keep class android.arch.util.paging.CountedDataSource {*;}
-dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource
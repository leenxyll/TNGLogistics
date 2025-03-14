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
#-renamesourcefileattribute SourceFile

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# รักษาข้อมูล line number สำหรับการดีบัก stack trace
#-keepattributes SourceFile,LineNumberTable
#
## ซ่อนชื่อไฟล์ต้นฉบับ
#-renamesourcefileattribute SourceFile
#
## กฎสำหรับ Android components
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.view.View
#-keep public class * extends androidx.fragment.app.Fragment
#
## กฎสำหรับ View binding
#-keep class * implements androidx.viewbinding.ViewBinding {
#    public static * inflate(android.view.LayoutInflater);
#    public static * bind(android.view.View);
#}
#
## กฎสำหรับ model classes (ปรับแต่งตามแพ็คเกจของคุณ)
#-keep class com.leenxyll.tnglogistics.models.** { *; }
#
## กฎสำหรับ Retrofit และ OkHttp (ถ้าคุณใช้)
#-keepattributes Signature
#-keepattributes *Annotation*
#-keep class retrofit2.** { *; }
#-keep class okhttp3.** { *; }
#-keep class okio.** { *; }
#-keepclasseswithmembers class * {
#    @retrofit2.http.* <methods>;
#}
#
## กฎสำหรับ Gson (ถ้าคุณใช้)
#-keepattributes Signature
#-keepattributes *Annotation*
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.** { *; }
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer
#
## กฎสำหรับ WebView + JS Interface (ถ้าคุณใช้)
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
#
## กฎสำหรับ enum (ป้องกันปัญหาที่อาจเกิดกับ enum)
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
## กฎสำหรับ Parcelable (ถ้าคุณใช้)
#-keepclassmembers class * implements android.os.Parcelable {
#    public static final android.os.Parcelable$Creator CREATOR;
#}
#
## กฎสำหรับ Serializable (ถ้าคุณใช้)
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    !static !transient <fields>;
#    !private <fields>;
#    !private <methods>;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}

# ปรับแต่งเพิ่มเติมตามไลบรารีที่คุณใช้
# สำหรับไลบรารีอื่นๆ ตรวจสอบเอกสารของไลบรารีนั้นๆ
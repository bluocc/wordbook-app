# Add project specific ProGuard rules here.

-keepattributes *Annotation*
-keep class com.wordbook.app.data.entity.** { *; }

# Gson
-keepattributes Signature
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }

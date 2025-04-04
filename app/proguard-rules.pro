# Mantener todas las clases de Firebase necesarias
-keep class com.google.firebase.** { *; }

# Evitar que Firebase Database sea ofuscado o eliminado
-keep class com.google.firebase.database.** { *; }

# Mantener las clases relacionadas con Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Mantener las clases relacionadas con Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }

# Mantener las clases de Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# Mantener las clases de Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }

# Reglas adicionales para Firebase App Check si estás usando esta característica
-keep class com.google.firebase.appcheck.** { *; }

# Reglas de configuración para Firebase Dynamic Links (si los estás usando)
-keep class com.google.firebase.dynamiclinks.** { *; }

# Evitar la ofuscación de las clases de modelo de Firebase Database (si las estás usando)
-keepclassmembers class * {
    @com.google.firebase.database.IgnoreExtraProperties <fields>;
}

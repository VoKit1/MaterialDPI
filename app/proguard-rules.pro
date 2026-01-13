# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class io.github.dovecoteescapee.byedpi.core.ByeDpiProxy { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.core.TProxyService { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.activities.** { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.services.** { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.receiver.** { *; }

-keep class io.github.dovecoteescapee.byedpi.data.Command { *; }
-keep class io.github.dovecoteescapee.byedpi.data.AppSettings { *; }

-keepattributes Signature
-keepattributes *Annotation*

-repackageclasses 'ru.gdlbo'
-renamesourcefileattribute ''
-keepattributes SourceFile,InnerClasses,EnclosingMethod,Signature,RuntimeVisibleAnnotations,*Annotation*,*Parcelable*
-allowaccessmodification
-overloadaggressively
-optimizationpasses 5
-verbose
-dontusemixedcaseclassnames
-adaptclassstrings
-adaptresourcefilecontents **.xml,**.json
-adaptresourcefilenames **.xml,**.json
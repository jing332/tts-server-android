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


#-------------- 去掉所有打印 -------------

-assumenosideeffects class android.util.Log {
public static *** d(...);

# public static *** e(...);

public static *** i(...);

public static *** v(...);

public static *** println(...);

public static *** w(...);

public static *** wtf(...);

}

-assumenosideeffects class android.util.Log {
public static *** d(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
# public static *** e(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
public static *** i(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
public static *** w(...);

public static *** v(...);

}

-assumenosideeffects class java.io.PrintStream {
public *** println(...);

public *** print(...);

}

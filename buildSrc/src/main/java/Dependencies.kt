/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

class Dependency(val prefix: String, val artifact: String, val version: String) {
    operator fun invoke(): String = "$prefix:$artifact:$version"

    fun variant(art: String, vers: String = version) = Dependency(prefix, art, vers)

}


val PlayPlugin = Dependency("com.github.triplet.play", "", "3.7.0")
val Applibs = Dependency("com.control-j.applibs", "common", "2.2.12-SNAPSHOT")
val ApplibsAndroid = Applibs.variant("android")
val ApplibsIos = Applibs.variant("ios")

val ThreetenAbp = Dependency("com.jakewharton.threetenabp", "threetenabp", "1.2.4")
val ThreetenBp = Dependency("org.threeten", "threetenbp", "1.4.4")

val AndroidSupport = Dependency("com.android.support", "support-v4", "28.0.0")

val TbruyelleRxpermissions2 = Dependency("com.github.tbruyelle", "rxpermissions", "0.12")

val Rxjava3 = Dependency("io.reactivex.rxjava3", "rxjava", "3.0.4")
val Rxjava3Android = Rxjava3.variant("rxandroid", "3.0.0")
val Rxjava3Kotlin = Rxjava3.variant("rxkotlin", "3.0.0")

val Gson = Dependency("com.google.code.gson", "gson", "2.8.5")
val Material = Dependency("com.google.android.material", "material", "1.1.0")

val AndroidMapboxSdk = Dependency("com.mapbox.mapboxsdk", "mapbox-android-sdk", "9.7.2")
val AndroidMapboxBuildings =
    Dependency("com.mapbox.mapboxsdk", "mapbox-android-plugin-building-v9", "0.7.0")
val AndroidMapboxAnnotations =
    Dependency("com.mapbox.mapboxsdk", "mapbox-android-plugin-annotation-v9", "0.8.0")

val SqlDroid = Dependency("org.sqldroid", "sqldroid", "1.0.3")

val Kotlin =
    Dependency("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", System.getProperty("kotlinVersion"))
val KotlinReflect = Kotlin.variant("kotlin-reflect")
val KotlinPlugin = Dependency("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.8.20")
val CJLog = Dependency("com.control-j.cjlog", "core", "2.3-SNAPSHOT")

object AndroidApp {
    const val minSdkVersion = 21
    const val targetSdkVersion = 33
}

val MaterialDrawer = Dependency("com.mikepenz", "materialdrawer", "8.1.3")

val AndroidBuildTools = Dependency("com.android.tools.build", "gradle", "7.3.1")
val GoogleServices = Dependency("com.google.gms", "google-services", "4.3.2")

val AndroidXLegacy = Dependency("androidx.legacy", "legacy-support-v4", "1.0.0")
val AndroidXAnnotation = Dependency("androidx.annotation", "annotation", "1.1.0")
val AndroidXMultidex = Dependency("androidx.multidex", "multidex", "2.0.1")
val AndroidXAppcompat = Dependency("androidx.appcompat", "appcompat", "1.4.0")
val AndroidSplashScreen = Dependency("androidx.core", "core-splashscreen", "1.0.0")
val AndroidXWork = Dependency("androidx.work", "work-runtime", "2.3.4")
val RecyclerView = Dependency("androidx.recyclerview", "recyclerview", "1.1.0")
val ConstraintLayout = Dependency("androidx.constraintlayout", "constraintlayout", "2.1.2")

val Ktorm = Dependency("org.ktorm", "ktorm-core", "3.5.0")
val KtormSqlite = Ktorm.variant("ktorm-support-sqlite")
val Retrofit = Dependency("com.squareup.retrofit2", "retrofit", "2.10.0-SNAPSHOT")
val RetrofitRxJava3 = Retrofit.variant("adapter-rxjava3")
val RetrofitGson = Retrofit.variant("converter-gson")
val RobopodsMapbox = Dependency("com.mobidevelop.robovm", "robopods-mapbox-ios", "2.2.4-SNAPSHOT")
val XLayout = Dependency("com.control-j.xlayout", "xcore", "1.0.1")
val XLayoutIos = XLayout.variant("xios")
val Robovm = Dependency("com.mobidevelop.robovm", "robovm-rt", "2.3.11")
val RobovmPlugin = Robovm.variant("robovm-gradle-plugin")
val RobovmCocoaTouch = Robovm.variant("robovm-cocoatouch")

// test dependencies
val AndroidTestEspresso =
    Dependency("com.android.support.test.espresso", "espresso-core", AndroidSupport.version)
val Junit = Dependency("junit", "junit", "4.12")
val Mockk = Dependency("io.mockk", "mockk", "1.10.0")
val Roboelectric = Dependency("org.robolectric", "robolectric", "4.3.1")




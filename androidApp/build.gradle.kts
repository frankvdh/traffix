/*
 * Copyright (c) 2022.  Control-J Pty Ltd
 * All rights reserved
 */

import com.github.triplet.gradle.androidpublisher.ResolutionStrategy

plugins {
    id(PlayPlugin.prefix) version (PlayPlugin.version)
    id("com.android.application")
    kotlin("android")
    //kotlin("android.extensions")
}

val buildNumber: Int by rootProject.extra
val appVersion: String by rootProject.extra
android {
    namespace="com.controlj.traffic"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    defaultConfig {
        versionCode = buildNumber
        versionName = appVersion
        applicationId = "com.controlj.traffic"
        compileSdk = AndroidApp.targetSdkVersion
        targetSdk = AndroidApp.targetSdkVersion
        minSdk = AndroidApp.minSdkVersion

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(findProperty("PLAY_MANAGED_STORE_FILE") as String)
            storePassword = findProperty("RELEASE_STORE_PASSWORD") as String
            keyAlias = findProperty("PLAY_MANAGED_KEY_ALIAS") as String
            keyPassword = findProperty("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    packagingOptions {
        resources.excludes.addAll(
            listOf(
                "DEPENDENCIES",
                "LICENSE",
                "LICENSE.txt",
                "license.txt",
                "NOTICE",
                "NOTICE.txt",
                "notice.txt",
                "ASL2.0"
            ).map { "META-INF/$it" }
        )
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

play {
    track.value("beta")
    serviceAccountCredentials.set(
        File(
            rootProject.projectDir,
            "secrets/publisher-credentials.json"
        )
    )
    resolutionStrategy.set(ResolutionStrategy.AUTO)
}
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val processedVersionCode = "${output.versionName.get()}.$buildNumber"
            //println("variant ${variant.name} version set to $processedVersionCode")
            output.versionName.set(processedVersionCode)
        }
    }
}

dependencies {
    listOf(
        Applibs,
        ApplibsAndroid,
        ThreetenAbp,
        AndroidXAnnotation,
        AndroidXAppcompat,
        AndroidXMultidex,
        AndroidSplashScreen,
        Rxjava3,
        Rxjava3Android,
        Rxjava3Kotlin,
        AndroidMapboxSdk,
        AndroidMapboxBuildings,
        AndroidMapboxAnnotations,
        Material,
        MaterialDrawer,
        ConstraintLayout,
        RecyclerView,
        SqlDroid,
        Kotlin,
        KotlinReflect,
        CJLog
    ).forEach { implementation(it()) }

    listOf(
        Junit,
        Roboelectric,
        Mockk
    ).forEach { testImplementation(it()) }
}

val asciidocSourceDir = File(projectDir, "asciidoc")
val htmlDestDir = File(projectDir, "src/main/assets/html")

task<Copy>("asciidoc") {
    from(asciidocSourceDir) {
        include("*.jpg", "*.png", "*.html")
    }
    into(htmlDestDir)
}

tasks.whenTaskAdded {
    if (name.startsWith("assemble") ||
        name.startsWith("mergeDebugAssets") ||
        name.startsWith("lintVitalAnalyzeRelease") ||
        name.startsWith("mergeReleaseAssets")) {
        dependsOn("asciidoc")
    }
    if (name.startsWith("publish") && name.endsWith("Bundle"))
        finalizedBy(":incBuild")
}

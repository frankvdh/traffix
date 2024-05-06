/*
 * Copyright (c) 2021.  Control-J Pty Ltd
 * All rights reserved
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.util.Properties

buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(KotlinPlugin())
        classpath(AndroidBuildTools())
        classpath(GoogleServices())
    }
}


val BUILD_NUMBER = "buildNumber"
val APP_VERSION = "appVersion"

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply { load(versionPropsFile.inputStream()) }
val buildNumber: Int by extra { (versionProps[BUILD_NUMBER] as String?)?.toInt() ?: 1 }
val appVersion: String by extra { (versionProps[APP_VERSION] as String) }

fun incrementBuildNumber() {
    versionProps[BUILD_NUMBER] = (buildNumber + 1).toString()
    versionProps.store(versionPropsFile.outputStream(), "version incremented to $buildNumber")
    println("version incremented to $buildNumber")
}


val javaVersion = "1.8"

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven {
            setUrl("https://mvnrepo-255701.appspot.com")
        }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/comcontrol-j-1006") }
        maven {
            credentials {
                // Do not change the username below.
                // This should always be `mapbox` (not your username).
                username = "mapbox"
                // Use the secret token you stored in gradle.properties as the password
                password = project.property("MAPBOX_DOWNLOADS_TOKEN") as String? ?: ""
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        maven { setUrl("https://jitpack.io") }
    }
    apply(plugin = "idea")
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion
            freeCompilerArgs += "-opt-in=kotlin.ExperimentalUnsignedTypes"
            jvmTarget = "1.8"
            apiVersion = "1.8"
            languageVersion = "1.8"


        }
    }
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

//--------- Version Increment ----------//


tasks.register("incBuild") {
    doLast {
        incrementBuildNumber()
    }
}

tasks.register("publishToBeta") {
    dependsOn("androidApp:publishReleaseBundle")
    dependsOn("iosApp:publishIPA")
    finalizedBy("incBuild")
}

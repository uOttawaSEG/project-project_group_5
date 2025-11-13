import org.gradle.kotlin.dsl.androidTestImplementation

plugins {
    alias(libs.plugins.android.application)

    // id("com.android.application")
    // Adding the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "ca.uottawa.seg.otams"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.uottawa.seg.otams"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += setOf("META-INF/NOTICE.md", "META-INF/LICENSE.md")
        }
    }
}

dependencies {
    // Importing the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // TODO: Add the dependencies for Firebase products you want to use
    // implementation("com.google.firebase:firebase-analytics")
    // Dependencies for realtime database
    implementation(libs.firebase.database)
    implementation(libs.firebase.database)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.rules)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    /*
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation ''

    androidTestImplementation (androidx.test:runner)
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'

    androidx.test.runner.AndroidJUnitRunner
    */
}
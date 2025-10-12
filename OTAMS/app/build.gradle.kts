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
}

dependencies {
    // Importing the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // implementation("com.google.firebase:firebase-analytics")
    // Dependencies for realtime database
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-database:22.0.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.e_canteenapp"
    compileSdk = 34 // ✅ Use only API 34

    defaultConfig {
        applicationId = "com.example.e_canteenapp"
        minSdk = 24
        targetSdk = 34 // ✅ Match with compileSdk
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" // ✅ Matches Compose 1.5.x
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")

    // ✅ Jetpack Compose 1.5.0 (Compatible with API 34)
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-text:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.foundation:foundation:1.5.0")
    implementation("androidx.compose.foundation:foundation-layout:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")

    // Lifecycle and activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.0.0")

    // Razorpay
    implementation("com.razorpay:checkout:1.6.33")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Layout and UI extras
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.compose.material:material-icons-extended")

}

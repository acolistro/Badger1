# Badger App - Setup Guide

## Prerequisites
1. Android Studio Hedgehog | 2023.1.1 or newer
2. JDK 17
3. Firebase account and project
4. Git

## Project Setup

### 1. Clone and Initial Setup
```bash
git clone [repository-url]
cd badger
```

### 2. Firebase Setup
1. Create a new Firebase project
2. Add Android app in Firebase console
    - Package name: com.example.badger
    - Download google-services.json
    - Place in app/ directory

### 3. Dependencies
Add to project-level build.gradle.kts:
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}
```

Add to app-level build.gradle.kts:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
}
```

### 4. Theme Setup
In res/values/themes.xml:
```xml
<style name="Theme.Badger" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- Primary brand color. -->
    <item name="colorPrimary">@color/purple_500</item>
    <item name="colorPrimaryVariant">@color/purple_700</item>
    <item name="colorOnPrimary">@color/white</item>
    <!-- Secondary brand color. -->
    <item name="colorSecondary">@color/teal_200</item>
    <item name="colorSecondaryVariant">@color/teal_700</item>
    <item name="colorOnSecondary">@color/black</item>
    <!-- Status bar color. -->
    <item name="android:statusBarColor">?attr/colorPrimaryVariant</item>
</style>
```

### 5. Build and Run
1. Sync project with Gradle files
2. Build project
3. Run on emulator or device

## Common Issues and Solutions

### 1. Material Theme Error
If seeing "Theme.MaterialComponents" error:
- Ensure proper theme inheritance
- Check Material Design dependency version
- Clean and rebuild project

### 2. Firebase Integration
If Firebase not connecting:
- Verify google-services.json placement
- Check package name matches
- Ensure Firebase project is properly configured

### 3. Navigation Issues
If navigation not working:
- Verify nav_graph.xml setup
- Check fragment declarations in manifest
- Ensure Safe Args plugin is applied

## Development Environment
- Minimum SDK: 24
- Target SDK: 34
- Kotlin Version: 1.9.22
- Gradle Version: 8.2.0
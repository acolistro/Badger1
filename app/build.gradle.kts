plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.badger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.badger"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
        testInstrumentationRunner = "com.example.badger.HiltTestRunner"
    }

    kapt {
        correctErrorTypes = true
        generateStubs = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        dataBinding = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

dependencies {

    // Core dependencies (from above)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    //Gson
    implementation(libs.gson)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.extensions)

    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.runner)
    kapt(libs.androidx.room.compiler)

    // Coroutines and Activity KTX
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.activity.ktx)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // For Hilt with View Model
    implementation(libs.androidx.hilt.navigation.fragment)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.storage.ktx)

    // Google Play Services Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // UI
    implementation(libs.androidx.swiperefreshlayout)

    // Testing Dependencies
    // Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.google.truth)
    testImplementation(libs.app.cash.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.testing)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.testing)
    testImplementation(libs.hilt.android)
    kaptTest(libs.hilt.android.compiler)
    testImplementation(libs.kotlin.test)

    // Android Tests
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.hilt.android)
    kaptAndroidTest(libs.hilt.android.compiler)

    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // SQLCipher for database encryption
    implementation("net.zetetic:android-database-sqlcipher:4.5.3")
    implementation("androidx.sqlite:sqlite-ktx:2.3.1")

    // Security library for key management
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
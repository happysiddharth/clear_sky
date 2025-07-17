plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.weather.clearsky"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.weather.clearsky"
        minSdk = 21
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Feature modules
    implementation(project(":feature:main"))
    implementation(project(":feature:weather"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:alerts"))
    
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Debug tools (only in debug builds)
    debugImplementation(libs.pluto)
    debugImplementation(libs.pluto.network)
    debugImplementation(libs.pluto.exceptions)
    debugImplementation(libs.pluto.logger)
    debugImplementation(libs.pluto.preferences)
    debugImplementation(libs.leakcanary)
    
    // No-op implementation for release builds
    releaseImplementation(libs.pluto.no.op)
    
    // Responsive Design
    implementation(libs.intuit.sdp)
    implementation(libs.intuit.ssp)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
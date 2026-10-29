plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.example.rufusmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.example.rufusmobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ultimate.filemanager"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.ultimate.filemanager"

        minSdk = 26
        targetSdk = 36

        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/{LGPL2.1,AL2.0}"
        }
    }
}

dependencies {

    implementation(
        platform(
            "androidx.compose:compose-bom:2025.08.00"
        )
    )

    implementation(
        "androidx.activity:activity-compose:1.10.1"
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.9.2"
    )

    implementation(
        "androidx.navigation:navigation-compose:2.9.3"
    )

    implementation(
        "androidx.documentfile:documentfile:1.1.0"
    )

    implementation(
        "androidx.security:security-crypto:1.1.0"
    )

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    cocoapods {
        summary = "NeuroDetect Lite shared business logic"
        homepage = "https://github.com/neurodetect/neurodetect-lite"
        ios.deploymentTarget = "16.0"
        pod("onnxruntime-mobile-objc") {
            version = "~> 1.17.1"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.onnxruntime.android)
            implementation(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
        }
    }
}

android {
    namespace = "com.neurodetect.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

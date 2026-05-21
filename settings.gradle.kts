rootProject.name = "NeuroDetectLite"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    }
}

include(":shared")

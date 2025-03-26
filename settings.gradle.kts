rootProject.name = "happy-svg"

pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

include(":happy-svg")
//include(":sample:composeApp")


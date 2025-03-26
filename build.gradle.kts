plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.mimimishkin"
version = "1.0.0"
description = "Create Happy Wheels levels in Kotlin"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("io.github.mimimishkin:path-utils:1.0.1")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

    }

}

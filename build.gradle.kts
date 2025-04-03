plugins {
    alias(libs.plugins.kotlin)
}

group = "io.github.mimimishkin"
version = "1.0.0"
description = "Create Happy Wheels levels in Kotlin"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.pathUtils)
    implementation(libs.svgSalamander)
    testImplementation(kotlin("test"))
}
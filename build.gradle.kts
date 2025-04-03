import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.mavenPublish)
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), name, version.toString())

    pom {
        name = project.name
        description = project.description
        inceptionYear = "2025"
        url = "https://github.com/mimimishkin/happy-svg"
        licenses {
            license {
                name = "MIT"
            }
        }
        developers {
            developer {
                id = "mimimishkin"
                name = "Mimimishkin"
                email = "printf.mika@gmail.com"
            }
        }
        scm {
            url = "https://github.com/mimimishkin/happy-svg"
            connection = "scm:git:git://github.com/mimimishkin/happy-svg"
        }
    }
}
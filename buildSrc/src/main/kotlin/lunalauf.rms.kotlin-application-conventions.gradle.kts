import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`

    kotlin("jvm")
}

group = "de.lunalauf-rms"
version = "2.0.6"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}
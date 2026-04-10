plugins {
    kotlin("plugin.serialization") version "2.1.0"

    id("lunalauf.rms.kotlin-library-conventions")
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.slf4jReload)

    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinJsonSerialization)
    implementation(libs.kotlinDateTime)
}
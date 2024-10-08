plugins {
    id("lunalauf.rms.kotlin-library-conventions")
    id("lunalauf.rms.kotlin-model-conventions")
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.slf4jReload)

    implementation(libs.kotlinCoroutines)
}
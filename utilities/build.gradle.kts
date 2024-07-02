plugins {
    id("lunalauf.rms.kotlin-library-conventions")
    id("lunalauf.rms.kotlin-model-conventions")
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.telegram:telegrambots:6.9.0")
    implementation(project(":modelAPI"))
}

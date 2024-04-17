plugins {
    id("lunalauf.rms.kotlin-library-conventions")
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.telegram:telegrambots:6.9.0")
    implementation(project(":modelAPI"))
    implementation("LunaLaufLanguage:LunaLaufLanguage:1.0.3")
}

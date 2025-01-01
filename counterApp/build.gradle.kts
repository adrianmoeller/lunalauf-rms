import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

    id("lunalauf.rms.kotlin-application-conventions")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.components.resources)

    implementation(libs.slf4j)
    implementation(libs.slf4jReload)

    implementation(libs.fontAwsome)
    implementation(libs.evaIcons)

    implementation(project(":utilities"))
}

compose.desktop {
    application {
        mainClass = "lunalauf.rms.counterapp.MainKt"

        nativeDistributions {
            modules("java.sql")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "lunalauf-rms-counter"
            packageVersion = version.toString()
            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
                menuGroup = "lunalauf-rms"
                perUserInstall = true
                upgradeUuid = "f8e5e131-d5d4-49a5-b3b4-fb0fb4eeb4c5".uppercase()
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
                menuGroup = "lunalauf-rms"
            }
        }
    }
}

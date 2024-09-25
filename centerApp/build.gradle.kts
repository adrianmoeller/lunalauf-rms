import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.compose")

    id("lunalauf.rms.kotlin-application-conventions")
    id("lunalauf.rms.kotlin-model-conventions")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    implementation(libs.slf4j)
    implementation(libs.slf4jReload)

    implementation(libs.fontAwsome)
    implementation(libs.evaIcons)

    implementation(libs.voyagerNavigator)
    implementation(libs.voyagerScreenmodel)
    implementation(libs.voyagerBottomSheetNavigator)
    implementation(libs.voyagerTabNavigator)
    implementation(libs.voyagerTransitions)

    implementation(project(":modelAPI"))
    implementation(project(":utilities"))
}

compose.desktop {
    application {
        mainClass = "lunalauf.rms.centralapp.MainKt"

        nativeDistributions {
            modules("java.sql")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "lunalauf-rms-center"
            packageVersion = version.toString()
            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
                menuGroup = "lunalauf-rms"
                perUserInstall = true
                upgradeUuid = "05880f6b-06f0-4d1f-bd2f-5451ad02358c".uppercase()
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
                menuGroup = "lunalauf-rms"
            }
        }
    }
}

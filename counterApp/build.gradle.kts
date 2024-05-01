import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id ("lunalauf.rms.kotlin-application-conventions")
}

group = "de.lunalauf-rms"
version = "2.0.2"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    val composeIconsVersion = "1.1.0"
    implementation("br.com.devsrsouza.compose.icons:font-awesome:$composeIconsVersion")
    implementation("br.com.devsrsouza.compose.icons:eva-icons:$composeIconsVersion")

    implementation(project(":utilities"))
}

compose.desktop {
    application {
        mainClass = "lunalauf.rms.counterapp.MainKt"

        nativeDistributions {
            modules("java.sql")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "lunalauf-rms-counter"
            packageVersion = "2.0.2"
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

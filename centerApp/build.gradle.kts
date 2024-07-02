import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("lunalauf.rms.kotlin-application-conventions")
    id("lunalauf.rms.kotlin-model-conventions")
}

group = "de.lunalauf-rms"
version = "2.0.3"

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

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    val composeIconsVersion = "1.1.0"
    implementation("br.com.devsrsouza.compose.icons:font-awesome:$composeIconsVersion")
    implementation("br.com.devsrsouza.compose.icons:eva-icons:$composeIconsVersion")

    val voyagerVersion = "1.0.0"
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

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
            packageVersion = "2.0.3"
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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.example"
version = "1.0-SNAPSHOT"

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

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    jvm {
        jvmToolchain(jdkVersion = 17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            apply(plugin = "lunalauf.rms.kotlin-application-conventions")

            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)

                val composeIconsVersion = "1.1.0"
                implementation("br.com.devsrsouza.compose.icons:font-awesome:$composeIconsVersion")
                implementation("br.com.devsrsouza.compose.icons:eva-icons:$composeIconsVersion")

                val voyagerVersion = "1.0.0-rc07"
                implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

                implementation("LunaLaufLanguage:LunaLaufLanguage:1.0.2")
                implementation(project(":modelAPI"))
                implementation(project(":utilities"))
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "lunalauf-rms-center"
            packageVersion = "1.0.0"
        }
    }
}

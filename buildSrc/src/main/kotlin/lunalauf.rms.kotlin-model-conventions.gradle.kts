plugins {
    `java-library`
}

dependencies {
    implementation("org.eclipse.emf:org.eclipse.emf.ecore:2.33.0")
    implementation("org.eclipse.emf:org.eclipse.emf.ecore.xmi:2.18.0")
    implementation("org.eclipse.emf:org.eclipse.emf.common:2.28.0")
    implementation(files("../buildSrc/libs/LunaLaufLanguage-1.0.3.jar"))
}
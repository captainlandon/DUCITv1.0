// Pure Kotlin/JVM module: deterministic risk classification, governance
// policy evaluation, and transaction state-machine enforcement. Depends
// only on core:domain-model — never on Android or on the intelligence
// gateway, so it stays JVM-testable without a device or emulator
// (Verbal Reference Implementation v1.0, section 22.1 "property tests").
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:domain-model"))
    testImplementation(libs.junit)
}

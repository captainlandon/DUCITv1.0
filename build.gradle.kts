// Root build file intentionally applies no plugins.
//
// Each module declares the plugins it needs directly (via the version
// catalog) so that Gradle's configuration-on-demand only resolves the
// Android Gradle Plugin / AndroidX artifacts for modules that actually need
// them. core:domain-model and core:domain-policy are pure Kotlin/JVM and
// must stay buildable and testable without the Android SDK or a Google
// Maven mirror present (Verbal Reference Implementation v1.0, section 5:
// "Pure Kotlin domain layer; JVM-testable without Android").
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

// Android library module: Room-backed local persistence for installed-app
// cache, PersonalContextRecords, and transaction/receipt state.
//
// NOTE ON BUILD ENVIRONMENT: this module (and :app) require the Android
// Gradle Plugin and AndroidX artifacts from Google's Maven repository.
// They were written and reviewed, but could not be compiled in the
// sandbox that produced this commit (no route to dl.google.com; see
// docs/adr/ADR-00-build-environment.md). CI builds this module against a
// full Android SDK — verify there before trusting it beyond review.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.ducit.data.local"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain-model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

// Application module: the offline app-grid launcher shell (Sprint 1, Days
// 2-4 of the Intelligence-to-Implementation Dossier v1.0 two-week plan).
// Deliberately AI-free — this module proves the "launcher usefulness
// cannot depend on AI or network access" invariant before any Now
// Card / Understanding / Governance surface is layered on top of it.
//
// See core:data-local's build.gradle.kts for the build-environment note:
// this module was not compiled locally (no Android SDK / no route to
// dl.google.com in this session); CI builds it against a full SDK.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ducit.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ducit.launcher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-sprint1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain-model"))
    implementation(project(":core:domain-policy"))
    implementation(project(":core:data-local"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    // Extended, not core: core only has ~20 icons and doesn't include
    // Bookmark, which the launcher's "open Memory" affordance uses. Sprint
    // 1 optimizes for correctness over APK size; revisit if size matters
    // before this ships beyond a private prototype.
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)

    // core:data-local's Room dependency is `implementation`-scoped (it
    // doesn't leak to consumers by design), but DucitApplication calls
    // Room.databaseBuilder(...) directly, so :app needs its own.
    implementation(libs.room.runtime)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
}

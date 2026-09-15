package com.ducit.domain.model

/**
 * A launchable app on the device, as surfaced by the offline app-grid
 * fallback (Intelligence-to-Implementation Dossier v1.0, section 6:
 * "Launch installed apps in airplane mode with all AI disabled"). This is
 * deliberately minimal — the launcher never needs more than identity and a
 * launch target to satisfy the release criterion.
 */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
)

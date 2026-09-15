package com.ducit.domain.model

/**
 * Coarse data-sensitivity classification. Drives storage, provider
 * routing, logging, retention, and approval-tier decisions
 * (Verbal Reference Implementation v1.0, section 21.2).
 */
enum class SensitivityClass {
    PUBLIC,
    STANDARD,
    FINANCIAL,
    HEALTH,
    LEGAL,
    IDENTITY_SECURITY,
    PRECISE_LOCATION,
    CHILD_DATA,
}

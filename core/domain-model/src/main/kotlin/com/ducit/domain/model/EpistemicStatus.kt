package com.ducit.domain.model

/**
 * Epistemic status of a [Proposition] or [PersonalContextRecord] value
 * (Verbal Reference Implementation v1.0, section 6.2). Status is immutable
 * except through an explicit promotion event carrying provenance (INV-01):
 * inference never silently becomes fact.
 */
enum class EpistemicStatus(val canDriveAction: Boolean) {
    /** Directly observed, reliable state. */
    FACT(canDriveAction = true),

    /** User explicitly asserted or confirmed this value. Highest semantic
     * precedence unless the user later revises it (INV-07). */
    USER_CONFIRMED(canDriveAction = true),

    /** Derived from evidence or a model. Only usable with confidence and
     * relevance checks; consequential use may require confirmation. */
    INFERRED(canDriveAction = false),

    /** Observed or stated preference. Advisory; never a hard constraint
     * unless the user makes it one. */
    PREFERENCE(canDriveAction = false),

    /** Plausible option or hypothesis. May be surfaced; cannot be treated
     * as true. */
    POSSIBILITY(canDriveAction = false),

    /** System-generated suggested course. Never authority by itself. */
    RECOMMENDATION(canDriveAction = false),

    /** Conflicting evidence exists. Consequential action should block or
     * request resolution. */
    DISPUTED(canDriveAction = false),

    /** Validity/freshness window exceeded. Cannot support a high-
     * consequence action without refresh. */
    STALE(canDriveAction = false),

    /** User has retracted this proposition. Excluded from retrieval. */
    RETRACTED(canDriveAction = false),
}

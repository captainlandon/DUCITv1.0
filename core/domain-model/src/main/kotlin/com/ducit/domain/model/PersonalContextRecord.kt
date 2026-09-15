package com.ducit.domain.model

/** Whether — and how — a user has corrected a [PersonalContextRecord]. */
enum class CorrectionState {
    NONE,
    USER_CONFIRMED,
    USER_CORRECTED,
    USER_DISPUTED,
}

/** Deletion lifecycle for a [PersonalContextRecord] (INV-11: deletion must
 * be real within declared retention/backup limits). */
enum class DeletionState {
    ACTIVE,
    PENDING_DELETION,
    DELETED,
}

/** Retention class, per Verbal Reference Implementation v1.0 section 17.3.
 * Actual periods are set by data class, jurisdiction, and product need —
 * "keep everything because it might improve AI" is prohibited. */
enum class RetentionClass {
    EPHEMERAL,
    SHORT_LIVED,
    DURABLE_USER_MODEL,
    AUDIT,
    CREDENTIAL,
}

/**
 * The canonical PersonalContextRecord (Intelligence-to-Implementation
 * Dossier v1.0, section 5 "Canonical PersonalContextRecord"). This is the
 * durable, user-governed, provenance-aware memory unit that the Understanding
 * plane persists and the Memory Inspector surface reads, corrects, and
 * deletes.
 *
 * Field groups mirror the dossier exactly: Identity, Epistemics,
 * Provenance, Time, Governance, Use.
 */
data class PersonalContextRecord(
    // --- Identity ---
    val recordId: String,
    val entityId: String,
    val subject: String,
    val predicate: String,
    val value: String,
    val valueRef: String? = null,

    // --- Epistemics ---
    val status: EpistemicStatus,
    val confidence: Double?,

    // --- Provenance ---
    val sourceRefs: List<String>,
    val sourceType: String,
    val capturedAt: Long,
    val lastVerifiedAt: Long?,
    val provenanceUnknown: Boolean = sourceRefs.isEmpty(),

    // --- Time ---
    val validFrom: Long,
    val validTo: Long? = null,
    val freshnessPolicy: RetentionClass,
    val supersedes: String? = null,
    val supersededBy: String? = null,

    // --- Governance ---
    val sensitivityClass: SensitivityClass,
    val purposeAllowlist: List<String>,
    val retentionPolicy: RetentionClass,
    val correctionState: CorrectionState = CorrectionState.NONE,
    val deletionState: DeletionState = DeletionState.ACTIVE,

    // --- Use ---
    val lastUsedAt: Long? = null,
    val usedForPlanIds: List<String> = emptyList(),
    /** The "Because..." explanation label shown by the Memory Inspector
     * (Verbal Reference Implementation v1.0, section 15.2/17). */
    val explanationLabel: String,
) {
    init {
        require(recordId.isNotBlank()) { "recordId must not be blank" }
        require(confidence == null || confidence in 0.0..1.0) {
            "confidence must be within 0.0..1.0, was $confidence"
        }
        require(provenanceUnknown || sourceRefs.isNotEmpty()) {
            "a record with known provenance must carry at least one source reference"
        }
    }

    /** Records are excluded from retrieval once deleted or superseded
     * (Memory, Learning, Correction, Retention and Deletion, section 17). */
    fun isRetrievable(): Boolean =
        deletionState == DeletionState.ACTIVE && supersededBy == null

    fun isStale(nowMillis: Long): Boolean =
        validTo != null && nowMillis > validTo
}

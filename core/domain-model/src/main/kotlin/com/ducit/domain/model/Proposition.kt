package com.ducit.domain.model

/**
 * A typed claim about an entity, relationship, state, or event, with
 * provenance, epistemic status, confidence, validity interval, and
 * permitted uses (Verbal Reference Implementation v1.0, section 6 and
 * Appendix A.1). This is the canonical unit of the Living User Model.
 *
 * Every durable proposition MUST have provenance or be explicitly marked
 * [provenanceUnknown] (INV-09).
 */
data class Proposition(
    val propositionId: String,
    val subjectRef: String,
    val predicate: String,
    val objectValue: String,
    val epistemicStatus: EpistemicStatus,
    /** 0..1, or null for USER_CONFIRMED/FACT where not meaningful. */
    val confidence: Double?,
    val evidenceRefs: List<String>,
    val provenanceUnknown: Boolean = evidenceRefs.isEmpty(),
    val validFrom: Long,
    val validTo: Long? = null,
    val freshnessUntil: Long? = null,
    val sensitivityClass: SensitivityClass,
    val purposeAllowlist: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val supersedesRef: String? = null,
) {
    init {
        require(propositionId.isNotBlank()) { "propositionId must not be blank" }
        require(confidence == null || confidence in 0.0..1.0) {
            "confidence must be within 0.0..1.0, was $confidence"
        }
        require(provenanceUnknown || evidenceRefs.isNotEmpty()) {
            "a proposition with known provenance must carry at least one evidence reference"
        }
    }

    /** Whether this proposition, as currently held, is eligible to drive a
     * consequential decision (INV-01, INV-07). Staleness and dispute are
     * evaluated on top of the underlying epistemic status. */
    fun isActionable(nowMillis: Long): Boolean {
        val notStale = freshnessUntil == null || nowMillis <= freshnessUntil
        val notExpired = validTo == null || nowMillis <= validTo
        return epistemicStatus.canDriveAction && notStale && notExpired
    }
}

package com.ducit.domain.model

/**
 * Ducit never collapses invocation success, execution success, and outcome
 * success into one state (INV-05, Verbal Reference Implementation v1.0
 * section 14.1). [VerificationStatus] is the outcome-success axis only.
 */
enum class VerificationStatus {
    VERIFIED,
    PARTIAL,
    UNVERIFIED,
    FAILED,
}

data class VerificationResult(
    val actionId: String,
    val expectedPostconditions: List<String>,
    val observedPostconditions: List<String>,
    val method: String,
    val sourceRefs: List<String>,
    val status: VerificationStatus,
    val confidence: Double?,
    val checkedAt: Long,
    val nextCheckAt: Long? = null,
)

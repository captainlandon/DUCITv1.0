package com.ducit.domain.model

/** The only three outcomes a governance evaluation may produce
 * (Verbal Reference Implementation v1.0, Appendix A.3). A model never
 * produces this type directly (INV-02, INV-04). */
enum class Decision {
    ALLOW,
    DENY,
    APPROVAL_REQUIRED,
}

data class GovernanceDecision(
    val decisionId: String,
    val actionId: String,
    val riskTier: RiskTier,
    val sensitivityClass: SensitivityClass,
    val matchedEnvelopeIds: List<String>,
    val decision: Decision,
    val reasonCodes: List<String>,
    val previewRequired: Boolean,
    val expiresAt: Long?,
    val policyVersion: String,
)

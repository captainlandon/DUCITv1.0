package com.ducit.domain.model

/**
 * The deterministic inputs the R0-R5 tier engine consumes (Verbal Reference
 * Implementation v1.0, section 13.3). A model MAY classify raw context into
 * candidate facts, but only the deterministic rules in `core:domain-policy`
 * choose the enforced tier — this class is data, never a decision.
 */
data class RiskFacts(
    val sensitivityClass: SensitivityClass,
    /** False for a pure read/analysis action (R0 candidate). */
    val causesStateChange: Boolean,
    val isReversible: Boolean,
    val externalVisibility: Boolean,
    val financialImpact: Boolean,
    val legalEffect: Boolean,
    val healthEffect: Boolean,
    val identitySecurityEffect: Boolean,
    val affectsOtherPersons: Boolean,
    /** Confidence in target identity, amount, recipient, or postcondition.
     * Low confidence blocks execution regardless of nominal tier. */
    val confidence: Double,
    val connectorTrusted: Boolean,
    val verificationStrength: VerificationStrength,
    val userPreauthorized: Boolean,
) {
    init {
        require(confidence in 0.0..1.0) { "confidence must be within 0.0..1.0, was $confidence" }
    }
}

enum class VerificationStrength {
    NONE,
    WEAK,
    SOURCE_OF_RECORD,
}

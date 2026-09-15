package com.ducit.domain.model

/**
 * R0-R5 risk tiers (Verbal Reference Implementation v1.0, section 13.2).
 * A model may classify raw context into candidate [RiskFacts]; only the
 * deterministic tier engine in `core:domain-policy` chooses the enforced
 * tier (INV-02, INV-03).
 */
enum class RiskTier(val ordinal5: Int, val description: String) {
    /** No external state change; authorized read/analysis. May proceed
     * silently within purpose/scope. */
    R0_OBSERVATION(0, "Observation — no external state change"),

    /** Local state change with easy reversal. May auto-execute under
     * standing permission. */
    R1_REVERSIBLE_LOCAL(1, "Reversible local change"),

    /** External change with reliable reversal and limited consequence.
     * Standing permission may allow; notify/receipt. */
    R2_REVERSIBLE_EXTERNAL(2, "Reversible external change"),

    /** Meaningful external communication, submission, or change. Explicit
     * contextual approval unless narrowly pre-authorized by documented
     * policy. */
    R3_CONSEQUENTIAL(3, "Consequential external action"),

    /** Money, health disclosure, legal/identity/security, or material
     * irreversible consequence. Mandatory explicit approval with preview;
     * no silent standing bypass (INV-04). */
    R4_SENSITIVE(4, "Sensitive / high-impact action"),

    /** Unsupported or unacceptably dangerous autonomous action. Ducit may
     * inform/prepare but cannot autonomously execute (INV-04). */
    R5_PROHIBITED_AUTONOMOUS(5, "Prohibited autonomous action"),
    ;

    val requiresExplicitApproval: Boolean get() = ordinal5 >= R3_CONSEQUENTIAL.ordinal5
    val isAutonomousExecutionForbidden: Boolean get() = this == R5_PROHIBITED_AUTONOMOUS
}

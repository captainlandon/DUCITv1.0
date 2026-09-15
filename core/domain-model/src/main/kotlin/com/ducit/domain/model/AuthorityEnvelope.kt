package com.ducit.domain.model

/**
 * A typed, bounded grant of authority (Verbal Reference Implementation
 * v1.0, section 13.1; Inner Workings & Data Flow Architecture v1.1,
 * section 6 "five boundary axes"). Authority is never a boolean: every
 * grant answers who authorized it, for which capability and target, for
 * what purpose, using which data, for how long, under what risk ceiling,
 * and whether it may be delegated.
 *
 * Only the user (Z0, the highest product authority) or a deterministic
 * policy evaluation can produce or match an envelope — a model cannot
 * grant, expand, or persist its own authority (INV-02).
 */
data class AuthorityEnvelope(
    val envelopeId: String,
    val principal: String,
    val capability: CapabilityDomain,
    val connectorId: String?,
    val targetRef: String?,
    val purposeId: String,
    /** Data axis: the specific fields this grant covers. Empty means "no
     * data access", not "all data". */
    val dataFields: Set<String>,
    val maxRiskTier: RiskTier,
    val grantedAt: Long,
    val expiresAt: Long?,
    val sharingAllowed: Boolean = false,
    val delegationAllowed: Boolean = false,
    val revoked: Boolean = false,
) {
    fun isActive(nowMillis: Long): Boolean =
        !revoked && (expiresAt == null || nowMillis <= expiresAt)

    /** Whether this envelope covers a proposed action's capability, target,
     * purpose, and risk tier. Does not by itself imply ALLOW — the policy
     * engine still applies escalation rules. */
    fun covers(
        capability: CapabilityDomain,
        purposeId: String,
        requestedTier: RiskTier,
        nowMillis: Long,
    ): Boolean =
        isActive(nowMillis) &&
            this.capability == capability &&
            this.purposeId == purposeId &&
            requestedTier.ordinal5 <= maxRiskTier.ordinal5
}

package com.ducit.domain.policy

import com.ducit.domain.model.AuthorityEnvelope
import com.ducit.domain.model.CapabilityDomain
import com.ducit.domain.model.Decision
import com.ducit.domain.model.GovernanceDecision
import com.ducit.domain.model.RiskFacts
import com.ducit.domain.model.RiskTier

/**
 * Deterministic governance evaluation (Verbal Reference Implementation
 * v1.0, section 13 and Appendix A.3). Consumes risk facts and matches
 * authority envelopes; emits ALLOW / DENY / APPROVAL_REQUIRED. Never
 * delegates the final decision to a model (INV-02, INV-04).
 *
 * Default rule per risk tier (section 13.2):
 *  - R0 observation:            proceeds silently.
 *  - R1/R2 reversible:          standing permission (a matching envelope)
 *                                may allow; otherwise approval is required.
 *  - R3 consequential:          explicit approval unless a matching
 *                                envelope narrowly pre-authorizes it.
 *  - R4 sensitive/high-impact:  mandatory explicit approval, always — an
 *                                envelope can never silently bypass it.
 *  - R5 prohibited autonomous:  never autonomously executable. DENY.
 *
 * A [RiskFacts.confidence] below [minConfidence] blocks the action
 * regardless of nominal tier ("low confidence... blocks execution
 * regardless of nominal tier", section 13.4).
 */
class GovernancePolicyEngine(
    private val policyVersion: String = "v0.1",
    private val minConfidence: Double = 0.6,
) {

    fun evaluate(
        actionId: String,
        decisionId: String,
        capability: CapabilityDomain,
        purposeId: String,
        facts: RiskFacts,
        envelopes: List<AuthorityEnvelope>,
        nowMillis: Long,
    ): GovernanceDecision {
        val tier = RiskClassifier.classify(facts)
        val reasonCodes = mutableListOf<String>()

        if (facts.confidence < minConfidence) {
            reasonCodes += "LOW_CONFIDENCE"
            return decision(decisionId, actionId, tier, facts, Decision.DENY, reasonCodes, nowMillis)
        }

        if (tier == RiskTier.R5_PROHIBITED_AUTONOMOUS) {
            reasonCodes += "R5_PROHIBITED_AUTONOMOUS"
            return decision(decisionId, actionId, tier, facts, Decision.DENY, reasonCodes, nowMillis)
        }

        val matched = envelopes.filter {
            it.covers(capability = capability, purposeId = purposeId, requestedTier = tier, nowMillis = nowMillis)
        }

        val outcome = when (tier) {
            RiskTier.R0_OBSERVATION -> {
                reasonCodes += "R0_SILENT_PROCEED"
                Decision.ALLOW
            }
            RiskTier.R1_REVERSIBLE_LOCAL, RiskTier.R2_REVERSIBLE_EXTERNAL -> {
                if (matched.isNotEmpty()) {
                    reasonCodes += "STANDING_PERMISSION_MATCHED"
                    Decision.ALLOW
                } else {
                    reasonCodes += "NO_STANDING_PERMISSION"
                    Decision.APPROVAL_REQUIRED
                }
            }
            RiskTier.R3_CONSEQUENTIAL -> {
                if (matched.isNotEmpty()) {
                    reasonCodes += "NARROWLY_PREAUTHORIZED"
                    Decision.ALLOW
                } else {
                    reasonCodes += "EXPLICIT_APPROVAL_REQUIRED"
                    Decision.APPROVAL_REQUIRED
                }
            }
            RiskTier.R4_SENSITIVE -> {
                // INV-04: mandatory explicit approval; no silent standing
                // bypass, even when an envelope nominally matches.
                reasonCodes += "R4_MANDATORY_EXPLICIT_APPROVAL"
                Decision.APPROVAL_REQUIRED
            }
            RiskTier.R5_PROHIBITED_AUTONOMOUS -> Decision.DENY // unreachable, handled above
        }

        return decision(decisionId, actionId, tier, facts, outcome, reasonCodes, nowMillis, matched)
    }

    private fun decision(
        decisionId: String,
        actionId: String,
        tier: RiskTier,
        facts: RiskFacts,
        outcome: Decision,
        reasonCodes: List<String>,
        nowMillis: Long,
        matched: List<AuthorityEnvelope> = emptyList(),
    ) = GovernanceDecision(
        decisionId = decisionId,
        actionId = actionId,
        riskTier = tier,
        sensitivityClass = facts.sensitivityClass,
        matchedEnvelopeIds = matched.map { it.envelopeId },
        decision = outcome,
        reasonCodes = reasonCodes,
        previewRequired = outcome != Decision.ALLOW,
        expiresAt = null,
        policyVersion = policyVersion,
    )
}

package com.ducit.domain.policy

import com.ducit.domain.model.CapabilityDomain
import com.ducit.domain.model.Proposition
import com.ducit.domain.model.UntrustedActionProposal

/** Structural/evidence screening, NOT an execution authorization.
 * All allowlists and propositions must come from trusted host state, not a model.
 * Risk facts, authority matching, approvals and postconditions remain host-owned. */
object ProposalIntake {
    fun rejectionReasons(
        proposal: UntrustedActionProposal,
        allowedCapability: CapabilityDomain,
        allowedTarget: String,
        allowedParameterKeys: Set<String>,
        propositions: List<Proposition>,
        purposeId: String,
        nowMillis: Long,
    ): List<String> {
        val reasons = mutableListOf<String>()
        if (proposal.capability != allowedCapability) reasons += "CAPABILITY_MISMATCH"
        if (proposal.targetRef.isBlank() || proposal.targetRef != allowedTarget) reasons += "TARGET_MISMATCH"
        if (proposal.parameters.size > 20 || !allowedParameterKeys.containsAll(proposal.parameters.keys) ||
            proposal.parameters.any { it.key.length > 100 || it.value.length > 4000 }) reasons += "INVALID_PARAMETERS"
        if (proposal.rationale.isBlank() || proposal.rationale.length > 4000) reasons += "INVALID_RATIONALE"
        if (proposal.evidenceRefs.isEmpty() || proposal.evidenceRefs.size > 20 ||
            proposal.evidenceRefs.distinct().size != proposal.evidenceRefs.size) reasons += "INVALID_EVIDENCE_REFS"
        for (ref in proposal.evidenceRefs.take(20)) {
            val matches = propositions.filter { it.propositionId == ref }
            val p = matches.singleOrNull()
            if (p == null || !p.isActionable(nowMillis) || p.validFrom > nowMillis ||
                purposeId !in p.purposeAllowlist || p.provenanceUnknown) reasons += "INELIGIBLE_EVIDENCE"
        }
        return reasons.distinct()
    }
}

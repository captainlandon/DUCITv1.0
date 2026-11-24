package com.ducit.domain.model

/** Provider-neutral candidate data only. No permission, risk decision or execution handle.
 * A future adapter must reject unknown JSON fields and never decode a model response
 * as ActionRequest, AuthorityEnvelope or GovernanceDecision. */
data class UntrustedActionProposal(
    val capability: CapabilityDomain,
    val targetRef: String,
    val parameters: Map<String, String>,
    val evidenceRefs: List<String>,
    val rationale: String,
)

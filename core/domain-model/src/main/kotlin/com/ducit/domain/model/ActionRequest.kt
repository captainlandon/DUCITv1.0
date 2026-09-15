package com.ducit.domain.model

/**
 * A single node's execution request (Verbal Reference Implementation v1.0,
 * Appendix A.2). Only an [ActionRequest] that has produced an
 * [GovernanceDecision] of ALLOW may cross into `execution-android`
 * adapters (INV-03).
 */
data class ActionRequest(
    val actionId: String,
    val transactionId: String,
    val capability: CapabilityDomain,
    val targetRef: String,
    val parameters: Map<String, String>,
    val connectorCandidates: List<String>,
    val evidenceRefs: List<String>,
    val expectedPostconditions: List<String>,
    val riskFacts: RiskFacts,
    val requestedAuthority: String?,
    val idempotencyKey: String,
    val retryPolicy: RetryPolicy,
    val compensationActionRef: String?,
    val verificationMethod: String,
)

data class RetryPolicy(
    val maxAttempts: Int,
    val backoffMillis: Long,
)

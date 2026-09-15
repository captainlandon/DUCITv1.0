package com.ducit.domain.model

/**
 * A tamper-evident audit record of one transaction (Verbal Reference
 * Implementation v1.0, section 14.3 and Appendix A). Receipts are hash-
 * chained via [previousReceiptHash] and are user-deletable; deletion
 * generates a minimal meta-receipt rather than erasing the chain silently
 * (Inner Workings & Data Flow Architecture v1.1, section 9.4) — the
 * meta-receipt itself is modeled by `MetaReceipt` once deletion ships.
 */
data class TrustReceipt(
    val receiptId: String,
    val transactionId: String,
    val createdAt: Long,
    val triggerSource: String,
    val triggerReason: String,
    val evidenceRefs: List<String>,
    val planVersion: String,
    val actionIds: List<String>,
    val riskTier: RiskTier,
    val authorityEnvelopeId: String?,
    val approvalEventId: String?,
    val connectorResultRefs: List<String>,
    val verification: VerificationResult?,
    val outcomeSummary: String,
    val previousReceiptHash: String?,
    val receiptHash: String,
    val propositionsChanged: List<String> = emptyList(),
)

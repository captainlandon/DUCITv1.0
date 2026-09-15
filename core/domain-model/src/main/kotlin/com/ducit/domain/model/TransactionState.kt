package com.ducit.domain.model

/**
 * The canonical transaction state machine (Verbal Reference Implementation
 * v1.0, section 12.3):
 *
 * DRAFT -> GOVERNANCE_PENDING -> APPROVAL_PENDING (if needed) -> READY ->
 * EXECUTING -> VERIFYING -> VERIFIED | PARTIAL | FAILED | CANCELLED ->
 * RECEIPTED
 *
 * State transitions MUST be atomic in persistent storage. Valid transitions
 * are enforced by `core:domain-policy`'s TransactionStateMachine, not by
 * this enum alone — this type only names the legal states.
 */
enum class TransactionState {
    DRAFT,
    GOVERNANCE_PENDING,
    APPROVAL_PENDING,
    READY,
    EXECUTING,
    VERIFYING,
    VERIFIED,
    PARTIAL,
    FAILED,
    CANCELLED,
    RECEIPTED,
    ;

    val isTerminalOutcome: Boolean
        get() = this == VERIFIED || this == PARTIAL || this == FAILED || this == CANCELLED

    val isReceipted: Boolean get() = this == RECEIPTED
}

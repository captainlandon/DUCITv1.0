package com.ducit.domain.policy

import com.ducit.domain.model.Decision
import com.ducit.domain.model.RiskTier
import com.ducit.domain.model.TransactionState
import com.ducit.domain.model.TransactionState.APPROVAL_PENDING
import com.ducit.domain.model.TransactionState.CANCELLED
import com.ducit.domain.model.TransactionState.DRAFT
import com.ducit.domain.model.TransactionState.EXECUTING
import com.ducit.domain.model.TransactionState.FAILED
import com.ducit.domain.model.TransactionState.GOVERNANCE_PENDING
import com.ducit.domain.model.TransactionState.PARTIAL
import com.ducit.domain.model.TransactionState.READY
import com.ducit.domain.model.TransactionState.RECEIPTED
import com.ducit.domain.model.TransactionState.VERIFIED
import com.ducit.domain.model.TransactionState.VERIFYING

/**
 * Enforces the canonical transaction state machine (Verbal Reference
 * Implementation v1.0, section 12.3):
 *
 * DRAFT -> GOVERNANCE_PENDING -> APPROVAL_PENDING (if needed) -> READY ->
 * EXECUTING -> VERIFYING -> VERIFIED | PARTIAL | FAILED | CANCELLED ->
 * RECEIPTED
 *
 * This is the single choke point through which every transaction must
 * pass on its way to EXECUTING: [transition] throws on any attempt to
 * reach READY without a governance ALLOW or a user-granted approval, and
 * on any attempt to auto-execute an R4/R5 action (INV-03, INV-04).
 */
object TransactionStateMachine {

    private val allowedTransitions: Map<TransactionState, Set<TransactionState>> = mapOf(
        DRAFT to setOf(GOVERNANCE_PENDING, CANCELLED),
        GOVERNANCE_PENDING to setOf(APPROVAL_PENDING, READY, FAILED, CANCELLED),
        APPROVAL_PENDING to setOf(READY, CANCELLED, FAILED),
        READY to setOf(EXECUTING, CANCELLED),
        EXECUTING to setOf(VERIFYING, FAILED, CANCELLED),
        VERIFYING to setOf(VERIFIED, PARTIAL, FAILED),
        VERIFIED to setOf(RECEIPTED),
        PARTIAL to setOf(RECEIPTED),
        FAILED to setOf(RECEIPTED),
        CANCELLED to setOf(RECEIPTED),
        RECEIPTED to emptySet(),
    )

    class IllegalTransitionException(message: String) : IllegalStateException(message)

    /**
     * @param governanceDecision required to justify a transition into READY.
     * @param approvalGranted whether the user has explicitly approved an
     *   APPROVAL_REQUIRED decision.
     * @param riskTier required to justify a transition into READY; used to
     *   hard-block R4/R5 auto-execution regardless of what the governance
     *   decision claims.
     */
    fun transition(
        from: TransactionState,
        to: TransactionState,
        governanceDecision: Decision? = null,
        approvalGranted: Boolean = false,
        riskTier: RiskTier? = null,
    ): TransactionState {
        val allowed = allowedTransitions[from].orEmpty()
        if (to !in allowed) {
            throw IllegalTransitionException("illegal transition from $from to $to")
        }

        if (to == READY) {
            guardReadyTransition(governanceDecision, approvalGranted, riskTier)
        }

        return to
    }

    private fun guardReadyTransition(
        governanceDecision: Decision?,
        approvalGranted: Boolean,
        riskTier: RiskTier?,
    ) {
        // R5 is never autonomously executable, whatever the nominal decision says.
        if (riskTier == RiskTier.R5_PROHIBITED_AUTONOMOUS) {
            throw IllegalTransitionException("R5 actions are never autonomously executable")
        }

        // R4 always requires an explicit, granted approval — an ALLOW
        // decision alone is never sufficient (INV-04: no silent standing
        // bypass).
        if (riskTier == RiskTier.R4_SENSITIVE && !approvalGranted) {
            throw IllegalTransitionException(
                "R4 actions require an explicit contextual approval, never a silent standing bypass",
            )
        }

        val cleared = when (governanceDecision) {
            Decision.ALLOW -> true
            Decision.APPROVAL_REQUIRED -> approvalGranted
            Decision.DENY, null -> false
        }
        if (!cleared) {
            throw IllegalTransitionException(
                "cannot reach READY without a governance ALLOW or a granted approval " +
                    "(governanceDecision=$governanceDecision, approvalGranted=$approvalGranted)",
            )
        }
    }
}

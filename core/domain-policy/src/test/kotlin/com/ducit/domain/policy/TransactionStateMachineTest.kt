package com.ducit.domain.policy

import com.ducit.domain.model.Decision
import com.ducit.domain.model.RiskTier
import com.ducit.domain.model.TransactionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TransactionStateMachineTest {

    @Test
    fun `happy path from DRAFT to RECEIPTED`() {
        var state = TransactionState.DRAFT
        state = TransactionStateMachine.transition(state, TransactionState.GOVERNANCE_PENDING)
        state = TransactionStateMachine.transition(
            state,
            TransactionState.READY,
            governanceDecision = Decision.ALLOW,
            riskTier = RiskTier.R1_REVERSIBLE_LOCAL,
        )
        state = TransactionStateMachine.transition(state, TransactionState.EXECUTING)
        state = TransactionStateMachine.transition(state, TransactionState.VERIFYING)
        state = TransactionStateMachine.transition(state, TransactionState.VERIFIED)
        state = TransactionStateMachine.transition(state, TransactionState.RECEIPTED)
        assertEquals(TransactionState.RECEIPTED, state)
    }

    @Test
    fun `illegal skip from DRAFT directly to EXECUTING is rejected`() {
        assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
            TransactionStateMachine.transition(TransactionState.DRAFT, TransactionState.EXECUTING)
        }
    }

    @Test
    fun `READY without a governance decision is rejected`() {
        assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
            TransactionStateMachine.transition(
                TransactionState.GOVERNANCE_PENDING,
                TransactionState.READY,
                governanceDecision = null,
            )
        }
    }

    @Test
    fun `READY on a DENY decision is rejected`() {
        assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
            TransactionStateMachine.transition(
                TransactionState.GOVERNANCE_PENDING,
                TransactionState.READY,
                governanceDecision = Decision.DENY,
            )
        }
    }

    @Test
    fun `APPROVAL_REQUIRED without a granted approval is rejected`() {
        assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
            TransactionStateMachine.transition(
                TransactionState.APPROVAL_PENDING,
                TransactionState.READY,
                governanceDecision = Decision.APPROVAL_REQUIRED,
                approvalGranted = false,
            )
        }
    }

    @Test
    fun `APPROVAL_REQUIRED with a granted approval reaches READY`() {
        val state = TransactionStateMachine.transition(
            TransactionState.APPROVAL_PENDING,
            TransactionState.READY,
            governanceDecision = Decision.APPROVAL_REQUIRED,
            approvalGranted = true,
            riskTier = RiskTier.R3_CONSEQUENTIAL,
        )
        assertEquals(TransactionState.READY, state)
    }

    // R5 is never autonomously executable, whatever the nominal decision claims.
    @Test
    fun `R5 can never reach READY even with a claimed ALLOW`() {
        assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
            TransactionStateMachine.transition(
                TransactionState.GOVERNANCE_PENDING,
                TransactionState.READY,
                governanceDecision = Decision.ALLOW,
                riskTier = RiskTier.R5_PROHIBITED_AUTONOMOUS,
            )
        }
    }

    /**
     * Property test (Verbal Reference Implementation v1.0, section 22.1):
     * "no R4 transaction reaches READY without explicit approval". Sweeps
     * every combination of governance decision and approval flag and
     * asserts the invariant holds in every case.
     */
    @Test
    fun `property - no R4 transaction ever reaches READY without a granted approval`() {
        val decisions = listOf(Decision.ALLOW, Decision.APPROVAL_REQUIRED, Decision.DENY, null)
        val approvalFlags = listOf(true, false)

        for (decision in decisions) {
            for (approved in approvalFlags) {
                // R4 requires BOTH an explicit granted approval AND a
                // governance decision that isn't an outright DENY — an
                // approval can never override a DENY, and the absence of
                // approval can never be excused by a nominal ALLOW.
                val shouldSucceed = approved && (decision == Decision.ALLOW || decision == Decision.APPROVAL_REQUIRED)
                if (shouldSucceed) {
                    val state = TransactionStateMachine.transition(
                        TransactionState.APPROVAL_PENDING,
                        TransactionState.READY,
                        governanceDecision = decision,
                        approvalGranted = approved,
                        riskTier = RiskTier.R4_SENSITIVE,
                    )
                    assertEquals(TransactionState.READY, state)
                } else {
                    assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
                        TransactionStateMachine.transition(
                            TransactionState.APPROVAL_PENDING,
                            TransactionState.READY,
                            governanceDecision = decision,
                            approvalGranted = approved,
                            riskTier = RiskTier.R4_SENSITIVE,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `terminal outcomes always transition to RECEIPTED and nowhere else`() {
        for (terminal in listOf(
            TransactionState.VERIFIED,
            TransactionState.PARTIAL,
            TransactionState.FAILED,
            TransactionState.CANCELLED,
        )) {
            val state = TransactionStateMachine.transition(terminal, TransactionState.RECEIPTED)
            assertEquals(TransactionState.RECEIPTED, state)
            assertThrows(TransactionStateMachine.IllegalTransitionException::class.java) {
                TransactionStateMachine.transition(TransactionState.RECEIPTED, TransactionState.DRAFT)
            }
        }
    }
}

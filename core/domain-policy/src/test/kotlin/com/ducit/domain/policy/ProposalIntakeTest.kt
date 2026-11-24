package com.ducit.domain.policy

import com.ducit.domain.model.*
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ProposalIntakeTest {
    private val proposal = UntrustedActionProposal(CapabilityDomain.CAPTURE, "local:notes", mapOf("text" to "Draft"), listOf("p1"), "Capture a reviewed note")
    private val fact = Proposition("p1", "person:user", "requested", "capture", EpistemicStatus.USER_CONFIRMED,
        null, listOf("source:user-confirmation"), false, 0L, null, 200L, SensitivityClass.STANDARD, listOf("notes"), 0L, 0L)
    private fun reasons(p: UntrustedActionProposal = proposal, facts: List<Proposition> = listOf(fact)) =
        ProposalIntake.rejectionReasons(p, CapabilityDomain.CAPTURE, "local:notes", setOf("text"), facts, "notes", 100L)

    @Test fun `accepts structurally scoped candidate without granting execution authority`() {
        assertTrue(reasons().isEmpty())
    }
    @Test fun `rejects target and capability expansion`() {
        assertTrue(reasons(proposal.copy(targetRef = "other:recipient")).contains("TARGET_MISMATCH"))
        assertTrue(reasons(proposal.copy(capability = CapabilityDomain.PAY)).contains("CAPABILITY_MISMATCH"))
    }
    @Test fun `rejects attempted authority or risk fields in parameters`() {
        assertTrue(reasons(proposal.copy(parameters = mapOf("authority" to "ALLOW", "riskTier" to "R0"))).contains("INVALID_PARAMETERS"))
    }
    @Test fun `rejects inferred disputed stale future and purpose restricted evidence`() {
        for (p in listOf(fact.copy(epistemicStatus = EpistemicStatus.INFERRED), fact.copy(epistemicStatus = EpistemicStatus.DISPUTED),
            fact.copy(freshnessUntil = 99L), fact.copy(validFrom = 101L), fact.copy(purposeAllowlist = listOf("other")))) {
            assertTrue(reasons(facts = listOf(p)).contains("INELIGIBLE_EVIDENCE"))
        }
    }
    @Test fun `rejects missing or ambiguous evidence and oversized payloads`() {
        assertFalse(reasons(facts = emptyList()).isEmpty())
        assertFalse(reasons(facts = listOf(fact, fact)).isEmpty())
        assertTrue(reasons(proposal.copy(parameters = mapOf("text" to "x".repeat(4001)))).contains("INVALID_PARAMETERS"))
    }
}

package com.ducit.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PropositionTest {

    private fun proposition(
        status: EpistemicStatus = EpistemicStatus.FACT,
        evidenceRefs: List<String> = listOf("evd-1"),
        validTo: Long? = null,
        freshnessUntil: Long? = null,
    ) = Proposition(
        propositionId = "prop-1",
        subjectRef = "person:user",
        predicate = "hasDeadline",
        objectValue = "2026-11-01",
        epistemicStatus = status,
        confidence = 0.9,
        evidenceRefs = evidenceRefs,
        validFrom = 0L,
        validTo = validTo,
        freshnessUntil = freshnessUntil,
        sensitivityClass = SensitivityClass.STANDARD,
        purposeAllowlist = listOf("education-deadline"),
        createdAt = 0L,
        updatedAt = 0L,
    )

    // INV-09: every durable proposition has provenance or is marked unknown.
    @Test
    fun `rejects known provenance with no evidence`() {
        assertThrows(IllegalArgumentException::class.java) {
            proposition(evidenceRefs = emptyList()).copy(provenanceUnknown = false)
        }
    }

    @Test
    fun `fact with evidence is actionable`() {
        assertTrue(proposition().isActionable(nowMillis = 100L))
    }

    // INV-01: inference never silently becomes fact — INFERRED cannot drive action.
    @Test
    fun `inferred proposition cannot drive action`() {
        assertFalse(proposition(status = EpistemicStatus.INFERRED).isActionable(nowMillis = 100L))
    }

    @Test
    fun `stale proposition cannot drive action past freshness window`() {
        val p = proposition(freshnessUntil = 50L)
        assertTrue(p.isActionable(nowMillis = 10L))
        assertFalse(p.isActionable(nowMillis = 51L))
    }

    @Test
    fun `rejects confidence outside 0 to 1`() {
        assertThrows(IllegalArgumentException::class.java) {
            proposition().copy(confidence = 1.5)
        }
    }
}

package com.ducit.domain.policy

import com.ducit.domain.model.AuthorityEnvelope
import com.ducit.domain.model.CapabilityDomain
import com.ducit.domain.model.Decision
import com.ducit.domain.model.RiskFacts
import com.ducit.domain.model.RiskTier
import com.ducit.domain.model.SensitivityClass
import com.ducit.domain.model.VerificationStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GovernancePolicyEngineTest {

    private val engine = GovernancePolicyEngine()
    private val now = 1_000_000L

    private fun facts(
        causesStateChange: Boolean = true,
        externalVisibility: Boolean = true,
        isReversible: Boolean = true,
        financialImpact: Boolean = false,
        confidence: Double = 0.9,
    ) = RiskFacts(
        sensitivityClass = SensitivityClass.STANDARD,
        causesStateChange = causesStateChange,
        isReversible = isReversible,
        externalVisibility = externalVisibility,
        financialImpact = financialImpact,
        healthEffect = false,
        legalEffect = false,
        identitySecurityEffect = false,
        affectsOtherPersons = false,
        confidence = confidence,
        connectorTrusted = true,
        verificationStrength = VerificationStrength.SOURCE_OF_RECORD,
        userPreauthorized = false,
    )

    private fun envelope(capability: CapabilityDomain, purposeId: String, maxTier: RiskTier) = AuthorityEnvelope(
        envelopeId = "env-1",
        principal = "user",
        capability = capability,
        connectorId = "calendar",
        targetRef = null,
        purposeId = purposeId,
        dataFields = setOf("event.title"),
        maxRiskTier = maxTier,
        grantedAt = now - 1000,
        expiresAt = now + 1000,
    )

    @Test
    fun `R0 observation always allows`() {
        val decision = engine.evaluate(
            actionId = "a1",
            decisionId = "d1",
            capability = CapabilityDomain.KNOW,
            purposeId = "answer",
            facts = facts(causesStateChange = false, externalVisibility = false),
            envelopes = emptyList(),
            nowMillis = now,
        )
        assertEquals(Decision.ALLOW, decision.decision)
    }

    @Test
    fun `R2 without standing permission requires approval`() {
        val decision = engine.evaluate(
            actionId = "a2",
            decisionId = "d2",
            capability = CapabilityDomain.COORDINATE,
            purposeId = "calendar-create",
            facts = facts(),
            envelopes = emptyList(),
            nowMillis = now,
        )
        assertEquals(Decision.APPROVAL_REQUIRED, decision.decision)
    }

    @Test
    fun `R2 with matching standing permission allows`() {
        val env = envelope(CapabilityDomain.COORDINATE, "calendar-create", RiskTier.R2_REVERSIBLE_EXTERNAL)
        val decision = engine.evaluate(
            actionId = "a3",
            decisionId = "d3",
            capability = CapabilityDomain.COORDINATE,
            purposeId = "calendar-create",
            facts = facts(),
            envelopes = listOf(env),
            nowMillis = now,
        )
        assertEquals(Decision.ALLOW, decision.decision)
        assertEquals(listOf("env-1"), decision.matchedEnvelopeIds)
    }

    // INV-04: R4 always requires explicit approval, even with a matching
    // standing envelope. "No R4 transaction reaches ALLOW silently."
    @Test
    fun `R4 never allows even with a matching envelope`() {
        val env = envelope(CapabilityDomain.PAY, "checkout", RiskTier.R4_SENSITIVE)
        val decision = engine.evaluate(
            actionId = "a4",
            decisionId = "d4",
            capability = CapabilityDomain.PAY,
            purposeId = "checkout",
            facts = facts(financialImpact = true),
            envelopes = listOf(env),
            nowMillis = now,
        )
        assertEquals(RiskTier.R4_SENSITIVE, decision.riskTier)
        assertEquals(Decision.APPROVAL_REQUIRED, decision.decision)
    }

    @Test
    fun `R5-equivalent low confidence denies regardless of tier`() {
        val decision = engine.evaluate(
            actionId = "a5",
            decisionId = "d5",
            capability = CapabilityDomain.CONNECT,
            purposeId = "send-message",
            facts = facts(causesStateChange = false, externalVisibility = false, confidence = 0.1),
            envelopes = emptyList(),
            nowMillis = now,
        )
        assertEquals(Decision.DENY, decision.decision)
        assertTrue(decision.reasonCodes.contains("LOW_CONFIDENCE"))
    }

    @Test
    fun `decisions requiring approval always set previewRequired`() {
        val decision = engine.evaluate(
            actionId = "a6",
            decisionId = "d6",
            capability = CapabilityDomain.ACT,
            purposeId = "reminder",
            facts = facts(),
            envelopes = emptyList(),
            nowMillis = now,
        )
        assertTrue(decision.previewRequired)
    }
}

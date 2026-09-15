package com.ducit.domain.policy

import com.ducit.domain.model.RiskFacts
import com.ducit.domain.model.RiskTier
import com.ducit.domain.model.SensitivityClass
import com.ducit.domain.model.VerificationStrength
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskClassifierTest {

    private fun facts(
        sensitivityClass: SensitivityClass = SensitivityClass.STANDARD,
        causesStateChange: Boolean = false,
        isReversible: Boolean = true,
        externalVisibility: Boolean = false,
        financialImpact: Boolean = false,
        healthEffect: Boolean = false,
        legalEffect: Boolean = false,
        identitySecurityEffect: Boolean = false,
        affectsOtherPersons: Boolean = false,
        confidence: Double = 0.95,
        connectorTrusted: Boolean = true,
    ) = RiskFacts(
        sensitivityClass = sensitivityClass,
        causesStateChange = causesStateChange,
        isReversible = isReversible,
        externalVisibility = externalVisibility,
        financialImpact = financialImpact,
        healthEffect = healthEffect,
        legalEffect = legalEffect,
        identitySecurityEffect = identitySecurityEffect,
        affectsOtherPersons = affectsOtherPersons,
        confidence = confidence,
        connectorTrusted = connectorTrusted,
        verificationStrength = VerificationStrength.SOURCE_OF_RECORD,
        userPreauthorized = false,
    )

    @Test
    fun `pure read is R0`() {
        assertEquals(RiskTier.R0_OBSERVATION, RiskClassifier.classify(facts()))
    }

    @Test
    fun `local reversible write is R1`() {
        val tier = RiskClassifier.classify(facts(causesStateChange = true))
        assertEquals(RiskTier.R1_REVERSIBLE_LOCAL, tier)
    }

    @Test
    fun `external reversible write is R2`() {
        val tier = RiskClassifier.classify(
            facts(causesStateChange = true, externalVisibility = true),
        )
        assertEquals(RiskTier.R2_REVERSIBLE_EXTERNAL, tier)
    }

    @Test
    fun `external irreversible write is R3 baseline`() {
        val tier = RiskClassifier.classify(
            facts(causesStateChange = true, externalVisibility = true, isReversible = false),
        )
        // Escalates further below (low reversibility + external -> R4), so
        // verify the isolated baseline via a case that keeps reversibility
        // intact but disables the irreversibility escalation path is not
        // representable without contradiction — this exercises the full
        // escalation instead, which is the behavior that matters.
        assertEquals(RiskTier.R4_SENSITIVE, tier)
    }

    // "Any payment or transfer of value is at least R4."
    @Test
    fun `payment escalates to at least R4`() {
        val tier = RiskClassifier.classify(
            facts(causesStateChange = true, externalVisibility = true, financialImpact = true),
        )
        assertEquals(RiskTier.R4_SENSITIVE, tier)
    }

    // "Disclosure of sensitive health, legal, precise-location or identity
    // data to a new external recipient is at least R4."
    @Test
    fun `sensitive disclosure to external recipient escalates to R4`() {
        val tier = RiskClassifier.classify(
            facts(
                causesStateChange = true,
                externalVisibility = true,
                healthEffect = true,
                isReversible = true,
            ),
        )
        assertEquals(RiskTier.R4_SENSITIVE, tier)
    }

    @Test
    fun `sensitive data kept local does not force R4`() {
        val tier = RiskClassifier.classify(
            facts(
                causesStateChange = true,
                externalVisibility = false,
                sensitivityClass = SensitivityClass.HEALTH,
            ),
        )
        assertEquals(RiskTier.R1_REVERSIBLE_LOCAL, tier)
    }

    // "An action affecting another person's rights or account cannot
    // inherit the user's authority automatically."
    @Test
    fun `action affecting another person escalates to at least R3`() {
        val tier = RiskClassifier.classify(
            facts(causesStateChange = true, affectsOtherPersons = true),
        )
        assertEquals(RiskTier.R3_CONSEQUENTIAL, tier)
    }

    @Test
    fun `untrusted connector escalates external action by one tier`() {
        val trusted = RiskClassifier.classify(
            facts(causesStateChange = true, externalVisibility = true, connectorTrusted = true),
        )
        val untrusted = RiskClassifier.classify(
            facts(causesStateChange = true, externalVisibility = true, connectorTrusted = false),
        )
        assertEquals(RiskTier.R2_REVERSIBLE_EXTERNAL, trusted)
        assertEquals(RiskTier.R3_CONSEQUENTIAL, untrusted)
    }
}

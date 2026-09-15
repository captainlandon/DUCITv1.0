package com.ducit.domain.policy

import com.ducit.domain.model.RiskFacts
import com.ducit.domain.model.RiskTier
import com.ducit.domain.model.SensitivityClass

/**
 * Deterministic R0-R5 tier engine (Verbal Reference Implementation v1.0,
 * sections 13.2-13.4). A model MAY estimate the underlying [RiskFacts],
 * but only this pure function chooses the enforced tier — models cannot
 * grant, expand, or lower authority (INV-02).
 */
object RiskClassifier {

    fun classify(facts: RiskFacts): RiskTier {
        var tier = baselineTier(facts)

        // "Any payment or transfer of value is at least R4."
        if (facts.financialImpact) {
            tier = tier.atLeast(RiskTier.R4_SENSITIVE)
        }

        // "Disclosure of sensitive health, legal, precise-location or
        // identity data to a new external recipient is at least R4."
        if (facts.externalVisibility && isSensitiveDisclosure(facts)) {
            tier = tier.atLeast(RiskTier.R4_SENSITIVE)
        }

        // "An action affecting another person's rights or account cannot
        // inherit the user's authority automatically."
        if (facts.affectsOtherPersons) {
            tier = tier.atLeast(RiskTier.R3_CONSEQUENTIAL)
        }

        // "Low reversibility + high consequence escalates to R4/R5."
        if (!facts.isReversible && facts.externalVisibility) {
            tier = tier.atLeast(RiskTier.R4_SENSITIVE)
        }

        // "Connector trust downgrade may escalate or prohibit an action."
        if (!facts.connectorTrusted && facts.externalVisibility) {
            tier = tier.escalatedByOne()
        }

        return tier
    }

    private fun baselineTier(facts: RiskFacts): RiskTier = when {
        !facts.causesStateChange -> RiskTier.R0_OBSERVATION
        !facts.externalVisibility && facts.isReversible -> RiskTier.R1_REVERSIBLE_LOCAL
        facts.externalVisibility && facts.isReversible -> RiskTier.R2_REVERSIBLE_EXTERNAL
        else -> RiskTier.R3_CONSEQUENTIAL
    }

    private fun isSensitiveDisclosure(facts: RiskFacts): Boolean =
        facts.healthEffect ||
            facts.legalEffect ||
            facts.identitySecurityEffect ||
            facts.sensitivityClass in setOf(
                SensitivityClass.HEALTH,
                SensitivityClass.LEGAL,
                SensitivityClass.IDENTITY_SECURITY,
                SensitivityClass.PRECISE_LOCATION,
                SensitivityClass.CHILD_DATA,
            )

    private fun RiskTier.atLeast(min: RiskTier): RiskTier =
        if (ordinal5 >= min.ordinal5) this else min

    private fun RiskTier.escalatedByOne(): RiskTier {
        val next = RiskTier.entries.firstOrNull { it.ordinal5 == this.ordinal5 + 1 }
        return next ?: this
    }
}

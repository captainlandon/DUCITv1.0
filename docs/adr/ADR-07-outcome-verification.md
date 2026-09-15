# ADR-07: What constitutes outcome verification for the first workflow?

**Status:** Open — blocked on ADR-01.

## Decision evidence needed

Observable postcondition, false-positive/negative cost, user confirmation
fallback (Dossier v1.0, section 12).

## Where this lands today

`core:domain-model`'s `VerificationResult`/`VerificationStatus` and
`core:domain-policy`'s state machine already enforce that a transaction
cannot claim `VERIFIED` without going through `VERIFYING` (Verbal
Reference Implementation v1.0 INV-05: execution success is not outcome
success). What counts as an *observable postcondition* is workflow-
specific and depends on ADR-01's answer.

# ADR-04: Model provider and on-device/cloud boundary?

**Status:** Open.

## Decision evidence needed

Latency, schema fidelity, privacy, offline behavior, cost, portability
(Dossier v1.0, section 12).

## Constraint already established (non-negotiable)

Verbal Reference Implementation v1.0, section 4.1: Gemini may be the
initial model adapter, but no business rule, schema, or permission
decision may depend on Gemini-specific output, and the model interface
should pass contract tests against a second provider or local model
before production. No `intelligence-gateway` module exists yet — it is
explicitly out of scope for Sprint 1 (the offline launcher and governance
layer ship with **no AI dependency at all**, by design).

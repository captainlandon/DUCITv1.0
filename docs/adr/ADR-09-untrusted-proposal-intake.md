# ADR-09: Provider-neutral proposal intake

Status: implemented as an isolated domain contract; model adapter and execution integration remain unbuilt.

A model can supply only UntrustedActionProposal: capability, target, parameters, evidence references and rationale. ProposalIntake screens this against host-owned capability/target/key allowlists and current, purpose-eligible, provenanced propositions. A successful screening is not ALLOW and must never be used as an execution signal.

The host must create IDs, idempotency keys, postconditions and trusted risk facts; resolve connector/target/data authority; call the existing deterministic policy engine; and follow the approval and transaction state machine before any adapter runs. Unknown JSON fields must be rejected by the future adapter. Risk flags and claimed permissions in generated text are never trusted inputs.

This contract does not implement a provider, authorize cloud data transfer, choose an execution adapter, fix every existing envelope-axis check, or establish device/process-death readiness. Those remain prerequisite work in ADR-01/04/07. Test with `./gradlew :core:domain-model:test :core:domain-policy:test`; device evidence requires real hardware.

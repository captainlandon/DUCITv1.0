# ADR-03: Trust Receipt integrity mechanism for v0.1?

**Status:** Open.

## Decision evidence needed

Threat model; whether append-only hashes add meaningful assurance locally
(Dossier v1.0, section 12).

## Where this lands today

`core:domain-model`'s `TrustReceipt` has `previousReceiptHash` /
`receiptHash` fields (Verbal Reference Implementation v1.0, Appendix A)
but no hashing or chaining implementation exists yet — this ships with
Sprint/Phase "Verification, Trust Receipts and Outcome Semantics." The
deletion + meta-receipt policy (Inner Workings & Data Flow Architecture
v1.1, section 9.4) is also undecided at the implementation level.

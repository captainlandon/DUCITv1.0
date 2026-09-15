# ADR-02: Living User Model — proposition store only or entity graph projection?

**Status:** Open — founder/technical-lead decision.

## Decision evidence needed

Query needs, correction propagation, performance, migration cost (Dossier
v1.0, section 12).

## Where this lands today

`core:domain-model`'s `Proposition` and `core:data-local`'s
`PersonalContextRecordEntity` currently implement the flat, typed
proposition store only (Verbal Reference Implementation v1.0, section 6).
No entity-graph projection (People/Places/Documents/... hub edges, per
Inner Workings & Data Flow Architecture v1.1, section 11) exists yet. This
ADR decides whether that graph is a read-time projection over the
proposition store or a materialized structure with its own tables.

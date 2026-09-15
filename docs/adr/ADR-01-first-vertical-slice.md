# ADR-01: Exact first vertical-slice use case?

**Status:** Open — founder decision.

## Decision evidence needed

Frequency, consequence, connector feasibility, verifiability, and value on
the founder's S25 (Dossier v1.0, section 12).

## Candidates on the table (Dossier v1.0, section 13)

| Workflow | Real action | Verification quality | Risk | Recommended |
|---|---|---|---|---|
| Capture shared content → retrieve → create calendar draft | Android share + Calendar insert intent | High | R2/R3 | **Best first slice** |
| Prepare for next meeting | Calendar + local records + note output | Medium | R2 | Second slice |
| Route/navigation suggestion | Maps deep link | Medium | R1/R2 | Good connector test |
| Message/email send | ACTION_SEND / API | High side effect, variable verification | R4 | Too consequential for first slice |
| Payment/booking | Closed APIs / web / GUI | High but brittle | R4/R5 | Explicitly defer |

## Decision

_Not yet made. This scaffold (Sprint 1) intentionally does not require this
decision — it proves the offline launcher and the governance/domain layer
first. This ADR blocks Sprint 2 (Weeks 3-4, "Governed memory") and Sprint 3
(Weeks 5-6, "Intent and capability core")._

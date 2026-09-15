# ADR-06: When does AppFunctions become a production provider?

**Status:** Open.

## Decision evidence needed

Stable API level, caller privileges, installed-app coverage, fallback
performance (Dossier v1.0, section 12).

## Where this lands today

The execution resolver hierarchy (Dossier v1.0, section 5:
"1. Direct Android intent/deep link → 2. AppFunction when available and
callable → 3. first-party/service API → 4. MCP/server tool →
5. user-mediated handoff") is not implemented yet. Sprint 1's launcher
uses only tier 1 (`PackageManager` + `Intent.ACTION_MAIN`/
`CATEGORY_LAUNCHER`) — the simplest, most stable rung, and deliberately
so: this ADR should not be resolved before tier 1 is proven.

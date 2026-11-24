# AI proposal boundary implementation

ADR-09 adds a provider-neutral candidate type and isolated intake screening. A model cannot submit authority envelopes, governance decisions, trusted risk facts or execution handles through this contract. Host-owned allowlists constrain capability, target and parameter keys; proposition checks reject missing, ambiguous, inferred, disputed, expired/future, provenance-unknown and purpose-ineligible evidence. Screening success never authorizes execution.

No provider or intelligence gateway is installed, and Sprint 1's offline/no-AI architecture remains intact. Existing deterministic policy, approvals, transactions and postconditions must still govern any future host-created ActionRequest. The future JSON adapter must reject unknown fields; that adapter is not implemented here. Device/process-death recovery and final target/data-axis enforcement remain open prerequisites, not inferred from structural screening.

The normal Gradle-wrapper test command was attempted and blocked downloading the distribution through Java networking. A checksummed Gradle distribution was obtained through the available download route, and its Kotlin 2.0.21 compiler compiled both pure domain modules and all their JUnit tests. JUnit 4.13.2 ran 42 tests successfully, including five new intake tests. This is isolated JVM evidence, not a project Gradle, Android, emulator or physical-device pass. The existing GitHub domain CI remains the authoritative project-toolchain check.

See docs/adr/ADR-09-untrusted-proposal-intake.md and docs/AI_CHANGE_EVIDENCE.md. No execution adapter, cloud transfer, new authority or product integration is enabled.

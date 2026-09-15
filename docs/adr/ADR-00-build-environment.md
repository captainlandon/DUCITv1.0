# ADR-00: Build environment constraints for this repo

**Status:** Recorded (not a founder decision — an environment fact worth
keeping so the next session doesn't re-discover it the hard way).

## Context

This scaffold was authored in a sandboxed session with:

- OpenJDK 21 and Gradle 8.14.3 available locally.
- Outbound network access proxied and allow-listed by host. Maven Central
  (`repo1.maven.org`) is reachable. **Google's Maven repository
  (`dl.google.com`) is not** — every request through the proxy returns
  `403`.

## Consequence

- `core:domain-model` and `core:domain-policy` are pure Kotlin/JVM modules
  with zero Android/AndroidX dependencies. They were fully compiled and
  unit-tested in this session (29 tests, all green — see
  `core/domain-policy/src/test` for the governance invariant tests).
- `core:data-local` (Room) and `:app` (Jetpack Compose) depend on the
  Android Gradle Plugin and AndroidX artifacts, which live on
  `dl.google.com`. **They were written and reviewed but could not be
  compiled or tested in this session.** CI (`.github/workflows/ci.yml`,
  job `android-build`) builds them against a real Android SDK with normal
  internet access — that is the first real compiler check they get.

## What this means for the next session or reviewer

1. Do not trust `:app` or `:core:data-local` to build on first push. Check
   the `android-build` CI job before merging anything that touches them.
2. If a future sandboxed session hits the same `dl.google.com` 403, this
   is expected — it is a proxy allow-list gap, not a project
   misconfiguration. Don't spend time debugging the Gradle files first.
3. Gradle's configuration-on-demand (`org.gradle.configureondemand=true`
   in `gradle.properties`) is load-bearing here: it's what lets
   `:core:domain-model:test` run without Gradle trying to resolve the
   Android plugin classpath for sibling modules. Don't remove it without
   checking that JVM-only tasks still work in a restricted environment.

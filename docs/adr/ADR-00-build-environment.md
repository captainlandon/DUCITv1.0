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

## Update: CI's `android-build` job (diagnosed after this ADR was written)

The first three CI runs on this branch all showed `android-build` failing
at a step called "Set up Android SDK" — **before Gradle ever ran**, so it
was not a signal about `:app`/`:core:data-local` compiling. The cause:
`android-actions/setup-android@v3` calls `sdkmanager tools`, and Google
removed the legacy `tools` package from the SDK repository years ago, so
that call exits 1 unconditionally. This is a broken third-party action,
not a project problem.

Fix applied: dropped `android-actions/setup-android@v3` entirely.
`ubuntu-latest` GitHub runners ship with an Android SDK preinstalled
(`ANDROID_HOME` already set) — the job now just accepts licenses and
installs the exact `platforms;android-35` / `build-tools;35.0.0` this
project's `compileSdk` needs, directly via `sdkmanager`.

**This means `domain-tests` passing on all runs so far is the only real
signal collected to date.** The `android-build` job's Gradle steps
(`assembleDebug`, `:core:data-local:test`, `lintDebug`) have still never
actually executed — check the next CI run after this fix lands before
trusting that `:app`/`:core:data-local` compile.

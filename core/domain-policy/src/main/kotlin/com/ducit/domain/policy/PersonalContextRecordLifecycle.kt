package com.ducit.domain.policy

import com.ducit.domain.model.CorrectionState
import com.ducit.domain.model.DeletionState
import com.ducit.domain.model.EpistemicStatus
import com.ducit.domain.model.PersonalContextRecord

/**
 * Pure lifecycle transitions for a [PersonalContextRecord]: capture,
 * confirm, correct, dispute, restrict-purpose, delete
 * (Intelligence-to-Implementation Dossier v1.0, section 6 "Memory
 * inspector" release criterion: "Confirm, correct, remove, restrict-
 * purpose actions work and propagate"; Verbal Reference Implementation
 * v1.0, section 17.2 "Correction propagation").
 *
 * Every function here is a pure data transformation — id and clock
 * values are supplied by the caller so this stays JVM-testable without
 * Android, matching `core:domain-policy`'s existing governance engines.
 * The Android-facing repository in `:app` calls these and persists the
 * result; it does not reimplement the rules.
 */
object PersonalContextRecordLifecycle {

    /** A brand-new, user-asserted record. Manual entry is the user's own
     * proposition about themselves, so it starts USER_CONFIRMED, not
     * INFERRED (INV-01: inference never silently becomes fact — this
     * sidesteps the question entirely by never being an inference). */
    fun capture(
        recordId: String,
        entityId: String,
        subject: String,
        predicate: String,
        value: String,
        sourceType: String = "manual-entry",
        sensitivityClass: com.ducit.domain.model.SensitivityClass,
        purposeAllowlist: List<String>,
        retentionPolicy: com.ducit.domain.model.RetentionClass,
        nowMillis: Long,
    ): PersonalContextRecord = PersonalContextRecord(
        recordId = recordId,
        entityId = entityId,
        subject = subject,
        predicate = predicate,
        value = value,
        status = EpistemicStatus.USER_CONFIRMED,
        confidence = null,
        sourceRefs = listOf("manual-entry:$recordId"),
        sourceType = sourceType,
        capturedAt = nowMillis,
        lastVerifiedAt = nowMillis,
        validFrom = nowMillis,
        freshnessPolicy = retentionPolicy,
        sensitivityClass = sensitivityClass,
        purposeAllowlist = purposeAllowlist,
        retentionPolicy = retentionPolicy,
        correctionState = CorrectionState.USER_CONFIRMED,
        explanationLabel = "Because you entered it directly.",
    )

    data class CorrectionResult(
        /** The prior record, now superseded — never mutated in place, so
         * the original evidence is preserved rather than falsified. */
        val supersededRecord: PersonalContextRecord,
        val newRecord: PersonalContextRecord,
    )

    /**
     * "When a user corrects a proposition, dependent inferences are
     * marked for reevaluation. The original evidence is not falsified;
     * the system records that the user disputes or supersedes the
     * inference." The old record is kept (with `supersededBy` set) rather
     * than deleted or overwritten in place.
     */
    fun correct(
        existing: PersonalContextRecord,
        newRecordId: String,
        newValue: String,
        nowMillis: Long,
    ): CorrectionResult {
        require(existing.isRetrievable()) {
            "cannot correct a record that is not currently retrievable (id=${existing.recordId})"
        }
        val newRecord = existing.copy(
            recordId = newRecordId,
            value = newValue,
            status = EpistemicStatus.USER_CONFIRMED,
            confidence = null,
            sourceRefs = listOf("user-correction:${existing.recordId}"),
            capturedAt = nowMillis,
            lastVerifiedAt = nowMillis,
            validFrom = nowMillis,
            validTo = null,
            supersedes = existing.recordId,
            supersededBy = null,
            correctionState = CorrectionState.USER_CONFIRMED,
            deletionState = DeletionState.ACTIVE,
            explanationLabel = "Because you corrected the previous value.",
        )
        val supersededRecord = existing.copy(
            correctionState = CorrectionState.USER_CORRECTED,
            supersededBy = newRecordId,
        )
        return CorrectionResult(supersededRecord, newRecord)
    }

    /** The user confirms an existing record is still right, without
     * changing its value — refreshes [PersonalContextRecord.lastVerifiedAt]
     * so it can't be treated as STALE. */
    fun confirm(existing: PersonalContextRecord, nowMillis: Long): PersonalContextRecord {
        require(existing.isRetrievable()) {
            "cannot confirm a record that is not currently retrievable (id=${existing.recordId})"
        }
        return existing.copy(
            status = EpistemicStatus.USER_CONFIRMED,
            correctionState = CorrectionState.USER_CONFIRMED,
            lastVerifiedAt = nowMillis,
        )
    }

    /** The user flags a record as wrong without yet supplying a
     * replacement value. DISPUTED propositions cannot drive action
     * (see [PersonalContextRecord.isActionable]'s underlying
     * [EpistemicStatus.DISPUTED]). */
    fun dispute(existing: PersonalContextRecord, nowMillis: Long): PersonalContextRecord {
        require(existing.isRetrievable()) {
            "cannot dispute a record that is not currently retrievable (id=${existing.recordId})"
        }
        return existing.copy(
            status = EpistemicStatus.DISPUTED,
            correctionState = CorrectionState.USER_DISPUTED,
            lastVerifiedAt = nowMillis,
        )
    }

    /** Restrict-purpose can only narrow the existing allowlist — a record
     * the user has scoped down can never be silently re-widened by this
     * path (INV-08: collection must be purpose-bound and minimized). */
    fun restrictPurpose(
        existing: PersonalContextRecord,
        newPurposeAllowlist: Set<String>,
    ): PersonalContextRecord {
        require(existing.purposeAllowlist.toSet().containsAll(newPurposeAllowlist)) {
            "restrict-purpose can only narrow the existing allowlist " +
                "(existing=${existing.purposeAllowlist}, requested=$newPurposeAllowlist), never widen it"
        }
        return existing.copy(purposeAllowlist = newPurposeAllowlist.toList())
    }

    /** Real deletion (INV-11), not a soft "hidden" flag with data still
     * live everywhere: this also clears the purpose allowlist so a
     * deleted record can never be matched as an active authority scope
     * by [com.ducit.domain.model.AuthorityEnvelope.covers] or similar
     * purpose-bound lookups that don't already filter on deletionState. */
    fun delete(existing: PersonalContextRecord): PersonalContextRecord =
        existing.copy(deletionState = DeletionState.DELETED, purposeAllowlist = emptyList())
}

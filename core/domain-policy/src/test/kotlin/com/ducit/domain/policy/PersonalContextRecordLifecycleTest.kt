package com.ducit.domain.policy

import com.ducit.domain.model.CorrectionState
import com.ducit.domain.model.DeletionState
import com.ducit.domain.model.EpistemicStatus
import com.ducit.domain.model.PersonalContextRecord
import com.ducit.domain.model.RetentionClass
import com.ducit.domain.model.SensitivityClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalContextRecordLifecycleTest {

    private fun captured(nowMillis: Long = 1_000L): PersonalContextRecord =
        PersonalContextRecordLifecycle.capture(
            recordId = "rec-1",
            entityId = "person:user",
            subject = "user",
            predicate = "hasAllergy",
            value = "peanuts",
            sensitivityClass = SensitivityClass.HEALTH,
            purposeAllowlist = listOf("meal-planning", "medical-context"),
            retentionPolicy = RetentionClass.DURABLE_USER_MODEL,
            nowMillis = nowMillis,
        )

    @Test
    fun `capture produces a retrievable, user-confirmed, provenance-bearing record`() {
        val record = captured()
        assertTrue(record.isRetrievable())
        assertEquals(EpistemicStatus.USER_CONFIRMED, record.status)
        assertFalse(record.provenanceUnknown)
        assertTrue(record.sourceRefs.isNotEmpty())
    }

    @Test
    fun `correct preserves the old record as superseded rather than mutating it away`() {
        val original = captured(nowMillis = 1_000L)
        val result = PersonalContextRecordLifecycle.correct(
            existing = original,
            newRecordId = "rec-2",
            newValue = "peanuts and shellfish",
            nowMillis = 2_000L,
        )

        // Old evidence is not falsified — it still exists, just superseded.
        assertEquals("rec-1", result.supersededRecord.recordId)
        assertEquals("peanuts", result.supersededRecord.value)
        assertEquals(CorrectionState.USER_CORRECTED, result.supersededRecord.correctionState)
        assertEquals("rec-2", result.supersededRecord.supersededBy)
        assertFalse(result.supersededRecord.isRetrievable())

        // New record carries the correction and points back at what it replaced.
        assertEquals("peanuts and shellfish", result.newRecord.value)
        assertEquals("rec-1", result.newRecord.supersedes)
        assertTrue(result.newRecord.isRetrievable())
        assertEquals(EpistemicStatus.USER_CONFIRMED, result.newRecord.status)
    }

    @Test
    fun `correcting an already-deleted record is rejected`() {
        val deleted = PersonalContextRecordLifecycle.delete(captured())
        assertThrows(IllegalArgumentException::class.java) {
            PersonalContextRecordLifecycle.correct(deleted, "rec-2", "new value", 2_000L)
        }
    }

    @Test
    fun `confirm refreshes lastVerifiedAt without changing the value`() {
        val original = captured(nowMillis = 1_000L)
        val confirmed = PersonalContextRecordLifecycle.confirm(original, nowMillis = 5_000L)
        assertEquals(original.value, confirmed.value)
        assertEquals(5_000L, confirmed.lastVerifiedAt)
        assertEquals(CorrectionState.USER_CONFIRMED, confirmed.correctionState)
    }

    @Test
    fun `dispute marks the record DISPUTED so it can no longer drive action`() {
        val disputed = PersonalContextRecordLifecycle.dispute(captured(), nowMillis = 3_000L)
        assertEquals(EpistemicStatus.DISPUTED, disputed.status)
        assertFalse(disputed.status.canDriveAction)
    }

    @Test
    fun `restrictPurpose narrows the allowlist`() {
        val restricted = PersonalContextRecordLifecycle.restrictPurpose(
            captured(),
            newPurposeAllowlist = setOf("medical-context"),
        )
        assertEquals(listOf("medical-context"), restricted.purposeAllowlist)
    }

    @Test
    fun `restrictPurpose rejects widening the allowlist`() {
        assertThrows(IllegalArgumentException::class.java) {
            PersonalContextRecordLifecycle.restrictPurpose(
                captured(),
                newPurposeAllowlist = setOf("meal-planning", "medical-context", "marketing"),
            )
        }
    }

    @Test
    fun `delete is real - the record becomes unretrievable and loses purpose scope`() {
        val deleted = PersonalContextRecordLifecycle.delete(captured())
        assertEquals(DeletionState.DELETED, deleted.deletionState)
        assertFalse(deleted.isRetrievable())
        assertTrue(deleted.purposeAllowlist.isEmpty())
    }
}

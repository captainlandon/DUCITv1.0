package com.ducit.launcher.data

import com.ducit.data.local.dao.PersonalContextRecordDao
import com.ducit.data.local.entity.toDomain
import com.ducit.data.local.entity.toEntity
import com.ducit.domain.model.PersonalContextRecord
import com.ducit.domain.model.RetentionClass
import com.ducit.domain.model.SensitivityClass
import com.ducit.domain.policy.PersonalContextRecordLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * The Android-facing half of the Memory Inspector (Intelligence-to-
 * Implementation Dossier v1.0, section 6). This class owns *nothing*
 * about what a correct capture/correction/deletion looks like — all of
 * that lives in the pure, JVM-tested
 * [com.ducit.domain.policy.PersonalContextRecordLifecycle]. Its only job
 * is id/clock plumbing and persistence: generate an id, ask the lifecycle
 * for the resulting record(s), write them through the DAO.
 */
class PersonalContextRecordRepository(
    private val dao: PersonalContextRecordDao,
    private val newId: () -> String = { UUID.randomUUID().toString() },
    private val now: () -> Long = { System.currentTimeMillis() },
) {

    fun observeRecords(): Flow<List<PersonalContextRecord>> =
        dao.observeRetrievable().map { entities -> entities.map { it.toDomain() } }

    suspend fun capture(
        entityId: String,
        subject: String,
        predicate: String,
        value: String,
        sensitivityClass: SensitivityClass,
        purposeAllowlist: List<String>,
        retentionPolicy: RetentionClass = RetentionClass.DURABLE_USER_MODEL,
    ): PersonalContextRecord = withContext(Dispatchers.IO) {
        val record = PersonalContextRecordLifecycle.capture(
            recordId = newId(),
            entityId = entityId,
            subject = subject,
            predicate = predicate,
            value = value,
            sensitivityClass = sensitivityClass,
            purposeAllowlist = purposeAllowlist,
            retentionPolicy = retentionPolicy,
            nowMillis = now(),
        )
        dao.upsert(record.toEntity())
        record
    }

    suspend fun confirm(recordId: String) = withContext(Dispatchers.IO) {
        val existing = dao.get(recordId)?.toDomain() ?: return@withContext
        dao.update(PersonalContextRecordLifecycle.confirm(existing, now()).toEntity())
    }

    suspend fun dispute(recordId: String) = withContext(Dispatchers.IO) {
        val existing = dao.get(recordId)?.toDomain() ?: return@withContext
        dao.update(PersonalContextRecordLifecycle.dispute(existing, now()).toEntity())
    }

    /** Correction propagation (Verbal Reference Implementation v1.0,
     * section 17.2): the old record is kept, marked superseded, and a new
     * record carries the corrected value — never an in-place overwrite. */
    suspend fun correct(recordId: String, newValue: String) = withContext(Dispatchers.IO) {
        val existing = dao.get(recordId)?.toDomain() ?: return@withContext
        val result = PersonalContextRecordLifecycle.correct(
            existing = existing,
            newRecordId = newId(),
            newValue = newValue,
            nowMillis = now(),
        )
        dao.update(result.supersededRecord.toEntity())
        dao.upsert(result.newRecord.toEntity())
    }

    suspend fun restrictPurpose(recordId: String, newPurposeAllowlist: Set<String>) = withContext(Dispatchers.IO) {
        val existing = dao.get(recordId)?.toDomain() ?: return@withContext
        dao.update(PersonalContextRecordLifecycle.restrictPurpose(existing, newPurposeAllowlist).toEntity())
    }

    /** Real deletion (INV-11) — not a filtered-out flag the data still
     * lives behind. [dao.markDeleted] is the same primitive
     * [com.ducit.domain.policy.PersonalContextRecordLifecycle.delete]
     * describes; both exist so a bulk/administrative delete path doesn't
     * need to round-trip a full entity read first. */
    suspend fun delete(recordId: String) = withContext(Dispatchers.IO) {
        dao.markDeleted(recordId)
    }
}

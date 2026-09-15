package com.ducit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducit.domain.model.CorrectionState
import com.ducit.domain.model.DeletionState
import com.ducit.domain.model.EpistemicStatus
import com.ducit.domain.model.PersonalContextRecord
import com.ducit.domain.model.RetentionClass
import com.ducit.domain.model.SensitivityClass

/**
 * Room-backed storage for [PersonalContextRecord]. Field-for-field mirror
 * of the domain type (Intelligence-to-Implementation Dossier v1.0,
 * section 5) with enums stored by name and lists flattened via
 * [com.ducit.data.local.Converters] — Room entities never leak into the
 * domain/policy layers directly, only through the `toDomain()`/`toEntity()`
 * mappers below, so the pure-Kotlin modules stay Android-free.
 */
@Entity(tableName = "personal_context_records")
data class PersonalContextRecordEntity(
    @PrimaryKey val recordId: String,
    val entityId: String,
    val subject: String,
    val predicate: String,
    val value: String,
    val valueRef: String?,

    val status: String,
    val confidence: Double?,

    val sourceRefs: List<String>,
    val sourceType: String,
    val capturedAt: Long,
    val lastVerifiedAt: Long?,
    val provenanceUnknown: Boolean,

    val validFrom: Long,
    val validTo: Long?,
    val freshnessPolicy: String,
    val supersedes: String?,
    val supersededBy: String?,

    val sensitivityClass: String,
    val purposeAllowlist: List<String>,
    val retentionPolicy: String,
    val correctionState: String,
    val deletionState: String,

    val lastUsedAt: Long?,
    val usedForPlanIds: List<String>,
    val explanationLabel: String,
)

fun PersonalContextRecordEntity.toDomain() = PersonalContextRecord(
    recordId = recordId,
    entityId = entityId,
    subject = subject,
    predicate = predicate,
    value = value,
    valueRef = valueRef,
    status = EpistemicStatus.valueOf(status),
    confidence = confidence,
    sourceRefs = sourceRefs,
    sourceType = sourceType,
    capturedAt = capturedAt,
    lastVerifiedAt = lastVerifiedAt,
    provenanceUnknown = provenanceUnknown,
    validFrom = validFrom,
    validTo = validTo,
    freshnessPolicy = RetentionClass.valueOf(freshnessPolicy),
    supersedes = supersedes,
    supersededBy = supersededBy,
    sensitivityClass = SensitivityClass.valueOf(sensitivityClass),
    purposeAllowlist = purposeAllowlist,
    retentionPolicy = RetentionClass.valueOf(retentionPolicy),
    correctionState = CorrectionState.valueOf(correctionState),
    deletionState = DeletionState.valueOf(deletionState),
    lastUsedAt = lastUsedAt,
    usedForPlanIds = usedForPlanIds,
    explanationLabel = explanationLabel,
)

fun PersonalContextRecord.toEntity() = PersonalContextRecordEntity(
    recordId = recordId,
    entityId = entityId,
    subject = subject,
    predicate = predicate,
    value = value,
    valueRef = valueRef,
    status = status.name,
    confidence = confidence,
    sourceRefs = sourceRefs,
    sourceType = sourceType,
    capturedAt = capturedAt,
    lastVerifiedAt = lastVerifiedAt,
    provenanceUnknown = provenanceUnknown,
    validFrom = validFrom,
    validTo = validTo,
    freshnessPolicy = freshnessPolicy.name,
    supersedes = supersedes,
    supersededBy = supersededBy,
    sensitivityClass = sensitivityClass.name,
    purposeAllowlist = purposeAllowlist,
    retentionPolicy = retentionPolicy.name,
    correctionState = correctionState.name,
    deletionState = deletionState.name,
    lastUsedAt = lastUsedAt,
    usedForPlanIds = usedForPlanIds,
    explanationLabel = explanationLabel,
)

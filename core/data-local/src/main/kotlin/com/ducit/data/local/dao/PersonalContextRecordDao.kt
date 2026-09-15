package com.ducit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ducit.data.local.entity.PersonalContextRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalContextRecordDao {

    /** Only records that are active and not superseded are retrievable —
     * mirrors [com.ducit.domain.model.PersonalContextRecord.isRetrievable]
     * at the query level so a deleted/superseded record can never leak
     * into a plan by accident. */
    @Query(
        "SELECT * FROM personal_context_records " +
            "WHERE deletionState = 'ACTIVE' AND supersededBy IS NULL " +
            "ORDER BY capturedAt DESC",
    )
    fun observeRetrievable(): Flow<List<PersonalContextRecordEntity>>

    @Query("SELECT * FROM personal_context_records WHERE recordId = :recordId")
    suspend fun get(recordId: String): PersonalContextRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: PersonalContextRecordEntity)

    @Update
    suspend fun update(record: PersonalContextRecordEntity)

    /** Correction propagation (Verbal Reference Implementation v1.0,
     * section 17.2): supersede the prior record rather than mutating it
     * away, preserving the audit trail. */
    @Query(
        "UPDATE personal_context_records SET supersededBy = :newRecordId " +
            "WHERE recordId = :oldRecordId",
    )
    suspend fun markSuperseded(oldRecordId: String, newRecordId: String)

    /** Deletion is real, not cosmetic (INV-11): this marks the row
     * DELETED, which the observeRetrievable() query already excludes. A
     * later retention sweep is responsible for physically purging rows
     * past their retention window. */
    @Query(
        "UPDATE personal_context_records SET deletionState = 'DELETED' " +
            "WHERE recordId = :recordId",
    )
    suspend fun markDeleted(recordId: String)
}

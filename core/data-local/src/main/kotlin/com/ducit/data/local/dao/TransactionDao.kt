package com.ducit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducit.data.local.entity.TransactionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    suspend fun get(transactionId: String): TransactionRecordEntity?

    /** Non-terminal transactions left over from a process death — the
     * recovery entry point for B-003 (process-death reconciliation). */
    @Query("SELECT * FROM transactions WHERE state NOT IN ('RECEIPTED', 'CANCELLED')")
    suspend fun getUnresolved(): List<TransactionRecordEntity>

    @Query("SELECT * FROM transactions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TransactionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: TransactionRecordEntity)
}

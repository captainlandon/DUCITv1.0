package com.ducit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Durable transaction/UI state row (Verbal Reference Implementation v1.0,
 * section 12.3 and Intelligence-to-Implementation Dossier v1.0 backlog
 * item B-003: "process death at every state restores or safely
 * reconciles"). Persists just enough of the transaction to resume or
 * safely reconcile it after process death — not the full plan graph,
 * which belongs to a later phase's orchestration schema.
 */
@Entity(tableName = "transactions")
data class TransactionRecordEntity(
    @PrimaryKey val transactionId: String,
    val capability: String,
    val state: String,
    val riskTier: String?,
    val governanceDecision: String?,
    val approvalGranted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val receiptId: String?,
)

package com.ducit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ducit.data.local.dao.InstalledAppDao
import com.ducit.data.local.dao.PersonalContextRecordDao
import com.ducit.data.local.dao.TransactionDao
import com.ducit.data.local.entity.InstalledAppEntity
import com.ducit.data.local.entity.PersonalContextRecordEntity
import com.ducit.data.local.entity.TransactionRecordEntity

/**
 * The v0.1 local database. Schema version starts at 1 — every future
 * change ships a Room migration rather than `fallbackToDestructiveMigration`,
 * since destroying a user's PersonalContextRecords on an app update would
 * violate the memory-governance invariants this schema exists to serve.
 */
@Database(
    entities = [
        InstalledAppEntity::class,
        PersonalContextRecordEntity::class,
        TransactionRecordEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DucitDatabase : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun personalContextRecordDao(): PersonalContextRecordDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "ducit.db"
    }
}

package com.ducit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducit.data.local.entity.InstalledAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledAppDao {

    @Query("SELECT * FROM installed_apps ORDER BY label ASC")
    fun observeAll(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE label LIKE '%' || :query || '%' ORDER BY label ASC")
    fun search(query: String): Flow<List<InstalledAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<InstalledAppEntity>)

    @Query("DELETE FROM installed_apps WHERE packageName NOT IN (:keepPackageNames)")
    suspend fun pruneNotIn(keepPackageNames: List<String>)

    @Query("DELETE FROM installed_apps")
    suspend fun clear()
}

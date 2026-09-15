package com.ducit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducit.domain.model.InstalledApp

/**
 * Cache of PackageManager-discovered launchable apps, backing the offline
 * app-grid fallback (Intelligence-to-Implementation Dossier v1.0, section
 * 6). Refreshed from PackageManager on demand; never the source of truth —
 * PackageManager is — so this table can always be safely rebuilt.
 */
@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
)

fun InstalledAppEntity.toDomain() = InstalledApp(
    packageName = packageName,
    label = label,
    isSystemApp = isSystemApp,
)

fun InstalledApp.toEntity() = InstalledAppEntity(
    packageName = packageName,
    label = label,
    isSystemApp = isSystemApp,
)

package com.ducit.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.ducit.data.local.dao.InstalledAppDao
import com.ducit.data.local.entity.toDomain
import com.ducit.data.local.entity.toEntity
import com.ducit.domain.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Offline app discovery and launch (Intelligence-to-Implementation
 * Dossier v1.0, section 6: "Launch installed apps in airplane mode with
 * all AI disabled"). Reads directly from [PackageManager] — Room is a
 * cache for fast, offline-safe listing, never the source of truth, so a
 * stale cache can never cause a launch to silently fail.
 */
class InstalledAppRepository(
    private val context: Context,
    private val dao: InstalledAppDao,
) {

    /** Cached, alphabetically-ordered app list for the grid. */
    fun observeApps(): Flow<List<InstalledApp>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun search(query: String): Flow<List<InstalledApp>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }

    /** Re-queries PackageManager and reconciles the cache. Safe to call
     * with no network and no AI — this is pure platform introspection. */
    suspend fun refresh() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        @Suppress("DEPRECATION")
        val resolved = pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)

        val apps = resolved
            .mapNotNull { info ->
                val packageName = info.activityInfo?.applicationInfo?.packageName ?: return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.ifBlank { packageName } ?: packageName
                val isSystemApp = (info.activityInfo?.applicationInfo?.flags ?: 0) and
                    ApplicationInfo.FLAG_SYSTEM != 0
                InstalledApp(packageName = packageName, label = label, isSystemApp = isSystemApp)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

        dao.upsertAll(apps.map { it.toEntity() })
        dao.pruneNotIn(apps.map { it.packageName })
    }

    /** Returns false (visibly, never silently) if the target can no longer
     * be launched — dossier release criterion: "invalid/uninstalled target
     * fails visibly." */
    fun launch(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: android.content.ActivityNotFoundException) {
            false
        }
    }
}

package com.ducit.launcher

import android.app.Application
import androidx.room.Room
import com.ducit.data.local.DucitDatabase
import com.ducit.launcher.data.InstalledAppRepository
import com.ducit.launcher.data.PersonalContextRecordRepository

/**
 * Owns the process-lifetime singletons: the Room database and the
 * repositories built on top of it. No dependency-injection framework yet —
 * deliberately, so this module stays legible while the domain/data
 * boundaries are still settling (see docs/adr/ADR-02).
 */
class DucitApplication : Application() {

    lateinit var database: DucitDatabase
        private set

    lateinit var installedAppRepository: InstalledAppRepository
        private set

    lateinit var personalContextRecordRepository: PersonalContextRecordRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, DucitDatabase::class.java, DucitDatabase.DATABASE_NAME)
            .build()
        installedAppRepository = InstalledAppRepository(
            context = applicationContext,
            dao = database.installedAppDao(),
        )
        personalContextRecordRepository = PersonalContextRecordRepository(
            dao = database.personalContextRecordDao(),
        )
    }
}

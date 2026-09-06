package com.zmstore.projectr.data.local

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zmstore.projectr.data.model.DoseHistory
import com.zmstore.projectr.data.model.Medication

import com.zmstore.projectr.data.model.Profile
import com.zmstore.projectr.data.model.HealthEntry
import com.zmstore.projectr.data.model.CaregiverLink

@Database(
    entities = [Medication::class, DoseHistory::class, Profile::class, HealthEntry::class, CaregiverLink::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "remedio_certo_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Insert default profile on creation
                        val contentValues = android.content.ContentValues().apply {
                            put("name", "Meu Perfil")
                            put("color", 0xFF008080.toInt())
                            put("isDefault", 1)
                            put("ownerId", "legacy")
                        }
                        db.insert("profiles", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, contentValues)
                    }
                })
                .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safer approach with try-catch for development iterations
                try {
                    db.execSQL("ALTER TABLE medications ADD COLUMN imageUrl TEXT")
                } catch (e: SQLiteException) {
                    if (e.message?.contains("duplicate column name", ignoreCase = true) != true) {
                        throw e
                    }
                }
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN ownerId TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("ALTER TABLE medications ADD COLUMN treatmentStartDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medications ADD COLUMN treatmentEndDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medications ADD COLUMN officialSourceUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE medications ADD COLUMN officialSourceLabel TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE dose_history ADD COLUMN ownerId TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("ALTER TABLE dose_history ADD COLUMN status TEXT NOT NULL DEFAULT 'TAKEN'")
                db.execSQL("ALTER TABLE dose_history ADD COLUMN scheduledTimestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE profiles ADD COLUMN ownerId TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("CREATE TABLE IF NOT EXISTS health_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ownerId TEXT NOT NULL, profileId INTEGER NOT NULL, type TEXT NOT NULL, primaryValue TEXT NOT NULL, secondaryValue TEXT NOT NULL, note TEXT NOT NULL, timestamp INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS caregiver_links (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ownerId TEXT NOT NULL, caregiverUid TEXT NOT NULL, caregiverName TEXT NOT NULL, notifyMissedDoses INTEGER NOT NULL, isActive INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medications_ownerId ON medications(ownerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_dose_history_ownerId ON dose_history(ownerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_profiles_ownerId ON profiles(ownerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_health_entries_ownerId ON health_entries(ownerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_caregiver_links_ownerId ON caregiver_links(ownerId)")
            }
        }
    }
}

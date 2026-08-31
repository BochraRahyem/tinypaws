package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LogEntry::class, FavoriteDiy::class, StrayReport::class, CatProfile::class, CatWeightLog::class, CatCheckInLog::class, Reminder::class, DailyCareLog::class, CatHistoryEntry::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun strayReportDao(): StrayReportDao
    abstract fun catProfileDao(): CatProfileDao
    abstract fun catWeightLogDao(): CatWeightLogDao
    abstract fun catCheckInLogDao(): CatCheckInLogDao
    abstract fun reminderDao(): ReminderDao
    abstract fun dailyCareLogDao(): DailyCareLogDao
    abstract fun catHistoryEntryDao(): CatHistoryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** True when the given table already has a column with that name. */
        private fun columnExists(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
            db.query("PRAGMA table_info(`$table`)").use { cursor ->
                val nameIdx = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIdx).equals(column, ignoreCase = true)) return true
                }
            }
            return false
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Idempotent ALTERs: check column existence explicitly instead of
                // swallowing every exception (a real failure must fail loudly so
                // Room never opens a schema-diverged database).
                if (!columnExists(db, "cat_weight_logs", "catId")) {
                    db.execSQL("ALTER TABLE cat_weight_logs ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                }
                if (!columnExists(db, "cat_history_entries", "catId")) {
                    db.execSQL("ALTER TABLE cat_history_entries ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                }
                if (!columnExists(db, "cat_check_in_logs", "catId")) {
                    db.execSQL("ALTER TABLE cat_check_in_logs ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                }
                if (!columnExists(db, "reminders", "catId")) {
                    db.execSQL("ALTER TABLE reminders ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                }
                if (!columnExists(db, "reminders", "catIds")) {
                    db.execSQL("ALTER TABLE reminders ADD COLUMN catIds TEXT NOT NULL DEFAULT ''")
                }
                // Composite-key rebuild for daily_care_logs (idempotent via IF NOT EXISTS).
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_care_logs_new (catId INTEGER NOT NULL DEFAULT 1, dateString TEXT NOT NULL, fed INTEGER NOT NULL DEFAULT 0, watered INTEGER NOT NULL DEFAULT 0, played INTEGER NOT NULL DEFAULT 0, litterCleaned INTEGER NOT NULL DEFAULT 0, groomed INTEGER NOT NULL DEFAULT 0, medicationGiven INTEGER NOT NULL DEFAULT 0, lastUpdated INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(catId, dateString))"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO daily_care_logs_new (catId, dateString, fed, watered, played, litterCleaned, groomed, medicationGiven, lastUpdated) SELECT 1, dateString, fed, watered, played, litterCleaned, groomed, medicationGiven, lastUpdated FROM daily_care_logs"
                )
                db.execSQL("DROP TABLE daily_care_logs")
                db.execSQL("ALTER TABLE daily_care_logs_new RENAME TO daily_care_logs")
            }
        }

        /**
         * Adds indices for every hot filter/sort column (per-cat queries,
         * reminder ordering, activity feeds). Names follow Room's default
         * `index_<table>_<column>` convention and match the @Entity index
         * declarations introduced in this version.
         */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cat_weight_logs_catId` ON `cat_weight_logs` (`catId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cat_check_in_logs_catId` ON `cat_check_in_logs` (`catId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cat_history_entries_catId` ON `cat_history_entries` (`catId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_catId` ON `reminders` (`catId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_timeMillis` ON `reminders` (`timeMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_care_logs_dateString` ON `daily_care_logs` (`dateString`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_log_entries_timestamp` ON `log_entries` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stray_reports_timestamp` ON `stray_reports` (`timestamp`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinypaws_database"
                )
                .addMigrations(MIGRATION_10_11, MIGRATION_11_12)
                // Versions 1-9 predate the current multi-cat schema; their table
                // shapes are no longer reconstructable reliably, so upgrades from
                // those ancient versions rebuild fresh. Documented limitation.
                .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

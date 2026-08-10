package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LogEntry::class, FavoriteDiy::class, StrayReport::class, CatProfile::class, CatWeightLog::class, CatCheckInLog::class, Reminder::class, DailyCareLog::class, CatHistoryEntry::class], version = 11, exportSchema = false)
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

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE cat_weight_logs ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                } catch (e: Exception) { /* Column might already exist */ }

                try {
                    db.execSQL("ALTER TABLE cat_history_entries ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                } catch (e: Exception) { /* Column might already exist */ }

                try {
                    db.execSQL("ALTER TABLE cat_check_in_logs ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                } catch (e: Exception) { /* Column might already exist */ }

                try {
                    db.execSQL("ALTER TABLE reminders ADD COLUMN catId INTEGER NOT NULL DEFAULT 1")
                } catch (e: Exception) { /* Column might already exist */ }

                try {
                    db.execSQL("ALTER TABLE reminders ADD COLUMN catIds TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) { /* Column might already exist */ }

                try {
                    db.execSQL("CREATE TABLE IF NOT EXISTS daily_care_logs_new (catId INTEGER NOT NULL DEFAULT 1, dateString TEXT NOT NULL, fed INTEGER NOT NULL DEFAULT 0, watered INTEGER NOT NULL DEFAULT 0, played INTEGER NOT NULL DEFAULT 0, litterCleaned INTEGER NOT NULL DEFAULT 0, groomed INTEGER NOT NULL DEFAULT 0, medicationGiven INTEGER NOT NULL DEFAULT 0, lastUpdated INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(catId, dateString))")
                    db.execSQL("INSERT OR IGNORE INTO daily_care_logs_new (catId, dateString, fed, watered, played, litterCleaned, groomed, medicationGiven, lastUpdated) SELECT 1, dateString, fed, watered, played, litterCleaned, groomed, medicationGiven, lastUpdated FROM daily_care_logs")
                    db.execSQL("DROP TABLE daily_care_logs")
                    db.execSQL("ALTER TABLE daily_care_logs_new RENAME TO daily_care_logs")
                } catch (e: Exception) { /* Fallback */ }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinypaws_database"
                )
                .addMigrations(MIGRATION_10_11)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.healthtracker.offline.data.database

import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration strategies for the Offline Doctor Tracker application.
 * 
 * This class contains all migration paths between different database versions.
 * Currently at version 1, but prepared for future schema changes.
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 1 to version 2 (placeholder for future use)
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Future migration logic will be added here
            // Example:
            // database.execSQL("ALTER TABLE doctors ADD COLUMN email TEXT")
        }
    }
    
    /**
     * Migration from version 2 to version 3 (placeholder for future use)
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Future migration logic will be added here
        }
    }
    
    /**
     * Gets all available migrations
     * @return Array of Migration objects
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // MIGRATION_1_2,
            // MIGRATION_2_3
            // Add migrations as they are implemented
        )
    }
    
    /**
     * Database initialization callback for setting up initial data or constraints
     */
    val DATABASE_CALLBACK = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Add any initial data setup here if needed
            // For example, inserting default specialities or common ward names
        }
        
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON")
        }
    }
}
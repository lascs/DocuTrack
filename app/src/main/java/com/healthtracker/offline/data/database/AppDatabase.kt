package com.healthtracker.offline.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.entities.Institution
import com.healthtracker.offline.data.entities.Ward
import com.healthtracker.offline.data.entities.DoctorInstitution
import com.healthtracker.offline.data.models.SavedSearchFilter
import com.healthtracker.offline.data.dao.DoctorDao
import com.healthtracker.offline.data.dao.InstitutionDao
import com.healthtracker.offline.data.dao.WardDao
import com.healthtracker.offline.data.dao.DoctorInstitutionDao
import com.healthtracker.offline.data.dao.SavedSearchFilterDao
import com.healthtracker.offline.data.converters.StringListConverter
import com.healthtracker.offline.data.converters.DutyShiftConverter
import com.healthtracker.offline.data.converters.SearchFilterTypeConverter

/**
 * Room database for the Offline Doctor Tracker application.
 * 
 * This database contains all entities and provides access to DAOs.
 * It's configured with proper type converters and migration strategies.
 */
@Database(
    entities = [
        Doctor::class,
        Institution::class,
        Ward::class,
        DoctorInstitution::class,
        SavedSearchFilter::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    DutyShiftConverter::class,
    SearchFilterTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    
    // Abstract DAO methods
    abstract fun doctorDao(): DoctorDao
    abstract fun institutionDao(): InstitutionDao
    abstract fun wardDao(): WardDao
    abstract fun doctorInstitutionDao(): DoctorInstitutionDao
    abstract fun savedSearchFilterDao(): SavedSearchFilterDao
    
    companion object {
        /**
         * Database name
         */
        const val DATABASE_NAME = "offline_doctor_tracker_db"
        
        /**
         * Singleton instance of the database
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Gets the singleton instance of the database
         * @param context Application context
         * @return AppDatabase instance
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Creates database instance for dependency injection
         * @param context Application context
         * @return AppDatabase instance
         */
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
    }
}
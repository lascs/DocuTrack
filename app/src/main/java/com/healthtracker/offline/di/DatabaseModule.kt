package com.healthtracker.offline.di

import android.content.Context
import androidx.room.Room
import com.healthtracker.offline.data.database.AppDatabase
import com.healthtracker.offline.data.database.DatabaseMigrations
import com.healthtracker.offline.data.dao.DoctorDao
import com.healthtracker.offline.data.dao.InstitutionDao
import com.healthtracker.offline.data.dao.WardDao
import com.healthtracker.offline.data.dao.DoctorInstitutionDao
import com.healthtracker.offline.data.dao.SavedSearchFilterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 * 
 * This module provides the Room database instance and all DAOs
 * as singletons throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the Room database instance
     * @param context Application context
     * @return AppDatabase singleton instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .addCallback(DatabaseMigrations.DATABASE_CALLBACK)
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }
    
    /**
     * Provides DoctorDao
     * @param database AppDatabase instance
     * @return DoctorDao
     */
    @Provides
    fun provideDoctorDao(database: AppDatabase): DoctorDao {
        return database.doctorDao()
    }
    
    /**
     * Provides InstitutionDao
     * @param database AppDatabase instance
     * @return InstitutionDao
     */
    @Provides
    fun provideInstitutionDao(database: AppDatabase): InstitutionDao {
        return database.institutionDao()
    }
    
    /**
     * Provides WardDao
     * @param database AppDatabase instance
     * @return WardDao
     */
    @Provides
    fun provideWardDao(database: AppDatabase): WardDao {
        return database.wardDao()
    }
    
    /**
     * Provides DoctorInstitutionDao
     * @param database AppDatabase instance
     * @return DoctorInstitutionDao
     */
    @Provides
    fun provideDoctorInstitutionDao(database: AppDatabase): DoctorInstitutionDao {
        return database.doctorInstitutionDao()
    }
    
    /**
     * Provides SavedSearchFilterDao
     * @param database AppDatabase instance
     * @return SavedSearchFilterDao
     */
    @Provides
    fun provideSavedSearchFilterDao(database: AppDatabase): SavedSearchFilterDao {
        return database.savedSearchFilterDao()
    }
}
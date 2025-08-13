package com.healthtracker.offline.di

import com.healthtracker.offline.data.repository.DoctorRepository
import com.healthtracker.offline.data.repository.DoctorRepositoryImpl
import com.healthtracker.offline.data.repository.InstitutionRepository
import com.healthtracker.offline.data.repository.InstitutionRepositoryImpl
import com.healthtracker.offline.data.repository.SearchRepository
import com.healthtracker.offline.data.repository.SearchRepositoryImpl
import com.healthtracker.offline.data.repository.ExportImportRepository
import com.healthtracker.offline.data.repository.ExportImportRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 * 
 * This module binds repository interfaces to their concrete implementations,
 * ensuring proper dependency injection throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds DoctorRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindDoctorRepository(
        doctorRepositoryImpl: DoctorRepositoryImpl
    ): DoctorRepository
    
    /**
     * Binds InstitutionRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindInstitutionRepository(
        institutionRepositoryImpl: InstitutionRepositoryImpl
    ): InstitutionRepository
    
    /**
     * Binds SearchRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository
    
    /**
     * Binds ExportImportRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindExportImportRepository(
        exportImportRepositoryImpl: ExportImportRepositoryImpl
    ): ExportImportRepository
}
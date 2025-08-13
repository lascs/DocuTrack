package com.healthtracker.offline.data.repository

import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Doctor
import com.healthtracker.offline.data.dao.DoctorWithAssignmentCount

/**
 * Repository interface for Doctor entity operations.
 * 
 * Provides a clean API for doctor-related data operations,
 * abstracting the underlying data source implementation.
 */
interface DoctorRepository {
    
    // ========== CREATE ==========
    
    /**
     * Adds a new doctor to the repository
     * @param doctor Doctor entity to add
     * @return Result containing the doctor ID if successful, or error
     */
    suspend fun addDoctor(doctor: Doctor): Result<Long>
    
    /**
     * Adds multiple doctors to the repository
     * @param doctors List of doctors to add
     * @return Result containing list of doctor IDs if successful, or error
     */
    suspend fun addDoctors(doctors: List<Doctor>): Result<List<Long>>
    
    // ========== READ ==========
    
    /**
     * Gets all doctors as a reactive Flow
     * @return Flow of list of all doctors
     */
    fun getAllDoctors(): Flow<List<Doctor>>
    
    /**
     * Gets a doctor by ID
     * @param doctorId Doctor ID to search for
     * @return Result containing doctor if found, or error
     */
    suspend fun getDoctorById(doctorId: Int): Result<Doctor>
    
    /**
     * Gets a doctor by PMDC number
     * @param pmdcNumber PMDC number to search for
     * @return Result containing doctor if found, or error
     */
    suspend fun getDoctorByPmdcNumber(pmdcNumber: String): Result<Doctor>
    
    /**
     * Searches doctors by name
     * @param searchQuery Search term for doctor name
     * @return Flow of matching doctors
     */
    fun searchDoctorsByName(searchQuery: String): Flow<List<Doctor>>
    
    /**
     * Gets doctors filtered by speciality
     * @param speciality Medical speciality to filter by
     * @return Flow of doctors with matching speciality
     */
    fun getDoctorsBySpeciality(speciality: String): Flow<List<Doctor>>
    
    /**
     * Gets all unique specialities
     * @return Flow of list of unique specialities
     */
    fun getAllSpecialities(): Flow<List<String>>
    
    /**
     * Searches doctors with multiple criteria
     * @param nameQuery Name search term (optional)
     * @param speciality Speciality filter (optional)
     * @return Flow of matching doctors
     */
    fun searchDoctors(nameQuery: String? = null, speciality: String? = null): Flow<List<Doctor>>
    
    /**
     * Gets doctors with their assignment count
     * @return Flow of doctors with assignment counts
     */
    fun getDoctorsWithAssignmentCount(): Flow<List<DoctorWithAssignmentCount>>
    
    /**
     * Gets total count of doctors
     * @return Total number of doctors
     */
    suspend fun getDoctorCount(): Int
    
    // ========== UPDATE ==========
    
    /**
     * Updates an existing doctor
     * @param doctor Doctor entity with updated information
     * @return Result indicating success or error
     */
    suspend fun updateDoctor(doctor: Doctor): Result<Unit>
    
    // ========== DELETE ==========
    
    /**
     * Deletes a doctor from the repository
     * @param doctor Doctor entity to delete
     * @return Result indicating success or error
     */
    suspend fun deleteDoctor(doctor: Doctor): Result<Unit>
    
    /**
     * Deletes a doctor by ID
     * @param doctorId ID of doctor to delete
     * @return Result indicating success or error
     */
    suspend fun deleteDoctorById(doctorId: Int): Result<Unit>
    
    /**
     * Checks if a doctor can be safely deleted (no active assignments)
     * @param doctorId Doctor ID to check
     * @return Result containing true if safe to delete, false otherwise
     */
    suspend fun canDeleteDoctor(doctorId: Int): Result<Boolean>
    
    // ========== VALIDATION ==========
    
    /**
     * Validates doctor data before saving
     * @param doctor Doctor entity to validate
     * @return Result indicating validation success or specific errors
     */
    suspend fun validateDoctor(doctor: Doctor): Result<Unit>
    
    /**
     * Checks if PMDC number is unique
     * @param pmdcNumber PMDC number to check
     * @param excludeDoctorId Doctor ID to exclude from check (for updates)
     * @return Result containing true if unique, false if already exists
     */
    suspend fun isPmdcNumberUnique(pmdcNumber: String, excludeDoctorId: Int? = null): Result<Boolean>
}
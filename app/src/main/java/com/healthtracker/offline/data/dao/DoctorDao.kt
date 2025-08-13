package com.healthtracker.offline.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Doctor

/**
 * Data Access Object for Doctor entity.
 * 
 * Provides methods for CRUD operations and search queries on doctors.
 * All suspend functions are designed to be called from coroutines.
 */
@Dao
interface DoctorDao {
    
    // ========== CREATE ==========
    
    /**
     * Inserts a new doctor into the database
     * @param doctor Doctor entity to insert
     * @return The row ID of the inserted doctor
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDoctor(doctor: Doctor): Long
    
    /**
     * Inserts multiple doctors into the database
     * @param doctors List of doctors to insert
     * @return List of row IDs of inserted doctors
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDoctors(doctors: List<Doctor>): List<Long>
    
    // ========== READ ==========
    
    /**
     * Gets all doctors as a Flow for reactive updates
     * @return Flow of list of all doctors
     */
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<Doctor>>
    
    /**
     * Gets all doctors as a one-time list
     * @return List of all doctors
     */
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    suspend fun getAllDoctorsList(): List<Doctor>
    
    /**
     * Gets a doctor by ID
     * @param doctorId The doctor's ID
     * @return Doctor entity or null if not found
     */
    @Query("SELECT * FROM doctors WHERE doctorId = :doctorId")
    suspend fun getDoctorById(doctorId: Int): Doctor?
    
    /**
     * Gets a doctor by PMDC number
     * @param pmdcNumber The doctor's PMDC number
     * @return Doctor entity or null if not found
     */
    @Query("SELECT * FROM doctors WHERE pmdcNumber = :pmdcNumber")
    suspend fun getDoctorByPmdcNumber(pmdcNumber: String): Doctor?
    
    /**
     * Searches doctors by name (case-insensitive)
     * @param searchQuery Search term for doctor name
     * @return Flow of matching doctors
     */
    @Query("SELECT * FROM doctors WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchDoctorsByName(searchQuery: String): Flow<List<Doctor>>
    
    /**
     * Filters doctors by speciality
     * @param speciality Medical speciality to filter by
     * @return Flow of doctors with matching speciality
     */
    @Query("SELECT * FROM doctors WHERE speciality = :speciality ORDER BY name ASC")
    fun getDoctorsBySpeciality(speciality: String): Flow<List<Doctor>>
    
    /**
     * Gets all unique specialities from doctors
     * @return Flow of list of unique specialities
     */
    @Query("SELECT DISTINCT speciality FROM doctors WHERE speciality != '' ORDER BY speciality ASC")
    fun getAllSpecialities(): Flow<List<String>>
    
    /**
     * Searches doctors by multiple criteria
     * @param nameQuery Search term for name (use '%' for wildcard)
     * @param speciality Speciality filter (use '%' for any)
     * @return Flow of matching doctors
     */
    @Query("""
        SELECT * FROM doctors 
        WHERE (:nameQuery = '%' OR name LIKE :nameQuery)
        AND (:speciality = '%' OR speciality = :speciality)
        ORDER BY name ASC
    """)
    fun searchDoctors(nameQuery: String, speciality: String): Flow<List<Doctor>>
    
    /**
     * Gets count of all doctors
     * @return Total number of doctors
     */
    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun getDoctorCount(): Int
    
    // ========== UPDATE ==========
    
    /**
     * Updates an existing doctor
     * @param doctor Doctor entity with updated information
     * @return Number of rows affected (should be 1 if successful)
     */
    @Update
    suspend fun updateDoctor(doctor: Doctor): Int
    
    /**
     * Updates multiple doctors
     * @param doctors List of doctors to update
     * @return Number of rows affected
     */
    @Update
    suspend fun updateDoctors(doctors: List<Doctor>): Int
    
    // ========== DELETE ==========
    
    /**
     * Deletes a doctor from the database
     * @param doctor Doctor entity to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Delete
    suspend fun deleteDoctor(doctor: Doctor): Int
    
    /**
     * Deletes a doctor by ID
     * @param doctorId ID of the doctor to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Query("DELETE FROM doctors WHERE doctorId = :doctorId")
    suspend fun deleteDoctorById(doctorId: Int): Int
    
    /**
     * Deletes all doctors from the database
     * @return Number of rows affected
     */
    @Query("DELETE FROM doctors")
    suspend fun deleteAllDoctors(): Int
    
    // ========== COMPLEX QUERIES ==========
    
    /**
     * Gets doctors with their assignment count
     * @return Flow of doctors with assignment counts
     */
    @Query("""
        SELECT d.*, COUNT(di.assignmentId) as assignmentCount
        FROM doctors d
        LEFT JOIN doctor_institutions di ON d.doctorId = di.doctorId
        GROUP BY d.doctorId
        ORDER BY d.name ASC
    """)
    fun getDoctorsWithAssignmentCount(): Flow<List<DoctorWithAssignmentCount>>
    
    /**
     * Checks if a PMDC number already exists (for validation)
     * @param pmdcNumber PMDC number to check
     * @param excludeDoctorId Doctor ID to exclude from check (for updates)
     * @return True if PMDC number exists for a different doctor
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM doctors 
        WHERE pmdcNumber = :pmdcNumber 
        AND (:excludeDoctorId IS NULL OR doctorId != :excludeDoctorId)
    """)
    suspend fun isPmdcNumberExists(pmdcNumber: String, excludeDoctorId: Int? = null): Boolean
}

/**
 * Data class for doctor with assignment count
 */
data class DoctorWithAssignmentCount(
    val doctorId: Int,
    val name: String,
    val speciality: String,
    val pmdcNumber: String,
    val mobileNumber: String,
    val qualifications: List<String>,
    val assignmentCount: Int
)
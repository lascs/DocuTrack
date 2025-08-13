package com.healthtracker.offline.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.DoctorInstitution
import com.healthtracker.offline.data.entities.DutyShift

/**
 * Data Access Object for DoctorInstitution entity.
 * 
 * Provides methods for CRUD operations and complex queries on doctor-institution assignments.
 * All suspend functions are designed to be called from coroutines.
 */
@Dao
interface DoctorInstitutionDao {
    
    // ========== CREATE ==========
    
    /**
     * Inserts a new doctor-institution assignment
     * @param assignment DoctorInstitution entity to insert
     * @return The row ID of the inserted assignment
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignment(assignment: DoctorInstitution): Long
    
    /**
     * Inserts multiple assignments
     * @param assignments List of assignments to insert
     * @return List of row IDs of inserted assignments
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignments(assignments: List<DoctorInstitution>): List<Long>
    
    // ========== READ ==========
    
    /**
     * Gets all assignments as a Flow for reactive updates
     * @return Flow of list of all assignments
     */
    @Query("SELECT * FROM doctor_institutions ORDER BY assignmentId ASC")
    fun getAllAssignments(): Flow<List<DoctorInstitution>>
    
    /**
     * Gets all assignments as a one-time list
     * @return List of all assignments
     */
    @Query("SELECT * FROM doctor_institutions ORDER BY assignmentId ASC")
    suspend fun getAllAssignmentsList(): List<DoctorInstitution>
    
    /**
     * Gets an assignment by ID
     * @param assignmentId The assignment's ID
     * @return DoctorInstitution entity or null if not found
     */
    @Query("SELECT * FROM doctor_institutions WHERE assignmentId = :assignmentId")
    suspend fun getAssignmentById(assignmentId: Int): DoctorInstitution?
    
    /**
     * Gets all assignments for a specific doctor
     * @param doctorId Doctor ID to filter by
     * @return Flow of assignments for the specified doctor
     */
    @Query("SELECT * FROM doctor_institutions WHERE doctorId = :doctorId ORDER BY assignmentId ASC")
    fun getAssignmentsByDoctor(doctorId: Int): Flow<List<DoctorInstitution>>
    
    /**
     * Gets all assignments for a specific institution
     * @param institutionId Institution ID to filter by
     * @return Flow of assignments for the specified institution
     */
    @Query("SELECT * FROM doctor_institutions WHERE institutionId = :institutionId ORDER BY assignmentId ASC")
    fun getAssignmentsByInstitution(institutionId: Int): Flow<List<DoctorInstitution>>
    
    /**
     * Gets all assignments for a specific ward
     * @param wardId Ward ID to filter by
     * @return Flow of assignments for the specified ward
     */
    @Query("SELECT * FROM doctor_institutions WHERE wardId = :wardId ORDER BY assignmentId ASC")
    fun getAssignmentsByWard(wardId: Int): Flow<List<DoctorInstitution>>
    
    /**
     * Gets assignments by duty shift
     * @param dutyShift Duty shift to filter by
     * @return Flow of assignments with the specified duty shift
     */
    @Query("SELECT * FROM doctor_institutions WHERE dutyShift = :dutyShift ORDER BY assignmentId ASC")
    fun getAssignmentsByDutyShift(dutyShift: DutyShift): Flow<List<DoctorInstitution>>
    
    /**
     * Gets assignments for doctors on duty on a specific day
     * @param day Day of the week
     * @return Flow of assignments for doctors on duty on the specified day
     */
    @Query("SELECT * FROM doctor_institutions WHERE dutyDays LIKE '%' || :day || '%' ORDER BY assignmentId ASC")
    fun getAssignmentsByDutyDay(day: String): Flow<List<DoctorInstitution>>
    
    /**
     * Gets count of assignments for a specific doctor
     * @param doctorId Doctor ID
     * @return Number of assignments for the doctor
     */
    @Query("SELECT COUNT(*) FROM doctor_institutions WHERE doctorId = :doctorId")
    suspend fun getAssignmentCountByDoctor(doctorId: Int): Int
    
    /**
     * Gets count of assignments for a specific institution
     * @param institutionId Institution ID
     * @return Number of assignments for the institution
     */
    @Query("SELECT COUNT(*) FROM doctor_institutions WHERE institutionId = :institutionId")
    suspend fun getAssignmentCountByInstitution(institutionId: Int): Int
    
    /**
     * Gets count of all assignments
     * @return Total number of assignments
     */
    @Query("SELECT COUNT(*) FROM doctor_institutions")
    suspend fun getAssignmentCount(): Int
    
    // ========== UPDATE ==========
    
    /**
     * Updates an existing assignment
     * @param assignment DoctorInstitution entity with updated information
     * @return Number of rows affected (should be 1 if successful)
     */
    @Update
    suspend fun updateAssignment(assignment: DoctorInstitution): Int
    
    /**
     * Updates multiple assignments
     * @param assignments List of assignments to update
     * @return Number of rows affected
     */
    @Update
    suspend fun updateAssignments(assignments: List<DoctorInstitution>): Int
    
    // ========== DELETE ==========
    
    /**
     * Deletes an assignment from the database
     * @param assignment DoctorInstitution entity to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Delete
    suspend fun deleteAssignment(assignment: DoctorInstitution): Int
    
    /**
     * Deletes an assignment by ID
     * @param assignmentId ID of the assignment to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Query("DELETE FROM doctor_institutions WHERE assignmentId = :assignmentId")
    suspend fun deleteAssignmentById(assignmentId: Int): Int
    
    /**
     * Deletes all assignments for a specific doctor
     * @param doctorId Doctor ID
     * @return Number of rows affected
     */
    @Query("DELETE FROM doctor_institutions WHERE doctorId = :doctorId")
    suspend fun deleteAssignmentsByDoctor(doctorId: Int): Int
    
    /**
     * Deletes all assignments for a specific institution
     * @param institutionId Institution ID
     * @return Number of rows affected
     */
    @Query("DELETE FROM doctor_institutions WHERE institutionId = :institutionId")
    suspend fun deleteAssignmentsByInstitution(institutionId: Int): Int
    
    /**
     * Deletes all assignments for a specific ward
     * @param wardId Ward ID
     * @return Number of rows affected
     */
    @Query("DELETE FROM doctor_institutions WHERE wardId = :wardId")
    suspend fun deleteAssignmentsByWard(wardId: Int): Int
    
    /**
     * Deletes all assignments from the database
     * @return Number of rows affected
     */
    @Query("DELETE FROM doctor_institutions")
    suspend fun deleteAllAssignments(): Int
    
    // ========== COMPLEX QUERIES ==========
    
    /**
     * Gets assignments with doctor and institution details
     * @return Flow of assignments with complete information
     */
    @Query("""
        SELECT di.*, d.name as doctorName, d.speciality, d.pmdcNumber,
               i.name as institutionName, i.areaBrick,
               w.wardName
        FROM doctor_institutions di
        INNER JOIN doctors d ON di.doctorId = d.doctorId
        INNER JOIN institutions i ON di.institutionId = i.institutionId
        LEFT JOIN wards w ON di.wardId = w.wardId
        ORDER BY d.name ASC, i.name ASC
    """)
    fun getAssignmentsWithDetails(): Flow<List<AssignmentWithDetails>>
    
    /**
     * Gets assignments for a specific doctor with institution details
     * @param doctorId Doctor ID
     * @return Flow of assignments with institution details
     */
    @Query("""
        SELECT di.*, i.name as institutionName, i.areaBrick, w.wardName
        FROM doctor_institutions di
        INNER JOIN institutions i ON di.institutionId = i.institutionId
        LEFT JOIN wards w ON di.wardId = w.wardId
        WHERE di.doctorId = :doctorId
        ORDER BY i.name ASC
    """)
    fun getDoctorAssignmentsWithDetails(doctorId: Int): Flow<List<DoctorAssignmentWithDetails>>
    
    /**
     * Gets assignments for a specific institution with doctor details
     * @param institutionId Institution ID
     * @return Flow of assignments with doctor details
     */
    @Query("""
        SELECT di.*, d.name as doctorName, d.speciality, d.pmdcNumber, w.wardName
        FROM doctor_institutions di
        INNER JOIN doctors d ON di.doctorId = d.doctorId
        LEFT JOIN wards w ON di.wardId = w.wardId
        WHERE di.institutionId = :institutionId
        ORDER BY d.name ASC
    """)
    fun getInstitutionAssignmentsWithDetails(institutionId: Int): Flow<List<InstitutionAssignmentWithDetails>>
    
    /**
     * Checks if a specific doctor-institution-ward combination already exists
     * @param doctorId Doctor ID
     * @param institutionId Institution ID
     * @param wardId Ward ID (nullable)
     * @param excludeAssignmentId Assignment ID to exclude from check (for updates)
     * @return True if combination already exists
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM doctor_institutions 
        WHERE doctorId = :doctorId 
        AND institutionId = :institutionId 
        AND (:wardId IS NULL OR wardId = :wardId)
        AND (:excludeAssignmentId IS NULL OR assignmentId != :excludeAssignmentId)
    """)
    suspend fun isAssignmentExists(
        doctorId: Int, 
        institutionId: Int, 
        wardId: Int?, 
        excludeAssignmentId: Int? = null
    ): Boolean
    
    /**
     * Advanced search for assignments with multiple criteria
     * @param doctorName Doctor name filter (use '%' for any)
     * @param speciality Speciality filter (use '%' for any)
     * @param institutionName Institution name filter (use '%' for any)
     * @param areaBrick Area brick filter (use '%' for any)
     * @param dutyShift Duty shift filter (use null for any)
     * @param dutyDay Duty day filter (use empty string for any)
     * @return Flow of matching assignments with details
     */
    @Query("""
        SELECT di.*, d.name as doctorName, d.speciality, d.pmdcNumber,
               i.name as institutionName, i.areaBrick, w.wardName
        FROM doctor_institutions di
        INNER JOIN doctors d ON di.doctorId = d.doctorId
        INNER JOIN institutions i ON di.institutionId = i.institutionId
        LEFT JOIN wards w ON di.wardId = w.wardId
        WHERE (:doctorName = '%' OR d.name LIKE :doctorName)
        AND (:speciality = '%' OR d.speciality = :speciality)
        AND (:institutionName = '%' OR i.name LIKE :institutionName)
        AND (:areaBrick = '%' OR i.areaBrick = :areaBrick)
        AND (:dutyShift IS NULL OR di.dutyShift = :dutyShift)
        AND (:dutyDay = '' OR di.dutyDays LIKE '%' || :dutyDay || '%')
        ORDER BY d.name ASC, i.name ASC
    """)
    fun searchAssignments(
        doctorName: String,
        speciality: String,
        institutionName: String,
        areaBrick: String,
        dutyShift: DutyShift?,
        dutyDay: String
    ): Flow<List<AssignmentWithDetails>>
}

/**
 * Data class for assignment with complete details
 */
data class AssignmentWithDetails(
    val assignmentId: Int,
    val doctorId: Int,
    val institutionId: Int,
    val wardId: Int?,
    val designation: String,
    val dutyShift: DutyShift,
    val dutyDays: List<String>,
    val doctorName: String,
    val speciality: String,
    val pmdcNumber: String,
    val institutionName: String,
    val areaBrick: String,
    val wardName: String?
)

/**
 * Data class for doctor assignment with institution details
 */
data class DoctorAssignmentWithDetails(
    val assignmentId: Int,
    val doctorId: Int,
    val institutionId: Int,
    val wardId: Int?,
    val designation: String,
    val dutyShift: DutyShift,
    val dutyDays: List<String>,
    val institutionName: String,
    val areaBrick: String,
    val wardName: String?
)

/**
 * Data class for institution assignment with doctor details
 */
data class InstitutionAssignmentWithDetails(
    val assignmentId: Int,
    val doctorId: Int,
    val institutionId: Int,
    val wardId: Int?,
    val designation: String,
    val dutyShift: DutyShift,
    val dutyDays: List<String>,
    val doctorName: String,
    val speciality: String,
    val pmdcNumber: String,
    val wardName: String?
)
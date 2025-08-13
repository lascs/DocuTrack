package com.healthtracker.offline.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Institution

/**
 * Data Access Object for Institution entity.
 * 
 * Provides methods for CRUD operations and search queries on institutions.
 * All suspend functions are designed to be called from coroutines.
 */
@Dao
interface InstitutionDao {
    
    // ========== CREATE ==========
    
    /**
     * Inserts a new institution into the database
     * @param institution Institution entity to insert
     * @return The row ID of the inserted institution
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInstitution(institution: Institution): Long
    
    /**
     * Inserts multiple institutions into the database
     * @param institutions List of institutions to insert
     * @return List of row IDs of inserted institutions
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInstitutions(institutions: List<Institution>): List<Long>
    
    // ========== READ ==========
    
    /**
     * Gets all institutions as a Flow for reactive updates
     * @return Flow of list of all institutions
     */
    @Query("SELECT * FROM institutions ORDER BY name ASC")
    fun getAllInstitutions(): Flow<List<Institution>>
    
    /**
     * Gets all institutions as a one-time list
     * @return List of all institutions
     */
    @Query("SELECT * FROM institutions ORDER BY name ASC")
    suspend fun getAllInstitutionsList(): List<Institution>
    
    /**
     * Gets an institution by ID
     * @param institutionId The institution's ID
     * @return Institution entity or null if not found
     */
    @Query("SELECT * FROM institutions WHERE institutionId = :institutionId")
    suspend fun getInstitutionById(institutionId: Int): Institution?
    
    /**
     * Searches institutions by name (case-insensitive)
     * @param searchQuery Search term for institution name
     * @return Flow of matching institutions
     */
    @Query("SELECT * FROM institutions WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchInstitutionsByName(searchQuery: String): Flow<List<Institution>>
    
    /**
     * Filters institutions by area brick
     * @param areaBrick Area brick to filter by
     * @return Flow of institutions in the specified area brick
     */
    @Query("SELECT * FROM institutions WHERE areaBrick = :areaBrick ORDER BY name ASC")
    fun getInstitutionsByAreaBrick(areaBrick: String): Flow<List<Institution>>
    
    /**
     * Gets institutions sorted by area brick
     * @return Flow of institutions sorted by area brick, then name
     */
    @Query("SELECT * FROM institutions ORDER BY areaBrick ASC, name ASC")
    fun getInstitutionsSortedByAreaBrick(): Flow<List<Institution>>
    
    /**
     * Gets all unique area bricks from institutions
     * @return Flow of list of unique area bricks
     */
    @Query("SELECT DISTINCT areaBrick FROM institutions WHERE areaBrick != '' ORDER BY areaBrick ASC")
    fun getAllAreaBricks(): Flow<List<String>>
    
    /**
     * Gets all unique segment names from institutions
     * @return Flow of list of unique segment names
     */
    @Query("SELECT DISTINCT segmentName FROM institutions WHERE segmentName != '' ORDER BY segmentName ASC")
    fun getAllSegmentNames(): Flow<List<String>>
    
    /**
     * Searches institutions by multiple criteria
     * @param nameQuery Search term for name (use '%' for wildcard)
     * @param areaBrick Area brick filter (use '%' for any)
     * @param segmentName Segment name filter (use '%' for any)
     * @return Flow of matching institutions
     */
    @Query("""
        SELECT * FROM institutions 
        WHERE (:nameQuery = '%' OR name LIKE :nameQuery)
        AND (:areaBrick = '%' OR areaBrick = :areaBrick)
        AND (:segmentName = '%' OR segmentName = :segmentName)
        ORDER BY name ASC
    """)
    fun searchInstitutions(nameQuery: String, areaBrick: String, segmentName: String): Flow<List<Institution>>
    
    /**
     * Gets count of all institutions
     * @return Total number of institutions
     */
    @Query("SELECT COUNT(*) FROM institutions")
    suspend fun getInstitutionCount(): Int
    
    // ========== UPDATE ==========
    
    /**
     * Updates an existing institution
     * @param institution Institution entity with updated information
     * @return Number of rows affected (should be 1 if successful)
     */
    @Update
    suspend fun updateInstitution(institution: Institution): Int
    
    /**
     * Updates multiple institutions
     * @param institutions List of institutions to update
     * @return Number of rows affected
     */
    @Update
    suspend fun updateInstitutions(institutions: List<Institution>): Int
    
    // ========== DELETE ==========
    
    /**
     * Deletes an institution from the database
     * @param institution Institution entity to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Delete
    suspend fun deleteInstitution(institution: Institution): Int
    
    /**
     * Deletes an institution by ID
     * @param institutionId ID of the institution to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Query("DELETE FROM institutions WHERE institutionId = :institutionId")
    suspend fun deleteInstitutionById(institutionId: Int): Int
    
    /**
     * Deletes all institutions from the database
     * @return Number of rows affected
     */
    @Query("DELETE FROM institutions")
    suspend fun deleteAllInstitutions(): Int
    
    // ========== COMPLEX QUERIES ==========
    
    /**
     * Gets institutions with their ward count
     * @return Flow of institutions with ward counts
     */
    @Query("""
        SELECT i.*, COUNT(w.wardId) as actualWardCount
        FROM institutions i
        LEFT JOIN wards w ON i.institutionId = w.institutionId
        GROUP BY i.institutionId
        ORDER BY i.name ASC
    """)
    fun getInstitutionsWithWardCount(): Flow<List<InstitutionWithWardCount>>
    
    /**
     * Gets institutions with their doctor assignment count
     * @return Flow of institutions with doctor counts
     */
    @Query("""
        SELECT i.*, COUNT(DISTINCT di.doctorId) as doctorCount
        FROM institutions i
        LEFT JOIN doctor_institutions di ON i.institutionId = di.institutionId
        GROUP BY i.institutionId
        ORDER BY i.name ASC
    """)
    fun getInstitutionsWithDoctorCount(): Flow<List<InstitutionWithDoctorCount>>
    
    /**
     * Checks if an institution has any wards
     * @param institutionId Institution ID to check
     * @return True if institution has wards
     */
    @Query("SELECT COUNT(*) > 0 FROM wards WHERE institutionId = :institutionId")
    suspend fun hasWards(institutionId: Int): Boolean
    
    /**
     * Checks if an institution has any doctor assignments
     * @param institutionId Institution ID to check
     * @return True if institution has doctor assignments
     */
    @Query("SELECT COUNT(*) > 0 FROM doctor_institutions WHERE institutionId = :institutionId")
    suspend fun hasDoctorAssignments(institutionId: Int): Boolean
}

/**
 * Data class for institution with ward count
 */
data class InstitutionWithWardCount(
    val institutionId: Int,
    val name: String,
    val msName: String,
    val dmsName: String,
    val areaBrick: String,
    val segmentName: String,
    val numberOfWards: Int,
    val actualWardCount: Int
)

/**
 * Data class for institution with doctor count
 */
data class InstitutionWithDoctorCount(
    val institutionId: Int,
    val name: String,
    val msName: String,
    val dmsName: String,
    val areaBrick: String,
    val segmentName: String,
    val numberOfWards: Int,
    val doctorCount: Int
)
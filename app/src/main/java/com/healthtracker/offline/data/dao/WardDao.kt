package com.healthtracker.offline.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.entities.Ward

/**
 * Data Access Object for Ward entity.
 * 
 * Provides methods for CRUD operations and search queries on wards.
 * All suspend functions are designed to be called from coroutines.
 */
@Dao
interface WardDao {
    
    // ========== CREATE ==========
    
    /**
     * Inserts a new ward into the database
     * @param ward Ward entity to insert
     * @return The row ID of the inserted ward
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWard(ward: Ward): Long
    
    /**
     * Inserts multiple wards into the database
     * @param wards List of wards to insert
     * @return List of row IDs of inserted wards
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWards(wards: List<Ward>): List<Long>
    
    // ========== READ ==========
    
    /**
     * Gets all wards as a Flow for reactive updates
     * @return Flow of list of all wards
     */
    @Query("SELECT * FROM wards ORDER BY wardName ASC")
    fun getAllWards(): Flow<List<Ward>>
    
    /**
     * Gets all wards as a one-time list
     * @return List of all wards
     */
    @Query("SELECT * FROM wards ORDER BY wardName ASC")
    suspend fun getAllWardsList(): List<Ward>
    
    /**
     * Gets a ward by ID
     * @param wardId The ward's ID
     * @return Ward entity or null if not found
     */
    @Query("SELECT * FROM wards WHERE wardId = :wardId")
    suspend fun getWardById(wardId: Int): Ward?
    
    /**
     * Gets all wards for a specific institution
     * @param institutionId Institution ID to filter by
     * @return Flow of wards in the specified institution
     */
    @Query("SELECT * FROM wards WHERE institutionId = :institutionId ORDER BY wardName ASC")
    fun getWardsByInstitution(institutionId: Int): Flow<List<Ward>>
    
    /**
     * Gets all wards for a specific institution as a one-time list
     * @param institutionId Institution ID to filter by
     * @return List of wards in the specified institution
     */
    @Query("SELECT * FROM wards WHERE institutionId = :institutionId ORDER BY wardName ASC")
    suspend fun getWardsByInstitutionList(institutionId: Int): List<Ward>
    
    /**
     * Searches wards by name (case-insensitive)
     * @param searchQuery Search term for ward name
     * @return Flow of matching wards
     */
    @Query("SELECT * FROM wards WHERE wardName LIKE '%' || :searchQuery || '%' ORDER BY wardName ASC")
    fun searchWardsByName(searchQuery: String): Flow<List<Ward>>
    
    /**
     * Gets wards that operate OPD on a specific day
     * @param day Day of the week
     * @return Flow of wards with OPD on the specified day
     */
    @Query("SELECT * FROM wards WHERE opdDays LIKE '%' || :day || '%' ORDER BY wardName ASC")
    fun getWardsByOpdDay(day: String): Flow<List<Ward>>
    
    /**
     * Gets wards that operate OT on a specific day
     * @param day Day of the week
     * @return Flow of wards with OT on the specified day
     */
    @Query("SELECT * FROM wards WHERE otDays LIKE '%' || :day || '%' ORDER BY wardName ASC")
    fun getWardsByOtDay(day: String): Flow<List<Ward>>
    
    /**
     * Gets count of wards for a specific institution
     * @param institutionId Institution ID
     * @return Number of wards in the institution
     */
    @Query("SELECT COUNT(*) FROM wards WHERE institutionId = :institutionId")
    suspend fun getWardCountByInstitution(institutionId: Int): Int
    
    /**
     * Gets count of all wards
     * @return Total number of wards
     */
    @Query("SELECT COUNT(*) FROM wards")
    suspend fun getWardCount(): Int
    
    // ========== UPDATE ==========
    
    /**
     * Updates an existing ward
     * @param ward Ward entity with updated information
     * @return Number of rows affected (should be 1 if successful)
     */
    @Update
    suspend fun updateWard(ward: Ward): Int
    
    /**
     * Updates multiple wards
     * @param wards List of wards to update
     * @return Number of rows affected
     */
    @Update
    suspend fun updateWards(wards: List<Ward>): Int
    
    // ========== DELETE ==========
    
    /**
     * Deletes a ward from the database
     * @param ward Ward entity to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Delete
    suspend fun deleteWard(ward: Ward): Int
    
    /**
     * Deletes a ward by ID
     * @param wardId ID of the ward to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Query("DELETE FROM wards WHERE wardId = :wardId")
    suspend fun deleteWardById(wardId: Int): Int
    
    /**
     * Deletes all wards for a specific institution
     * @param institutionId Institution ID
     * @return Number of rows affected
     */
    @Query("DELETE FROM wards WHERE institutionId = :institutionId")
    suspend fun deleteWardsByInstitution(institutionId: Int): Int
    
    /**
     * Deletes all wards from the database
     * @return Number of rows affected
     */
    @Query("DELETE FROM wards")
    suspend fun deleteAllWards(): Int
    
    // ========== COMPLEX QUERIES ==========
    
    /**
     * Gets wards with their doctor assignment count
     * @return Flow of wards with doctor counts
     */
    @Query("""
        SELECT w.*, COUNT(di.assignmentId) as doctorCount
        FROM wards w
        LEFT JOIN doctor_institutions di ON w.wardId = di.wardId
        GROUP BY w.wardId
        ORDER BY w.wardName ASC
    """)
    fun getWardsWithDoctorCount(): Flow<List<WardWithDoctorCount>>
    
    /**
     * Gets wards with institution information
     * @return Flow of wards with their institution details
     */
    @Query("""
        SELECT w.*, i.name as institutionName, i.areaBrick
        FROM wards w
        INNER JOIN institutions i ON w.institutionId = i.institutionId
        ORDER BY i.name ASC, w.wardName ASC
    """)
    fun getWardsWithInstitution(): Flow<List<WardWithInstitution>>
    
    /**
     * Checks if a ward has any doctor assignments
     * @param wardId Ward ID to check
     * @return True if ward has doctor assignments
     */
    @Query("SELECT COUNT(*) > 0 FROM doctor_institutions WHERE wardId = :wardId")
    suspend fun hasDoctorAssignments(wardId: Int): Boolean
    
    /**
     * Gets wards by multiple criteria
     * @param institutionId Institution ID filter (use 0 for any)
     * @param opdDay OPD day filter (use empty string for any)
     * @param otDay OT day filter (use empty string for any)
     * @return Flow of matching wards
     */
    @Query("""
        SELECT * FROM wards 
        WHERE (:institutionId = 0 OR institutionId = :institutionId)
        AND (:opdDay = '' OR opdDays LIKE '%' || :opdDay || '%')
        AND (:otDay = '' OR otDays LIKE '%' || :otDay || '%')
        ORDER BY wardName ASC
    """)
    fun searchWards(institutionId: Int, opdDay: String, otDay: String): Flow<List<Ward>>
}

/**
 * Data class for ward with doctor count
 */
data class WardWithDoctorCount(
    val wardId: Int,
    val institutionId: Int,
    val wardName: String,
    val opdDays: List<String>,
    val otDays: List<String>,
    val doctorCount: Int
)

/**
 * Data class for ward with institution information
 */
data class WardWithInstitution(
    val wardId: Int,
    val institutionId: Int,
    val wardName: String,
    val opdDays: List<String>,
    val otDays: List<String>,
    val institutionName: String,
    val areaBrick: String
)
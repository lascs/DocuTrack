package com.healthtracker.offline.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.healthtracker.offline.data.models.SavedSearchFilter
import com.healthtracker.offline.data.models.SearchFilterType

/**
 * Data Access Object for SavedSearchFilter entity.
 * 
 * Provides methods for managing saved search filters with
 * usage tracking and favorites functionality.
 */
@Dao
interface SavedSearchFilterDao {
    
    // ========== CREATE ==========
    
    /**
     * Inserts a new saved search filter
     * @param filter SavedSearchFilter entity to insert
     * @return The row ID of the inserted filter
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedFilter(filter: SavedSearchFilter): Long
    
    /**
     * Inserts multiple saved search filters
     * @param filters List of filters to insert
     * @return List of row IDs of inserted filters
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedFilters(filters: List<SavedSearchFilter>): List<Long>
    
    // ========== READ ==========
    
    /**
     * Gets all saved search filters as a Flow for reactive updates
     * @return Flow of list of all saved filters
     */
    @Query("SELECT * FROM saved_search_filters ORDER BY lastUsed DESC, createdAt DESC")
    fun getAllSavedFilters(): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets all saved search filters as a one-time list
     * @return List of all saved filters
     */
    @Query("SELECT * FROM saved_search_filters ORDER BY lastUsed DESC, createdAt DESC")
    suspend fun getAllSavedFiltersList(): List<SavedSearchFilter>
    
    /**
     * Gets a saved search filter by ID
     * @param filterId The filter's ID
     * @return SavedSearchFilter entity or null if not found
     */
    @Query("SELECT * FROM saved_search_filters WHERE filterId = :filterId")
    suspend fun getSavedFilterById(filterId: Long): SavedSearchFilter?
    
    /**
     * Gets saved filters by type
     * @param filterType Type of filters to retrieve
     * @return Flow of filters of the specified type
     */
    @Query("SELECT * FROM saved_search_filters WHERE filterType = :filterType ORDER BY lastUsed DESC")
    fun getSavedFiltersByType(filterType: SearchFilterType): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets favorite saved filters
     * @return Flow of favorite filters
     */
    @Query("SELECT * FROM saved_search_filters WHERE isFavorite = 1 ORDER BY lastUsed DESC")
    fun getFavoriteSavedFilters(): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets recently used saved filters
     * @param limit Maximum number of filters to return
     * @return Flow of recently used filters
     */
    @Query("SELECT * FROM saved_search_filters WHERE lastUsed > 0 ORDER BY lastUsed DESC LIMIT :limit")
    fun getRecentlyUsedFilters(limit: Int = 10): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets most frequently used saved filters
     * @param limit Maximum number of filters to return
     * @return Flow of most used filters
     */
    @Query("SELECT * FROM saved_search_filters WHERE useCount > 0 ORDER BY useCount DESC LIMIT :limit")
    fun getMostUsedFilters(limit: Int = 10): Flow<List<SavedSearchFilter>>
    
    /**
     * Searches saved filters by name or description
     * @param searchQuery Search term for filter name or description
     * @return Flow of matching saved filters
     */
    @Query("""
        SELECT * FROM saved_search_filters 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR description LIKE '%' || :searchQuery || '%'
        ORDER BY useCount DESC, lastUsed DESC
    """)
    fun searchSavedFilters(searchQuery: String): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets saved filters by tag
     * @param tag Tag to search for
     * @return Flow of filters containing the specified tag
     */
    @Query("SELECT * FROM saved_search_filters WHERE tags LIKE '%' || :tag || '%' ORDER BY lastUsed DESC")
    fun getSavedFiltersByTag(tag: String): Flow<List<SavedSearchFilter>>
    
    /**
     * Gets count of saved filters
     * @return Total number of saved filters
     */
    @Query("SELECT COUNT(*) FROM saved_search_filters")
    suspend fun getSavedFilterCount(): Int
    
    /**
     * Gets count of saved filters by type
     * @param filterType Type of filters to count
     * @return Number of filters of the specified type
     */
    @Query("SELECT COUNT(*) FROM saved_search_filters WHERE filterType = :filterType")
    suspend fun getSavedFilterCountByType(filterType: SearchFilterType): Int
    
    // ========== UPDATE ==========
    
    /**
     * Updates an existing saved search filter
     * @param filter SavedSearchFilter entity with updated information
     * @return Number of rows affected (should be 1 if successful)
     */
    @Update
    suspend fun updateSavedFilter(filter: SavedSearchFilter): Int
    
    /**
     * Updates multiple saved search filters
     * @param filters List of filters to update
     * @return Number of rows affected
     */
    @Update
    suspend fun updateSavedFilters(filters: List<SavedSearchFilter>): Int
    
    /**
     * Updates the usage statistics for a saved filter
     * @param filterId ID of the filter to update
     * @param lastUsed Timestamp of last use
     * @param useCount New use count
     * @return Number of rows affected
     */
    @Query("""
        UPDATE saved_search_filters 
        SET lastUsed = :lastUsed, useCount = :useCount 
        WHERE filterId = :filterId
    """)
    suspend fun updateFilterUsage(filterId: Long, lastUsed: Long, useCount: Int): Int
    
    /**
     * Increments the use count for a saved filter
     * @param filterId ID of the filter to update
     * @return Number of rows affected
     */
    @Query("""
        UPDATE saved_search_filters 
        SET lastUsed = :currentTime, useCount = useCount + 1 
        WHERE filterId = :filterId
    """)
    suspend fun incrementFilterUsage(filterId: Long, currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Toggles the favorite status of a saved filter
     * @param filterId ID of the filter to toggle
     * @param isFavorite New favorite status
     * @return Number of rows affected
     */
    @Query("UPDATE saved_search_filters SET isFavorite = :isFavorite WHERE filterId = :filterId")
    suspend fun setFilterFavorite(filterId: Long, isFavorite: Boolean): Int
    
    // ========== DELETE ==========
    
    /**
     * Deletes a saved search filter from the database
     * @param filter SavedSearchFilter entity to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Delete
    suspend fun deleteSavedFilter(filter: SavedSearchFilter): Int
    
    /**
     * Deletes a saved search filter by ID
     * @param filterId ID of the filter to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    @Query("DELETE FROM saved_search_filters WHERE filterId = :filterId")
    suspend fun deleteSavedFilterById(filterId: Long): Int
    
    /**
     * Deletes all saved search filters of a specific type
     * @param filterType Type of filters to delete
     * @return Number of rows affected
     */
    @Query("DELETE FROM saved_search_filters WHERE filterType = :filterType")
    suspend fun deleteSavedFiltersByType(filterType: SearchFilterType): Int
    
    /**
     * Deletes old unused saved search filters
     * @param olderThan Timestamp - filters older than this and never used will be deleted
     * @return Number of rows affected
     */
    @Query("""
        DELETE FROM saved_search_filters 
        WHERE createdAt < :olderThan AND useCount = 0 AND isFavorite = 0
    """)
    suspend fun deleteOldUnusedFilters(olderThan: Long): Int
    
    /**
     * Deletes all saved search filters from the database
     * @return Number of rows affected
     */
    @Query("DELETE FROM saved_search_filters")
    suspend fun deleteAllSavedFilters(): Int
    
    // ========== COMPLEX QUERIES ==========
    
    /**
     * Gets saved filters with usage statistics
     * @return Flow of filters with calculated usage metrics
     */
    @Query("""
        SELECT *, 
        CASE 
            WHEN useCount = 0 THEN 0
            ELSE (julianday('now') - julianday(lastUsed/1000, 'unixepoch')) 
        END as daysSinceLastUse
        FROM saved_search_filters 
        ORDER BY isFavorite DESC, useCount DESC, lastUsed DESC
    """)
    fun getSavedFiltersWithUsageStats(): Flow<List<SavedFilterWithStats>>
    
    /**
     * Checks if a filter with the same name already exists
     * @param name Filter name to check
     * @param excludeFilterId Filter ID to exclude from check (for updates)
     * @return True if name exists for a different filter
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM saved_search_filters 
        WHERE name = :name 
        AND (:excludeFilterId IS NULL OR filterId != :excludeFilterId)
    """)
    suspend fun isFilterNameExists(name: String, excludeFilterId: Long? = null): Boolean
    
    /**
     * Gets all unique tags used in saved filters
     * @return Flow of list of unique tags
     */
    @Query("""
        SELECT DISTINCT tag FROM (
            SELECT TRIM(value) as tag 
            FROM saved_search_filters, json_each('[' || REPLACE(REPLACE(tags, '[', ''), ']', '') || ']')
            WHERE tag != ''
        ) ORDER BY tag
    """)
    fun getAllTags(): Flow<List<String>>
}

/**
 * Data class for saved filter with usage statistics
 */
data class SavedFilterWithStats(
    val filterId: Long,
    val name: String,
    val description: String,
    val filterType: SearchFilterType,
    val filterData: String,
    val tags: List<String>,
    val createdAt: Long,
    val lastUsed: Long,
    val useCount: Int,
    val isFavorite: Boolean,
    val daysSinceLastUse: Double
) {
    fun getUsageDescription(): String {
        return when {
            useCount == 0 -> "Never used"
            daysSinceLastUse < 1 -> "Used today"
            daysSinceLastUse < 7 -> "Used this week"
            daysSinceLastUse < 30 -> "Used this month"
            else -> "Used ${daysSinceLastUse.toInt()} days ago"
        }
    }
}
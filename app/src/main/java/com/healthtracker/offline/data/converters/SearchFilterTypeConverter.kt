package com.healthtracker.offline.data.converters

import androidx.room.TypeConverter
import com.healthtracker.offline.data.models.SearchFilterType

/**
 * Room TypeConverter for converting SearchFilterType enum to/from string for database storage.
 */
class SearchFilterTypeConverter {
    
    /**
     * Converts SearchFilterType enum to string for database storage
     * @param filterType SearchFilterType enum value
     * @return String representation of the enum, or "GLOBAL" if null
     */
    @TypeConverter
    fun fromSearchFilterType(filterType: SearchFilterType?): String {
        return filterType?.name ?: SearchFilterType.GLOBAL.name
    }
    
    /**
     * Converts string back to SearchFilterType enum from database
     * @param value String value from database
     * @return SearchFilterType enum, or GLOBAL if null/invalid
     */
    @TypeConverter
    fun toSearchFilterType(value: String?): SearchFilterType {
        return if (value.isNullOrBlank()) {
            SearchFilterType.GLOBAL
        } else {
            try {
                SearchFilterType.valueOf(value)
            } catch (e: IllegalArgumentException) {
                // If enum parsing fails, return default
                SearchFilterType.GLOBAL
            }
        }
    }
}
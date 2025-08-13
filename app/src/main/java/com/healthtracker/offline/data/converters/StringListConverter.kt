package com.healthtracker.offline.data.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Room TypeConverter for converting List<String> to/from JSON string for database storage.
 * Used for storing qualifications, OPD days, OT days, and duty days.
 */
class StringListConverter {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Converts a List<String> to JSON string for database storage
     * @param value List of strings to convert
     * @return JSON string representation, or empty JSON array if null
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) {
            "[]"
        } else {
            json.encodeToString(value)
        }
    }
    
    /**
     * Converts JSON string back to List<String> from database
     * @param value JSON string from database
     * @return List of strings, or empty list if null/invalid
     */
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return if (value.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<String>>(value)
            } catch (e: Exception) {
                // If JSON parsing fails, return empty list
                emptyList()
            }
        }
    }
}